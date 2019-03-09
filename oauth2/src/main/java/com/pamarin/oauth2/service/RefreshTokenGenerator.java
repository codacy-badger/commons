/*
 * Copyright 2017-2019 Pamarin.com
 */
package com.pamarin.oauth2.service;

import com.pamarin.oauth2.model.TokenBase;

/**
 * @author jittagornp &lt;http://jittagornp.me&gt; create : 2017/12/05
 */
public interface RefreshTokenGenerator {

    String generate(TokenBase base);

}
