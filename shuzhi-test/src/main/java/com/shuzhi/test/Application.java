package com.shuzhi.test;

import com.shuzhi.test.test.TestService;
import com.shuzhi.test.test.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        TestService testService = (TestService) context.getBean("testService");
        User user = new User();
        user.setUserName("xuyonghong");
        user.setAge(20);
        testService.cacheString(user);

        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            User cacheUser = testService.getUserFromCache();
            System.out.println("cacheUser is " + cacheUser);
        });

        Thread.sleep(5000);
    }

}
