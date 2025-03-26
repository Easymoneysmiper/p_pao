package com.itpk.usercenter.service.impl;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itpk.usercenter.Exception.BusinessException;
import com.itpk.usercenter.common.errorCode;
import com.itpk.usercenter.mapper.UserMapper;
import com.itpk.usercenter.model.User;
import com.itpk.usercenter.service.UserService;
import com.itpk.usercenter.utils.FileUploadUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.itpk.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.itpk.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author easymoneysniper
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-02-05 02:47:33
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    private static final  String SALT="ppkk";

    @Resource
    private UserMapper userMapper;
    @Autowired
    private FileUploadUtil fileUploadUtil;
    @Autowired
    private Gson gson;

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 用户校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {
        //1.校验
        //校验非空
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
        throw new BusinessException(errorCode.NULL_ERROR,"参数为空");
        }
        //校验字数
        if(userAccount.length()<4)
            throw  new BusinessException(errorCode.PARAMS_ERROR,"账号长度不能小于4");
        if(userPassword.length()<8||checkPassword.length()<8||planetCode.length()>=6)
           throw new BusinessException(errorCode.PARAMS_ERROR,"密码长度不能小于8");
        //账号不能包含特殊字符
        String  regEx  =  "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if(!matcher.matches()){
            throw new BusinessException(errorCode.PARAMS_ERROR,"账号包含特殊字符");
        }
        //校验密码和密码
        if(!userPassword.equals(checkPassword))
            throw new  BusinessException(errorCode.PARAMS_ERROR,"两次密码不匹配");
        //账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if(count>0){
           throw new BusinessException(errorCode.PARAMS_ERROR,"账号已存在");
        }
        //星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
         count = userMapper.selectCount(queryWrapper);
        if(count>0){
           throw new BusinessException(errorCode.PARAMS_ERROR,"星球编号已存在");
        }
        //2.密码加密
        final String SALT="ppkk";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
       //3.储存用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(errorCode.PARAMS_ERROR,"用户数据保存失败");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        //校验非空
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            return null;
        }
        //校验字数
        if(userAccount.length()<4)
            return null;
        if(userPassword.length()<8)
            return null;
        //账号不能包含特殊字符
        String  regEx  =  "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if(!matcher.matches()){
            return null;
        }
        //2.密码加密比对
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user= userMapper.selectOne(queryWrapper);
        if(user==null){
            log.info("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(errorCode.PARAMS_ERROR,"密码或账号错误，登陆失败");
        }
        //3.信息脱敏
        User safetyUser=getSafetyUser(user);
        //4.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }
    //信息脱敏
    public  User  getSafetyUser(User originalUser){
        User safetyUser = new User();
        safetyUser.setId(originalUser.getId());
        safetyUser.setUsername(originalUser.getUsername());
        safetyUser.setUserAccount(originalUser.getUserAccount());
        safetyUser.setAvatarUrl(originalUser.getAvatarUrl());
        safetyUser.setGender(originalUser.getGender());
        safetyUser.setPhone(originalUser.getPhone());
        safetyUser.setEmail(originalUser.getEmail());
        safetyUser.setUserRole(originalUser.getUserRole());
        safetyUser.setPlanetCode(originalUser.getPlanetCode());
        safetyUser.setUserStatus(originalUser.getUserStatus());
        safetyUser.setCreateTime(originalUser.getCreateTime());
        safetyUser.setTags(originalUser.getTags());
        return safetyUser;
    }
    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        if(request.getSession().getAttribute(USER_LOGIN_STATE)==null){
            throw new BusinessException(errorCode.NOT_LOGIN,"用户未登录");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public User getCurrentUser(HttpServletRequest request) {
        if(request.getSession().getAttribute(USER_LOGIN_STATE)==null){
            throw new BusinessException(errorCode.NOT_LOGIN);
        }
        Object userInfo = request.getSession().getAttribute(USER_LOGIN_STATE);
        return (User) userInfo;
    }

    @Override
    public int changeUserRole(Long userId, Integer userRole) {
    User targetUser = userMapper.selectById(userId);
    if(targetUser==null){
        throw new BusinessException(errorCode.NOT_FOUND_ERROR,"用户不存在");
    }
    if(targetUser.getUserRole()==userRole){
        return userRole;
    }
    targetUser.setUserRole(userRole);
    userMapper.updateById(targetUser);
    return userRole;
    }

    @Override
    public Long createUserByAdmin(String userAccount, String userPassword, String checkPassword, String planetCode, Integer userRole) {
        Long newUserId=userRegister(userAccount,userPassword,checkPassword,planetCode);
        User newUser=userMapper.selectById(newUserId);
        newUser.setUserRole(userRole);
        newUser.setUsername("用户"+newUser.getId());
        userMapper.updateById(newUser);
        return newUserId;
    }
    @Override
    public Long updateUserInfo(User user) {
        if(user==null)
            throw  new BusinessException(errorCode.NULL_ERROR);
        userMapper.updateById(user);
        return user.getId();
    }
    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        // 1. 上传文件到OSS
        String avatarUrl = fileUploadUtil.uploadToOss(file);

        // 2. 更新用户的头像URL
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(errorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);

        // 3. 返回头像的访问URL
        return avatarUrl;
    }
    @Override
    public List<User> searchUserByTags(List<String> tagList) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //使用内存查询
        List<User> userList=userMapper.selectList(queryWrapper);
        return userList.stream().filter(user -> {
            String tagsStr=user.getTags();
            Set<String> tempTagsList=gson.fromJson(tagsStr,new TypeToken<Set<String>>(){}.getType());
            //消除if减少分支
            tempTagsList= Optional.ofNullable(tempTagsList).orElse(new HashSet<>());
            for(String tag:tagList){
                if(!tempTagsList.contains(tag)){
                    return  false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public Page<User> recommendUsers(Long PageNum, Long PageSize, HttpServletRequest request) {

        return null;
    }

    @Override
    public boolean isAdminUser(HttpServletRequest request) {
        Object session = request.getSession().getAttribute(USER_LOGIN_STATE);
        User safeUser = (User)session;
        return (safeUser.getUserRole().equals(ADMIN_ROLE));
    }

    @Deprecated
    private List<User> searchUserByTagsBySQL(List<String> tagList) {

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //使用sql查询
        if (CollectionUtils.isEmpty(tagList))
            throw new BusinessException(errorCode.NULL_ERROR);
        queryWrapper = new QueryWrapper<>();
        for(String tag:tagList){
            queryWrapper=queryWrapper.like("tags",tag);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }
}




