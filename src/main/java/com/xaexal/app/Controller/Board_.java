package com.xaexal.app.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xaexal.app.Common.LogRequest;
import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.DTO.BoardList;
import com.xaexal.app.DTO.BoardResult;
import com.xaexal.app.Entity.Attached;
import com.xaexal.app.Entity.Board;
import com.xaexal.app.Entity.BoardAux;
import com.xaexal.app.Entity.BoardType;
import com.xaexal.app.Repository.AttachedRep;
import com.xaexal.app.Repository.BoardAuxRep;
import com.xaexal.app.Repository.BoardRep;

import jakarta.servlet.http.HttpSession;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


@RestController
@RequestMapping("/board")
public class Board_ {

	@Autowired private BoardRep _brd;
	@Autowired private BoardAuxRep _brdx;
	@Autowired private AttachedRep _atch;

	private final S3Client s3Client;

	public Board_(S3Client s3Client) {
		this.s3Client=s3Client;
	}
	@GetMapping("/list")
	public ResponseEntity<?> doList(@RequestParam Map<String,String> req){
		int church = Integer.parseInt(req.getOrDefault("church", "0"));
		int type = Integer.parseInt(req.getOrDefault("type", "1"));
	    int page = Integer.parseInt(req.getOrDefault("page", "1"));
	    String keyword = req.getOrDefault("keyword", "");
	    int size = Integer.parseInt(req.getOrDefault("size", "20"));

	    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
	    Page<BoardList> posts = _brd.getList(church, type, keyword, pageable);
	    int cnt = this._brd.countPost(church, type);

	    System.out.println("size [" + posts.getSize() + "] countPost [" + cnt + "]");

	    return ResponseEntity.ok(new Result<Page<BoardList>>(cnt, "success", posts));
	}
	@LoginCheck
	@GetMapping("/urgent")
	public ResponseEntity<?> getUrgentNotices(@RequestParam(name="churchId", defaultValue="0") int churchId){
		List<BoardResult> list = this._brd.getUrgentNotices(churchId);
		return ResponseEntity.ok(new Result<List<BoardResult>>(1,"",list));
	}
	@GetMapping("/type_list/")
	public ResponseEntity<?> getTypeList(){
		System.out.println("/board/type_list/");

		List<BoardType> typelist = this._brd.selectType();
		System.out.println("typelist size ["+typelist.size()+"]");
		return ResponseEntity.ok(new Result<List<BoardType>>(0,"",typelist));
	}
	@GetMapping("/{id}")
	public ResponseEntity<?> doView(@PathVariable("id") int id,
			@RequestParam(name="addHit", defaultValue="true") boolean addHit) throws Exception {

		BoardResult post = this._brd.searchById(id);
		if(addHit) this._brd.addHit(id);
		return ResponseEntity.ok(new Result<BoardResult>(1,"",post));
	}
	@LoginCheck
	@PostMapping("/")
	public ResponseEntity<?> doWrite(@RequestPart("data") Map<String, Object> req,
									 @RequestPart(value="images",required=false) List<MultipartFile> images,
									 HttpSession s) {
		try {
			Integer boardId = req.get("id") != null ? ((Number)req.get("id")).intValue() : null;

			if (boardId != null) {
				// 수정
				_brd.updateContent(boardId, req.get("content").toString());
				int type = req.get("type") != null ? ((Number)req.get("type")).intValue() : 0;
				boolean urgent = Boolean.TRUE.equals(req.get("urgent"));
				_brdx.updateByBoardId(boardId, req.get("title").toString(), type, urgent);
			} else {
				// 신규 등록
				Board board = new Board();
				board.setParId(null);
				board.setContent(req.get("content").toString());
				board.setWriter(((Number)req.get("writer")).intValue());
				board.setHit(req.get("hit") == null ? 0 : ((Number)req.get("hit")).intValue());
				board = _brd.save(board);
				boardId = board.getId();

				BoardAux bdaux = new BoardAux();
				bdaux.setBoardId(boardId);
				bdaux.setTitle(req.get("title").toString());
				bdaux.setLevel(req.get("level") != null ? ((Number)req.get("level")).intValue() : null);
				bdaux.setChurchId(req.get("churchId") != null ? ((Number)req.get("churchId")).intValue() : null);
				bdaux.setType(req.get("type") != null ? ((Number)req.get("type")).intValue() : null);
				bdaux.setUrgent(Boolean.TRUE.equals(req.get("urgent")));
				_brdx.save(bdaux);
			}

			// 신규 이미지 업로드 (수정·신규 공통)
			if (images != null && !images.isEmpty()) {
				int seqno = _atch.findByBoardIdOrderBySeqno(boardId).size() + 1;
				int writerVal = req.get("writer") != null ? ((Number)req.get("writer")).intValue()
								: (int) s.getAttribute("member_id");
				for (MultipartFile file : images) {
					String originalName = file.getOriginalFilename();
					String ext = "";
					if (originalName != null && originalName.lastIndexOf(".") != -1)
						ext = originalName.substring(originalName.lastIndexOf("."));
					String newFileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + seqno + ext;

					PutObjectRequest putReq = PutObjectRequest.builder()
						.bucket("xaexal").key(newFileName).contentType(file.getContentType()).build();
					s3Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));

					Attached attached = new Attached();
					attached.setSource("b");
					attached.setBoardId(boardId);
					attached.setOrgName(originalName);
					attached.setNewName(newFileName);
					attached.setSeqno(seqno++);
					attached.setWriter(writerVal);
					_atch.save(attached);
				}
			}
			return ResponseEntity.ok(new Result<Board>(1, "등록성공"));
		} catch(Exception e) {
			throw new RuntimeException("처리 중 오류가 발생했습니다: " + e.getMessage());
		}
	}
	@DeleteMapping("/{id}")
	public ResponseEntity<?> doDelete(@PathVariable("id") int id) {
    	System.out.println("/board/delete/");

    	this._brdx.deleteByBoardId(id);
    	this._brd.deleteById(id);
		return ResponseEntity.ok(new Result<List<Board>>(1,"삭제성공"));
	}
  @GetMapping("/addHit")
  public ResponseEntity<?> addHit(@RequestParam("id") String id){
      System.out.println("/addHit");
      if(id==null || id.isBlank()) throw new RuntimeException("게시물 아이디가 지정되지 않았습니다.");

      this._brd.addHit(Integer.parseInt(id));
      return ResponseEntity.ok(new Result<List<Board>>(1,"Hit count"));
  }
}
