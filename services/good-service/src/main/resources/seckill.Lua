
--判断我们的秒杀的用户集合是否存在该用户
if redis.call('sismember',KEYS[2],AGRV[1]) ==1
    then
    return 0--重复下单
end
--判断我们的库存是否>1
local stock =tonumber(redis.call('get',KEYS[1])or 0)
if stock <=0
    then
    return -1 --库存不足
end

redis.call('decr',KEYS[1])
redis.call('sadd',KEYS[2],AGRV[1])

return 1;