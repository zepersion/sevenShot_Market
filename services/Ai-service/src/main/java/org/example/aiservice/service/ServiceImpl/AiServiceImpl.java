package org.example.aiservice.service.ServiceImpl;

import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.aiservice.service.AiService;
import org.example.common.DTO.aiDTO.AiGoodsEstimateDTO;
import org.example.common.DTO.aiDTO.AiGoodsTextDTO;
import org.example.common.VO.AiVO.AiGoodsTextVO;
import org.example.common.VO.AiVO.AiclassifyVO;
import org.example.common.VO.AiVO.GoodsEstimateVO;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.example.common.AiConstants.*;
import static org.example.common.RedisConstants.*;

@Slf4j
@Service
public class AiServiceImpl implements AiService {
@Resource
private StringRedisTemplate stringRedisTemplate;
@Resource
private ChatModel chatModel;
    @PostConstruct
    public void initRule() {
        DegradeRule rule = new DegradeRule("aiCopywriting")
                .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
                .setCount(3000)           // RT阈值3秒
                .setSlowRatioThreshold(0.5) // 50%慢调用
                .setMinRequestAmount(5)    // 最少5个请求
                .setTimeWindow(30);       // 熔断30秒
        DegradeRuleManager.loadRules(Collections.singletonList(rule));
    }
    @Override
    @SentinelResource(value = "aiCopywriting", blockHandler = "copywritingFallback")
    public AiGoodsTextVO copywriting(AiGoodsTextDTO dto) {
        //查缓存
        String cacheKey=AI_COPYWRITING_KEY+dto.getCategoryId()+":"+dto.getKeywords();
        String s = stringRedisTemplate.opsForValue().get(cacheKey);
        if(!s.isEmpty()||s!=null){
            log.info("{}",s);
          return  JSONUtil.toBean(s, AiGoodsTextVO.class);
        }
       String degreeName = switch (dto.getDegree()) {
            case 1 -> "全新";
            case 2 -> "几乎全新";
            case 3 -> "轻微使用痕迹";
            case 4 -> "明显使用痕迹";
            default -> "未知";
        };
        String styleName = dto.getStyle() != null
                ? switch (dto.getStyle()) { case 1 -> "简洁";
            case 2 -> "详细";
            case 3 -> "活泼";
            default -> "详细"; }
                : "详细";

        String userInfo=String.format("分类ID:%d, 关键词:%s, 原价:%s, 售价:%s, 成色:%s, 风格:%s",dto.getStyle(),dto.getCategoryId(),dto.getKeywords(),dto.getDegree(),  degreeName, styleName);


        String content = chatModel
                .call(new Prompt(List.of(
                        new SystemMessage( AI_GOODS_TEXT_PROMPT),
                        new UserMessage(userInfo)
                )))
                        .getResult()
                                .getOutput()
                                        .getContent();
        log.info("当前生成文案{}",content);

        stringRedisTemplate.opsForValue().set(cacheKey,content,AI_CACHE_TTL, TimeUnit.MINUTES);

        return JSONUtil.toBean(content, AiGoodsTextVO.class);
    }
    @SentinelResource(value = "estimate", blockHandler = "estimeteFallback")
    @Override
    public GoodsEstimateVO estimate(AiGoodsEstimateDTO dto) {
        String cacheKey=AI_ESTIMATE_KEY+":"+dto.getCategoryId()+dto.getBrand();
        String value = stringRedisTemplate.opsForValue().get(cacheKey);
        if(!value.isEmpty()||value!=null){
            log.info("返回{}",value);
        return JSONUtil.toBean(value, GoodsEstimateVO.class);
        }

        String degreeName = switch (dto.getDegree()) {
            case 1 -> "全新";
            case 2 -> "几乎全新";
            case 3 -> "轻微使用痕迹";
            case 4 -> "明显使用痕迹";
            default -> "未知";
        };
        String userInfo=String.format("分类ID:%d, 型号:%s, 成色:%s, 品牌:%s, 原价:%ld, 购买时间:%Y-%m-%d,补充描述:%s,成色数字代表的含义:%s",
               dto.getCategoryId(),dto.getModel(),
                dto.getDegree(), dto.getBrand(),dto.getOriginalPrice(),dto.getPurchaseDate(),dto.getDescription(),degreeName);
        var prompt = List.of(new SystemMessage(AI_ESTIMETE_PROMPT),
        new UserMessage(userInfo));
        String content = chatModel.call(new Prompt((Message) prompt))
                .getResult()
                .getOutput()
                .getContent();

        log.info("当前生成文案{}",content);
        stringRedisTemplate.opsForValue().set(cacheKey,content,AI_CACHE_TTL, TimeUnit.MINUTES);
        return JSONUtil.toBean(content, GoodsEstimateVO.class);
    }
    @SentinelResource(value = "classify",blockHandler = " classifyFallback")
    @Override
    public AiclassifyVO classify(String title, String description) {
        String cacheKey=AI_CLASSIFY_KEY+":"+title;
        String value = stringRedisTemplate.opsForValue().get(cacheKey);
        if(!value.isEmpty()||value!=null){
            log.info("返回{}",value);
            return JSONUtil.toBean(value, AiclassifyVO.class);
        }
        String userInfo=String.format("标题:%s,描述:%s",title,description);
        String content = chatModel.call(new Prompt(List.of(
                        new UserMessage(userInfo),
                        new SystemMessage(AI_CLASSIFY_PROMPT)
                )))
                .getResult()
                .getOutput()
                .getContent();
   log.info("当前生成文案:{}",content);
        stringRedisTemplate.opsForValue().set(cacheKey,content,AI_CACHE_TTL, TimeUnit.MINUTES);

        return JSONUtil.toBean(content, AiclassifyVO.class);
    }

    //熔断兜底
    public AiGoodsTextVO copywritingFallback(AiGoodsTextDTO dto, BlockException ex){
        AiGoodsTextVO vo = new AiGoodsTextVO();
        vo.setDescription(ex.getMessage());
        vo.setTitle(dto.getKeywords() + " 低价转让");
        vo.setDescription("商品" + dto.getKeywords()
                + "\n价格" + dto.getSellingPrice() + "元"
                + "\n成色 详见实物");
        vo.setTags(List.of(dto.getKeywords().split(" ")));
        return vo;

    }
    public GoodsEstimateVO estimeteFallback(AiGoodsEstimateDTO dto,BlockException ex){
        GoodsEstimateVO vo = new GoodsEstimateVO();
        vo.setSuggestedPrice(dto.getOriginalPrice());
        return vo;
    }
    public AiclassifyVO classifyFallback(String title,String descirption){
        AiclassifyVO vo = new AiclassifyVO();
        vo.setCategoryName(title);
        vo.setCategoryName(descirption);
        return vo;

    }
}
