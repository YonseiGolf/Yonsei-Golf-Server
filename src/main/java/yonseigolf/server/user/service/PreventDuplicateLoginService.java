package yonseigolf.server.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PreventDuplicateLoginService {

    private final RedisTemplate redisTemplate;

    @Autowired
    public PreventDuplicateLoginService(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void registerLogin(long userId, String token) {

        redisTemplate.opsForValue().getAndDelete(userId);
        redisTemplate.opsForValue().set(userId, token, 30, TimeUnit.MINUTES);
    }

    // 같으면 true, 다르면 false
    public boolean checkDuplicatedLogin(long userId, String token) {

        return redisTemplate.opsForValue().get(userId).equals(token);
    }
}
