package com.shoesbox;

import com.shoesbox.domain.friend.FriendService;
import com.shoesbox.domain.friend.FriendState;
import com.shoesbox.domain.friend.dto.FriendRequestDto;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.domain.photo.Photo;
import com.shoesbox.domain.photo.PhotoRepository;
import com.shoesbox.domain.post.Post;
import com.shoesbox.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.TimeZone;

import static java.time.temporal.ChronoUnit.DAYS;

@EnableJpaAuditing
@SpringBootApplication
@RequiredArgsConstructor
public class ShoesBoxApplication {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PhotoRepository photoRepository;
    private final FriendService friendService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(ShoesBoxApplication.class, args);
    }

    @PostConstruct
    public void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        fillDB(LocalDate.of(2022, 8, 10), LocalDate.of(2022, 9, 10));
    }

    @Transactional
    protected void fillDB(LocalDate start, LocalDate end) {
        var member1 = Member.builder()
                .nickname("java")
                .email("a@a")
                .profileImageUrl("https://i.ibb.co/N27FwdP/image.png")
                .password(bCryptPasswordEncoder.encode("1234"))
                .build();
        memberRepository.save(member1);

        var member2 = Member.builder()
                .nickname("kotlin")
                .email("b@a")
                .profileImageUrl("https://i.ibb.co/N27FwdP/image.png")
                .password(bCryptPasswordEncoder.encode("1234"))
                .build();
        memberRepository.save(member2);

        friendService.requestFriend(
                member1.getId(),
                member1.getNickname(),
                FriendRequestDto.builder()
                        .email(member2.getEmail())
                        .build());

        friendService.acceptFriend(member1.getId(), member2.getId(), FriendState.REQUEST);

        // 임의의 날짜 사이에 일기 작성
        createPosts(start, end, member1);
    }

    private void createPosts(LocalDate start, LocalDate end, Member member) {
        for (int i = 0; i < DAYS.between(start, end) + 1; i++) {
            var index = postRepository.count();
            var post = Post.builder()
                    .nickname(member.getNickname())
                    .member(member)
                    .title("제목 " + (index + 1))
                    .content("본문 " + (index + 1))
                    .build();
            post.setPhotos(new ArrayList<>());
            post.setCreatedDate(LocalDate.of(
                    start.getYear(),
                    start.plusDays(index).getMonthValue(),
                    start.plusDays(index).getDayOfMonth()));
            post = postRepository.save(post);

            Photo photo = Photo.builder()
                    .url("url" + (index + 1))
                    .post(post)
                    .member(member)
                    .build();
            photoRepository.save(photo);
            post.getPhotos().add(photo);
        }
    }
}
