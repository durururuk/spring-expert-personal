package org.example.expert.domain.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuthService authService;

    @Test
    void 회원_가입_성공() throws Exception {
        SignupRequest request = new SignupRequest(
                "test@email.com",
                "Qwerasdf1234!",
                "USER"
        );
        given(authService.signup(request)).willReturn(new SignupResponse("Bearer jwt"));

        ResultActions resultActions = mockMvc.perform(post("/auth/signup",request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)));

        resultActions.andExpect(status().isOk());
    }

    @Test
    void 로그인_성공() throws Exception {
        SigninRequest request = new SigninRequest(
                "test@email.com",
                "Qwerasdf1234!"
        );
        given(authService.signin(request)).willReturn(new SigninResponse("Bearer jwt"));

        ResultActions resultActions = mockMvc.perform(post("/auth/signin",request)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)));

        resultActions.andExpect(status().isOk());
    }
}
