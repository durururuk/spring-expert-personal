package org.example.expert.domain.common.module;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@org.aspectj.lang.annotation.Aspect
public class Aspect {

    //포인트컷
    @Pointcut("@annotation(org.example.expert.domain.common.annotation.AdminOnly)")
    private void adminOnlyAnnotation() {}

    //어드바이스
    @Around("adminOnlyAnnotation()")
    public Object advicePackage(ProceedingJoinPoint joinPoint) throws Throwable {
        //API 요청 URL 가져오기
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String requestUrl = request.getRequestURI();

        //요청한 사용자의 ID 가져오기
        AuthUser authUser = extractAuthUser(joinPoint.getArgs());
        Long userId = authUser.getId();

        log.info("요청한 사용자의 ID : {}", userId);
        log.info("API 요청 시각 : {}", LocalDateTime.now());
        log.info("API 요청 URL : {}", requestUrl);

        return joinPoint.proceed();
    }

    //API 요청 할 때 인자로 들어간 AuthUser 추출하기
    private AuthUser extractAuthUser(Object[] args) {
        for(Object arg : args) {
            if(arg instanceof AuthUser authUser) {
                return authUser;
            }
        }
        return null;
    }
}
