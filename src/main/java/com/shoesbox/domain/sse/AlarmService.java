package com.shoesbox.domain.sse;

import com.shoesbox.domain.sse.dto.AlarmResponseDto;
import com.shoesbox.global.exception.runtime.EntityNotFoundException;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AlarmService {
    private final AlarmRepository alarmRepository;

    @Transactional(readOnly = true)
    public List<AlarmResponseDto> getAlarmList(long currentMemberId) {
        return alarmRepository.findAllByReceiverMemberId(currentMemberId)
                .stream()
                .map(this::toAlarmResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public String deleteAlarm(long currentMemberId, long alarmId) {
        Alarm alarm = getAlarm(alarmId);
        checkSelfAuthorization(currentMemberId, alarm.getReceiverMemberId());
        alarmRepository.delete(alarm);
        return "댓글 삭제 성공";
    }

    @Transactional
    public String deleteAllAlarm(long currentMemberId) {
        List<Alarm> alarms = alarmRepository.findAllByReceiverMemberId(currentMemberId);
        for (Alarm alarm : alarms) {
            checkSelfAuthorization(currentMemberId, alarm.getReceiverMemberId());
            alarmRepository.delete(alarm);
        }
        return "댓글 전체 삭제 성공";
    }

    private AlarmResponseDto toAlarmResponseDto(Alarm alarm) {
        // content 형태 : postId or commentId + "," + month + "," + day
        String[] data = alarm.getContent().split(",");
        return AlarmResponseDto.builder()
                .alarmId(alarm.getId())
                .senderMemberId(alarm.getSenderMember().getId())
                .senderMemberNickname(alarm.getSenderMember().getNickname())
                .receiverMemberId(alarm.getReceiverMemberId())
                .messageType(alarm.getMessageType())
                .postId(Long.parseLong(data[0]))
                .month(Integer.parseInt(data[1]))
                .day(Integer.parseInt(data[2]))
                .build();
    }

    private Alarm getAlarm(long alarmId) {
        log.info(">>>>>>>>>>>>>> GET Alarm");
        return alarmRepository.findById(alarmId).orElseThrow(
                () -> new EntityNotFoundException(Alarm.class.getPackageName()));
    }

    private void checkSelfAuthorization(long currentMemberId, long targetId) {
        log.info(">>>>>>>>>>>>>> Check Authorization ");
        if (currentMemberId != targetId) {
            throw new UnAuthorizedException("접근 권한이 없습니다.");
        }
    }
}
