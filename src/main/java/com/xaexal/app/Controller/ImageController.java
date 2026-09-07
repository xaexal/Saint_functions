package com.xaexal.app.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final S3Client s3Client;

    public ImageController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @GetMapping("/{key}")
    public ResponseEntity<byte[]> getImage(@PathVariable("key") String key) {
        String bucketName = "xaexal";

    	System.out.println("key ["+key+"]");
        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .body(response.asByteArray());
    }
}
