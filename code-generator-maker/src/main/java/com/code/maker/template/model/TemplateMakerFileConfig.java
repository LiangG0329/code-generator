package com.code.maker.template.model;

import com.code.maker.meta.Meta;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模板制作工具文件配置封装类
 * @author Liang
 * @create 2024/2/23
 */
@Data
public class TemplateMakerFileConfig {
    private List<TemplateMakerFileConfig.FileConfigDTO> files;

    @Data
    @NoArgsConstructor
    public static class FileConfigDTO {
        private String path;
        private List<FileFilterConfig> fileFilterConfigList;
    }
}
