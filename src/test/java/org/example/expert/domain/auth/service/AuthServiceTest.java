package org.example.expert.domain.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    JwtUtil jwtUtil;

    @Value("${jwt.secret.key}")
    private String secretKey;

    @InjectMocks
    AuthService authService;

    @Nested
    class 회원_가입_테스트 {
        //1.성공
        @Test
        void 가입_성공() {
            //given
            SignupRequest request = new SignupRequest(
                    "tester@email.com",
                    "qwerasdf1234!",
                    "ADMIN"
            );

            UserRole userRole = UserRole.of(request.getUserRole());
            User savedUser = new User(
                    request.getEmail(),
                    request.getPassword(),
                    userRole);
            ReflectionTestUtils.setField(savedUser,"id",1L);

            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(jwtUtil.createToken(anyLong(),anyString(),any())).willReturn("Bearer 어쩌구");
            //when
            SignupResponse response = authService.signup(request);
            //then
            Assertions.assertEquals("Bearer",response.getBearerToken().substring(0,6));
        }
        //2.이미 존재하는 이메일 예외
        @Test
        void 이미_있는_이메일로_가입() {
            SignupRequest request = new SignupRequest(
                    "tester@email.com",
                    "qwerasdf1234!",
                    "ADMIN"
            );

            given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

            InvalidRequestException e = Assertions.assertThrows(InvalidRequestException.class, () -> authService.signup(request));
            Assertions.assertEquals("이미 존재하는 이메일입니다.", e.getMessage());
        }
    }

    @Nested
    class 로그인 {
        @Test
        void 로그인_성공() {
            SigninRequest request = new SigninRequest("tester@email.com","qwerasdf1234!");
            User user = new User(
                    request.getEmail(),
                    request.getPassword(),
                    UserRole.USER);
            ReflectionTestUtils.setField(user,"id",1L);

            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
            given(jwtUtil.createToken(anyLong(),anyString(),any())).willReturn("Bearer 어쩌구");
            given(passwordEncoder.matches(any(),any())).willReturn(true);

            SigninResponse response = authService.signin(request);
            //then
            Assertions.assertEquals("Bearer",response.getBearerToken().substring(0,6));
        }

        @Test
        void 가입되지_않은_유저로_로그인() {
            SigninRequest request = new SigninRequest("tester@email.com","qwerasdf1234!");
            InvalidRequestException e = Assertions.assertThrows(InvalidRequestException.class, ()->authService.signin(request));
            Assertions.assertEquals("가입되지 않은 유저입니다.",e.getMessage());
        }

        @Test
        void 비밀번호가_일치하지_않는_경우() {
            SigninRequest request = new SigninRequest("tester@email.com","qwerasdf1234!");
            User user = new User(
                    request.getEmail(),
                    request.getPassword(),
                    UserRole.USER);
            ReflectionTestUtils.setField(user,"id",1L);

            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

            AuthException e = Assertions.assertThrows(AuthException.class, ()->authService.signin(request));
            Assertions.assertEquals("잘못된 비밀번호입니다.",e.getMessage());
        }
    }
}
