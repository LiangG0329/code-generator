package com.code.maker.model;

import lombok.Data;

/**
 * 数据模型
 * @author Liang
 * @create 2024/2/13
 */
@Data
public class DataModel {

    /**
     * 是否生成循环,默认true
     */
    private Boolean loop = true;

    /**
     * 是否生成 .gitignore README文件,默认true
     */
    private Boolean neeGit = true;

    /**
     * 核心模板参数
     */
    private MainTemplate mainTemplate = new MainTemplate();

    /**
     * 用于生成核心模板
     */
    @Data
    public static class MainTemplate {
        /**
         * 作者注释
         */
        private String author = "admin";

        /**
         * 输出信息
         */
        private String outputText = "output:";
    }
}
