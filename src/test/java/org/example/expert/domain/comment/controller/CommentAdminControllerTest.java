package org.example.expert.domain.comment.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.comment.service.CommentAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentAdminController.class)
@Slf4j
class CommentAdminControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    CommentAdminService commentAdminService;

    @Test
    void 댓글_삭제_성공() throws Exception {
        long commentId  = 1L;
        doNothing().when(commentAdminService).deleteComment(commentId);

        ResultActions resultActions = mockMvc.perform(delete("/admin/comments/{commentId}",commentId));

        resultActions.andExpect(status().isOk());
    }

}