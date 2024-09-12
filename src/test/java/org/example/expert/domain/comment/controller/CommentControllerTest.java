package org.example.expert.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    CommentService commentService;

    @Test
    void 댓글_저장_성공() throws Exception {
        long todoId = 2L;
        CommentSaveRequest request = new CommentSaveRequest("내용");
        CommentSaveResponse response = new CommentSaveResponse(3L,"내용", new UserResponse(1L, "tester@email.com"));
        given(commentService.saveComment(any(AuthUser.class),eq(todoId),eq(request))).willReturn(response);

        ResultActions resultActions = mockMvc.perform(post("/todos/{todoId}/comments",todoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request))
                .requestAttr("userId",1L)
                .requestAttr("email","a@a")
                .requestAttr("userRole","USER"));

        resultActions.andExpect(status().isOk());
    }

    @Test
    void 댓글_조회_성공() throws Exception {
        long todoId = 1L;
        given(commentService.getComments(todoId)).willReturn(List.of());

        ResultActions resultActions = mockMvc.perform(get("/todos/{todoId}/comments",todoId));

        resultActions.andExpect(status().isOk());
    }

}