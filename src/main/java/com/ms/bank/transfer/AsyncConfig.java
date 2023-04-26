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

    @Bean("transferDepositRequestAsyncExecutor")
    public Executor transferDepositRequestAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("t-d-r-service-a-t");
        executor.setDaemon(true);
        executor.initialize(); // 꼭 써줘야 한다.
        return executor;
    }

    @Bean("transferDepositProcessAsyncExecutor")
    public Executor transferDepositProcessAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("t-d-p-service-a-t");
        executor.setDaemon(true);
        executor.initialize(); // 꼭 써줘야 한다.
        return executor;
    }

    @Bean("transferDepositSuccessResponseAsyncExecutor")
    public Executor transferDepositSuccessResponseAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("t-d-s-r-service-a-t");
        executor.setDaemon(true);
        executor.initialize(); // 꼭 써줘야 한다.
        return executor;
    }
}

