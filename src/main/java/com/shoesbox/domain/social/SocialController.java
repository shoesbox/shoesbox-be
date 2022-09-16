package com.shoesbox.domain.social;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shoesbox.global.common.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SocialController {

    private final ProviderService providerService;

    @GetMapping("/oauth2/authorization/{provider}")
    public ResponseEntity<Object> socialLogin(@RequestParam String code, @PathVariable String provider) throws JsonProcessingException {
        System.out.println("code: " + code + " / provider: " + provider);
        return ResponseHandler.ok(providerService.SocialLogin(code, provider));
    }
}
