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

import javax.imageio.ImageIO;
import java.io.*;
import java.util.Objects;
import java.util.UUID;

import static com.shoesbox.global.exception.ExceptionCode.*;

@Slf4j
@Component
public class ImageUtil {
    // 리사이징 할 파일 크기
    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;
    private byte[] buffer;

    public File resizeImage(File originalFile) {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(originalFile))) {
            // 리사이즈용 임시 파일 생성
            File tempFile = new File("thumbnail_" + UUID.randomUUID() + ".webp");
            // 재사용할 수 있게 Byte 배열에 저장
            // Thumbnailator로 리사이징
            Thumbnails.of(inputStream)
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .crop(Positions.CENTER)
                    .toFile(tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new ImageProcessException(IMAGE_RESIZE_FAILURE, e.getLocalizedMessage(), e);
        }
    }

    public File convertToWebp(MultipartFile originalFile) {
        checkExtension(Objects.requireNonNull(originalFile.getOriginalFilename()).toLowerCase());
        try (InputStream inputStream = new BufferedInputStream(originalFile.getInputStream())) {
            // 인코딩할 빈 파일
            File tempFile = new File(UUID.randomUUID() + ".webp");
            // Thumbnailator로 리사이징
            copyInputStream(inputStream);
            InputStream is = getBufferedInputStream();
            int orientation = getOrientation(is);
            int degrees = getDegrees(orientation);
            // Thumbnailator로 리사이징
            is = getBufferedInputStream();
            Thumbnails.of(ImageIO.read(is))
                    .scale(1)
                    .rotate(degrees)
                    .outputFormat("webp")
                    .toFile(tempFile);
            is.close();
            return tempFile;
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
    }

    private int getOrientation(InputStream inputStream) {
        // 사진 방향 : 1~6, 1이 정상
        int orientation;
        try {
            // 메타데이터
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
            // orientatino 가져오기
            Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            orientation = directory == null ? 1 : directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            inputStream.close();
        } catch (IOException | ImageProcessingException | MetadataException e) {
            throw new ImageProcessException(IMAGE_ROTATE_FAILURE, e.getLocalizedMessage(), e);
        }
        if (orientation == 0) {
            throw new ImageProcessException(IMAGE_ROTATE_FAILURE, "Image orientation is 0!");
        }
        return orientation;
    }

    private int getDegrees(int orientation) {
        switch (orientation) {
            // -90도
            case 6:
                return 90;
            // 180도
            case 3:
                return 180;
            // 90도
            case 8:
                return -90;
            default:
                return 0;
        }
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

    private void copyInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(inputStream.readAllBytes());
        baos.flush();
        buffer = baos.toByteArray();
        inputStream.close();
        baos.close();
    }

    private InputStream getBufferedInputStream() {
        return new BufferedInputStream(new ByteArrayInputStream(buffer));
    }
}
