package com.code.maker.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.code.maker.template.enums.FileFilterRangeEnum;
import com.code.maker.template.enums.FileFilterRuleEnum;
import com.code.maker.template.model.FileFilterConfig;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件过滤器
 *
 * @author Liang
 * @create 2024/2/23
 */
public class FileFilter {

    /**
     *  对某个文件或目录进行过滤，返回过滤后的文件列表
     * @param filePath 文件绝对路径(可以当个文件或文件目录)
     * @param fileFilterConfigList  过滤条件配置列表
     * @return 过滤后的文件列表
     */
    public static List<File> doFilter(String filePath, List<FileFilterConfig> fileFilterConfigList) {
        // 根据路径获取所有文件
        List<File> files = FileUtil.loopFiles(filePath);

        // 过滤,并返回结果
        return files.stream()
                .filter(file -> doSingleFileFilter(fileFilterConfigList, file))
                .collect(Collectors.toList());
    }

    /**
     * 对单个文件过滤
     * @param fileFilterConfigList 过滤条件配置列表
     * @param file 需要过滤的文件
     * @return 过滤结果
     */
    public static boolean doSingleFileFilter(List<FileFilterConfig> fileFilterConfigList, File file) {
        String fileName = file.getName();
        String fileContent = FileUtil.readUtf8String(file);

        // 所有过滤器校验结束的结果
        boolean result = true;

        if (CollUtil.isEmpty(fileFilterConfigList)) {
            return true;
        }

        for (FileFilterConfig fileFilterConfig: fileFilterConfigList) {
            String range = fileFilterConfig.getRange();
            String rule = fileFilterConfig.getRule();
            String value = fileFilterConfig.getValue();

            // 对文件范围过滤
            FileFilterRangeEnum fileFilterRangeEnum = FileFilterRangeEnum.getEnumByValue(range);
            if (fileFilterRangeEnum == null) {
                continue;
            }

            // 要过滤的原内容
            String content = fileName;

            switch (fileFilterRangeEnum) {
                case FILE_NAME:
                    content = fileName;
                    break;
                case FILE_CONTENT:
                    content = fileContent;
                    break;
                default:
                    content = fileName;
            }

            // 对文件规则过滤
            FileFilterRuleEnum fileFilterRuleEnum = FileFilterRuleEnum.getEnumByValue(rule);
            if (fileFilterRuleEnum == null) {
                continue;
            }
            switch (fileFilterRuleEnum) {
                case CONTAINS:
                    result = content.contains(value);
                    break;
                case STARTS_WITH:
                    result = content.startsWith(value);
                    break;
                case ENDS_WITH:
                    result = content.endsWith(value);
                    break;
                case REGEX:
                    result = content.matches(value);
                    break;
                case EQUALS:
                    result = content.equals(value);
                    break;
                default:
            }

            // 存在不满足过滤条件,直接返回
            if (!result) {
                return false;
            }
        }

        // 所有过滤条件都满足
        return true;
    }
}
