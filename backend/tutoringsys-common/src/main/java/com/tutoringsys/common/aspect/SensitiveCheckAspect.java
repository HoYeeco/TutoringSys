package com.tutoringsys.common.aspect;

import com.tutoringsys.common.annotation.SensitiveCheck;
import com.tutoringsys.common.exception.BusinessException;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@Aspect
public class SensitiveCheckAspect {
    
    @Value("${sensitive.white-list:}")
    private List<String> whiteList;

    @Around("execution(* com.tutoringsys.llm.LLMService.generateFeedback(..)) && args(answer,..)")
    public Object checkSensitive(ProceedingJoinPoint joinPoint, String answer) throws Throwable {
        if (containsSensitiveWord(answer)) {
            throw new BusinessException("答案包含敏感词");
        }
        return joinPoint.proceed();
    }

    private boolean containsSensitiveWord(String content) {
        boolean hasSensitive = SensitiveWordHelper.contains(content);
        if (hasSensitive && !whiteList.isEmpty()) {
            for (String white : whiteList) {
                if (content.contains(white)) {
                    return false;
                }
            }
        }
        return hasSensitive;
    }
}
