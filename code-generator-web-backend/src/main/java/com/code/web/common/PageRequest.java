package com.code.web.common;

import com.code.web.constant.CommonConstant;
import lombok.Data;

/**
 * 分页请求
 *
 * @author Liang
 * @from <a href="https://github.com/LiangG0329/code-generator">代码生成</a>
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}
