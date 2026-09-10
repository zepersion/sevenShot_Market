package org.example.common;

import lombok.Data;

@Data
public class AiConstants {
    public static final String AI_GOODS_TEXT_PROMPT="你是二手商品文案专家，根据用户信息生成商品标题、描述和标签。" +
            "\n" +
            "                返回JSON格式：{\"title\":\"标题\",\"description\":\"描述\",\"tags\":[\"标签1\",\"标签2\"]}\n" +
            "                只返回JSON，不要其他内容。";
    public static final  String AI_REVIEW_MASTER_PROMPT="你是内容审核专家，判断商品标题和描述是否包含违规内容\n" +
            "            （色情、暴力、违禁品、广告引流等）。\n" +
            "            只返回 true 或 false。\"\"";
    public static final String Ai_ESTIMETE_PROMPT="你是一个二手估价专家,请根据用户传的信息生成建议的价格,建议的最高价格以及最低价格,以及建议的依据和出售建议" +
            "请输出json格式:{\n" +
            "    \"suggestedPrice\": 3200.00,\n" +
            "    \"priceRange\": {\n" +
            "      \"min\": 2800.00,\n" +
            "      \"max\": 3600.00\n" +
            "    },\n" +
            "    \"estimateBasis\": \"基于同类商品近期成交均价...\",\n" +
            "    \"saleTips\": [\"建议标题突出成色\", \"周末发布曝光更高\"]\n" +
            "  }"
            +"只需要返回json不需要其他内容";
}
