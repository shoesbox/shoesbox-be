package com.shoesbox.domain.sse;

import com.shoesbox.global.common.ResponseHandler;
import com.shoesbox.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/alarm")
@RequiredArgsConstructor
@RestController
public class AlarmController {
    private final AlarmService alarmService;

    @GetMapping
    public ResponseEntity<Object> getAlarmList() {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(alarmService.getAlarmList(currentMemberId));
    }

    @DeleteMapping("/{alarmId}")
    public ResponseEntity<Object> deleteAlarm() {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(alarmService.deleteAlarm(currentMemberId));
    }
}
