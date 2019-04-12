/*
 * Copyright 2017-2019 Pamarin.com
 */
package com.pamarin.oauth2;

import com.pamarin.oauth2.repository.redis.RedisOAuth2RefreshTokenRepository;
import com.pamarin.oauth2.collection.OAuth2RefreshToken;
import com.pamarin.oauth2.repository.mongodb.MongodbOAuth2RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.pamarin.oauth2.repository.OAuth2RefreshTokenRepository;

/**
 *
 * @author jitta
 */
public class OAuth2RefreshTokenRepoImpl implements OAuth2RefreshTokenRepository {

    @Autowired
    private RedisOAuth2RefreshTokenRepository redisOAuth2RefreshTokenRepository;

    @Autowired
    private MongodbOAuth2RefreshTokenRepository mongodbOAuth2RefreshTokenRepository;

    @Override
    public OAuth2RefreshToken save(OAuth2RefreshToken token) {
        return mongodbOAuth2RefreshTokenRepository.save(redisOAuth2RefreshTokenRepository.save(token));
    }

    @Override
    public OAuth2RefreshToken findByTokenId(String tokenId) {
        return redisOAuth2RefreshTokenRepository.findByTokenId(tokenId);
    }

    @Override
    public void deleteByTokenId(String tokenId) {
        redisOAuth2RefreshTokenRepository.deleteByTokenId(tokenId);
        mongodbOAuth2RefreshTokenRepository.deleteByTokenId(tokenId);
    }

}
