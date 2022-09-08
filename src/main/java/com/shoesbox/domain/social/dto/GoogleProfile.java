package com.shoesbox.domain.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleProfile {
    private String email;
    private String name;
    private String picture;
}
