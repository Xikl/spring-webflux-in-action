package com.ximo.springwebfluxinaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * mvc config:
 * {@code
 *  @Configuration
 *  @EnableWebSecurity
 *  public class SecurityConfig extends WebSecurityConfigurerAdapter {
 *  @Override
 *  protected void configure(HttpSecurity http) throws Exception {
 *   http
 *    .authorizeRequests()
 *    .antMatchers("/design", "/orders").hasAuthority("USER")
 *    .antMatchers("/**").permitAll();
 *   }
 *  }
 *
 * }
 *
 *
 * @author xikl
 * @date 2019/11/25
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
        return serverHttpSecurity
                .authorizeExchange()
//                .pathMatchers("/flux/**").hasAuthority("USER")
                .anyExchange().permitAll()
                .and()
                .build();
    }


}
