/*
 * Copyright 2017-2019 Pamarin.com
 */
package com.pamarin.oauth2.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author jittagornp &lt;http://jittagornp.me&gt; create : 2017/11/12
 */
@Getter
@Setter
@Builder
public class TokenBase {
    
    private String id;

    private String userId;
    
    private String clientId;
    
    private String sessionId;

}
