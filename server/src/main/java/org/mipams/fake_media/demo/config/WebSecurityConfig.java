package org.mipams.fake_media.demo.config;

import org.mipams.fake_media.demo.services.UserInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

@Component
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        public static final String AUTHORITIES_CLAIM_NAME = "roles";

        @Autowired
        PasswordEncoder passwordEncoder;

        @Autowired
        private JwtRequestFilter jwtRequestFilter;

        @Autowired
        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

        @Autowired
        UserInitializer userInitializer;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
                http.cors()
                                .and().exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
                                .csrf().disable()
                                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                                .and()
                                .authorizeRequests(configurer -> configurer
                                                .antMatchers(
                                                                "/error",
                                                                "/login")
                                                .permitAll()
                                                .anyRequest()
                                                .authenticated());

                http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
                http.headers().cacheControl();
        }

        @Bean
        @Override
        protected UserDetailsService userDetailsService() {
                InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

                UserDetails producer = User
                                .withUsername("nickft")
                                .authorities("PRODUCER", "CONSUMER")
                                .passwordEncoder(passwordEncoder::encode)
                                .password("nickft123!")
                                .build();

                manager.createUser(producer);

                UserDetails consumer = User
                                .withUsername("user")
                                .authorities("CONSUMER")
                                .passwordEncoder(passwordEncoder::encode)
                                .password("user123!")
                                .build();
                manager.createUser(consumer);

                userInitializer.initializeUserContext(producer.getUsername());
                userInitializer.initializeUserContext(consumer.getUsername());
                return manager;
        }

}
