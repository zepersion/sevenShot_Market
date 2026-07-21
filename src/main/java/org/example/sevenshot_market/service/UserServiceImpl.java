package org.example.sevenshot_market.service;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.sevenshot_market.dto.RegisterDTO;
import org.example.sevenshot_market.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.example.sevenshot_market.dto.CodeDTO;
import org.example.sevenshot_market.dto.UserDto;
import org.example.sevenshot_market.mapper.UserMapper;
import org.example.sevenshot_market.vo.RegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.example.sevenshot_market.common.RedisConstants.*;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
@Autowired
private StringRedisTemplate stringRedisTemplate;
    @Override
    public void sendCode(CodeDTO codeDto) {
        String phone = codeDto.getPhone();
        Integer type= codeDto.getType();
        if (type < 1||type > 3){
            log.error("验证码类型参数非法，type:{}", type);
            throw new RuntimeException("类型参数错误，只能选择1、2、3");
        }
        //生成验证码
        String code = RandomUtil.randomNumbers(6);

        //验证码+手机好传回前端
        stringRedisTemplate.opsForValue().set(
                SEND_CODE_KEY+type+":"+phone,
                code,
                SEND_CODE_TTL,
                TimeUnit.MINUTES
                );
                //暂时未接入第三方库
        log.info("手机号:{},类型:{},发送验证码成功,验证码: {}", phone, type, code);
    }

    @Override
    public RegisterVO register(RegisterDTO registerDTO) {
    String phone = registerDTO.getPhone();
    String inputcode = registerDTO.getCode();

    String codeKey=SEND_CODE_KEY+"1:"+phone;
        String cachecode = stringRedisTemplate.opsForValue().get(codeKey);

        if(cachecode==null ||!cachecode.equals(inputcode)){
            throw new RuntimeException("验证码错误或已过期");
        }
        //验证成功删除key
        stringRedisTemplate.delete(codeKey);
        User exitsUser = this.query().eq("phone", phone).last("limit 1").one();
        if(exitsUser!=null){
            throw new RuntimeException("用户已存在");
        }
        User user = new User();
        user.setPhone(phone);
        user.setUsername(registerDTO.getUsername());
        user.setStudentNo(registerDTO.getStudentNo());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword(), BCrypt.gensalt()));
        user.setSchool(registerDTO.getSchool());
        user.setStatus(1);
        user.setCreditScore(100);
        this.save(user);
        //BCrypt.hashpw(registerDTO.getPassword(), BCrypt.gensalt()
        String token = UUID.randomUUID().toString();
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setPhone(phone);
        userDto.setUsername(registerDTO.getUsername());
        userDto.setStudentNo(registerDTO.getStudentNo());
        //选择hash结构是因为它存多个字段
        String TokenKey=LOGIN_TOKEN_KEY+token;
      //将我们的用户信息存入hashMAp中
        Map<String, Object> map= BeanUtil.beanToMap(userDto,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)//如果值为空则则不转化
                        .setFieldValueEditor((fieldName, fieldVal) -> fieldVal.toString()));//字段名字段值都变为string
        stringRedisTemplate.opsForHash().putAll(TokenKey,map);
        // 设置token有效期30分钟
        stringRedisTemplate.expire(TokenKey,LOGIN_TOKEN_TTL,TimeUnit.SECONDS);
        // ========== 步骤5：封装返回数据 ==========
        RegisterVO vo = new RegisterVO();
        vo.setUserId(user.getId());
        vo.setToken(token);
        return vo;
    }
}
