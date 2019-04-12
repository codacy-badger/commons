/*
 * Copyright 2017-2019 Pamarin.com
 */
package com.pamarin.oauth2.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author jitta
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2AuthorizationCode implements OAuth2Token {

    private String id;

    private String tokenId;

    private long issuedAt;

    private long expiresAt;

    private String userId;

    private String clientId;

    private int expireMinutes;

    private String secretKey;

    private String sessionId;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
