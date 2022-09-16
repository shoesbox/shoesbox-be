package com.shoesbox.domain.photo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    // 이미지 업로드
    public String uploadImage(MultipartFile file) {
        // 파일 이름 받아오기
        String fileName = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();

        // 확장자 점검
        if (!(fileName.endsWith(".bmp") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png"))) {
            throw new IllegalArgumentException("bmp,jpg,jpeg,png 형식의 이미지 파일이 요구됨.");
        }

        String fileExtension = fileName.substring(fileName.lastIndexOf("."));

        // 파일이름을 무작위 값으로 변경
        fileName = UUID.randomUUID() + fileExtension;

        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(file.getSize());
        objMeta.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            // 파일 업로드
            s3Client.putObject(new PutObjectRequest(bucket, fileName, inputStream, objMeta)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("S3 Bucket 객체 업로드 실패.");
        }

        return s3Client.getUrl(bucket, fileName).toString();    ///url string 리턴
    }

    // 파일 삭제
    public void deleteObjectByImageUrl(String imageUrl) {
        // split을 통해 나누고 나눈 length에서 1을 빼서 마지막 값(파일명)을 사용함.
        String sourceKey = imageUrl.split("/")[imageUrl.split("/").length - 1];

        // 소스키로 s3에서 삭제
        s3Client.deleteObject(bucket, sourceKey);
    }

    public String uploadThumbnail(MultipartFile mfile) throws IOException {
        // 파일 이름 받아오기
        String originalName = Objects.requireNonNull(mfile.getOriginalFilename()).toLowerCase();
        String fileName = originalName.substring(originalName.lastIndexOf("\\") + 1);
        String uuid = UUID.randomUUID().toString();
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        String saveName = "s_" + uuid + fileExtension;

        // 리사이징 할 파일 크기
        int targetWidth = 200;
        int targetHeight = 200;

        File outPutStream = new File(saveName);

        // Thumbnailator로 리사이징
        Thumbnails.of(mfile.getInputStream())
                .size(targetWidth, targetHeight)
                .toFile(outPutStream);

        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(outPutStream.length());
        objMeta.setContentType(mfile.getContentType());

        try (InputStream inputStream = new FileInputStream(outPutStream)) {
            // 파일 업로드
            s3Client.putObject(new PutObjectRequest(bucket, saveName, inputStream, objMeta)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("S3 Bucket 객체 업로드 실패.");
        }

        outPutStream.delete();
        return s3Client.getUrl(bucket, saveName).toString();    ///url string 리턴
    }
}
