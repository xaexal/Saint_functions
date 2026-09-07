package com.xaexal.app.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xaexal.app.Common.LoginCheck;
import com.xaexal.app.Common.Result;
import com.xaexal.app.Entity.Attached;
import com.xaexal.app.Entity.Bulletin;
import com.xaexal.app.Repository.AttachedRep;
import com.xaexal.app.Repository.BulletinRep;

import jakarta.servlet.http.HttpSession;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RestController
@RequestMapping("/bulletin")
public class Bulletin_ {

    @Autowired private BulletinRep _bul;
    @Autowired private AttachedRep _atch;

    private final S3Client s3Client;

    public Bulletin_(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @GetMapping("/church/{churchId}")
    public ResponseEntity<?> doList(@PathVariable("churchId") Integer churchId) {
        List<Bulletin> list = _bul.findByChurchIdOrderByIdDesc(churchId);
        return ResponseEntity.ok(new Result<>(1, "", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> doView(@PathVariable("id") Integer id) {
        _bul.addHit(id);
        return ResponseEntity.ok(new Result<>(1, "", _bul.findById(id).orElseThrow()));
    }

    @LoginCheck
    @PostMapping("/")
    public ResponseEntity<?> doWrite(
            @RequestPart("data") Map<String, Object> req,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            HttpSession s) {
        try {
            Bulletin bulletin = new Bulletin();
            bulletin.setTitle(req.get("title").toString());
            bulletin.setChurchId(((Number) req.get("churchId")).intValue());
            bulletin.setHit(0);
            bulletin.setWriter(((Number) req.get("writer")).intValue());
            bulletin = _bul.save(bulletin);

            if (files != null && !files.isEmpty()) {
                int writerVal = ((Number) req.get("writer")).intValue();
                int seqno = 1;
                for (MultipartFile file : files) {
                    String originalName = file.getOriginalFilename();
                    String ext = "";
                    if (originalName != null && originalName.lastIndexOf(".") != -1)
                        ext = originalName.substring(originalName.lastIndexOf("."));
                    String newFileName = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + seqno + ext;

                    PutObjectRequest putReq = PutObjectRequest.builder()
                        .bucket("xaexal").key(newFileName)
                        .contentType(file.getContentType()).build();
                    s3Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));

                    Attached attached = new Attached();
                    attached.setSource("w");
                    attached.setBoardId(bulletin.getId());
                    attached.setOrgName(originalName);
                    attached.setNewName(newFileName);
                    attached.setSeqno(seqno++);
                    attached.setWriter(writerVal);
                    _atch.save(attached);
                }
            }
            return ResponseEntity.ok(new Result<>(1, "등록성공", bulletin));
        } catch (Exception e) {
            throw new RuntimeException("처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @LoginCheck
    @DeleteMapping("/{id}")
    public ResponseEntity<?> doDelete(@PathVariable("id") Integer id) {
        _bul.deleteById(id);
        return ResponseEntity.ok(new Result<>(1, "삭제성공"));
    }
}
