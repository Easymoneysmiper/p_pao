package com.itpk.usercenter.service;

import com.itpk.usercenter.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.util.StopWatch;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class InsertUserTest {

    @Autowired
    private UserService
    userService;
    private ExecutorService executorService=new ThreadPoolExecutor(60,1000,1000, TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>(1000));
    @Test
    public void insertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
       // final int INSERT_NUM = 1000;
        int i=0;
        int bathSize=1000;
    List<CompletableFuture<Void>> futures=new ArrayList<>();
      for(int j=0;j<10;j++) {
          List<User> users=new ArrayList<>();
          while(true) {
              i++;
              User user = new User();
              user.setUsername("fakeUser");
              user.setUserAccount("00001");
              user.setAvatarUrl("https://pk-p_pao-backend.oss-cn-beijing.aliyuncs.com/d28b1289-4d13-4fa7-b43e-065d17311f2b_Riders Republic Steam2023-7-20-19-49-13.jpg");
              user.setGender(1);
              user.setUserPassword("12345678");
              user.setPlanetCode("0102");
              user.setPhone("15362489721");
              user.setEmail("2005@mall");
              user.setTags("");
              users.add(user);
              if (i%10000==0)
                  break;
          }
          CompletableFuture<Void> future= CompletableFuture.runAsync(()->{
              System.out.println(Thread.currentThread().getName());
             userService.saveBatch(users,bathSize);
          });
          futures.add(future);
      }
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
