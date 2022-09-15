package com.shoesbox.domain.post;

import com.shoesbox.domain.friend.FriendService;
import com.shoesbox.domain.post.dto.PostRequestDto;
import com.shoesbox.global.common.ResponseHandler;
import com.shoesbox.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final FriendService friendService;
    private final TemporalField fieldISO = WeekFields.of(Locale.KOREA).dayOfWeek();

    // 생성
    @PostMapping
    public ResponseEntity<Object> createPost(PostRequestDto postRequestDto) {
        long memberId = SecurityUtil.getCurrentMemberId();
        String nickname = SecurityUtil.getCurrentMemberNickname();

        postService.validatePostRequest(postRequestDto, memberId);
        return ResponseHandler.ok(postService.createPost(nickname, memberId, postRequestDto));
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<Object> getPosts(
            @RequestParam(value = "id", defaultValue = "0", required = false) long memberId,
            @RequestParam(value = "y", defaultValue = "0", required = false) int year,
            @RequestParam(value = "m", defaultValue = "0", required = false) int month) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        // 0이면 자기 자신
        if (memberId == 0) {
            memberId = currentMemberId;
        } else {
            // 아니면 친구인지 확인
            if (memberId != currentMemberId && !friendService.isFriend(memberId, currentMemberId)) {
                throw new IllegalArgumentException("해당 회원과 친구 상태가 아닙니다.");
            }
        }

        if (year == 0) {
            year = LocalDate.now().getYear();
        }

        if (month == 0) {
            month = LocalDate.now().getMonthValue();
        }

        // 찾으려는 달의 첫 번째 일요일의 날짜를 구한다
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate firstMonday = firstDay.with(fieldISO, 1);

        // 찾으려는 달의 마지막 토요일의 날짜를 구한다
        LocalDate lastDay = LocalDate.of(year, month, LocalDate.now().getMonth().maxLength());
        LocalDate lastSaturday = lastDay.with(fieldISO, 7);

        // 총 몇 주를 표시해야 하는지 계산한다.
        int weeks = (int) ChronoUnit.WEEKS.between(firstMonday, lastSaturday) + 1;

        return ResponseHandler.ok(postService.getPosts(memberId, firstMonday, lastSaturday, weeks));
    }

    // 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<Object> getPost(@PathVariable long postId) {
        long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(postService.getPost(memberId, postId));
    }

    // 수정
    @PutMapping("/{postId}")
    public ResponseEntity<Object> updatePost(@PathVariable long postId, PostRequestDto postRequestDto) {
        long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(postService.updatePost(memberId, postId, postRequestDto));
    }

    // 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Object> deletePost(@PathVariable long postId) {
        long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(postService.deletePost(memberId, postId));
    }
}
