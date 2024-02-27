package com.code.web.meta;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Liang
 * @create 2024/2/17
 */
@NoArgsConstructor
@Data
public class Meta {

    private String name;
    private String description;
    private String basePackage;
    private String version;
    private String author;
    private String createTime;
    private FileConfigDTO fileConfig;
    private ModelConfigDTO modelConfig;

    @NoArgsConstructor
    @Data
    public static class FileConfigDTO {
        private String sourceRootPath;
        private String inputRootPath;
        private String outputRootPath;
        private String type;
        private List<FileDTO> files;

        @NoArgsConstructor
        @Data
        public static class FileDTO {
            private String inputPath;
            private String outputPath;
            private String type;
            private String generateType;
            private String condition;
            // 分组属性
            private String groupKey;
            private String groupName;
            private List<FileDTO> files;
        }
    }

    @NoArgsConstructor
    @Data
    public static class ModelConfigDTO {
        private List<ModelDTO> models;

        @NoArgsConstructor
        @Data
        public static class ModelDTO {
            private String fieldName;
            private String type;
            private String description;
            private Object defaultValue;
            private String abbr;
            // 分组属性
            private String groupKey;
            private String groupName;
            private String condition;
            private List<ModelDTO> models;

            // 中间参数
            // 该分组下所有参数拼接字符串
            private String allArgsStr;
        }
    }
}
