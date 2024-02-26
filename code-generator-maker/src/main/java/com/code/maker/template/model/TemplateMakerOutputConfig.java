package com.code.maker.template.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模板制作工具输出配置类
 *
 * @author Liang
 * @create 2024/2/26
 */
@Data
public class TemplateMakerOutputConfig {
    /**
     * 从未分组文件中移除组内的同名文件，默认true
     */
    private boolean removeGroupFilesFromRoot = true;

    /**
     * 从分组文件中移除指定文件，默认false,当次参数为true时，removeGroupFilesFromRoot应为false,保留分组外的同名文件
     */
    private boolean removeFilesFromGroup = false;

    /**
     * 指定文件列表
     */
    private List<TemplateMakerOutputConfig.FileConfigDTO> files;

    @Data
    @NoArgsConstructor
    public static class FileConfigDTO {
        private String path;
    }
}
