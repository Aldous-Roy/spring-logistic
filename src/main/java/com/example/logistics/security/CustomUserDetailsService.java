package com.example.logistics.security;

import com.example.logistics.exception.UserNotFoundException;
import com.example.logistics.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmployeeId(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UserNotFoundException("User not found for employeeId: " + username));
    }
}
