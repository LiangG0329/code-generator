package com.code.web.model.dto.generator;

import com.code.maker.meta.Meta;
import lombok.Data;

import java.io.Serializable;

/**
 * 使用请求
 *
 * @author Liang
 * @create 2024/3/1
 */
@Data
public class GeneratorMakeRequest implements Serializable {
    /**
     * 元信息
     */
    private Meta meta;

    /**
     * 模板文件压缩包路径
     */
    private String zipFilePath;

    private static final long serialVersionUID = 1L;
}
