package com.shoesbox;

import com.shoesbox.post.Post;
import com.shoesbox.post.PostRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;

@EnableJpaAuditing
@SpringBootApplication
public class ShoesBoxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoesBoxApplication.class, args);
    }


    @Bean
    public CommandLineRunner demo(PostRepository repository) {
        return (args) -> {
            Post post1 = new Post("제목1", "내용1"); // 생성
            repository.save(post1); // 삽입

            List<Post> postList = repository.findAll(); // 조회
            for (int i = 0; i < postList.size(); i++) {
                Post p = postList.get(i);
                System.out.println(p.getTitle());
                System.out.println(p.getContent());
            }
        };
    }
}
