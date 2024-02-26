package com.code.maker.template.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模板制作工具文件配置封装类
 *
 * @author Liang
 * @create 2024/2/23
 */
@Data
public class TemplateMakerFileConfig {

    private List<TemplateMakerFileConfig.FileConfigDTO> files;

    private FileGroupConfig fileGroupConfig;

    /**
     * 文件路径,生成条件和文件过滤配置类
     */
    @Data
    @NoArgsConstructor
    public static class FileConfigDTO {
        private String path;
        private String condition;
        private List<FileFilterConfig> filterConfigList;
    }

    /**
     * 文件分组配置类
     */
    @Data
    @NoArgsConstructor
    public static class FileGroupConfig {
        private String condition;
        private String groupKey;
        private String groupName;
    }
}
