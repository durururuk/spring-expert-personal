package org.example.expert.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Spy
    @InjectMocks
    UserService userService;

    @Nested
    class 유저_조회{
        @Test
        void 유저_조회_성공() {
            AuthUser authUser = new AuthUser(1L,"2@2", UserRole.USER);
            User user = User.fromAuthUser(authUser);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            UserResponse response = userService.getUser(1L);

            assertEquals(1,response.getId());
            assertEquals("2@2",response.getEmail());
        }

        @Test
        void userId로_유저를_찾지_못한_경우() {
            long userId = 1L;
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());
            InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> userService.getUser(userId));
            assertEquals("User not found",e.getMessage());

        }
    }

    @Nested
    class 비밀번호_변경{
        @Test
        void 비밀번호_변경_성공() {long userId = 1L;
            UserChangePasswordRequest request = new UserChangePasswordRequest("1234","Qwerasdf1234!");
            User user = new User("a@a","1234",UserRole.USER);
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(passwordEncoder.encode(request.getNewPassword())).willReturn(request.getNewPassword());
            given(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).willReturn(true);
            given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(false);

            userService.changePassword(userId,request);

            assertEquals("Qwerasdf1234!",user.getPassword());
        }

        @Test
        void userId로_유저를_찾을_수_없는_경우() {
            long userId = 1L;
            doNothing().when(userService).validatePassword(anyString());
            UserChangePasswordRequest request = new UserChangePasswordRequest("1234","Qwerasdf1234!");
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            InvalidRequestException e = assertThrows(InvalidRequestException.class,() -> userService.changePassword(userId,request));

            assertEquals("User not found",e.getMessage());
        }

        @Test
        void 새_비밀번호와_기존_비밀번호가_같을_경우() {
            long userId = 1L;
            doNothing().when(userService).validatePassword(anyString());
            UserChangePasswordRequest request = new UserChangePasswordRequest("1234","Qwerasdf1234!");
            User user = new User("a@a","1234",UserRole.USER);
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getNewPassword(),user.getPassword())).willReturn(true);

            InvalidRequestException e = assertThrows(InvalidRequestException.class, ()->userService.changePassword(userId,request));

            assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.",e.getMessage());
        }

        @Test
        void 기존_비밀번호와_요청_속_비밀번호가_다를_경우() {
            long userId = 1L;
            doNothing().when(userService).validatePassword(anyString());
            UserChangePasswordRequest request = new UserChangePasswordRequest("1234","Qwerasdf1234!");
            User user = new User("a@a","1234",UserRole.USER);
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getNewPassword(),user.getPassword())).willReturn(false);
            given(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).willReturn(false);

            InvalidRequestException e = assertThrows(InvalidRequestException.class,()->userService.changePassword(userId,request));

            assertEquals("잘못된 비밀번호입니다.",e.getMessage());
        }
    }

    @Nested
    class 비밀번호_검증{
        @Test
        void 비밀번호_검증_성공() {
            String password = "Qwerasdf1234!";
            userService.validatePassword(password);
            verify(userService, times(1)).validatePassword(password);
        }

        @Test
        void 비밀번호_검증_실패() {
            String password = "1234";
            InvalidRequestException e = assertThrows(InvalidRequestException.class,()->userService.validatePassword(password));
            assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.",e.getMessage());
        }
    }
}
