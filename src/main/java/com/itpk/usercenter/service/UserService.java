package com.itpk.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itpk.usercenter.model.User;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author easymoneysniper
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-02-05 02:47:33
 */
public interface UserService extends IService<User> {
   /**
    * 用户注册
    * 
    * @param userAccount
    * @param userPassword
    * @param username
    * @return
    */
   long userRegister(String userAccount, String userPassword, String username, String planetCode);

   /**
    * 用户登录
    *
    * @param userAccount
    * @param userPassword
    * @param request
    * @return 用户信息
    */
   User userLogin(String userAccount, String userPassword, HttpServletRequest request);

   /**
    * 用户脱敏
    * 
    * @param originalUser 原始用户
    * @return 脱敏用户
    */
   User getSafetyUser(User originalUser);

   /**
    * 用户注销
    * 
    * @param request
    * @return
    */
   int userLogout(HttpServletRequest request);

   /**
    * 获得当前用户
    * 
    * @param request
    * @return
    */
   User getCurrentUser(HttpServletRequest request);

   /**
    * 更改权限
    * 
    * @param userId
    * @param roleId
    * @return
    */
   int changeUserRole(Long userId, Integer roleId);

   /**
    * 管理员创建用户
    * 
    * @param userAccount
    * @param userPassword
    * @param checkPassword
    * @param planetCode
    * @param userRole
    * @return
    */
   Long createUserByAdmin(String userAccount, String userPassword, String checkPassword, String planetCode,
         Integer userRole);

   /**
    * 更新用户信息
    * 
    * @param user
    * @return
    */
   Long updateUserInfo(User user);

   /**
    * 头像上传
    * 
    * @param file
    * @param userId
    * @return
    */
   String uploadAvatar(MultipartFile file, Long userId);

   /**
    *
    * @param tagList
    * @return
    */
   List<User> searchUserByTags(List<String> tagList);

   /**
    *
    * @param PageNum
    * @param PageSize
    * @param request
    * @return
    */
   Page<User> recommendUsers(Long PageNum, Long PageSize, HttpServletRequest request);

   /**
    * 是否为管理员
    * @param request
    * @return
    */
    boolean isAdminUser(HttpServletRequest request);

   /**
    * 匹配用户
    *
    * @param request
    * @return
    */
    List<User> match(HttpServletRequest request);
}
