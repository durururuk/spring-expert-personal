package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Nested
    class 매니저_추가 {
        @Test
        void todo가_정상적으로_등록된다() {
            // given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

            long todoId = 1L;
            Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

            long managerUserId = 2L;
            User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
            ReflectionTestUtils.setField(managerUser, "id", managerUserId);

            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
            given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

            // then
            assertNotNull(response);
            assertEquals(managerUser.getId(), response.getUser().getId());
            assertEquals(managerUser.getEmail(), response.getUser().getEmail());
        }

        @Test
        void todo의_user가_null인_경우_예외가_발생한다() {
            // given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            long todoId = 1L;
            long managerUserId = 2L;

            Todo todo = new Todo();
            ReflectionTestUtils.setField(todo, "user", null);

            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                    managerService.saveManager(authUser, todoId, managerSaveRequest)
            );

            assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
        }

        @Test
        void 담당자를_등록하려는_유저가_일정을_만든_유저와_같지_않거나_유효하지_않은_경우() {
            //given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            Todo todo = new Todo("Test Title", "Test Contents", "Sunny", null);
            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            //when & then
            InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> managerService.saveManager(authUser,1L,null));
            assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.",e.getMessage());
        }

        @Test
        void 등록하려고_하는_담당자_유저가_존재하지_않는_경우() {
            //given
            AuthUser authUser = new AuthUser(1L,"a@a",UserRole.ADMIN);
            User user = User.fromAuthUser(authUser);
            ReflectionTestUtils.setField(user,"id",1L);

            Todo todo = new Todo();
            ReflectionTestUtils.setField(todo,"user",user);
            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            ManagerSaveRequest request = new ManagerSaveRequest();
            ReflectionTestUtils.setField(request,"managerUserId",1L);
            given(userRepository.findById(anyLong())).willReturn(Optional.empty());

            //when & then
            InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> managerService.saveManager(authUser, 1L, request));

            assertEquals("등록하려고 하는 담당자 유저가 존재하지 않습니다.",e.getMessage());
        }

        @Test
        void 일정_작성자가_매니저가_되려는_경우() {
            // given
            AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
            User user = User.fromAuthUser(authUser);

            long todoId = 1L;
            Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

            long managerUserId = 1L;
            User managerUser = new User("b@b.com", "password", UserRole.USER);
            ReflectionTestUtils.setField(managerUser, "id", managerUserId);

            ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));

            //when & then
            InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> managerService.saveManager(authUser,todoId,managerSaveRequest));

            assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", e.getMessage());
        }

    }

    @Nested
    class 매니저_조회 {
        @Test // 테스트코드 샘플
        void manager_목록_조회에_성공한다() {
            // given
            long todoId = 1L;
            User user = new User("user1@example.com", "password", UserRole.USER);
            Todo todo = new Todo("Title", "Contents", "Sunny", user);
            ReflectionTestUtils.setField(todo, "id", todoId);

            Manager mockManager = new Manager(todo.getUser(), todo);
            List<Manager> managerList = List.of(mockManager);

            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

            // when
            List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

            // then
            assertEquals(1, managerResponses.size());
            assertEquals(mockManager.getId(), managerResponses.get(0).getId());
            assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
        }

        @Test
        void manager_목록_조회_시_Todo가_없다면_InvalidRequestException_에러를_던진다() {
            // given
            long todoId = 1L;
            given(todoRepository.findById(todoId)).willReturn(Optional.empty());

            // when & then
            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
            assertEquals("Todo not found", exception.getMessage());
        }
    }

    @Nested
    class 매니저_삭제 {
        @Test
        void manager_삭제_성공() {
            //given
            AuthUser authUser = new AuthUser(1L,"a@a",UserRole.ADMIN);
            User user = new User("a@a","1234",UserRole.ADMIN);
            ReflectionTestUtils.setField(user, "id",1L);
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            Todo todo = new Todo("제목","내용","날씨",user);
            ReflectionTestUtils.setField(todo,"id",1L);
            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            Manager manager = new Manager(user, todo);
            given(managerRepository.findById(anyLong())).willReturn(Optional.of(manager));

            doNothing().when(managerRepository).delete(manager);

            //when
            managerService.deleteManager(authUser,todo.getId(),user.getId());

            //then
            verify(managerRepository, times(1)).delete(manager);
        }

        @Test
        void AuthUser로_유저를_찾지_못한_경우() {
            //given
            AuthUser authUser = new AuthUser(1L,"a@a",UserRole.ADMIN);
            given(userRepository.findById(authUser.getId())).willReturn(Optional.empty());

            //when & then
            InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(authUser,1L,1L));
            assertEquals("User not found", e.getMessage());
        }

        @Test
        void todoId로_일정을_찾지_못한_경우() {
            //given
            AuthUser authUser = new AuthUser(1L,"a@a",UserRole.ADMIN);
            long todoId = 1L;
            given(todoRepository.findById(todoId)).willReturn(Optional.empty());
            given(userRepository.findById(anyLong())).willReturn(Optional.of(new User()));

            //when & then
            InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(authUser, todoId,1L));
            assertEquals("Todo not found", e.getMessage());
        }

        @Test
        void 일정을_만든_유저가_유효하지_않은_경우() {
            //given
            AuthUser authUser = new AuthUser(1L,"a@a",UserRole.ADMIN);
            User user1 = new User("a@a","1234",UserRole.ADMIN);
            ReflectionTestUtils.setField(user1, "id",1L);
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user1));

            User user2 = new User("b@b","1234",UserRole.ADMIN);
            ReflectionTestUtils.setField(user2, "id",2L);

            Todo todo = new Todo("제목","내용","날씨",user2);
            ReflectionTestUtils.setField(todo,"id",1L);
            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            //when & then
            InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(authUser,1L,1L));

            assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", e.getMessage());
        }

        @Test
        void ManagerId로_매니저를_찾지_못한_경우() {
            //given
            AuthUser authUser = new AuthUser(1L,"a@a",UserRole.ADMIN);
            User user = new User("a@a","1234",UserRole.ADMIN);
            ReflectionTestUtils.setField(user, "id",1L);
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            Todo todo = new Todo("제목","내용","날씨",user);
            ReflectionTestUtils.setField(todo,"id",1L);
            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

            given(managerRepository.findById(anyLong())).willReturn(Optional.empty());

            //when & then
            InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(authUser,1L,1L));
            assertEquals("Manager not found", e.getMessage());
        }

        @Test
        void 해당_일정에_등록된_담당자가_아닌_경우() {
            //given
            AuthUser authUser = new AuthUser(1L,"a@a",UserRole.ADMIN);
            User user = new User("a@a","1234",UserRole.ADMIN);
            ReflectionTestUtils.setField(user, "id",1L);
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

            Todo todo1 = new Todo("제목","내용","날씨",user);
            ReflectionTestUtils.setField(todo1,"id",1L);
            given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo1));

            Todo todo2 = new Todo("제목","내용","날씨",user);
            ReflectionTestUtils.setField(todo1,"id",2L);

            Manager manager = new Manager(user, todo2);
            given(managerRepository.findById(anyLong())).willReturn(Optional.of(manager));

            //when & then
            InvalidRequestException e = assertThrows(InvalidRequestException.class, () -> managerService.deleteManager(authUser,1L,1L));
            assertEquals("해당 일정에 등록된 담당자가 아닙니다.", e.getMessage());
        }

    }


}
