package org.example.aiservice.Controller;

import jakarta.annotation.Resource;
import org.example.aiservice.service.AiService;
import org.example.common.DTO.aiDTO.AiGoodsEstimateDTO;
import org.example.common.Result;
import org.example.common.DTO.aiDTO.AiGoodsTextDTO;
import org.example.common.VO.AiVO.AiGoodsTextVO;
import org.example.common.VO.AiVO.AiclassifyVO;
import org.example.common.VO.AiVO.GoodsEstimateVO;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.example.common.AiConstants.AI_REVIEW_MASTER_PROMPT;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Resource
    private AiService aiService;
    @Resource
    ChatModel chatModel;
@PostMapping("/goods/copywriting")
    public Result<AiGoodsTextVO> copyWriting(@RequestBody AiGoodsTextDTO dto) {
    AiGoodsTextVO vo = aiService.copywriting(dto);
    return Result.success(vo);
}
    @PostMapping("/reviewPic")
public Boolean reviewPic(String title,String  description)
{
    String userMsg="标题"+":"+title+","+"描述"+":"+description;
    String result = chatModel.call(new Prompt(List.of(new SystemMessage(AI_REVIEW_MASTER_PROMPT)
                    , new SystemMessage(userMsg))))
            .getResult()
            .getOutput()
            .getContent();
    return Boolean.parseBoolean(result.trim().toLowerCase());

}
    @PostMapping("/goods/estimate")
    public Result aiEstimate(@RequestBody AiGoodsEstimateDTO dto){
        GoodsEstimateVO vo=aiService.estimate(dto);
        return Result.success(vo);
    }
    @PostMapping("/goods/classify")
    public Result aiClassify(@RequestParam String title,@RequestParam String description){
        AiclassifyVO vo=aiService.classify(title,description);
        return Result.success(vo);

    }
    @GetMapping("/recommend")
    public String recommend(String text){
     String msg=  aiService.recommend(text);
    return msg;
    }

}
