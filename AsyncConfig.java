package com.learnSpringBoot.File.Processor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    @Bean("fileProcessorExecutor")
    public TaskExecutor fileProcessorExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(3);
        exec.setMaxPoolSize(6);
        exec.setQueueCapacity(50);
        exec.setThreadNamePrefix("file-proc-");
        exec.initialize();
        return exec;
    }
}
