package com.example.logistics.security;

import com.example.logistics.entity.AppUser;
import com.example.logistics.entity.enums.UserRole;
import com.example.logistics.exception.UserNotFoundException;
import com.example.logistics.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserFacade {

    private final AppUserRepository userRepository;

    public AppUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new UserNotFoundException("Authenticated user not found");
        }
        return userRepository.findByEmployeeId(details.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found for employeeId: " + details.getUsername()));
    }

    public UserRole currentRole() {
        return currentUser().getRole();
    }
}
