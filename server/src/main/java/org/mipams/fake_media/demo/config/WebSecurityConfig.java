package org.mipams.fake_media.demo.config;

import org.mipams.fake_media.demo.services.ProducerInitializer;
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
        ProducerInitializer producerInitializer;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
                http.cors()
                                .and().exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
                                .csrf().disable()
                                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                                .and()
                                .authorizeRequests(configurer -> configurer
                                                .antMatchers("/error",
                                                                "/login",
                                                                "/metadata/**/*")
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

                UserDetails producer1 = User
                                .withUsername("nickft")
                                .authorities("PRODUCER", "CONSUMER")
                                .passwordEncoder(passwordEncoder::encode)
                                .password("nickft123!")
                                .build();

                manager.createUser(producer1);
                producerInitializer.initializeUserContext(producer1.getUsername(), "CNN");

                UserDetails producer2 = User
                                .withUsername("reporter")
                                .authorities("PRODUCER", "CONSUMER")
                                .passwordEncoder(passwordEncoder::encode)
                                .password("reporter123!")
                                .build();

                manager.createUser(producer2);
                producerInitializer.initializeUserContext(producer2.getUsername(), "BBC");

                UserDetails consumer = User
                                .withUsername("user")
                                .authorities("CONSUMER")
                                .passwordEncoder(passwordEncoder::encode)
                                .password("user123!")
                                .build();
                manager.createUser(consumer);

                return manager;
        }

}
