//package com.ideaflow.parseshortvideo.parseshortvideo;// java
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.core.env.Environment;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.core.task.AsyncTaskExecutor;
//
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.junit.jupiter.api.Assumptions.assumeTrue;
//
//@SpringBootTest
//class ParseShortVideoApplicationTests {
//
//    @Autowired
//    private Environment env;
//
//    @Qualifier("applicationTaskExecutor")
//    @Autowired(required = false)
//    private AsyncTaskExecutor applicationTaskExecutor;
//
//    @Test
//    void virtualThreadConfigEnabled() {
//        assertEquals("true",
//                env.getProperty("spring.threads.virtual.enabled"),
//                "spring.threads.virtual.enabled 未开启");
//    }
//
//    @Test
//    void virtualThreadExecutorWorks() throws Exception {
//        assumeTrue(applicationTaskExecutor != null, "未定义虚拟线程执行器 Bean");
//        Boolean isVirtual = applicationTaskExecutor.submit(() -> Thread.currentThread().isVirtual())
//                .get(3, TimeUnit.SECONDS);
//        assertTrue(isVirtual, "提交任务未运行在虚拟线程上");
//    }
//}
