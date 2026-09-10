package org.example.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.example.common.DTO.AllUserDTO.RegisterDTO;
import org.example.common.DTO.AllUserDTO.UserLoginDto;
import org.example.common.DTO.AllUserDTO.UserUpdateDTO;
import org.example.common.VO.UserAllVO.RegisterVO;
import org.example.common.VO.UserAllVO.UserLoginVo;
import org.example.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.example.common.DTO.AllUserDTO.CodeDTO;
import org.example.common.DTO.AllUserDTO.UserDTO;
import org.example.user.mapper.UserMapper;
import org.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.example.common.RedisConstants.*;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;



    @Override
    public void sendCode(CodeDTO codeDto) {
        String phone = codeDto.getPhone();
        Integer type = codeDto.getType();
        if (type < 1 || type > 3) {
            log.error("验证码类型参数非法，type:{}", type);
            throw new RuntimeException("类型参数错误，只能选择1、2、3");
        }
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(
                SEND_CODE_KEY + type + ":" + phone,
                code,
                SEND_CODE_TTL,
                TimeUnit.MINUTES
        );
        log.info("手机号:{}, 类型:{}, 发送验证码成功, 验证码: {}", phone, type, code);
    }

    @Override
    public RegisterVO register(RegisterDTO registerDTO) {
        String phone = registerDTO.getPhone();
        String inputCode = registerDTO.getCode();

        // 校验验证码
        String codeKey = SEND_CODE_KEY + "1:" + phone;
        String cacheCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (cacheCode == null || !cacheCode.equals(inputCode)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        stringRedisTemplate.delete(codeKey);

        // 检查用户是否已存在
        User existUser = this.query().eq("phone", phone).last("limit 1").one();
        if (existUser != null) {
            throw new RuntimeException("用户已存在");
        }

        // 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setUsername(registerDTO.getUsername());
        user.setStudentNo(registerDTO.getStudentNo());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword(), BCrypt.gensalt()));
        user.setSchool(registerDTO.getSchool());
        user.setStatus(1);
        user.setCreditScore(100);
        this.save(user);

        // 生成 token 并存入 Redis
        String token = UUID.randomUUID().toString();
        UserDTO userDto = new UserDTO();
        userDto.setId(user.getId());
        userDto.setPhone(phone);
        userDto.setUsername(registerDTO.getUsername());
        userDto.setStudentNo(registerDTO.getStudentNo());

        String tokenKey = LOGIN_TOKEN_KEY + token;
        Map<String, Object> objMap = BeanUtil.beanToMap(userDto, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldVal) -> {
                            if (fieldVal == null) {
                                return null;
                            }
                            return fieldVal.toString();
                        }));
        Map<String, String> strMap = new HashMap<>();
        objMap.forEach((k, v) -> strMap.put(k, v != null ? v.toString() : ""));

        stringRedisTemplate.opsForHash().putAll(tokenKey, strMap);
        stringRedisTemplate.expire(tokenKey, LOGIN_TOKEN_TTL, TimeUnit.SECONDS);

        RegisterVO vo = new RegisterVO();
        vo.setUserId(user.getId());
        vo.setToken(token);
        return vo;
    }

    @Override
    public UserLoginVo login(UserLoginDto loginDto, HttpSession session) {
        String phone = loginDto.getPhone();
        String inputCode = loginDto.getCode();

        // 校验验证码
        String codeKey = SEND_CODE_KEY + "2:" + phone;
        String cacheCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (cacheCode == null || !cacheCode.equals(inputCode)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        stringRedisTemplate.delete(codeKey);

        // 查用户，没有就自动注册
        User user = this.query().eq("phone", phone).last("limit 1").one();
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setUsername("用户" + phone.substring(7));
            user.setStatus(1);
            user.setCreditScore(100);
            this.save(user);
        }

        // 生成 token 存入 Redis
        String token = UUID.randomUUID().toString();
        UserDTO userDto = new UserDTO();
        userDto.setId(user.getId());
        userDto.setPhone(phone);
        userDto.setUsername(user.getUsername());
        userDto.setNickname(user.getNickname());
        userDto.setAvatar(user.getAvatar());
        userDto.setStatus(user.getStatus());

        String tokenKey = LOGIN_TOKEN_KEY + token;
        Map<String, Object> objMap = BeanUtil.beanToMap(userDto, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldVal) -> {
                            if (fieldVal == null) {
                                return null;
                            }
                            return fieldVal.toString();
                        }));

        Map<String, String> strMap = new HashMap<>();
        objMap.forEach((k, v) -> strMap.put(k, v != null ? v.toString() : ""));

        stringRedisTemplate.opsForHash().putAll(tokenKey, strMap);
        stringRedisTemplate.expire(tokenKey, LOGIN_TOKEN_TTL, TimeUnit.SECONDS);

        // 存 session
        session.setAttribute("loginUser", userDto);

        // 返回
        UserLoginVo vo = new UserLoginVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setToken(token);
        return vo;
    }
    @Override
    public void updateInfo(Long userId, UserUpdateDTO dto) {
        this.lambdaUpdate()
                .eq(User::getId, userId)
                .set(dto.getNickname() != null, User::getNickname, dto.getNickname())
                .set(dto.getAvatar() != null, User::getAvatar, dto.getAvatar())
                .set(dto.getGender() != null, User::getGender, dto.getGender())
                .set(dto.getSchool() != null, User::getSchool, dto.getSchool())
                .update();
    }

    @Override
    public User getById(Long id) {
        User id1 = query().eq("id", id).one();
        return id1;
    }


}
