package com.silver.dan.stockcast;

import java.util.Date;

/**
 * Created by dan on 9/10/17.
 */

class GoogleToken {
    private String accessToken;
    private Date expiresAt;

    GoogleToken(String jwtString, Date time) {
        this.expiresAt = time;
        this.accessToken = jwtString;
    }

    boolean isExpired() {
        return this.expiresAt.before(new Date());
    }

    public String getAccessToken() {
        return this.accessToken;
    }

}
