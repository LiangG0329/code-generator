package com.code.web.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 * @author Liang
 * @from <a href="https://github.com/LiangG0329/code-generator">代码工坊</a>
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}