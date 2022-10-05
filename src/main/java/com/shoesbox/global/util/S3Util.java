package com.shoesbox.global.util;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.shoesbox.global.exception.runtime.image.ImageProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.shoesbox.global.exception.ExceptionCode.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Util {
    private AmazonS3 s3Client;
    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secretkey}")
    private String secretKey;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    @Value("${cloud.aws.region.static}")
    private String region;
    @Value("${cloud.aws.prefix}")
    private String urlPrefix;
    private static final String CONTENT_TYPE_WEBP = "image/webp";

    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    // 이미지 업로드 요청 생성
    public PutObjectRequest createPutObjectRequest(File createdImageFile) {
        // 확장자 검사
        if (!createdImageFile.getName().toLowerCase().endsWith(".webp")) {
            throw new ImageProcessException(IMAGE_UPLOAD_FAILURE, "오직 WebP 파일만 업로드 가능합니다.");
        }
        // 메타 데이터 생성
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(createdImageFile.length());
        metadata.setContentType(CONTENT_TYPE_WEBP);
        // PutObjectRequest 생성
        log.info(">>>>>>>>>>>>> create PutObjectRequest: " + createdImageFile.getName());
        return new PutObjectRequest(bucketName, createdImageFile.getName(), createdImageFile)
                .withCannedAcl(CannedAccessControlList.PublicRead);
    }

    // 한 장의 이미지 삭제 요청 생성
    public DeleteObjectRequest createDeleteRequest(String imageUrl) {
        // split을 통해 나누고 나눈 length에서 1을 빼서 마지막 값(파일명)을 사용함.
        String key = imageUrl.split("/")[imageUrl.split("/").length - 1];
        log.info(">>>>>>>>>>>>> create DeleteObjectRequest: " + key);
        return new DeleteObjectRequest(bucketName, key);
    }

    // 여러 장의 이미지 삭제 요청 생성
    public DeleteObjectsRequest createDeleteRequest(List<String> imageUrls) {
        var keys = imageUrls.stream()
                .map((imageUrl) -> imageUrl.split("/")[imageUrl.split("/").length - 1])
                .map(DeleteObjectsRequest.KeyVersion::new)
                .peek((key) -> log.info(">>>>>>>>>>>>> create DeleteObjectRequest: " + key.getKey()))
                .collect(Collectors.toList());
        return new DeleteObjectsRequest(bucketName)
                .withKeys(keys)
                .withQuiet(false);
    }

    // 업로드 + url 반환
    public String executePutRequest(PutObjectRequest putObjectRequest) {
        try {
            s3Client.putObject(putObjectRequest);
            log.info(">>>>>>>>>>>>> 이미지 업로드 성공!");
        } catch (SdkClientException e) {
            log.info(">>>>>>>>>>>>> 이미지 업로드 실패!");
            throw new ImageProcessException(IMAGE_UPLOAD_FAILURE, e.getLocalizedMessage(), e);
        } finally {
            putObjectRequest.getFile().delete();
        }
        return urlPrefix + putObjectRequest.getFile().getName();
    }

    // 다중 업로드 + url List 반환
    public List<String> executePutRequest(List<PutObjectRequest> putObjectRequests) {
        var urls = new ArrayList<String>();
        try {
            for (PutObjectRequest putObjectRequest : putObjectRequests) {
                s3Client.putObject(putObjectRequest);
                urls.add(urlPrefix + putObjectRequest.getFile().getName());
            }
        } catch (SdkClientException e) {
            log.info(">>>>>>>>>>>>> failed to execute PutObjectRequest!");
            throw new ImageProcessException(IMAGE_UPLOAD_FAILURE, e.getLocalizedMessage(), e);
        } finally {
            putObjectRequests.forEach((request) -> request.getFile().delete());
        }
        return urls;
    }

    // 단일 삭제
    public void executeDeleteRequest(DeleteObjectRequest deleteObjectRequest) {
        try {
            s3Client.deleteObject(deleteObjectRequest);
            log.info(">>>>>>>>>>>>> execute DeleteObjectRequest: " + deleteObjectRequest.getKey());
        } catch (SdkClientException e) {
            log.info(">>>>>>>>>>>>> failed to execute DeleteObjectRequest: " + deleteObjectRequest.getKey());
            throw new ImageProcessException(IMAGE_DELETE_FAILURE, e.getLocalizedMessage(), e);
        }
    }

    // 여러 개 삭제
    public void executeDeleteRequest(DeleteObjectsRequest deleteObjectsRequest) {
        if (deleteObjectsRequest.getKeys().size() == 0) {
            log.info(">>>>>>>>>>>>> failed to execute DeleteObjectsRequest: no keys inside!");
            return;
        }
        try {
            s3Client.deleteObjects(deleteObjectsRequest);
            log.info(">>>>>>>>>>>>> execute DeleteObjectsRequest: " + deleteObjectsRequest.getKeys());
        } catch (SdkClientException e) {
            log.info(">>>>>>>>>>>>> failed to execute DeleteObjectRequest: " + deleteObjectsRequest.getKeys());
            throw new ImageProcessException(IMAGE_DELETE_FAILURE, "이미지 삭제 실패!", e);
        }
    }

    // 이미지 다운로드
    public File getFileFromUrl(String imageUrl) {
        S3Object s3Object = null;
        try {
            String key = imageUrl.split("/")[imageUrl.split("/").length - 1];
            s3Object = s3Client.getObject(new GetObjectRequest(bucketName, key));
            log.info("get S3Object: " + s3Object.getKey());
            log.info("Content-Type: " + s3Object.getObjectMetadata().getContentType());
        } catch (SdkClientException e) {
            if (s3Object != null) {
                try {
                    s3Object.close();
                } catch (IOException ex) {
                    throw new ImageProcessException(IMAGE_DOWNLOAD_FAILURE, ex.getLocalizedMessage(), ex);
                }
            }
            throw new ImageProcessException(IMAGE_DOWNLOAD_FAILURE, e.getLocalizedMessage(), e);
        }
        return getFileFromS3Object(s3Object);
    }

    private File getFileFromS3Object(S3Object s3Object) {
        File originalFile = new File(s3Object.getKey());
        try {
            java.nio.file.Files.copy(
                    s3Object.getObjectContent(),
                    originalFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            s3Object.close();
        } catch (IOException e) {
            try {
                s3Object.close();
            } catch (IOException ex) {
                throw new ImageProcessException(IMAGE_DOWNLOAD_FAILURE, ex.getLocalizedMessage(), ex);
            }
            throw new ImageProcessException(IMAGE_DOWNLOAD_FAILURE, e.getLocalizedMessage(), e);
        }
        log.info(">>>>>>>>>>>>> S3Object: " + s3Object.getKey() + " is closed.");
        log.info(">>>>>>>>>>>>> S3Object: " + s3Object.getKey() + " is converted to: " + originalFile.getName());
        return originalFile;
    }
}
