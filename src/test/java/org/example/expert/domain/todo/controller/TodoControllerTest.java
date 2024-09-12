package org.example.expert.domain.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
class TodoControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    TodoService todoService;

    @Test
    void 일정_저장_성공() throws Exception {
        TodoSaveRequest request = new TodoSaveRequest("제목","내용");
        TodoSaveResponse response = new TodoSaveResponse(null,null,null,null,null);
        given(todoService.saveTodo(any(AuthUser.class),eq(request))).willReturn(response);

        ResultActions resultActions = mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request))
                .requestAttr("userId",1L)
                .requestAttr("email","a@a")
                .requestAttr("userRole","USER"));

        resultActions.andExpect(status().isOk());
    }

    @Test
    void 일정_다건_조회_성공() throws Exception {
        int page = 1;
        int size = 10;
        List<TodoResponse> resposes = new ArrayList<>();
        Page<TodoResponse> pageResponses = new PageImpl<>(resposes);
        given(todoService.getTodos(page,size)).willReturn(pageResponses);

        ResultActions resultActions = mockMvc.perform(get("/todos"));

        resultActions.andExpect(status().isOk());
    }

    @Test
    void 일정_단건_조회_성공() throws Exception {
        long todoId = 1L;
        TodoResponse response = new TodoResponse(null,null,null,null,null,null,null);
        given(todoService.getTodo(eq(todoId))).willReturn(response);

        ResultActions resultActions = mockMvc.perform(get("/todos/{todoId}",todoId));

        resultActions.andExpect(status().isOk());
    }

}