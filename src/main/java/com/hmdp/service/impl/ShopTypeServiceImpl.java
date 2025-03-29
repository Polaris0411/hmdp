package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result typeList() {
        String key = "cache:shopType";
        // 1.查询redis有无list数据
        List<ShopType> shopTypeList = new ArrayList<>();
        // 2.判断是否存在
        if (stringRedisTemplate.opsForList().size(key) != 0) {
            // 3.存在，返回数据
            for (int i = 0; i < stringRedisTemplate.opsForList().size(key); i++) {
                String shopTypeJson = stringRedisTemplate.opsForList().index(key,i);
                ShopType shopType = JSONUtil.toBean(shopTypeJson, ShopType.class);
                shopTypeList.add(shopType);
            }
            return Result.ok(shopTypeList);
        }


        // 4.不存在，查询数据库
        shopTypeList = query().orderByAsc("sort").list();

        // 5.数据库中不存在则返回错误
        if (shopTypeList.isEmpty() || shopTypeList.size() == 0) {
            return Result.fail("店铺类型不存在！");
        }

        // 6.存在，先写入redis
        for (ShopType shopType : shopTypeList) {
            stringRedisTemplate.opsForList().rightPush(key, JSONUtil.toJsonStr(shopType));
        }

        // 7.返回
        return Result.ok(shopTypeList);
    }
}
