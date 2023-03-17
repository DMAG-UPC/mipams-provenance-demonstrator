package org.mipams.provenance.demo.controller;

import org.mipams.jumbf.util.MipamsException;
import org.mipams.provenance.demo.config.JwtTokenUtil;
import org.mipams.provenance.demo.entities.requests.JwtRequest;
import org.mipams.provenance.demo.entities.responses.ApiError;
import org.mipams.provenance.demo.entities.responses.JwtResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class JwtAuthenticationController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        try {

            final UserDetails userDetails = userService.loadUserByUsername(authenticationRequest.getUsername());

            if (passwordEncoder.matches(authenticationRequest.getPassword(), userDetails.getPassword())) {

                final String token = jwtTokenUtil.generateToken(userDetails);

                final long expirationTime = jwtTokenUtil.getExpirationTimeFromTokenInSeconds();
                return ResponseEntity.ok(new JwtResponse(token, expirationTime));
            }

            throw new MipamsException("Authentication failed");
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiError(new Date(), e.getMessage(), HttpStatus.FORBIDDEN.getReasonPhrase()));
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<?> invalidateToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        System.out.println("Logging out");
        return ResponseEntity.ok("");
    }
}