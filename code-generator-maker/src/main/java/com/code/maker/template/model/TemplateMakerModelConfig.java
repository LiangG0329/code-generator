package com.code.maker.template.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模板制作工具模型配置封装类
 *
 * @author Liang
 * @create 2024/2/24
 */
@Data
public class TemplateMakerModelConfig {

    private List<TemplateMakerModelConfig.ModelConfigDTO> models;

    private ModelGroupConfig modelGroupConfig;

    /**
     * 模型基本配置类
     */
    @Data
    @NoArgsConstructor
    public static class ModelConfigDTO {
        private String fieldName;
        private String type;
        private String description;
        private Object defaultValue;
        private String abbr;

        // 用于替换哪些文本
        private String replaceText;
    }

    /**
     * 模型分组配置类
     */
    @Data
    @NoArgsConstructor
    public static class ModelGroupConfig {
        private String condition;
        private String groupKey;
        private String groupName;
        private String type;
        private String description;
    }
}
