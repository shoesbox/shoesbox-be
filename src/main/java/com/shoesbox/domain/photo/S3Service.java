package com.shoesbox.domain.photo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@NoArgsConstructor
@Service
public class S3Service {
    private AmazonS3 s3Client;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretkey}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    public String uploadImage(MultipartFile file) {
        // 파일 이름 받아오기
        String fileName = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();

        // 확장자 점검
        if (!(fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"))) {
            throw new IllegalArgumentException("bmp,jpg,jpeg,png 형식의 이미지 파일이 요구됨.");
        }

        String fileExtension = fileName.substring(fileName.length() - 4);

        // 파일이름을 무작위 값으로 변경
        fileName = UUID.randomUUID() + fileExtension;

        try {
            // 파일 업로드
            ObjectMetadata objMeta = new ObjectMetadata();
            byte[] bytes = IOUtils.toByteArray(file.getInputStream());
            objMeta.setContentLength(bytes.length);
            ByteArrayInputStream byteArrayIs = new ByteArrayInputStream(bytes);
            PutObjectRequest putObjReq = new PutObjectRequest(bucket, fileName, byteArrayIs, objMeta).withCannedAcl(CannedAccessControlList.PublicRead);
            s3Client.putObject(putObjReq);

            // // 파일 업로드
            // s3Client.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), null)
            //         .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("S3 Bucket 객체 업로드 실패.");
        }

        return s3Client.getUrl(bucket, fileName).toString();    ///url string 리턴
    }
}
