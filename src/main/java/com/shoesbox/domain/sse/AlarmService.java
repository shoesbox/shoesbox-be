package com.shoesbox.domain.sse;

import com.shoesbox.domain.sse.dto.AlarmResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AlarmService {
    private final AlarmRepository alarmRepository;

    @Transactional(readOnly = true)
    public List<AlarmResponseDto> getAlarmList(long currentMemberId) {
        List<Alarm> alarms = alarmRepository.findAllByReceiveMemberId(currentMemberId);
        List<AlarmResponseDto> alarmList = new ArrayList<>();
        for (Alarm alarm : alarms) {
            alarmList.add(toAlarmResponseDto(alarm));
        }

        return alarmList;
    }

    @Transactional
    public String deleteAlarm(long currentMemberId) {
        Alarm alarm = alarmRepository.findByReceiveMemberId(currentMemberId).orElseThrow(
                () -> new IllegalArgumentException("해당 알람을 찾을 수 없습니다."));

        alarmRepository.delete(alarm);

        return "댓글 삭제 성공";
    }

    private AlarmResponseDto toAlarmResponseDto(Alarm alarm) {
        // content 형태 : postId or commentId + "," + month + "," + day
        String[] data = alarm.getContent().split(",");
        return AlarmResponseDto.builder()
                .alarmId(alarm.getId())
                .sendMemberId(alarm.getSendMember().getId())
                .sendMemberNickname(alarm.getSendMember().getNickname())
                .receiveMemberId(alarm.getReceiveMemberId())
                .messageType(alarm.getMessageType())
                .postId(Long.parseLong(data[0]))
                .month(Integer.parseInt(data[1]))
                .day(Integer.parseInt(data[2]))
                .build();
    }
}
