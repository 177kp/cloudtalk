package com.zhangwuji.im.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheHelper {

   private  Logger logger = LoggerFactory.getLogger(getClass());
   public static final String KEY_PREFIX_VALUE = "ct:value:";
   public static final String KEY_PREFIX_SET = "ct:set:";
   public static final String KEY_PREFIX_LIST = "ct:list:";
   public static final String KEY_PREFIX_HASH = "ct:hash:";
   public static final String KEY_PREFIX_GEO = "ct:geo:";

   //注： 这里不能用Autowired按类型装配注入,必须用@Resource
   // StringRedisTemplate默认采用的是String的序列化策略, 
   // RedisTemplate默认采用的是JDK的序列化策略，保存的key和value都是采用此策略序列化保存的
   
   @Resource
   private RedisTemplate<String, Object> redisTemplate;

   /**
    * 指定缓存失效时间
    * @param key 键
    * @param time 时间(秒)
    * @return
    */
   public boolean expire(String key, long time) {
      try {
         if (time > 0) {
            redisTemplate.expire(key, time, TimeUnit.SECONDS);
         }
         return true;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * 根据key 获取过期时间
    * @param key 键 不能为null
    * @return 时间(秒) 返回0代表为永久有效
    */
   public long getExpire(String key) {
      return redisTemplate.getExpire(key, TimeUnit.SECONDS);
   }

   /**
    * 判断key是否存在
    * @param key 键
    * @return true 存在 false不存在
    */
   public boolean hasKey(String key) {
      try {
         return redisTemplate.hasKey(key);
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * 删除缓存
    * @param key 可以传一个值 或多个
    */
   @SuppressWarnings("unchecked")
   public void del(String... key) {
      if (key != null && key.length > 0) {
         if (key.length == 1) {
            redisTemplate.delete(key[0]);
         } else {
            redisTemplate.delete(CollectionUtils.arrayToList(key));
         }
      }
   }

   // ============================String=============================
   /**
    * 普通缓存获取
    * @param key 键
    * @return 值
    */
   public Object get(String key) {
      return key == null ? null : redisTemplate.opsForValue().get(key);
   }

   /**
    * 普通缓存放入
    * @param key 键
    * @param value 值
    * @return true成功 false失败
    */
   public boolean set(String key, Object value) {
      try {
         redisTemplate.opsForValue().set(key, value);
         return true;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * 普通缓存放入并设置时间
    * @param key 键
    * @param value 值
    * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
    * @return true成功 false 失败
    */
   public boolean set(String key, Object value, long time) {
      try {
         if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
         } else {
            set(key, value);
         }
         return true;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * 递增
    * @param key
    * @param delta 要增加几(大于0)
    * @return
    */
   public long incr(String key, long delta) {
      if (delta < 0) {
         throw new RuntimeException("递增因子必须大于0");
      }
      return redisTemplate.opsForValue().increment(key, delta);
   }

   /**
    * 递减
    * @param key 键
    * @param delta 要减少几(小于0)
    * @return
    */
   public long decr(String key, long delta) {
      if (delta < 0) {
         throw new RuntimeException("递减因子必须大于0");
      }
      return redisTemplate.opsForValue().increment(key, -delta);
   }

   // ================================Map=================================
   /**
    * HashGet
    * @param key 键 不能为null
    * @param item 项 不能为null
    * @return 值
    */
   public Object hget(String key, String item) {
      return redisTemplate.opsForHash().get(key, item);
   }

   /**
    * 获取hashKey对应的所有键值
    * @param key 键
    * @return 对应的多个键值
    */
   public Map<Object, Object> hmget(String key) {
      return redisTemplate.opsForHash().entries(key);
   }

   /**
    * HashSet
    * @param key 键
    * @param map 对应多个键值
    * @return true 成功 false 失败
    */
   public boolean hmset(String key, Map<String, Object> map) {
      try {
         redisTemplate.opsForHash().putAll(key, map);
         return true;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * HashSet 并设置时间
    * @param key 键
    * @param map 对应多个键值
    * @param time 时间(秒)
    * @return true成功 false失败
    */
   public boolean hmset(String key, Map<String, Object> map, long time) {
      try {
         redisTemplate.opsForHash().putAll(key, map);
         if (time > 0) {
            expire(key, time);
         }
         return true;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * 向一张hash表中放入数据,如果不存在将创建
    * @param key 键
    * @param item 项
    * @param value 值
    * @return true 成功 false失败
    */
   public boolean hset(String key, String item, Object value) {
      try {
         redisTemplate.opsForHash().put(key, item, value);
         return true;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * 向一张hash表中放入数据,如果不存在将创建
    * @param key 键
    * @param item 项
    * @param value  值
    * @param time 时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
    * @return true 成功 false失败
    */
   public boolean hset(String key, String item, Object value, long time) {
      try {
         redisTemplate.opsForHash().put(key, item, value);
         if (time > 0) {
            expire(key, time);
         }
         return true;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * 删除hash表中的值
    * @param key 键 不能为null
    * @param item 项 可以使多个 不能为null
    */
   public void hdel(String key, Object... item) {
      redisTemplate.opsForHash().delete(key, item);
   }

   /**
    * 判断hash表中是否有该项的值
    * @param key 键 不能为null
    * @param item 项 不能为null
    * @return true 存在 false不存在
    */
   public boolean hHasKey(String key, String item) {
      return redisTemplate.opsForHash().hasKey(key, item);
   }

   /**
    * hash递增 如果不存在,就会创建一个 并把新增后的值返回
    * @param key 键
    * @param item 项
    * @param by 要增加几(大于0)
    * @return
    */
   public double hincr(String key, String item, double by) {
      return redisTemplate.opsForHash().increment(key, item, by);
   }

   /**
    * hash递减
    * @param key 键
    * @param item 项
    * @param by 要减少记(小于0)
    * @return
    */
   public double hdecr(String key, String item, double by) {
      return redisTemplate.opsForHash().increment(key, item, -by);
   }

   // ============================set=============================
   /**
    * 根据key获取Set中的所有值
    * @param key 键
    * @return
    */
   public Set<Object> sGet(String key) {
      try {
         return redisTemplate.opsForSet().members(key);
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return null;
      }
   }

   /**
    * 根据value从一个set中查询,是否存在
    * @param key 键
    * @param value 值
    * @return true 存在 false不存在
    */
   public boolean sHasKey(String key, Object value) {
      try {
         return redisTemplate.opsForSet().isMember(key, value);
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * 将数据放入set缓存
    * @param key 键
    * @param values 值 可以是多个
    * @return 成功个数
    */
   public long sSet(String key, Object... values) {
      try {

         return redisTemplate.opsForSet().add(key, values);
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return 0;
      }
   }

   /**
    * 将set数据放入缓存
    * 
    * @param key 键
    * @param time 时间(秒)
    * @param values 值可以是多个
    * @return 成功个数
    */
   public long sSetAndTime(String key, long time, Object... values) {
      try {
         Long count = redisTemplate.opsForSet().add(key, values);
         if (time > 0)
            expire(key, time);
         return count;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return 0;
      }
   }

   /**
    * 获取set缓存的长度
    * @param key 键
    * @return
    */
   public long sGetSetSize(String key) {
      try {
         return redisTemplate.opsForSet().size(key);
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return 0;
      }
   }

   /**
    * 移除值为value的
    * @param key 键
    * @param values 值可以是多个
    * @return 移除的个数
    */
   public long setRemove(String key, Object... values) {
      try {
         Long count = redisTemplate.opsForSet().remove(key, values);
         return count;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return 0;
      }
   }

   // ===============================list=================================
   /**
    * 获取list缓存的内容
    * @param key 键
    * @param start 开始
    * @param end 结束 0 到 -1代表所有值
    * @return
    */
   public List<Object> lGet(String key, long start, long end) {
      try {
         return redisTemplate.opsForList().range(key, start, end);
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return null;
      }
   }

   /**
    * 获取list缓存的长度
    * @param key 键
    * @return
    */
   public long lGetListSize(String key) {
      try {
         return redisTemplate.opsForList().size(key);
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return 0;
      }
   }

   /**
    * 通过索引 获取list中的值
    * @param key 键
    * @param index  索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
    * @return
    */
   public Object lGetIndex(String key, long index) {
      try {
         return redisTemplate.opsForList().index(key, index);
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return null;
      }
   }

   /**
    * 将list放入缓存
    * 
    * @param key 键
    * @param value 值
    * @return
    */
   public boolean lSet(String key, Object value) {
      try {
         redisTemplate.opsForList().rightPush(key, value);
         return true;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * 将list放入缓存
    * @param key 键
    * @param value 值
    * @param time 时间(秒)
    * @return
    */
   public boolean lSet(String key, Object value, long time) {
      try {
         redisTemplate.opsForList().rightPush(key, value);
         if (time > 0)
            expire(key, time);
         return true;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * 将list放入缓存
    * @param key 键
    * @param value 值
    * @return
    */
   public boolean lSet(String key, List<Object> value) {
      try {
         redisTemplate.opsForList().rightPushAll(key, value);
         return true;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * 将list放入缓存
    * @param key 键
    * @param value 值
    * @param time 时间(秒)
    * @return
    */
   public boolean lSet(String key, List<Object> value, long time) {
      try {
         redisTemplate.opsForList().rightPushAll(key, value);
         if (time > 0)
            expire(key, time);
         return true;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * 根据索引修改list中的某条数据
    * @param key 键
    * @param index 索引
    * @param value 值
    * @return
    */
   public boolean lUpdateIndex(String key, long index, Object value) {
      try {
         redisTemplate.opsForList().set(key, index, value);
         return true;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return false;
      }
   }

   /**
    * 移除N个值为value
    * @param key 键
    * @param count 移除多少个
    * @param value 值
    * @return 移除的个数
    */
   public long lRemove(String key, long count, Object value) {
      try {
         Long remove = redisTemplate.opsForList().remove(key, count, value);
         return remove;
      } catch (Throwable e) {
         logger.error("redis操作异常:",e);
         return 0;
      }
   }
   /**
    *
    * @MethodName：cacheGeo
    * @param x
    * @param y
    * @param member
    * @param time(单位秒)  <=0 不过期
    * @return
    * @ReturnType：boolean
    * @Description：缓存地理位置信息
    * @Modifier：
    * @ModifyTime：
    */
   public boolean cacheGeo(String k, double x, double y, String member, long time) {
      String key = KEY_PREFIX_GEO + k;
      try {
         GeoOperations<String, Object> geoOps = redisTemplate.opsForGeo();
         geoOps.geoAdd(key, new Point(x, y) , member);
         if (time > 0) redisTemplate.expire(key, time, TimeUnit.SECONDS);
      } catch (Throwable t) {
         logger.error("缓存[" + key +"]" + "失败, point["+ x + "," +
                 y + "], member[" + member + "]" +", error[" + t + "]");
      }
      return true;
   }

   /**
    *
    * @MethodName：cacheGeo
    * @param locations
    * @param time(单位秒)  <=0 不过期
    * @return
    * @ReturnType：boolean
    * @Description：
    * @Modifier：
    * @ModifyTime：
    */
   public boolean cacheGeo(String k, Iterable<RedisGeoCommands.GeoLocation<String>> locations, long time) {
      try {
         for (RedisGeoCommands.GeoLocation<String> location : locations) {
            cacheGeo(k, location.getPoint().getX(), location.getPoint().getY(), location.getName(), time);
         }
      } catch (Throwable t) {
         logger.error("缓存[" + KEY_PREFIX_GEO + k + "]" + "失败" +", error[" + t + "]");
      }
      return true;
   }

   /**
    *
    * @MethodName：removeGeo
    * @param members
    * @return
    * @ReturnType：boolean
    * @Description：移除地理位置信息
    * @Modifier：
    * @ModifyTime：
    */
   public boolean removeGeo(String k, String...members) {
      String key = KEY_PREFIX_GEO + k;
      try {
         GeoOperations<String, Object> geoOps = redisTemplate.opsForGeo();
         geoOps.geoRemove(key, members);
      } catch (Throwable t) {
         logger.error("移除[" + key +"]" + "失败" +", error[" + t + "]");
      }
      return true;
   }

   /**
    *
    * @MethodName：distanceGeo
    * @param member1
    * @param member2
    * @return Distance
    * @ReturnType：Distance
    * @Description：根据两个成员计算两个成员之间距离
    * @Modifier：
    * @ModifyTime：
    */
   public Distance distanceGeo(String k, String member1, String member2) {
      String key = KEY_PREFIX_GEO + k;
      try {
         GeoOperations<String, Object> geoOps = redisTemplate.opsForGeo();
         return geoOps.geoDist(key, member1, member2);
      } catch (Throwable t) {
         logger.error("计算距离[" + key +"]" + "失败, member[" + member1 + "," + member2 +"], error[" + t + "]");
      }
      return null;
   }

   /**
    *
    * @MethodName：getGeo
    * @param members
    * @return
    * @ReturnType：List<Point>
    * @Description：根据key和member获取这些member的坐标信息
    * @Modifier：
    * @ModifyTime：
    */
   public List<Point> getGeo(String k, String...members) {
      String key = KEY_PREFIX_GEO + k;
      try {
         GeoOperations<String, Object> geoOps = redisTemplate.opsForGeo();
         return geoOps.geoPos(key, members);
      } catch (Throwable t) {
         logger.error("获取坐标[" + key +"]" + "失败]" + ", error[" + t + "]");
      }
      return null;
   }

   /**
    *
    * @MethodName：radiusGeo
    * @param key
    * @param x
    * @param y
    * @param distance km
    * @return
    * @ReturnType：GeoResults<GeoLocation<String>>
    * @Description：通过给定的坐标和距离(km)获取范围类其它的坐标信息
    * @Modifier：
    * @ModifyTime：
    */
   public GeoResults<RedisGeoCommands.GeoLocation<Object>> radiusGeo(String key, double x, double y, double distance, Sort.Direction direction, long limit) {
      try {
         String k = KEY_PREFIX_GEO + key;

         GeoOperations<String, Object> geoOps = redisTemplate.opsForGeo();

         //设置geo查询参数
         RedisGeoCommands.GeoRadiusCommandArgs geoRadiusArgs = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs();
         geoRadiusArgs = geoRadiusArgs.includeCoordinates().includeDistance();//查询返回结果包括距离和坐标
         if (Sort.Direction.ASC.equals(direction)) {//按查询出的坐标距离中心坐标的距离进行排序
            geoRadiusArgs.sortAscending();
         } else if (Sort.Direction.DESC.equals(direction)) {
            geoRadiusArgs.sortDescending();
         }
         geoRadiusArgs.limit(limit);//限制查询数量

         GeoResults<RedisGeoCommands.GeoLocation<Object>> radiusGeo=geoOps.radius(k, new Circle(new Point(x, y), new Distance(distance, RedisGeoCommands.DistanceUnit.METERS)), geoRadiusArgs);

         return radiusGeo;
      } catch (Throwable t) {
         logger.error("通过坐标[" + x + "," + y +"]获取范围[" + distance + "km的其它坐标失败]" + ", error[" + t + "]");
      }
      return null;
   }
}
