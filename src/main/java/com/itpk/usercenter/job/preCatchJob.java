package com.itpk.usercenter.job;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itpk.usercenter.model.User;
import com.itpk.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class preCatchJob {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private RedissonClient redissonClient;
    List<Long> mainUsers= Arrays.asList(1L);
    @Scheduled(cron = "0 55 22 * * *")
    public void preCatchRecommendUsers() {
        RLock lock = redissonClient.getLock("p_pao:job:preCatchJob");
        try {
           if( lock.tryLock(0,-1,TimeUnit.MILLISECONDS))
            {
                System.out.println("currentLock:"+lock.getName());
                for(Long userId : mainUsers) {
                String redisKey = String.format("p_pao:user:recommend:%s",userId);
                ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                Page<User>list = userService.page(new Page<>(1,20),queryWrapper);
                try {
                    valueOperations.set(redisKey, list,30000, TimeUnit.MILLISECONDS);
                }
                catch (Exception e) {
                    log.error("redis set key error", e);
                }
            }}
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
