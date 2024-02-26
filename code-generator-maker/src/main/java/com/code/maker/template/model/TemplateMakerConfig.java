package com.code.maker.template.model;

import com.code.maker.meta.Meta;
import lombok.Data;

/**
 * 模板制作方法参数封装的配置类
 *
 * @author Liang
 * @create 2024/2/25
 */
@Data
public class TemplateMakerConfig {
    private Meta meta = new Meta();
    private String originProjectPath;
    private TemplateMakerFileConfig fileConfig = new TemplateMakerFileConfig();
    private TemplateMakerModelConfig modelConfig = new TemplateMakerModelConfig();
    private TemplateMakerOutputConfig outputConfig;
    private Long id;
}
