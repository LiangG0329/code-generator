package com.code.maker.model;

import lombok.Data;

/**
 * 用于生成核心模板
 * @author Liang
 * @create 2024/2/21
 */
@Data
public class MainTemplate {
    /**
     * 作者注释
     */
    private String author = "admin";

    /**
     * 输出信息
     */
    private String outputText = "output:";
}
