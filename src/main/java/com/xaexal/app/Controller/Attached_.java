package com.xaexal.app.Controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.DTO.ImageResponse;
import com.xaexal.app.Entity.Attached;
import com.xaexal.app.Repository.AttachedRep;
import com.xaexal.app.Service.S3Service;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/attached")
@RequiredArgsConstructor // Lombok 사용 시 생성자 자동 생성
public class Attached_ {
	@Autowired private AttachedRep _atch;
    private final S3Service s3Service;

    @GetMapping("/{board_id}")
    public ResponseEntity<Result<List<ImageResponse>>> getAttached(@PathVariable("board_id") int board_id) {
    	Attached atch = new Attached();
        atch.setBoardId(board_id);
        List<Attached> filelist = _atch.findByBoardIdOrderBySeqno(board_id);

        List<ImageResponse> resultList = new ArrayList<>();

        for (Attached file : filelist) {
            // DB에 저장된 S3 key 가져오기 (예: "upload/20250920/abc.jpg")
            String key = file.getNewName();

            // 프리사인드 URL 생성 (10분 유효)
            System.out.println("key ["+key+"]");
            String url = s3Service.generatePresignedUrl(key, Duration.ofMinutes(10));
            System.out.println(key+","+url+"]");
            // 응답 DTO에 담기
            resultList.add(new ImageResponse(file.getId(), file.getNewName(), url));
        }
        return ResponseEntity.ok(new Result<>(0, "", resultList));
    }

    @GetMapping("/{boardId}/{source}")
    public ResponseEntity<Result<List<ImageResponse>>> getAttachedBySource(
            @PathVariable("boardId") int boardId,
            @PathVariable("source") String source) {
        List<Attached> filelist = _atch.findByBoardIdAndSourceOrderBySeqno(boardId, source);
        List<ImageResponse> resultList = new ArrayList<>();
        for (Attached file : filelist) {
            String url = s3Service.generatePresignedUrl(file.getNewName(), Duration.ofMinutes(10));
            resultList.add(new ImageResponse(file.getId(), file.getNewName(), url));
        }
        return ResponseEntity.ok(new Result<>(0, "", resultList));
    }

    @LoginCheck
    @DeleteMapping("/name/{name}")
    public ResponseEntity<Result<?>> deleteByName(@PathVariable("name") String name) {
        _atch.findByNewName(name).ifPresent(file -> {
            s3Service.deleteObject(file.getNewName());
            _atch.deleteById(file.getId());
        });
        return ResponseEntity.ok(new Result<>(1, "삭제됨"));
    }
}
