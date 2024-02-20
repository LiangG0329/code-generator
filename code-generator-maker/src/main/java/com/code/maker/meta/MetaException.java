package com.code.maker.meta;

/**
 * 元信息异常类
 * @author Liang
 * @create 2024/2/19
 */
public class MetaException extends RuntimeException {

    public MetaException(String message) {
        super(message);
    }

    public MetaException(String message, Throwable cause) {
        super(message, cause);
    }
}
