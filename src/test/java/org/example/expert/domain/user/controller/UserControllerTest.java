package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;

    @Test
    void 유저_단건_조회_성공() throws Exception {
        long userId = 1L;
        UserResponse response = new UserResponse(1L, "a@a");
        given(userService.getUser(eq(userId))).willReturn(response);

        ResultActions resultActions = mockMvc.perform(get("/users/{userId}", userId));
        resultActions.andExpect(status().isOk());
    }

    @Test
    void 비밀번호_변경_성공() throws Exception {
        UserChangePasswordRequest request = new UserChangePasswordRequest("1234", "Qwerasdf1234!");
        doNothing().when(userService).changePassword(eq(anyLong()), request);

        ResultActions resultActions = mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request))
                .requestAttr("userId", 1L)
                .requestAttr("email", "a@a")
                .requestAttr("userRole", "USER"));

        resultActions.andExpect(status().isOk());
    }
}