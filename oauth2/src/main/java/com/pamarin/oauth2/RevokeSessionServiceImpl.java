/*
 * Copyright 2017-2019 Pamarin.com
 */
package com.pamarin.oauth2;

import com.pamarin.oauth2.service.RevokeSessionService;
import com.pamarin.oauth2.service.RevokeTokenService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;
import com.pamarin.oauth2.repository.UserSessionRepository;

/**
 *
 * @author jitta
 */
@Service
public class RevokeSessionServiceImpl implements RevokeSessionService {

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private RevokeTokenService revokeTokenService;

    @Override
    public void revokeBySessionId(String sessionId) {
        if (hasText(sessionId)) {
            sessionRepository.delete(sessionId);
            userSessionRepository.deleteBySessionId(sessionId);
            revokeTokenService.revokeBySessionId(sessionId);
        }
    }

    @Override
    public void revokeBySessionIds(List<String> sessionIds) {
        if (!isEmpty(sessionIds)) {
            sessionIds.forEach(sessionId -> revokeBySessionId(sessionId));
        }
    }

    @Override
    public void revokeAllOnSameUserAgentBySessionId(String sessionId) {
        if (hasText(sessionId)) {
            revokeBySessionIds(userSessionRepository.findAllSessionIdsOnSameUserAgentBySessionId(sessionId));
        }
    }

    @Override
    public void revokeByUserId(String userId) {
        if (hasText(userId)) {
            revokeBySessionIds(userSessionRepository.findAllSessionIdsByUserId(userId));
        }
    }

    @Override
    public void revokeOthersOnSameUserAgentBySessionId(String sessionId) {
        if (hasText(sessionId)) {
            revokeBySessionIds(userSessionRepository.findOtherSessionIdsOnSameUserAgentBySessionId(sessionId));
        }
    }

}
