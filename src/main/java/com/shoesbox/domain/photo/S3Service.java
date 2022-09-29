package com.shoesbox.domain.photo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.luciad.imageio.webp.WebPWriteParam;
import com.shoesbox.domain.photo.exception.ImageUploadFailureException;
import io.jsonwebtoken.lang.Assert;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
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

    // 리사이징 할 파일 크기
    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;
    private static final String CONTENT_TYPE = "image/webp";
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

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
        checkExtension(fileName);

        // 파일이름을 무작위 값으로 변경
        fileName = UUID.randomUUID() + ".webp";
        try {
            // WebP로 변환
            File createdImage = ConvertToWebp(file.getInputStream());

            // 이미지 업로드
            uploadImageToS3(createdImage);
        } catch (java.io.IOException e) {
            throw new ImageUploadFailureException(e.getMessage(), e);
        }

        // url string 리턴
        return s3Client.getUrl(bucket, fileName).toString();
    }

    // 파일 삭제
    public void deleteObjectByImageUrl(String imageUrl) {
        // split을 통해 나누고 나눈 length에서 1을 빼서 마지막 값(파일명)을 사용함.
        String sourceKey = imageUrl.split("/")[imageUrl.split("/").length - 1];

        // 소스키로 s3에서 삭제
        s3Client.deleteObject(bucket, sourceKey);
    }

    public String uploadThumbnail(MultipartFile file) {
        // 파일 이름 받아오기
        String fileName = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase();
        // 확장자 점검
        checkExtension(fileName);
        // 확장자 추출
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        try {
            // 리사이즈용 임시 파일 생성
            File originalThumbnail = File.createTempFile(
                    "s_" + UUID.randomUUID(),
                    fileExtension,
                    new File((System.getProperty("user.dir") + "/tmp/")));
            // Thumbnailator로 리사이징
            Thumbnails.of(file.getInputStream())
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .toFile(originalThumbnail);
            // WebP로 변환
            var originalInputStream = new FileInputStream(originalThumbnail);
            File encodedThumbnail = ConvertToWebp(originalInputStream);
            // 임시 파일 삭제
            originalInputStream.close();
            Assert.isTrue(originalThumbnail.delete(), "임시 파일이 삭제되지 않음!");
            // 이미지 업로드
            uploadImageToS3(encodedThumbnail);
        } catch (java.io.IOException e) {
            throw new ImageUploadFailureException(e.getMessage(), e);
        }

        return s3Client.getUrl(bucket, fileName).toString();    ///url string 리턴
    }

    private File ConvertToWebp(InputStream originalInputStream) throws IOException {
        // 기존 파일
        BufferedImage originalImage = ImageIO.read(originalInputStream);

        // 인코딩할 빈 파일
        File createdImage = new File(System.getProperty("user.dir") + "/tmp/" + UUID.randomUUID() + ".webp");

        // WebP ImageWriter 인스턴스 생성
        ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();

        // 인코딩 설정
        WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionType(writeParam.getCompressionTypes()[WebPWriteParam.LOSSY_COMPRESSION]);
        writeParam.setCompressionQuality(1f);

        // ImageWriter 반환값(빈 파일) 설정
        var createdOutputStream = new FileImageOutputStream(createdImage);
        writer.setOutput(createdOutputStream);

        // 인코딩
        writer.write(null, new IIOImage(originalImage, null, null), writeParam);
        createdOutputStream.close();
        originalInputStream.close();
        return createdImage;
    }

    private void checkExtension(String fileName) {
        if (!(fileName.endsWith(".bmp")
                || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png"))) {
            throw new ImageUploadFailureException("이미지 파일 형식은 bmp, jpg, jpeg, png 중 하나여야 합니다.");
        }
    }

    private void uploadImageToS3(File imageFile) throws IOException {
        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(imageFile.length());
        objMeta.setContentType(CONTENT_TYPE);

        // 파일 업로드
        InputStream inputStream = new FileInputStream(imageFile);
        s3Client.putObject(new PutObjectRequest(bucket, imageFile.getName(), inputStream, objMeta)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        inputStream.close();
        // 임시 파일 삭제
        Assert.isTrue(imageFile.delete(), "임시 파일이 삭제되지 않음!");
    }
}
