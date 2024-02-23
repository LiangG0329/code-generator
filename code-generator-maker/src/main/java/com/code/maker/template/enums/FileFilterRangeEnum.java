package com.code.maker.template.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 文件过滤范围枚举
 *
 * @author Liang
 * @create 2024/2/23
 */
@Getter
public enum FileFilterRangeEnum {
    FILE_NAME("文件名称", "fileName"),
    FILE_CONTENT("文件内容", "fileContent");

    private final String text;

    private final String value;

    FileFilterRangeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取 文件过滤范围枚举对象
     * @param value 目标枚举值
     * @return 文件过滤范围枚举对象
     */
    public static FileFilterRangeEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (FileFilterRangeEnum anEnum: FileFilterRangeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
