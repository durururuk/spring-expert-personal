package org.example.expert.domain.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManagerController.class)
class ManagerControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ManagerService managerService;
    @MockBean
    JwtUtil jwtUtil;

    @Test
    void 매니저_추가_성공() throws Exception {
        long todoId = 1L;
        ManagerSaveRequest request = new ManagerSaveRequest(1L);
        ManagerSaveResponse response = new ManagerSaveResponse(1L,new UserResponse(1L,"a@a"));
        given(managerService.saveManager(any(AuthUser.class),eq(todoId),eq(request))).willReturn(response);

        ResultActions resultActions = mockMvc.perform(post("/todos/{todoId}/managers", todoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request))
                .requestAttr("userId",1L)
                .requestAttr("email","a@a")
                .requestAttr("userRole","USER"));

        resultActions.andExpect(status().isOk());
    }

    @Test
    void 매니저_조회_성공() throws Exception {
        long todoId = 1L;
        given(managerService.getManagers(todoId)).willReturn(List.of());

        ResultActions resultActions = mockMvc.perform(get("/todos/{todoId}/managers",todoId));

        resultActions.andExpect(status().isOk());
    }

    @Test
    void 매니저_삭제_성공() throws Exception {
        long todoId = 1L;
        long managerId = 2L;
        doNothing().when(managerService).deleteManager(any(AuthUser.class),eq(todoId),eq(managerId));

        ResultActions resultActions = mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todoId,managerId)
                .requestAttr("userId",1L)
                .requestAttr("email","a@a")
                .requestAttr("userRole","USER"));

        resultActions.andExpect(status().isOk());
    }

}