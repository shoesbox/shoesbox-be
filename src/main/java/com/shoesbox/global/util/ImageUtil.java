package com.shoesbox.global.util;

import com.shoesbox.domain.photo.exception.ImageConvertFailureException;
import com.shoesbox.domain.photo.exception.ImageUploadFailureException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class ImageUtil {
    // 리사이징 할 파일 크기
    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;

    public File convertMultipartFiletoWebP(MultipartFile file) {
        var origianlFile = convertMultipartFiletoFile(file);
        return convertToWebp(origianlFile);
    }

    public File resizeImage(MultipartFile multipartFile) {
        // 확장자 검사
        checkExtension(Objects.requireNonNull(multipartFile.getOriginalFilename()).toLowerCase());
        // 리사이즈용 임시 파일 생성
        File tempFile = new File("tmp\\" + "thumbnail_" + UUID.randomUUID() + ".webp");

        try {
            // Thumbnailator로 리사이징
            Thumbnails.of(multipartFile.getInputStream())
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .outputFormat("webp")
                    .toFile(tempFile);
            multipartFile.getInputStream().close();
        } catch (IOException e) {
            log.error("이미지 리사이즈 실패!");
            throw new ImageUploadFailureException(e.getMessage(), e);
        }

        return tempFile;
    }

    public File resizeImage(File originalFile) {
        // 리사이즈용 임시 파일 생성
        File tempFile = new File("tmp\\" + "thumbnail_" + UUID.randomUUID() + ".webp");
        try (InputStream inputStream = new FileInputStream(originalFile)) {
            Thumbnails.of(ImageIO.read(inputStream))
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .outputFormat("webp")
                    .toFile(tempFile);
        } catch (IOException e) {
            throw new ImageConvertFailureException(e.getLocalizedMessage(), e);
        }

        return tempFile;
    }

    public File convertToWebp(File originalFile) {
        try (FileInputStream inputStream = new FileInputStream(originalFile)) {
            // 인코딩할 빈 파일
            File tempFile = new File("tmp\\" + UUID.randomUUID() + ".webp");
            var image = ImageIO.read(originalFile);
            // Thumbnailator로 리사이징
            Thumbnails.of(inputStream)
                    .size(image.getWidth(), image.getHeight())
                    .outputQuality(1f)
                    .outputFormat("webp")
                    .toFile(tempFile);
            inputStream.close();
            originalFile.delete();
            return tempFile;
        } catch (IOException e) {
            log.error("이미지 변환 실패!");
            originalFile.delete();
            throw new ImageConvertFailureException(e.getMessage(), e);
        }
    }

    private String checkExtension(String fileName) {
        if (!(fileName.endsWith(".bmp")
                || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png")
                || fileName.endsWith(".webp"))) {
            throw new ImageUploadFailureException("이미지 파일 형식은 bmp, jpg, jpeg, png, webp 중 하나여야 합니다.", null);
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private File convertMultipartFiletoFile(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            throw new ImageConvertFailureException("변환하려는 file이 null입니다.");
        }
        try {
            // 파일 이름
            var tempImageFile = new File("tmp\\" + file.getOriginalFilename());
            file.transferTo(tempImageFile.toPath());
            file.getInputStream().close();
            return tempImageFile;
        } catch (IOException e) {
            throw new RuntimeException("multipart file transfer failed", e);
        }
    }
}
