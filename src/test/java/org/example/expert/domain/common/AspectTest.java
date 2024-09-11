package org.example.expert.domain.common;

import org.aspectj.lang.ProceedingJoinPoint;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.module.Aspect;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.Mockito.*;

public class AspectTest {

    @InjectMocks
    private Aspect aspect;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    @DisplayName("어드민 권한 AOP 테스트")
    void Test1() throws Throwable {
        //given
        request.setRequestURI("/test/api");
        AuthUser user = new AuthUser(1L, "tester@email.com", UserRole.ADMIN);
        when(proceedingJoinPoint.getArgs()).thenReturn(new Object[]{user});

        //when
        aspect.advicePackage(proceedingJoinPoint);

        //then
        verify(proceedingJoinPoint, times(1)).proceed();
    }
}
