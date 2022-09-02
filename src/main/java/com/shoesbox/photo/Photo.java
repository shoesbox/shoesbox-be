//package com.shoesbox.photo;
//
//import com.shoesbox.post.Post;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//
//@Getter
//@NoArgsConstructor
//@Entity
//@Table(name = "photo")
//public class Photo {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "post_id")
//    private Post post;
//
//    // 동일한 이름을 가진 파일이 업로드될 경우 오류가 발생
//    // 파일 원본명과 파일 저장 경로를 따로 지정
//    @Column
//    @NotNull
//    private String origFileName;
//
//    @Column
//    @NotNull
//    private String filePath;
//
//    private Long fileSize;
//
//    @Builder
//    public Photo(String origFileName, String filePath, Long fileSize) {
//        this.origFileName = origFileName;
//        this.filePath = filePath;
//        this.fileSize = fileSize;
//    }
//
//    // 사진과 맵핑된 게시글 저장
//    public void setPost(Post post) {
//        this.post = post;
//        if (!post.getPhoto().contains(this)) {
//            post.getPhoto().add(this);
//        }
//    }
//}
