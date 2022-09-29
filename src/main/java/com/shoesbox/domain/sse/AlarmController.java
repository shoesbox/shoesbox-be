package com.shoesbox.domain.sse;

import com.shoesbox.domain.guest.GuestService;
import com.shoesbox.global.common.ResponseHandler;
import com.shoesbox.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/alarm")
@RequiredArgsConstructor
@RestController
public class AlarmController {
    private final AlarmService alarmService;
    private final GuestService guestService;

    @GetMapping
    public ResponseEntity<Object> getAlarmList() {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        guestService.guestCheck(currentMemberId);

        return ResponseHandler.ok(alarmService.getAlarmList(currentMemberId));
    }

    @DeleteMapping("/{alarmId}")
    public ResponseEntity<Object> deleteAlarm(@PathVariable long alarmId) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        guestService.guestCheck(currentMemberId);

        return ResponseHandler.ok(alarmService.deleteAlarm(currentMemberId, alarmId));
    }

    @DeleteMapping()
    public ResponseEntity<Object> deleteAllAlarm() {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        guestService.guestCheck(currentMemberId);

        return ResponseHandler.ok(alarmService.deleteAllAlarm(currentMemberId));
    }
}
