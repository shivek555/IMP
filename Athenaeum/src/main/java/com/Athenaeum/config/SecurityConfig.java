package com.Athenaeum.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private UserDetailsService userDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      // Keep CSRF enabled for form-based apps; Thymeleaf will submit the token
      .csrf().and()
      .authorizeRequests()
        .antMatchers("/auth/login", "/auth/register", "/css/**", "/js/**", "/images/**", "/static/**").permitAll()
        .anyRequest().authenticated()
      .and()
      .formLogin()
        .loginPage("/auth/login")               // GET renders the login page
        .loginProcessingUrl("/auth/login")      // POST handled by Spring Security
        .defaultSuccessUrl("/dashboard", true)  // on success
        .failureUrl("/auth/login?error=true")   // on failure
        .permitAll()
      .and()
      .logout()
        .logoutUrl("/auth/logout")
        .logoutSuccessUrl("/auth/login?logout=true")
        .permitAll();

    // Intentionally NOT configuring stateless sessions and NOT adding any JWT filter
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }
}
