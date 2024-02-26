package com.code.maker.template.utils;

import cn.hutool.core.util.StrUtil;
import com.code.maker.meta.Meta;
import com.code.maker.template.model.TemplateMakerOutputConfig;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**.
 * 工具类
 *
 * @author Liang
 * @create 2024/2/26
 */
public class TemplateMakerUtils {
    /**
     * 从非分组文件中移除分组内已有的文件
     *
     * @param fileDTOList 文件列表
     * @return 去重后的文件列表
     */
    public static List<Meta.FileConfigDTO.FileDTO> removeGroupFilesFromRoot(List<Meta.FileConfigDTO.FileDTO> fileDTOList) {
        // 获取所有分组类型文件 (groupKey不为空)
        List<Meta.FileConfigDTO.FileDTO> groupFileDTOList = fileDTOList.stream()
                .filter(fileDTO -> StrUtil.isNotBlank(fileDTO.getGroupKey()))
                .collect(Collectors.toList());

        // 获取所有分组内的文件列表
        List<Meta.FileConfigDTO.FileDTO> allGroupInnerFileDTOList = groupFileDTOList.stream()
                .flatMap(fileDTO -> fileDTO.getFiles().stream())
                .collect(Collectors.toList());

        // 获取所有分组内文件输入路径的集合
        Set<String> allGroupInnerFilesInputPathSet = allGroupInnerFileDTOList.stream()
                .map(Meta.FileConfigDTO.FileDTO::getInputPath)
                .collect(Collectors.toSet());

        // 移除所有名称在 set 中的外层文件
        return fileDTOList.stream()
                .filter(fileDTO -> !allGroupInnerFilesInputPathSet.contains(fileDTO.getInputPath()))
                .collect(Collectors.toList());
    }

    /**
     * 从分组文件中移除指定文件
     *
     * @param fileDTOList 文件对象列表
     * @param removeFiles 指定文件列表
     * @return 移除指定文件的对象列表
     */
    public static List<Meta.FileConfigDTO.FileDTO> removeFilesFromGroup(
            List<Meta.FileConfigDTO.FileDTO> fileDTOList,
            List<TemplateMakerOutputConfig.FileConfigDTO> removeFiles) {
        // 获取所有需移除的文件路径集合
        Set<String> removeFilePathsSet = removeFiles.stream()
                .map(TemplateMakerOutputConfig.FileConfigDTO::getPath)
                .collect(Collectors.toSet());

        for (Meta.FileConfigDTO.FileDTO fileDTO: fileDTOList) {
            if (StrUtil.isNotBlank(fileDTO.getGroupKey())) {
                List<Meta.FileConfigDTO.FileDTO> newGroupFiles = fileDTO.getFiles().stream()
                        .filter(groupFile -> !removeFilePathsSet.contains(groupFile.getOutputPath()))
                        .collect(Collectors.toList());
                fileDTO.setFiles(newGroupFiles);
            }
        }
        return fileDTOList;
    }
}
