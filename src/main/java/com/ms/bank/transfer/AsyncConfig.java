package com.ms.bank.transfer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean("transferSchedulerAsyncExecutor")
    public Executor transferSchedulerAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setThreadNamePrefix("schedule-a-t");
        executor.initialize(); // 꼭 써줘야 한다.
        return executor;
    }

//    @Bean("depositProcessAsyncExecutor")
//    public Executor depositProcessAsyncExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(63);
//        executor.setMaxPoolSize(63);
//        executor.setThreadNamePrefix("d-p-service-a-t");
//        executor.setDaemon(true);
//        executor.initialize(); // 꼭 써줘야 한다.
//        return executor;
//    }
}

