package com.shoesbox.domain.photo;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.shoesbox.domain.photo.exception.ImageDeleteFailureException;
import com.shoesbox.domain.photo.exception.ImageDownloadFailureException;
import com.shoesbox.domain.photo.exception.ImageUploadFailureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {
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
            throw new ImageUploadFailureException("오직 WebP 파일만 업로드 가능합니다.", null);
        }
        // 메타 데이터 생성
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(createdImageFile.length());
        metadata.setContentType(CONTENT_TYPE_WEBP);

        // PutObjectRequest 생성
        return new PutObjectRequest(bucketName, createdImageFile.getName(), createdImageFile)
                .withCannedAcl(CannedAccessControlList.PublicRead);
    }

    // 한 장의 이미지 삭제 요청 생성
    public DeleteObjectRequest createDeleteRequest(String imageUrl) {
        // split을 통해 나누고 나눈 length에서 1을 빼서 마지막 값(파일명)을 사용함.
        String key = imageUrl.split("/")[imageUrl.split("/").length - 1];
        return new DeleteObjectRequest(bucketName, key);
    }

    // 여러 장의 이미지 삭제 요청 생성
    public DeleteObjectsRequest createDeleteRequest(List<String> imageUrls) {
        var keys = imageUrls.stream()
                .map((imageUrl) -> imageUrl.split("/")[imageUrl.split("/").length - 1])
                .map(DeleteObjectsRequest.KeyVersion::new)
                .collect(Collectors.toList());
        return new DeleteObjectsRequest(bucketName)
                .withKeys(keys)
                .withQuiet(false);
    }

    // 업로드 + url 반환
    public String executePutRequest(PutObjectRequest putObjectRequest) {
        try {
            s3Client.putObject(putObjectRequest);
        } catch (SdkClientException e) {
            throw new ImageUploadFailureException("이미지 업로드 실패!", e);
        }
        putObjectRequest.getFile().delete();
        return urlPrefix + putObjectRequest.getFile().getName();
    }

    // 다중 업로드 + url List 반환
    public List<String> executePutRequest(List<PutObjectRequest> putObjectRequests) {
        var urls = new ArrayList<String>();
        try {
            for (var putObjectRequest : putObjectRequests) {
                s3Client.putObject(putObjectRequest);
                urls.add(urlPrefix + putObjectRequest.getFile().getName());
            }
        } catch (SdkClientException e) {
            throw new ImageUploadFailureException("이미지 업로드 실패!", e);
        } finally {
            putObjectRequests.forEach((request) -> request.getFile().delete());
        }
        return urls;
    }

    // 단일 삭제
    public void executeDeleteRequest(DeleteObjectRequest deleteObjectRequest) {
        try {
            s3Client.deleteObject(deleteObjectRequest);
        } catch (SdkClientException e) {
            throw new ImageDeleteFailureException("이미지 삭제 실패!", e);
        }
    }

    // 여러 개 삭제
    public void executeDeleteRequest(DeleteObjectsRequest deleteObjectsRequest) {
        try {
            var result = s3Client.deleteObjects(deleteObjectsRequest);
            var deletedObjects = result.getDeletedObjects();
            if (deletedObjects.isEmpty()) {
                throw new ImageDeleteFailureException("이미지 삭제 실패!", null);
            }
        } catch (SdkClientException e) {
            throw new ImageDeleteFailureException("이미지 삭제 실패!", e);
        }
    }

    // 이미지 다운로드
    public S3Object getObject(String imageUrl) throws IOException {
        S3Object s3Object = null;
        try {
            String key = imageUrl.split("/")[imageUrl.split("/").length - 1];
            s3Object = s3Client.getObject(new GetObjectRequest(bucketName, key));
            log.info("Content-Type: " + s3Object.getObjectMetadata().getContentType());
        } catch (SdkClientException e) {
            if (s3Object != null) {
                try {
                    s3Object.close();
                } catch (IOException ex) {
                    throw new ImageDownloadFailureException(ex.getLocalizedMessage(), ex);
                }
            }
            throw new ImageDownloadFailureException(e.getLocalizedMessage(), e);
        }
        return s3Object;
    }

    public File getFileFromS3Object(S3Object s3Object) throws IOException {
        File originalFile = new File(UUID.randomUUID() + ".webp");
        java.nio.file.Files.copy(
                s3Object.getObjectContent(),
                originalFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        return originalFile;
    }
}
