/*
 * Copyright 2017-2019 Pamarin.com
 */
package com.pamarin.oauth2.client.sdk;

import com.pamarin.commons.exception.AuthenticationException;
import com.pamarin.commons.exception.AuthorizationException;
import com.pamarin.commons.provider.HostUrlProvider;
import com.pamarin.commons.util.QuerystringBuilder;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static org.springframework.util.StringUtils.hasText;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author jitta
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OAuth2SessionFilter extends OncePerRequestFilter {

    private final HostUrlProvider hostUrlProvider;

    private final OAuth2ClientOperations clientOperations;

    private final OAuth2AccessTokenResolver accessTokenResolver;

    private final OAuth2RefreshTokenResolver refreshTokenResolver;

    private final OAuth2TokenResolver accessTokenHeaderResolver;

    private final OAuth2LoginSession loginSession;

    private final OAuth2AccessTokenRepository accessTokenRepository;

    private final OAuth2AuthorizationState authorizationState;

    @Value("${oauth2.session-filter.disabled}")
    private Boolean disabled;

    @Autowired
    public OAuth2SessionFilter(
            HostUrlProvider hostUrlProvider,
            OAuth2ClientOperations clientOperations,
            OAuth2AccessTokenResolver accessTokenResolver,
            OAuth2RefreshTokenResolver refreshTokenResolver
    ) {
        this.hostUrlProvider = hostUrlProvider;
        this.clientOperations = clientOperations;
        this.accessTokenResolver = accessTokenResolver;
        this.refreshTokenResolver = refreshTokenResolver;
        this.accessTokenHeaderResolver = new RequestHeaderOAuth2TokenResolver();
        this.loginSession = new DefaultOAuth2LoginSession(clientOperations);
        this.accessTokenRepository = new DefaultOAuth2AccessTokenRepository(hostUrlProvider, clientOperations);
        this.authorizationState = new DefaultOAuth2AuthorizationState();
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if (disabled == null) {
            return false;
        }
        return disabled;
    }

    private String getAuthorizationUrl(HttpServletRequest httpReq) {
        String state = authorizationState.create(httpReq);
        return "{server}/authorize?".replace("{server}", clientOperations.getAuthorizationServerHostUrl())
                + new QuerystringBuilder()
                        .addParameter("response_type", "code")
                        .addParameter("client_id", clientOperations.getClientId())
                        .addParameter("redirect_uri", hostUrlProvider.provide())
                        .addParameter("scope", clientOperations.getScope())
                        .addParameter("state", state)
                        .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpReq, HttpServletResponse httpResp, FilterChain chain) throws ServletException, IOException {
        try {
            filter(httpReq, httpResp);
            chain.doFilter(httpReq, httpResp);
        } catch (AuthorizationException ex) {
            httpResp.sendRedirect(getAuthorizationUrl(httpReq));
        } catch (RequireRedirectException ex) {
            httpResp.sendRedirect("/");
        }
    }

    private void filter(HttpServletRequest httpReq, HttpServletResponse httpResp) {
        try {
            doFilter(httpReq, httpResp);
        } catch (Exception ex) {
            loginSession.logout(httpReq);
            throw ex;
        }
    }

    private void doFilter(HttpServletRequest httpReq, HttpServletResponse httpResp) {
        String accessToken = accessTokenHeaderResolver.resolve(httpReq);
        if (hasText(accessToken)) {
            loginSession.login(accessToken, httpReq);
            return;
        }

        if (isAuthorizationCode(httpReq)) {
            getAccessTokenByCode(httpReq, httpResp);
            return;
        }

        if (isError(httpReq)) {
            throwError(httpReq);
            return;
        }

        doLogin(httpReq, httpResp);
    }

    private boolean isError(HttpServletRequest httpReq) {
        return hasText(httpReq.getParameter("error"))
                && hasText(httpReq.getParameter("error_status"));
    }

    private boolean isAuthorizationCode(HttpServletRequest httpReq) {
        return hasText(httpReq.getParameter("code"))
                && hasText(httpReq.getParameter("state"));
    }

    private void getAccessTokenByCode(HttpServletRequest httpReq, HttpServletResponse httpResp) {
        authorizationState.verify(httpReq);
        OAuth2AccessToken accessToken = accessTokenRepository.getAccessTokenByAuthenticationCode(
                httpReq.getParameter("code"),
                httpReq,
                httpResp
        );

        if (accessToken != null) {
            throw new RequireRedirectException("Get accessToken from authorizationCode success.");
        }
    }

    private void throwError(HttpServletRequest httpReq) {
        String state = httpReq.getParameter("state");
        if (hasText(state)) {
            authorizationState.verify(httpReq);
        }
        throw OAuth2ErrorException.builder()
                .error(httpReq.getParameter("error"))
                .errorCode(httpReq.getParameter("error_code"))
                .errorDescription(httpReq.getParameter("error_description"))
                .errorStatus(Integer.valueOf(httpReq.getParameter("error_status")))
                .errorUri(httpReq.getParameter("error_uri"))
                .state(state)
                .build();
    }

    private void doLogin(HttpServletRequest httpReq, HttpServletResponse httpResp) {
        try {
            String accessToken = accessTokenResolver.resolve(httpReq);
            loginSession.login(accessToken, httpReq);
        } catch (AuthenticationException ex) {;
            try {
                String refreshToken = refreshTokenResolver.resolve(httpReq);
                String accessToken = accessTokenRepository.getAccessTokenByRefreshToken(
                        refreshToken,
                        httpReq,
                        httpResp
                ).getAccessToken();
                loginSession.login(accessToken, httpReq);
            } catch (AuthenticationException e) {
                throw new AuthorizationException("Please authorize.");
            }
        }
    }
}
