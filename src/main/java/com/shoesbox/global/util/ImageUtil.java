package com.shoesbox.global.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.shoesbox.domain.photo.exception.ImageConvertFailureException;
import com.shoesbox.domain.photo.exception.ImageUploadFailureException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
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

    public File convertMultipartFiletoWebP(MultipartFile file) {
        var origianlFile = convertMultipartFiletoFile(file);
        return convertToWebp(origianlFile);
    }

    public File resizeImage(MultipartFile multipartFile) {
        // 확장자 검사
        checkExtension(Objects.requireNonNull(multipartFile.getOriginalFilename()).toLowerCase());
        // 리사이즈용 임시 파일 생성
        File tempFile = new File("thumbnail_" + UUID.randomUUID() + ".webp");
        try {
            // Thumbnailator로 리사이징
            Thumbnails.of(multipartFile.getInputStream())
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .outputFormat("webp")
                    .crop(Positions.CENTER)
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
        File tempFile = new File("thumbnail_" + UUID.randomUUID() + ".webp");
        try (InputStream inputStream = new FileInputStream(originalFile)) {
            Thumbnails.of(ImageIO.read(inputStream))
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .outputFormat("webp")
                    .crop(Positions.CENTER)
                    .toFile(tempFile);
        } catch (IOException e) {
            throw new ImageConvertFailureException(e.getLocalizedMessage(), e);
        }
        originalFile.delete();
        return tempFile;
    }

    public File convertToWebp(File originalFile) {
        try (FileInputStream inputStream = new FileInputStream(originalFile)) {
            // 인코딩할 빈 파일
            File tempFile = new File(UUID.randomUUID() + ".webp");
            var image = ImageIO.read(originalFile);
            // Thumbnailator로 리사이징
            Thumbnails.of(inputStream)
                    .size(image.getWidth(), image.getHeight())
                    .outputQuality(1f)
                    .outputFormat("webp")
                    .crop(Positions.CENTER)
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

    // 이미지 회전 수정
    public List<File> correctImageRotation(List<MultipartFile> imageFiles) {
        List<File> files = new ArrayList<>();
        for (MultipartFile mFile : imageFiles) {
            String fileExtension = checkExtension(Objects.requireNonNull(mFile.getOriginalFilename()).toLowerCase());
            // 회전 방향 : 1인 경우 정상
            int orientation = getOrientation(mFile);
            if (orientation == 0) {
                throw new ImageConvertFailureException("Image orientation is wrong!");
            }
            try {
                File originalFile = convertMultipartFiletoFile(mFile);
                BufferedImage rotatedImage = rotateImageForMobile(originalFile, orientation);
                File file = new File(mFile.getOriginalFilename());
                ImageIO.write(rotatedImage, fileExtension, file);
                files.add(file);
                mFile.getInputStream().close();
            } catch (IOException e) {
                files.add((File) mFile);
                throw new ImageConvertFailureException("POST SERVICE - IMAGE_ROTATION : " + e);
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
            throw new ImageConvertFailureException("Thumbnail Orientation : " + e);
        }
        return orientation;
    }

    private BufferedImage rotateImageForMobile(File imageToRotate, int orientation) {
        log.info("rotation begin");
        try {
            BufferedImage bufferedImage = ImageIO.read(imageToRotate);
            if (orientation == 6) { //정위치
                return bufferedImage;
            } else if (orientation == 1) {//왼쪽으로 눞였을때
                return rotateImage(bufferedImage, 90);
            } else if (orientation == 3) {//오른쪽으로 눞였을때
                return rotateImage(bufferedImage, 180);
            } else if (orientation == 8) {//180도
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
            var tempImageFile = new File(file.getOriginalFilename());
            file.transferTo(tempImageFile.toPath());
            file.getInputStream().close();
            return tempImageFile;
        } catch (IOException e) {
            throw new RuntimeException("multipart file transfer failed", e);
        }
    }
}
