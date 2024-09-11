package org.example.expert.domain.todo.service;

import lombok.extern.slf4j.Slf4j;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
    @Mock
    TodoRepository todoRepository;
    @Mock
    WeatherClient weatherClient;
    @InjectMocks
    TodoService todoService;

    @Nested
    class 일정_추가{
        @Test
        void 일정_추가_성공() {
            //given
            AuthUser authUser = new AuthUser(1L,"a@a", UserRole.USER);
            User user = User.fromAuthUser(authUser);

            String weather = "맑음";
            given(weatherClient.getTodayWeather()).willReturn("맑음");

            TodoSaveRequest request = new TodoSaveRequest("제목","내용");
            Todo savedTodo = new Todo(request.getTitle(),request.getContents(),weather,user);
            ReflectionTestUtils.setField(savedTodo,"id",1L);
            given(todoRepository.save(any())).willReturn(savedTodo);

            //when
            TodoSaveResponse response = todoService.saveTodo(authUser,request);

            //then
            assertEquals(1,response.getId());
            assertEquals("제목",response.getTitle());
            assertEquals("내용",response.getContents());
            assertEquals(1,response.getUser().getId());
        }
    }

    @Nested
    class 일정_단건_조회{
        @Test
        void 일정_단건_조회_성공() {
            User user = new User();
            ReflectionTestUtils.setField(user,"id",1L);
            ReflectionTestUtils.setField(user,"email","a@a");

            Todo todo = new Todo("제목","내용","맑음",user);
            ReflectionTestUtils.setField(todo,"id",1L);

            given(todoRepository.findByIdWithUser(todo.getId())).willReturn(Optional.of(todo));

            //when
            TodoResponse response = todoService.getTodo(todo.getId());

            //then
            assertEquals(1,response.getId());
            assertEquals("제목",response.getTitle());
            assertEquals("내용",response.getContents());
            assertEquals("맑음",response.getWeather());
            assertEquals(1,response.getUser().getId());
        }

        @Test
        void todoId로_일정을_못_찾았을_경우() {
            given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.empty());

            InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> todoService.getTodo(1L));
            assertEquals("Todo not found", e.getMessage());
        }
    }

    @Nested
    class 일정_다건_조회{
        @Test
        void 일정_다건_조회_성공() {
            User user = new User();
            ReflectionTestUtils.setField(user,"id",1L);
            ReflectionTestUtils.setField(user,"email","a@a");

            Todo todo = new Todo("제목","내용","맑음",user);
            ReflectionTestUtils.setField(todo,"id",1L);

            List<Todo> todos = new ArrayList<>();
            todos.add(todo);
            Page<Todo> page = new PageImpl<>(todos);
            given(todoRepository.findAllByOrderByModifiedAtDesc(any())).willReturn(page);

            Page<TodoResponse> responses = todoService.getTodos(1,10);
            TodoResponse response = responses.getContent().get(0);

            assertEquals(1, responses.getTotalElements());
            assertEquals(1,response.getId());
            assertEquals("제목",response.getTitle());
            assertEquals("내용",response.getContents());
            assertEquals("맑음",response.getWeather());
            assertEquals(1,response.getUser().getId());
        }

    }
}
