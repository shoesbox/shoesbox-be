package com.shoesbox.domain.social;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.LinkedMultiValueMap;

@Getter
@AllArgsConstructor
public class OAuthRequest {
    private String tokenUrl;
    private LinkedMultiValueMap<String, String> map;
}
