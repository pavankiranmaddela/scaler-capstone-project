package com.pk.ev.vehicle.catalog.user.security;

import com.pk.ev.vehicle.catalog.user.model.EvUser;
import com.pk.ev.vehicle.catalog.user.repositoty.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

@Service
public class EvUserDetailsService implements UserDetailsService {
  @Autowired
  private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    EvUser evUser = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    return org.springframework.security.core.userdetails.User
        .withUsername(evUser.getEmail())
        .password(evUser.getPassword())
        .authorities(evUser.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.toString())).toList())
        .build();
  }
}
