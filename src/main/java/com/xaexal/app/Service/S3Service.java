package com.xaexal.app.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3Service {
    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket-name}")
    private String bucket;

    @Value("${aws.access-key-id}")
    private String accessKey;

    @Value("${aws.secret-access-key}")
    private String secretKey;

    private S3Client s3;
    private S3Presigner presigner;

    @PostConstruct
    public void init() {
    	AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
    		    accessKey,
    		    secretKey
    		);

    		s3 = S3Client.builder()
    		        .region(Region.of(region))
    		        .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
    		        .build();

    		presigner = S3Presigner.builder()
    		        .region(Region.of(region))
    		        .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
    		        .build();
//        s3 = S3Client.builder()
//                .region(Region.of(region))
//                .credentialsProvider(DefaultCredentialsProvider.create())
//                .build();
//        presigner = S3Presigner.builder()
//                .region(Region.of(region))
//                .credentialsProvider(DefaultCredentialsProvider.create())
//                .build();
    }

    // 1) 버킷의 객체 키 목록 가져오기
    public List<String> listKeys(String prefix) {
        ListObjectsV2Request req = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix == null ? "" : prefix)
                .build();
        ListObjectsV2Response resp = s3.listObjectsV2(req);
        return resp.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    // 2) 공개 버킷일 때 직접 URL 생성
    public String publicUrlForKey(String key) {
        // region 기반 일반 URL 포맷
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

    // 3) S3 객체 삭제
    public void deleteObject(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    // 4) 프리사인드 URL 생성 (기본 만료: 10분)
    public String generatePresignedUrl(String key, Duration expiry) {
    	System.out.println("region ["+region+"] bucket ["+bucket+"] key ["+key+"] expiry ["+expiry+"]");
    	try {
	        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
	                .bucket(bucket)
	                .key(key)
	                .build();

	        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
	                .getObjectRequest(getObjectRequest)
	                .signatureDuration(expiry) // URL 유효기간 (예: 10분)
	                .build();
	        return presigner.presignGetObject(presignRequest).url().toString();

    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
}
