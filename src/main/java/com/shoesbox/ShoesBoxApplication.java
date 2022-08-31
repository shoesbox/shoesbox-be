package com.shoesbox;

import com.shoesbox.post.Post;
import com.shoesbox.post.PostRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class ShoesBoxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoesBoxApplication.class, args);
    }


    @Bean
    public CommandLineRunner demo(PostRepository repository) {
        return (args) -> {
            Post post1 = new Post("제목1", "내용1", true, 2022, 8, 29, ""); // 생성
            Post post2 = new Post("제목2", "내용2", true, 2022, 8, 30, ""); // 생성
            Post post3 = new Post("제목3", "내용3", true, 2022, 8, 31, ""); // 생성
            repository.save(post1); // 삽입
            repository.save(post2); // 삽입
            repository.save(post3); // 삽입
            List<Post> postList = repository.findAll(); // 조회
            for (int i = 0; i < postList.size(); i++) {
                Post p = postList.get(i);
                System.out.println(p.getTitle());
                System.out.println(p.getContent());
            }
        };
    }
}
