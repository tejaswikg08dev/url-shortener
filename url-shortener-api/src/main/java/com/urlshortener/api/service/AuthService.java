package com.urlshortener.api.service;

import com.urlshortener.api.model.RefreshToken;
import com.urlshortener.api.model.Role;
import com.urlshortener.api.model.User;
import com.urlshortener.api.repository.RefreshTokenRepository;
import com.urlshortener.api.repository.UserRepository;
import com.urlshortener.common.dto.*;
import com.urlshortener.common.exception.InvalidCredentialsException;
import com.urlshortener.common.exception.InvalidTokenException;
import com.urlshortener.common.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository  refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())){
            throw new UserAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(Role.USER)
                .apiKey(UUID.randomUUID().toString().replace("-", ""))
                .build();

        user = userRepository.save(user);

        log.info("User registered: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        log.info("User logged in: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request){
        RefreshToken storedToken = refreshTokenRepository
                .findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if(storedToken.getExpiresAt().isBefore(Instant.now())){
            refreshTokenRepository.delete(storedToken);
            throw new InvalidTokenException("Refresh token expired");
        }

        User user = storedToken.getUser();

        refreshTokenRepository.delete(storedToken);

        return generateAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshToken){
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    private AuthResponse generateAuthResponse(User user){
        String accessToken = jwtService.generateAccessToken(user);

        String refreshTokenStr = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(refreshToken);

        UserDto userDto = new UserDto(user.getId(), user.getEmail(),
                user.getName(), user.getRole().name(),
                user.getCreatedAt());

        return new AuthResponse(accessToken, refreshTokenStr, 900, userDto);
    }
}
