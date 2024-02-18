package com.code.maker.model;

import lombok.Data;

/**
 * 数据模型
 * @author Liang
 * @create 2024/2/13
 */
@Data
public class DataModel {
    /* 需求: 1.添加作者 2.输出信息 3.可选循环 */
    /**
     * 是否生成循环,默认true
     */
    private boolean loop = true;

    /**
     * 作者注释
     */
    private String author = "admin";

    /**
     * 输出信息
     */
    private String outputText = "output:";
}
