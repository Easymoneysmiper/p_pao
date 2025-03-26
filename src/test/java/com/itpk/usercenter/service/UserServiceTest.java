package com.itpk.usercenter.service;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.itpk.usercenter.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 用户测试
 *
 * @author pk
 */

@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;
    @Test
    public void testAdduser() {
        User user = new User();
        user.setUsername("easyMoneySniper");
        user.setUserAccount("123");
        user.setAvatarUrl("https://i0.hdslb.com/bfs/article/30923190e7c489dd5779e9f4c0c96cb696586b47.jpg@1192w.avif");
        user.setGender(0);
        user.setUserPassword("1223");
        user.setPhone("123");
        user.setEmail("456");
     boolean result=userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    void userRegister() {

        //试密码
        String userAccount = "easygoing";
        String userPassword = "";
        String checkPassword = "123456";
        String planetCode ="1";
        long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userAccount="ea";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userPassword="123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userPassword="12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        checkPassword="123456789";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userPassword="123456789";
        userAccount="easy going";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userAccount="easyMoneySniper";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userAccount="easygoing";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertTrue(result>0);




    }
    @Test
    public  void searchUserByTags() {
        List<String> tags= Collections.singletonList("java");
        List<User> users = userService.searchUserByTags(tags);
        Assertions.assertNotNull(users);
    }
}