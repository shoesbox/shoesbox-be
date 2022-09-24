package com.shoesbox.domain.social;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shoesbox.global.common.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SocialController {

    private final ProviderService providerService;

    @GetMapping("/oauth2/authorization/{provider}")
    public ResponseEntity<Object> socialLogin(@RequestParam String code, @PathVariable String provider) throws JsonProcessingException {
        System.out.println("code: " + code + " / provider: " + provider);
        log.info(">>>>>>>>> authorization code : " + code + " / provider: " + provider);
        return ResponseHandler.ok(providerService.SocialLogin(code, provider));
    }
}
