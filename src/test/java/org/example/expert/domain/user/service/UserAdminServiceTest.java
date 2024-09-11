package org.example.expert.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {
    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserAdminService userAdminService;

    @Test
    void 유저_권한_변경_성공() {
        UserRoleChangeRequest request = new UserRoleChangeRequest("USER");
        User user = new User("a@a","1234", UserRole.ADMIN);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        userAdminService.changeUserRole(1L,request);

        assertEquals(UserRole.USER,user.getUserRole());
    }

    @Test
    void userId로_유저를_찾지_못한_경우() {
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> userAdminService.changeUserRole(1L,null));
        assertEquals("User not found",e.getMessage());
    }
}
