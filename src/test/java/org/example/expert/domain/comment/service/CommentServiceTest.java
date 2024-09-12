package org.example.expert.domain.comment.service;

import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private ManagerRepository managerRepository;
    @Spy
    @InjectMocks
    private CommentService commentService;

    @Nested
    class 댓글_등록 {
        @Test
        public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
            // given
            long todoId = 1;
            CommentSaveRequest request = new CommentSaveRequest("contents");
            AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
            doNothing().when(commentService).isUserManager(anyLong(),any(User.class));

            given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

            // when
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                commentService.saveComment(authUser, todoId, request);
            });

            // then
            assertEquals("Todo not found", exception.getMessage());
        }

        @Test
        public void comment를_정상적으로_등록한다() {
            // given
            long todoId = 1;
            CommentSaveRequest request = new CommentSaveRequest("contents");
            AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
            User user = User.fromAuthUser(authUser);
            Todo todo = new Todo("title", "title", "contents", user);
            Comment comment = new Comment(request.getContents(), user, todo);
            doNothing().when(commentService).isUserManager(anyLong(),any(User.class));

            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
            given(commentRepository.save(any())).willReturn(comment);

            // when
            CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

            // then
            assertNotNull(result);
        }
    }

    @Nested
    class 댓글_조회 {
        @Test
        void 댓글_전체_조회_성공_댓글X() {
            //given
            long todoId = 1L;
            List<Comment> commentList = new ArrayList<>();
            given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(commentList);

            //when
            List<CommentResponse> dtoList = commentService.getComments(todoId);

            //then
            assertTrue(dtoList.isEmpty());
        }

        @Test
        void 댓글_전체_조회_성공_댓글_존재() {
            //given
            long todoId = 1L;
            Comment comment = new Comment("테스트", new User(), new Todo());
            List<Comment> commentList = new ArrayList<>();
            commentList.add(comment);
            given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(commentList);

            //when
            List<CommentResponse> dtoList = commentService.getComments(todoId);

            //then
            assertFalse(dtoList.isEmpty());
            assertEquals(1, dtoList.size());
            assertEquals("테스트", dtoList.get(0).getContents());
        }
    }

    @Nested
    class 댓글_등록시_등록유저와_일정담당자_확인 {
        @Test
        void 일정_등록하려고_하는_유저가_일정_담당자인지_확인_성공() {
            User user = new User();
            ReflectionTestUtils.setField(user,"id",1L);
            List<Manager> managerList = new ArrayList<>();
            Manager manager = new Manager();
            ReflectionTestUtils.setField(manager,"user",user);
            managerList.add(manager);
            given(managerRepository.findByTodoIdWithUser(anyLong())).willReturn(managerList);

            assertDoesNotThrow(()->commentService.isUserManager(1L,user));
        }

        @Test
        void 일정_등록하려고_하는_유저가_담당자가_아닌_경우() {
            User user1 = new User();
            User user2 = new User();
            ReflectionTestUtils.setField(user1,"id",1L);
            ReflectionTestUtils.setField(user2,"id",2L);

            List<Manager> managerList = new ArrayList<>();
            Manager manager = new Manager();
            ReflectionTestUtils.setField(manager,"user",user1);
            managerList.add(manager);
            given(managerRepository.findByTodoIdWithUser(anyLong())).willReturn(managerList);

            InvalidRequestException e = assertThrows(InvalidRequestException.class, ()-> commentService.isUserManager(1L,user2));
            assertEquals("해당 일정의 담당자만 댓글을 달 수 있습니다!",e.getMessage());
        }
    }



}
