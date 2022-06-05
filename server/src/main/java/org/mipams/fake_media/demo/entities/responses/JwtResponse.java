package org.mipams.fake_media.demo.entities.responses;

import java.io.Serializable;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JwtResponse implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwttoken;
    private final long expirationTime;

    public String getToken() {
        return this.jwttoken;
    }

    public long getExpirationTime() {
        return this.expirationTime;
    }
}