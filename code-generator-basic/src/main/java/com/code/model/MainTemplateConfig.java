package com.code.model;

import lombok.Data;

/**
 * 动态模板配置
 * @author Liang
 * @create 2024/2/13
 */
@Data
public class MainTemplateConfig {
    /* 需求: 1.添加作者 2.输出信息 3.可选循环 */
    /**
     * 是否生成循环,默认true
     */
    private boolean loop = true;

    /**
     * 作者注释
     */
    private String author = "unknown";

    /**
     * 输出信息
     */
    private String outputText = "output:";
}
