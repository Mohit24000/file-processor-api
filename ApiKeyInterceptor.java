package com.learnSpringBoot.File.Processor.config;

import com.learnSpringBoot.File.Processor.exception.ApiKeyMissingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class ApiKeyInterceptor implements HandlerInterceptor {

    private final String requiredKey;

    public ApiKeyInterceptor(String requiredKey) {
        this.requiredKey = requiredKey;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Only enforce on non-GET methods (so GET for status can be public if desired)
        String apiKey = request.getHeader("X-Api-Key");
        if (requiredKey == null || requiredKey.isBlank()) {
            // no enforcement if property not set; but recommending set in props
            return true;
        }
        if (apiKey == null || !apiKey.equals(requiredKey)) {
            throw new ApiKeyMissingException("Missing or invalid X-Api-Key header");
        }
        return true;
    }
}
