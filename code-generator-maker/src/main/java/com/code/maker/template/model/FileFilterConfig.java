package com.code.maker.template.model;

import lombok.Builder;
import lombok.Data;

/**
 * 文件过滤配置
 * @author Liang
 * @create 2024/2/23
 */
@Data
@Builder
public class FileFilterConfig {
    /**
     * 过滤范围
     */
    private String range;

    /**
     * 过滤规则
     */
    private String rule;

    /**
     * 过滤值
     */
    private String value;
}
