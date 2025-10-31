package com.learnSpringBoot.File.Processor.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.learnSpringBoot.FileProcessor.service.*.*(..)) || execution(* com.learnSpringBoot.FileProcessor.controller.*.*(..))")
    public Object log(ProceedingJoinPoint pjp) throws Throwable {
        String sig = pjp.getSignature().toShortString();
        long start = System.currentTimeMillis();
        try {
            System.out.println("-> " + sig + " entered");
            return pjp.proceed();
        } finally {
            System.out.println("<- " + sig + " exited (took " + (System.currentTimeMillis() - start) + "ms)");
        }
    }
}
