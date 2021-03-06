/*
 * Copyright 2017-2019 Pamarin.com
 */
package com.pamarin.commons.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author jittagornp &lt;http://jittagornp.me&gt; create : 2017/11/12
 */
@Component
public class DefaultHostUrlProvider implements HostUrlProvider {

    @Value("${server.hostUrl:#{http://localhost:8080}}")
    private String hostUrl;

    @Override
    public String provide() {
        return hostUrl;
    }

}
