package com.Athenaeum.service;

import com.Athenaeum.entity.User;
import com.Athenaeum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User u = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    // Map account flags as needed; here we assume enabled and non-locked/non-expired
    return org.springframework.security.core.userdetails.User
        .withUsername(u.getUsername())
        .password(u.getPassword())   // BCrypt hash from DB
        .authorities("USER")
        .accountLocked(false)
        .disabled(false)
        .accountExpired(false)
        .credentialsExpired(false)
        .build();
  }
}
