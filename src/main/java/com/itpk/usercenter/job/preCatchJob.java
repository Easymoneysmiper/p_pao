package com.itpk.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itpk.usercenter.mapper.UserMapper;
import com.itpk.usercenter.model.User;
import com.itpk.usercenter.service.UserService;
import com.itpk.usercenter.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;



@Component
@Slf4j
public class preCatchJob {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedissonClient redissonClient;
    List<Long> mainUsers= Arrays.asList(6L);
    @Scheduled(cron = "0 55 22 * * *")
    public void preCatchRecommendUsers() {
        RLock lock = redissonClient.getLock("p_pao:job:preCatchJob");
        try {
           if( lock.tryLock(0,-1,TimeUnit.MILLISECONDS))
            {
                System.out.println("currentLock:"+lock.getName());
                for(Long userId : mainUsers) {
                String redisKey = String.format("p_pao:user:match:%s",userId);
                ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    int num=10;
                    User loginUser = userService.getById(userId);
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    queryWrapper.select("id","tags");
                    List<User> userList=userMapper.selectList(queryWrapper);
                    String tagsStr=loginUser.getTags();
                    Gson gson=new Gson();
                    List<String> tags = gson.fromJson(tagsStr, new TypeToken<List<String>>() {
                    }.getType());
                    List<Pair<User,Long>>indexDistance=new ArrayList<>();
                    for (int i=0;i<15;i++){
                        User user=userList.get(i);
                        String userTags=userList.get(i).getTags();
                        if (StringUtils.isBlank(userTags)|| user.getId().equals(loginUser.getId())){
                            continue;
                        }
                        List<String> userTagsList = gson.fromJson(userTags, new TypeToken<List<String>>() {
                        }.getType());
                        long distance= AlgorithmUtils.minDistance(tags,userTagsList);
                        indexDistance.add(new Pair<>(user,distance));
                    }
                    List<Pair<User,Long>> topUserList =indexDistance.stream().sorted((a,b)->(int)(a.getValue()-b.getValue())) .limit(num).collect(Collectors.toList());
                    List<Long> collectIds = topUserList.stream().map(pair -> pair.getKey().getId()
                    ).collect(Collectors.toList());
                    List<User> users = collectIds.stream().map(id -> userService.getSafetyUser(userService.getById(id))).collect(Collectors.toList());
                    Page<User>list=new Page<User>(1L,10L).setRecords(users);
                try {
                    valueOperations.set(redisKey, list,30000, TimeUnit.MILLISECONDS);
                }
                catch (Exception e) {
                    log.error("redis set key error", e);
                }
            }
            }
        }
        catch (Exception e) {
            log.error("Redisson error", e);
        }
        finally {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }

    }
}
