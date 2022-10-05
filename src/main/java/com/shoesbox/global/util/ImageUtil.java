package com.shoesbox.global.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.shoesbox.global.exception.runtime.image.ImageProcessException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class ImageUtil {
    // 리사이징 할 파일 크기
    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;

    public File createThumbnail(File originalFile) {
        // 파일 이름
        String fileName = Objects.requireNonNull(originalFile.getName()).toLowerCase();
        // 확장자 추출
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        // 리사이즈용 임시 파일 생성
        File tempFile = new File("thumbnail_" + UUID.randomUUID() + fileExtension);
        try (InputStream inputStream = new FileInputStream(originalFile)) {
            Thumbnails.of(inputStream)
                    .outputQuality(0.8f)
                    .crop(Positions.CENTER_LEFT)
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .toFile(tempFile);
        } catch (IOException e) {
            throw new ImageProcessException(IMAGE_RESIZE_FAILURE, e.getLocalizedMessage(), e);
        }
        return convertToWebp(tempFile);
    }

    public File convertToWebp(File originalFile) {
        try (InputStream originalInputStream = new FileInputStream(originalFile)) {
            // 기존 파일
            BufferedImage originalImage = ImageIO.read(originalInputStream);

            // 인코딩할 빈 파일
            File createdImage = new File("s_" + UUID.randomUUID() + ".webp");

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
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>> [File to WebP] 이미지 변환 성공!");
            return createdImage;
        } catch (IOException e) {
            log.error("failed to convertToWebp(): " + originalFile.getName());
            throw new ImageProcessException(IMAGE_CONVERT_FAILURE, e.getLocalizedMessage(), e);
        } finally {
            try {
                originalFile.getInputStream().close();
            } catch (IOException e) {
                log.error("failed to close InputStream: " + originalFile.getName());
                throw new ImageProcessException(IMAGE_CONVERT_FAILURE, e.getLocalizedMessage(), e);
            }
        }
        return files;
    }

    private int getOrientation(MultipartFile file) {
        // 사진 방향 : 1~6, 1이 정상
        int orientation;
        try {
            // 이미지로부터 바이트 생성
            byte[] imgBytes = file.getBytes();

            // 스트림에 넣어주기
            BufferedInputStream bufferedIS = new BufferedInputStream(new ByteArrayInputStream(imgBytes));

            // 메타데이터
            Metadata metadata = ImageMetadataReader.readMetadata(bufferedIS);

            // orientatino 가져오기
            Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            orientation = directory == null ? 1 : directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);

            // 스트림 종료
            bufferedIS.close();
        } catch (IOException | ImageProcessingException | MetadataException e) {
            throw new ImageProcessException(IMAGE_ROTATE_FAILURE, e.getLocalizedMessage(), e);
        }
        if (orientation == 0) {
            throw new ImageProcessException(IMAGE_ROTATE_FAILURE, "Image orientation is 0!");
        }
        return orientation;
    }

    private BufferedImage rotateImageForMobile(InputStream imageToRotate, int orientation) {
        log.info("rotation begin");
        try {
            BufferedImage bufferedImage = ImageIO.read(imageToRotate);
            imageToRotate.close();
            if (orientation == 1) { //정위치
                return bufferedImage;
            } else if (orientation == 6) {//왼쪽으로 눞였을때
                return rotateImage(bufferedImage, 90);
            } else if (orientation == 3) {//180도
                return rotateImage(bufferedImage, 180);
            } else if (orientation == 8) {//오른쪽으로 눞였을때
                return rotateImage(bufferedImage, 270);
            } else {
                return bufferedImage;
            }
        } catch (IOException e) {
            throw new ImageConvertFailureException(e.getLocalizedMessage(), e);
        }
    }

    private BufferedImage rotateImage(BufferedImage orgImage, int radians) {
        BufferedImage newImage;

        if (radians == 90 || radians == 270) {
            newImage = new BufferedImage(orgImage.getHeight(), orgImage.getWidth(), orgImage.getType());
        } else if (radians == 180) {
            newImage = new BufferedImage(orgImage.getWidth(), orgImage.getHeight(), orgImage.getType());
        } else {
            return orgImage;
        }

        Graphics2D graphics = (Graphics2D) newImage.getGraphics();
        graphics.rotate(Math.toRadians(radians), newImage.getWidth() / 2, newImage.getHeight() / 2);
        graphics.translate((newImage.getWidth() - orgImage.getWidth()) / 2, (newImage.getHeight() - orgImage.getHeight()) / 2);
        graphics.drawImage(orgImage, 0, 0, orgImage.getWidth(), orgImage.getHeight(), null);

        return newImage;
    }

    private String checkExtension(String fileName) {
        if (!(fileName.endsWith(".bmp")
                || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png")
                || fileName.endsWith(".webp"))) {
            throw new ImageProcessException(INVALID_IMAGE_FORMAT);
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private File convertMultipartFiletoFile(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            throw new ImageProcessException(IMAGE_CONVERT_FAILURE, "변환하려는 file이 null입니다.");
        }
        try {
            // 파일 이름
            var tempImageFile = new File(file.getOriginalFilename());
            file.transferTo(tempImageFile.toPath());
            file.getInputStream().close();
            return tempImageFile;
        } catch (IOException e) {
            throw new RuntimeException("multipart file transfer failed", e);
        }
    }
}
