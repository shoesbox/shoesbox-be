package com.shoesbox.domain.photo;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@NoArgsConstructor
@Service
public class PhotoService {

    public int getOrientation(MultipartFile file) {
        // 사진 방향 : 1~6, 1이 정상
        int orientation = 1;

        try {
            // 이미지로부터 바이트 생성
            byte[] imgBytes = file.getBytes();

            // 스트림에 넣어주기
            BufferedInputStream bufferedIS = new BufferedInputStream(new ByteArrayInputStream(imgBytes));

            // 메타데이터
            Metadata metadata = ImageMetadataReader.readMetadata(bufferedIS);

            // orientatino 가져오기
            Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);

            // 스트림 종료
            bufferedIS.close();
        } catch (IOException e) {
            throw new RuntimeException("Thumbnail Orientation : " + e);
        } catch (ImageProcessingException e) {
            throw new RuntimeException("Thumbnail Orientation : " + e);
        } catch (MetadataException e) {
            throw new RuntimeException("Thumbnail Orientation : " + e);
        }
        return orientation;
    }

    public static BufferedImage rotateImageForMobile(InputStream imageToRotate, int orientation) {
        log.info("rotation begin");
        try {
            BufferedImage bufferedImage = ImageIO.read(imageToRotate);
            if (orientation == 6) { //정위치

                return rotateImage(bufferedImage, 90);

            } else if (orientation == 1) { //왼쪽으로 눞였을때

                return bufferedImage;

            } else if (orientation == 3) {//오른쪽으로 눞였을때

                return rotateImage(bufferedImage, 180);

            } else if (orientation == 8) {//180도

                return rotateImage(bufferedImage, 270);

            } else {

                return bufferedImage;

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedImage rotateImage(BufferedImage orgImage, int radians) {
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

}
