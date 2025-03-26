package com.itpk.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itpk.usercenter.Exception.BusinessException;
import com.itpk.usercenter.common.BasicResponse;
import com.itpk.usercenter.common.ResultUtils;
import com.itpk.usercenter.common.errorCode;
import com.itpk.usercenter.model.User;
import com.itpk.usercenter.model.request.ChangeUserRoleRequest;
import com.itpk.usercenter.model.request.UpdateUserInfoRequest;
import com.itpk.usercenter.model.request.UserLoginRequest;
import com.itpk.usercenter.model.request.UserRegisterRequest;
import com.itpk.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.itpk.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.itpk.usercenter.constant.UserConstant.USER_LOGIN_STATE;


@RestController
@RequestMapping("api/user")
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:5173"}, allowCredentials = "true")
@Slf4j
public class UserController {
    @Resource private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @PostMapping("/register")
    public BasicResponse<Long> UserRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if(userRegisterRequest == null)
        {
            log.info("userRegisterRequest is null");
            throw new BusinessException(errorCode.NULL_ERROR,"request参数为空");
        }
        if(userRegisterRequest.getUserRole()!=0)
        throw  new BusinessException(errorCode.NO_AUTH,"无权限设置用户权限");
        log.info("UserRegisterRequest : {}", userRegisterRequest.getUserAccount());
        String userAccount =userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode))
        {
            log.info("userRegister fail,data is null");
            throw new BusinessException(errorCode.NULL_ERROR,"userRegister fail,data is null");
        }
        long list = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(list);
    }
    @PostMapping("/login")
    public BasicResponse<User> UserLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if(userLoginRequest == null)
        {
            log.info("userLoginRequest is null");
            throw new BusinessException(errorCode.NULL_ERROR,"userLoginRequest is null");
        }
        log.info("UserLoginRequest : {}", userLoginRequest.getUserAccount());
        String userAccount =userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword))
        {
            log.info("userLoginRequest fail,data is null");
            throw new BusinessException(errorCode.NULL_ERROR,"userLoginRequest fail,data is null");
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }
    @GetMapping("/search")
    public BasicResponse<List<User>> searchUsers (String username,HttpServletRequest request)
    {
     if(username == null) {
         log.info("username is null");
         throw new BusinessException(errorCode.NULL_ERROR,"username is null");
     }
     if(!userService.isAdminUser(request))
     {
         log.info("user is not admin:username:{}",username);
            throw new BusinessException(errorCode.NO_AUTH,"用户无管理员权限");
     }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("username", username);
        List<User> list = userService.list(queryWrapper).stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }
    @GetMapping("/search/tags")
    public BasicResponse<List<User> >searchUserByTags(@RequestParam(required = false) List<String> tagList)
    {
        if(CollectionUtils.isEmpty(tagList))
            throw new BusinessException(errorCode.NULL_ERROR);
        return ResultUtils.success(userService.searchUserByTags(tagList));
    }
    @PostMapping("/delete")
    public BasicResponse<Boolean> deleteUser(@RequestBody long userId,HttpServletRequest request) {
        if(userId<=0) {
            log.info("userId <=0");
           throw new BusinessException(errorCode.PARAMS_ERROR,"userId <=0");
        }
        if (!userService.isAdminUser(request))
        {
            log.info("user is not admin:userId:{}", userId);
            throw new BusinessException(errorCode.NO_AUTH,"用户无管理员权限");
        }
        boolean b = userService.removeById(userId);
        return ResultUtils.success(b);
    }
    @PostMapping("/logout")
    public BasicResponse<Integer> logout(HttpServletRequest request) {
        if(request==null)
        {
            log.info("request is null");
            throw new BusinessException(errorCode.NULL_ERROR,"request is null");
        }
        int i = userService.userLogout(request);
        return ResultUtils.success(i);

    }
    @GetMapping("/current")
    public BasicResponse<User> getCurrentUser(HttpServletRequest request) {
        if(request==null)
            throw new BusinessException(errorCode.NULL_ERROR,"request is null");
        return ResultUtils.success(userService.getCurrentUser(request));
    }
    @PostMapping("/changeUserRole")
    public BasicResponse<Integer> changeUserRole(@RequestBody ChangeUserRoleRequest changeUserRoleRequest, HttpServletRequest request )
    {
        Long userId = changeUserRoleRequest.getUserId();
        Integer userRole= changeUserRoleRequest.getUserRole();
        if(userId==null || userRole==null||userId<=0 || userRole<0||userRole>2)
                throw new BusinessException(errorCode.PARAMS_ERROR);
        if(!userService.isAdminUser(request))
            throw new BusinessException(errorCode.NO_AUTH);

        int role= userService.changeUserRole(userId,userRole);
        return ResultUtils.success(role);
    }
    @PostMapping("/createUserByAdmin")
    public BasicResponse<Long> createUserByAdmin(@RequestBody UserRegisterRequest userRegisterRequest, HttpServletRequest request)
    {
        if(!userService.isAdminUser(request))
            throw new BusinessException(errorCode.NO_AUTH);
        if(userRegisterRequest == null)
        {
            log.info("userRegisterRequest is null");
            throw new BusinessException(errorCode.NULL_ERROR,"request参数为空");
        }
        log.info("UserRegisterRequest : {}", userRegisterRequest.getUserAccount());
        String userAccount =userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        Integer userRole = userRegisterRequest.getUserRole();
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)||userRole==null)
        {
            log.info("userRegister fail,data is null");
            throw new BusinessException(errorCode.NULL_ERROR,"userRegister fail,data is null");
        }
        log.info("create User : {}", userAccount+"By Admin");
        Long newUserId = userService.createUserByAdmin(userAccount,userPassword,checkPassword,planetCode,userRole);
        return ResultUtils.success(newUserId);
    }
    @PostMapping("/updateUserInfo")
    public BasicResponse<Long> updateUserInfo(@RequestBody UpdateUserInfoRequest updateUserInfoRequest, HttpServletRequest request) {
        // 1. 检查请求参数是否为空
        if (updateUserInfoRequest == null) {
            throw new BusinessException(errorCode.NULL_ERROR, "user is null");
        }
        if(StringUtils.isAllBlank(updateUserInfoRequest.getUserPassword(),
                updateUserInfoRequest.getUsername(),
                updateUserInfoRequest.getAvatarUrl(),
                updateUserInfoRequest.getPhone(),
                updateUserInfoRequest.getEmail(),
                updateUserInfoRequest.getPlanetCode()
                )&&updateUserInfoRequest.getGender()==null)
            throw new BusinessException(errorCode.PARAMS_ERROR);
        // 2. 获取当前登录用户
        User currentUser = userService.getCurrentUser(request);
        // 3. 打印日志
        log.info("updateUserInfoRequest : {}",currentUser.getId());
        // 4. 将请求参数拷贝到 User 对象中
        User updateUser = new User();
        BeanUtils.copyProperties(updateUserInfoRequest, updateUser);
        // 5. 设置当前登录用户的 ID，确保只能更新自己的信息
        updateUser.setId(currentUser.getId());
        // 6. 更新用户信息
        userService.updateUserInfo(updateUser);
        // 7. 返回更新后的用户 ID
        updateUser=userService.getById(updateUser.getId());
        request.getSession().setAttribute(USER_LOGIN_STATE,userService.getSafetyUser(updateUser));
    return ResultUtils.success(updateUser.getId());
    }
    @PostMapping("/uploadAvatar")
    public BasicResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 1. 检查文件是否为空
        if (file.isEmpty()) {
            log.info("上传文件为空");
            throw new BusinessException(errorCode.PARAMS_ERROR, "上传文件为空");
        }
        // 2. 获取当前登录用户
        User currentUser = userService.getCurrentUser(request);
        // 3. 上传文件到OSS
        String avatarUrl;
        try {
            avatarUrl = userService.uploadAvatar(file,currentUser.getId());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(errorCode.SYSTEM_ERROR, "文件上传失败");
        }
        // 4. 更新当前用户的头像URL
        currentUser.setAvatarUrl(avatarUrl);
        userService.updateUserInfo(currentUser);
        // 5. 返回头像的访问URL
        log.info("用户 {} 上传头像成功，头像URL: {}", currentUser.getUserAccount(), avatarUrl);
        request.getSession().setAttribute(USER_LOGIN_STATE,currentUser);
        return ResultUtils.success(avatarUrl);
    }
    @GetMapping("/recommend")
    public BasicResponse<Page<User>> userRecommend(Long PageNum, Long PageSize, HttpServletRequest request) {

        User currentUser = userService.getCurrentUser(request);
        String redisKey = String.format("p_pao:user:recommend:%s", currentUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Page<User> list = (Page<User>) valueOperations.get(redisKey);
        if (list != null) {
            return ResultUtils.success(list);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        list = userService.page(new Page<>(PageNum,PageSize),queryWrapper);
    try {
        valueOperations.set(redisKey, list,100000, TimeUnit.MILLISECONDS);
    }
    catch (Exception e) {
        log.error("redis set key error", e);
    }
        return ResultUtils.success(list);
    }

}
