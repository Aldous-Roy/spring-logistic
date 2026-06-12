package com.example.logistics.service;

import com.example.logistics.dto.auth.UserResponse;
import com.example.logistics.dto.common.PageResponse;
import com.example.logistics.entity.AppUser;
import com.example.logistics.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository userRepository;

    public PageResponse<UserResponse> list(Pageable pageable, String search) {
        Page<AppUser> page = StringUtils.hasText(search)
                ? userRepository.findByEmployeeIdContainingIgnoreCaseOrNameContainingIgnoreCase(search, search, pageable)
                : userRepository.findAll(pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getName(),
                user.getRole(),
                user.isActive()
        );
    }
}
