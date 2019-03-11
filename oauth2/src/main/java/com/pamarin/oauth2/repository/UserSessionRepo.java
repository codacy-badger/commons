/*
 * Copyright 2017-2019 Pamarin.com
 */
package com.pamarin.oauth2.repository;

import com.pamarin.oauth2.domain.UserSession;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author jitta
 */
public interface UserSessionRepo extends JpaRepository<UserSession, String> {

    @Query("SELECT DISTINCT us.sourceId FROM UserSession us WHERE us.id = ?1")
    String findUserSourceIdBySessionId(String sessionId);

    List<UserSession> findBySourceId(String sourceId);

    @Modifying
    @Query(
            "UPDATE UserSession us "
            + "SET us.updatedDate = ?2, "
            + "    us.updatedUser = ?3, "
            + "    us.ipAddress = ?4 "
            + "WHERE us.id = ?1 "
    )
    public void update(
            String sessionId,
            LocalDateTime date,
            String userId,
            String ipAddress
    );

}
