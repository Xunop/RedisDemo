package com.example.redisdemo.Util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author xun
 * @create 2022/5/11 22:33
 */
@Component
public class RedisHelper {

    private final RedisTemplate<String, String> writeRedisTemplate;
    private final RedisTemplate<String, String>  readRedisTemplate;

    public RedisHelper(RedisTemplate<String, String> writeRedisTemplate, RedisTemplate<String, String> readRedisTemplate) {
        this.writeRedisTemplate = writeRedisTemplate;
        this.readRedisTemplate = readRedisTemplate;
    }

    /**
     * 设置值
     */
    public <T> boolean set(String key, T value) {
        if (value instanceof String) {
            return set(key, (String) value);
        }
        return set(key, JSON.toJSONString(value));
    }

    /**
     * 设置值
     */
    public <T> boolean set(String key, T value, long validTime) {
        if (value instanceof String) {
            return set(key, (String) value, validTime);
        }
        return set(key, JSON.toJSONString(value), validTime);
    }

    /**
     * 设置值
     */
    private boolean set(String key, String value, long validTime) {
        Boolean res = this.writeRedisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer<String> serializer = this.writeRedisTemplate.getStringSerializer();
            byte[] keyByte = Objects.requireNonNull(serializer.serialize(key));
            byte[] valueByte = Objects.requireNonNull(serializer.serialize(value));
            connection.set(keyByte, valueByte);
            connection.expire(keyByte, validTime);
            return true;
        });
        return res != null && res;
    }

    /**
     * 设置某个值的缓存时间
     */
    public boolean setExpire(String key, long validTime) {
        Boolean res = this.writeRedisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer<String> serializer = this.writeRedisTemplate.getStringSerializer();
            byte[] keyByte = Objects.requireNonNull(serializer.serialize(key));
            connection.expire(keyByte, validTime);
            return true;
        });
        return res != null && res;
    }

    /**
     * 设置值
     */
    private boolean set(String key, String value) {
        Boolean res = this.writeRedisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer<String> serializer = this.writeRedisTemplate.getStringSerializer();
            connection.set(Objects.requireNonNull(serializer.serialize(key)), Objects.requireNonNull(serializer.serialize(value)));
            return true;
        });
        return res != null && res;
    }

    /**
     * 获取值
     */
    public <T> T get(String key, Class<T> clazz) {
        return JSON.parseObject(getValue(key), clazz);
    }

    /**
     * 获取值
     */
    public <T> List<T> getList(String key, Class<T> clazz) {
        return JSONArray.parseArray(getValue(key), clazz);
    }

    /**
     * 获取值
     */
    public String get(String key) {
        return getValue(key);
    }

    /**
     * 获取值
     */
    private String getValue(String key) {
        return this.readRedisTemplate.execute((RedisCallback<String>) connection -> {
            RedisSerializer<String> serializer = this.readRedisTemplate.getStringSerializer();
            byte[] value = connection.get(Objects.requireNonNull(serializer.serialize(key)));
            return serializer.deserialize(value);
        });
    }

    /**
     * 删除值
     */
    public void del(String key) {
        this.writeRedisTemplate.delete(key);
    }

    /**
     * 批量删除相同前缀的key
     */
    public void batchDel(String prefix) {
        Set<String> keys = keys(prefix);
        if (null != keys && !keys.isEmpty()) {
            this.writeRedisTemplate.delete(keys);
        }
    }

    /**
     * 批量删除
     */
    public void batchDel(Collection<String> keys) {
        this.writeRedisTemplate.delete(keys);
    }

    /**
     * 判断值缓存key是否存在
     */
    public boolean exist(String key) {
        Boolean res = this.writeRedisTemplate.hasKey(key);
        return res != null && res;
    }

    /**
     * 获取相同前缀的key
     */
    public Set<String> keys(String prefix) {
        return this.readRedisTemplate.keys(prefix + "*");
    }

    /**
     * 如果key不存在则设置，此方法使用了redis的原子性
     */
    public boolean setNx(String key, String value, long validTime) {
        return setNx(key, value, validTime, TimeUnit.SECONDS);
    }

    /**
     * 如果key不存在则设置，此方法使用了redis的原子性
     */
    public boolean setNx(String key, String value, long validTime, TimeUnit timeUnit) {
        try {
            ValueOperations<String, String> operations = this.writeRedisTemplate.opsForValue();
            Boolean lock = operations.setIfAbsent(key, value, validTime, timeUnit);
            return lock != null && lock;
        } catch (Exception e) {
            this.del(key);
            e.printStackTrace();
        }
        return false;
    }
}

