package com.code.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.code.maker.meta.Meta;
import com.code.maker.meta.enums.FileGenerateTypeEnum;
import com.code.maker.meta.enums.FileTypeEnum;
import com.code.maker.template.enums.FileFilterRangeEnum;
import com.code.maker.template.enums.FileFilterRuleEnum;
import com.code.maker.template.model.FileFilterConfig;
import com.code.maker.template.model.TemplateMakerFileConfig;
import com.code.maker.template.model.TemplateMakerModelConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Filter;
import java.util.stream.Collectors;

/**
 * 模板制作工具
 * @author Liang
 * @create 2024/2/22
 */
public class TemplateMaker {
    /**
     * 模板制作
     * @param meta 提供项目基本信息,保存文件和模型信息的对象,用于生成配置文件
     * @param originProjectPath 原始模板项目路径
     * @param templateMakerFileConfig 模板制作工具文件配置封装类,包含文件路径和过滤配置列表
     * @param templateMakerModelConfig 模板制作工具模型配置封装类,包含模型基本信息和模型分组信息
     * @param id 工作空间 id
     * @return 生成的模板对应的工作空间id.若指定的id不存在或未指定,则自动生成id; 否则返回id等于指定id
     */
    public static long makeTemplate(Meta meta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, Long id) {
        // 没有id则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId(); // 每次制作分配一个唯一 id（使用雪花算法），作为工作空间的名称,从而实现隔离
        }
        // 业务逻辑:制作模板
        // 0.每次制作模板时，不直接修改原始项目的任何文件，而是先复制原项目到一个临时的、专门用于制作模板的工作空间
        // 获得项目路径,原始模板项目路径由参数 originProjectPath 提供
        String projectPath = System.getProperty("user.dir");

        // 复制目录
        String tempDirPath = projectPath + File.separator + ".temp";  // .temp 临时目录作为工作空间的根目录
        String templatePath = tempDirPath + File.separator + id;  // .temp/id/..
        // 是否为首次制作模板
        // 支持多次制作: 目录不存在，则是首次制作;否则非首次,不再执行复制
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originProjectPath, templatePath, true);
            System.out.println("未指定id或id指定的工作空间不存在,生成工作空间. templatePath = " + templatePath);
        } else {
            System.out.println("已指定工作空间. templatePath = " + templatePath);
        }

        // 一.输入信息
        // 1.输入项目基本信息由参数 meta 提供

        // 2.输入文件信息,输入文件路径由 fileInputPath 参数提供
        // sourceRootPath 工作空间内复制项目的路径
        String sourceRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString();
        // 注意 win 系统需要对路径进行转义
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");

        // 3.输入模型参数信息由 modelDTO 参数提供

        // 二.使用字符串替换，生成模板文件
        // 本次新增的文件配置列表
        List<Meta.FileConfigDTO.FileDTO> fileDTOList = new ArrayList<>();
        List<TemplateMakerFileConfig.FileConfigDTO> templateMakerFileConfigList = templateMakerFileConfig.getFiles();
        // 支持输入多个路径,遍历文件路径列表,多次执行生成模板
        for (TemplateMakerFileConfig.FileConfigDTO fileConfigDTO: templateMakerFileConfigList) {
            String fileInputAbsolutePath = fileConfigDTO.getPath();
            // 如果填的是相对路径，要改为绝对路径
            if (!fileInputAbsolutePath.startsWith(sourceRootPath)) {
                fileInputAbsolutePath = sourceRootPath + File.separator + fileInputAbsolutePath;
            }
            // 获取根据过滤配置过滤后的文件列表(过滤后不会存在目录)
            List<File> files = FileFilter.doFilter(fileInputAbsolutePath, fileConfigDTO.getFileFilterConfigList());
            // 过滤处理,不处理已生成的 FTL 模板文件
            files = files.stream()
                    .filter(file -> !file.getAbsolutePath().endsWith(".ftl"))
                    .collect(Collectors.toList());
            // 遍历过滤后的文件列表,支持批量制作模板文件
            for (File file: files) {
                Meta.FileConfigDTO.FileDTO fileDTO = makeFileTemplate(templateMakerModelConfig, sourceRootPath, file);
                fileDTOList.add(fileDTO);
            }
        }

        // 如果是文件组(一次制作可作为一个分组)
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if (fileGroupConfig != null) {
            String condition = fileGroupConfig.getCondition();
            String groupName = fileGroupConfig.getGroupName();
            String groupKey = fileGroupConfig.getGroupKey();

            // 新增分组配置
            Meta.FileConfigDTO.FileDTO groupFileDTO = new Meta.FileConfigDTO.FileDTO();
            groupFileDTO.setCondition(condition);
            groupFileDTO.setGroupName(groupName);
            groupFileDTO.setGroupKey(groupKey);
            // fileDTOList文件列表放入一个分组内
            groupFileDTO.setFiles(fileDTOList);
            fileDTOList = new ArrayList<>();
            fileDTOList.add(groupFileDTO);  // 将分组文件包装后加入新列表
        }

        // 处理模型信息
        List<TemplateMakerModelConfig.ModelConfigDTO> templateMakerModelConfigModels = templateMakerModelConfig.getModels();
        // 转换为配置接受的 Meta.ModelConfigDTO.ModelDTO 对象列表
        List<Meta.ModelConfigDTO.ModelDTO> inputModelDTOList = templateMakerModelConfigModels.stream()
                .map(modelConfigDTO -> {
                    Meta.ModelConfigDTO.ModelDTO modelDTO = new Meta.ModelConfigDTO.ModelDTO();
                    BeanUtil.copyProperties(modelConfigDTO, modelDTO);
                    return modelDTO;
                }).collect(Collectors.toList());

        // 本次新增的模型配置列表
        List<Meta.ModelConfigDTO.ModelDTO> modelDTOList = new ArrayList<>();
        
        // 如果是模型组(一次制作可作为一个分组)
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            String condition = modelGroupConfig.getCondition();
            String groupName = modelGroupConfig.getGroupName();
            String groupKey = modelGroupConfig.getGroupKey();

            // 新增分组配置
            Meta.ModelConfigDTO.ModelDTO groupModelDTO = new Meta.ModelConfigDTO.ModelDTO();
            groupModelDTO.setCondition(condition);
            groupModelDTO.setGroupName(groupName);
            groupModelDTO.setGroupKey(groupKey);
            // 转化后的inputModelDTOList模型列表放入一个分组内
            groupModelDTO.setModels(inputModelDTOList);
            modelDTOList = new ArrayList<>();
            modelDTOList.add(groupModelDTO);  // 将分组模型包装后加入新列表
        } else {
            // 不分组，添加所有的模型信息到列表
            modelDTOList.addAll(inputModelDTOList);
        }

        // 三.生成配置文件 meta.json
        String metaOutputPath = templatePath + File.separator + "meta.json";
        // 1.配置参数 fileConfig modelConfig

        // 支持多次制作: 如果已有 meta.json 文件，说明不是第一次制作,则在 meta 基础上进行修改,追加新配置参数;否则执行生成
        if (FileUtil.exist(metaOutputPath)) {
            meta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);  // 读取json文件为String,再转化为对象
            // 追加配置参数
            // fileConfig
            List<Meta.FileConfigDTO.FileDTO> oldFiles = meta.getFileConfig().getFiles();
            oldFiles.addAll(fileDTOList);
            // modelConfig
            List<Meta.ModelConfigDTO.ModelDTO> oldModels = meta.getModelConfig().getModels();
            oldModels.addAll(modelDTOList);

            // 配置去重
            meta.getFileConfig().setFiles(distinctFiles(oldFiles));
            meta.getModelConfig().setModels(distinctModels(oldModels));
        } else {
            // fileConfig
            Meta.FileConfigDTO fileConfigDTO = new Meta.FileConfigDTO();
            fileConfigDTO.setSourceRootPath(sourceRootPath);
            List<Meta.FileConfigDTO.FileDTO> newFileDTOList = new ArrayList<>();
            newFileDTOList.addAll(fileDTOList);
            fileConfigDTO.setFiles(newFileDTOList);
            meta.setFileConfig(fileConfigDTO);

            // modelConfig
            Meta.ModelConfigDTO modelConfigDTO = new Meta.ModelConfigDTO();
            List<Meta.ModelConfigDTO.ModelDTO> newModelDTOList = new ArrayList<>();
            newModelDTOList.addAll(modelDTOList);
            modelConfigDTO.setModels(newModelDTOList);
            meta.setModelConfig(modelConfigDTO);
        }
        // 2.输出新的 meta.json 元信息文件或更新元信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(meta), metaOutputPath);  // 使用JSONUtil将对象转为String,再写文件

        return id;
    }

    /**
     * 制作单个模板文件
     * @param templateMakerModelConfig 模板制作工具模型配置封装类对象
     * @param sourceRootPath 工作空间内复制项目的路径
     * @param inputFile 需制作模板的文件对象
     * @return fileDTO配置文件信息对象
     */
    public static Meta.FileConfigDTO.FileDTO makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath, File inputFile) {
        // 一.输入信息
        // 制作模板的原始文件的绝对路径（用于制作模板）
        // 注意 win 系统需要对路径进行转义
        String fileInputAbsolutePath = inputFile.getAbsolutePath().replaceAll("\\\\", "/");
        String fileOutputAbsolutePath = fileInputAbsolutePath + ".ftl";

        // 文件输入输出相对路径（用于生成配置）
        String fileInputPath = fileInputAbsolutePath.replace(sourceRootPath + "/", "");  // 其他文件 .java .yml ...
        String fileOutputPath = fileInputPath + ".ftl";  // FTL文件

        // 3.输入模型参数信息由 modelDTO 参数提供

        // 二.使用字符串替换，生成模板文件
        String fileContent;
        // 支持多次制作: 如果已有 .ftl模板文件，说明不是第一次制作，则在已生成的模板基础上再次修改模板
        boolean hasTemplateFile = FileUtil.exist(fileOutputAbsolutePath);
        if (hasTemplateFile) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);  // 读取已生成的模板文件内容
        } else {
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);  // 读取原始文件作为模板原型
        }

        // 支持多个模型:对同一个文件的内容,遍历模型进行多轮替换
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        String newFileContent = fileContent;
        String replacement;
        for (TemplateMakerModelConfig.ModelConfigDTO modelConfigDTO: templateMakerModelConfig.getModels()) {
            // 不是分组
            if (modelGroupConfig == null) {
                replacement = String.format("${%s}", modelConfigDTO.getFieldName());
            } else {
                String groupKey = modelGroupConfig.getGroupKey();
                // 替换文本添加 groupKey 分组层级
                replacement = String.format("${%s.%s}", groupKey, modelConfigDTO.getFieldName());
            }
            //多次替换
            newFileContent = StrUtil.replace(newFileContent, modelConfigDTO.getReplaceText(), replacement);
        }

        // 三.生成配置文件信息对象,并返回
        // fileDTO
        Meta.FileConfigDTO.FileDTO fileDTO = new Meta.FileConfigDTO.FileDTO();
        // 注意文件输入路径要和输出路径反转,即 meta文件(代码生成器配置文件) 内容应该以FTL文件作为输入路径,生成文件作为输出
        fileDTO.setInputPath(fileOutputPath);
        fileDTO.setOutputPath(fileInputPath);
        fileDTO.setType(FileTypeEnum.FILE.getValue());
        fileDTO.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        // - 生成文件和原文件内容一致且没有同名.ftl文件，则当前文件未制作模板，为static静态生成;
        // - 若内容一致但存在同名.ftl模板文件,则此次生成未修改旧模板,内容不同则此次生成修改旧模版,类型都为dynamic;
        // 是否更改了文件内容
        boolean contentEquals = newFileContent.equals(fileContent);
        if (!hasTemplateFile) {
            if (contentEquals) {
                // 静态生成,meta.json中 输入路径=生成文件路径(即输出文件不是.ftl文件), generateType=static
                fileDTO.setInputPath(fileInputPath);
                fileDTO.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            } else {
                // 不存在.ftl模版文件,且生成文件内容与原始文件不同,即添加了动态参数,为动态生成, generateType=dynamic,输出.ftl 模板文件
                FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
                System.out.println("生成新模板文件 fileOutputAbsolutePath = " + fileOutputAbsolutePath);
            }
        } else if (!contentEquals) {
            // 存在.ftl模版文件,且生成文件内容与原始文件不同,即添加了动态参数,为动态生成
            FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
            System.out.println("修改已有模板文件 fileOutputAbsolutePath = " + fileOutputAbsolutePath);
        }  // 否则即存在.ftl模版文件,且内容未修改,保持 generateType=dynamic,且不重新生成

        return fileDTO;
    }

    /**
     * 文件去重
     *
     * @param fileDTOList fileDTO列表
     * @return 去重的 fileDTO 列表
     */
    public static List<Meta.FileConfigDTO.FileDTO> distinctFiles(List<Meta.FileConfigDTO.FileDTO> fileDTOList) {
        // 支持对分组文件去重
        // 策略:同分组文件merge,不同分组文件保留

        // 1.所有分组文件,以组为单位划分
        Map<String, List<Meta.FileConfigDTO.FileDTO>> groupKeyFileDTOListMap = fileDTOList.stream()
                .filter(fileDTO -> StrUtil.isNotBlank(fileDTO.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.FileConfigDTO.FileDTO::getGroupKey)
                );

        // 2.同组内的文件配置合并
        // 保存每个组对应的合并后的对象 map
        Map<String, Meta.FileConfigDTO.FileDTO> groupKeyMergeFileDTOMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.FileConfigDTO.FileDTO>> entry: groupKeyFileDTOListMap.entrySet()) {
            List<Meta.FileConfigDTO.FileDTO> tempFileDTOList = entry.getValue();
            List<Meta.FileConfigDTO.FileDTO> mergeFileDTOList = new ArrayList<>(tempFileDTOList.stream()
                    .flatMap(fileDTO -> fileDTO.getFiles().stream())
                    .collect(
                            Collectors.toMap(Meta.FileConfigDTO.FileDTO::getOutputPath, o -> o, (e, r) -> r)  // 同组内文件去重
                    ).values());

            // 每个group使用最新的group配置(tempFileDTOList中最后一个)
            Meta.FileConfigDTO.FileDTO lastFileDTO = CollUtil.getLast(tempFileDTOList);
            lastFileDTO.setFiles(mergeFileDTOList);  // 设置为merge后的列表
            String groupKey = entry.getKey();
            groupKeyMergeFileDTOMap.put(groupKey, lastFileDTO);
        }

        // 3.将文件分组添加到去重结果列表
        ArrayList<Meta.FileConfigDTO.FileDTO> resultList = new ArrayList<>(groupKeyMergeFileDTOMap.values());

        // 4.将未分组的文件添加到去重结果列表
        List<Meta.FileConfigDTO.FileDTO> noGroupKeyFileDTOList = fileDTOList.stream()
                .filter(fileDTO -> StrUtil.isBlank(fileDTO.getGroupKey())).collect(Collectors.toList());
        resultList.addAll(new ArrayList<>(noGroupKeyFileDTOList.stream()
                .collect(
                        Collectors.toMap(Meta.FileConfigDTO.FileDTO::getOutputPath, o -> o, (e, r) -> r)  // 非分组文件去重
                ).values()));

        return resultList;
    }

    /**
     * 模型去重
     *
     * @param modelDTOList modelDTO列表
     * @return 去重的 modelDTO 列表
     */
    public static List<Meta.ModelConfigDTO.ModelDTO> distinctModels(List<Meta.ModelConfigDTO.ModelDTO> modelDTOList) {
        // 支持对分组模型去重
        // 策略:同分组模型merge,不同分组模型保留

        // 1.所有分组模型,以组为单位划分
        Map<String, List<Meta.ModelConfigDTO.ModelDTO>> groupKeyModelDTOListMap = modelDTOList.stream()
                .filter(modelDTO -> StrUtil.isNotBlank(modelDTO.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.ModelConfigDTO.ModelDTO::getGroupKey)
                );

        // 2.同组内的模型配置合并
        // 保存每个组对应的合并后的对象 map
        Map<String, Meta.ModelConfigDTO.ModelDTO> groupKeyMergeModelDTOMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.ModelConfigDTO.ModelDTO>> entry: groupKeyModelDTOListMap.entrySet()) {
            List<Meta.ModelConfigDTO.ModelDTO> tempModelDTOList = entry.getValue();
            List<Meta.ModelConfigDTO.ModelDTO> mergeModelDTOList = new ArrayList<>(tempModelDTOList.stream()
                    .flatMap(modelDTO -> modelDTO.getModels().stream())
                    .collect(
                            Collectors.toMap(Meta.ModelConfigDTO.ModelDTO::getFieldName, o -> o, (e, r) -> r)  // 同组内模型去重
                    ).values());

            // 每个group使用最新的group配置(tempModelDTOList中最后一个)
            Meta.ModelConfigDTO.ModelDTO lastModelDTO = CollUtil.getLast(tempModelDTOList);
            lastModelDTO.setModels(mergeModelDTOList);  // 设置为merge后的列表
            String groupKey = entry.getKey();
            groupKeyMergeModelDTOMap.put(groupKey, lastModelDTO);
        }

        // 3.将模型分组添加到去重结果列表
        ArrayList<Meta.ModelConfigDTO.ModelDTO> resultList = new ArrayList<>(groupKeyMergeModelDTOMap.values());

        // 4.将未分组的模型添加到去重结果列表
        List<Meta.ModelConfigDTO.ModelDTO> noGroupKeyModelDTOList = modelDTOList.stream()
                .filter(modelDTO -> StrUtil.isBlank(modelDTO.getGroupKey())).collect(Collectors.toList());
        resultList.addAll(new ArrayList<>(noGroupKeyModelDTOList.stream()
                .collect(
                        Collectors.toMap(Meta.ModelConfigDTO.ModelDTO::getFieldName, o -> o, (e, r) -> r)  // 非分组模型去重
                ).values()));

        return resultList;
    }

    /** 测试 */
    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir");
        String originProjectPath = new File(projectPath).getParent() + File.separator + "code-generator-demo-projects/springboot-init";
        String inputFilePath1 = "src/main/java/com/code/springbootinit/common";
        String inputFilePath2 = "src/main/resources/application.yml";
        List<String> fileInputPathList = Arrays.asList(inputFilePath1, inputFilePath2);

        Meta meta = new Meta();
        String name = "spring-init";
        String description = "spring 示例模板生成器";
        meta.setName(name);
        meta.setDescription(description);

        Meta.ModelConfigDTO.ModelDTO modelDTO = new Meta.ModelConfigDTO.ModelDTO();

        // 第一次测试
//        modelDTO.setFieldName("outputText");
//        modelDTO.setType("String");
//        modelDTO.setDefaultValue("sum = ");
//
//        String searchStr = "Sum: ";

        // 第二次测试 多次生成 不同参数
        modelDTO.setFieldName("className");
        modelDTO.setType("String");
        modelDTO.setDefaultValue("className");

        String searchStr = "PageRequest";

        // 文件过滤测试
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileConfigDTO fileConfig1 = new TemplateMakerFileConfig.FileConfigDTO();
        fileConfig1.setPath(inputFilePath1);

        List<FileFilterConfig> fileFilterConfigList = new ArrayList<>();
        FileFilterConfig fileFilterConfig = FileFilterConfig.builder()
                .range(FileFilterRangeEnum.FILE_NAME.getValue())
                .rule(FileFilterRuleEnum.CONTAINS.getValue())
                .value("java")
                .build();
        fileFilterConfigList.add(fileFilterConfig);
        fileConfig1.setFileFilterConfigList(fileFilterConfigList);

        TemplateMakerFileConfig.FileConfigDTO fileConfig2 = new TemplateMakerFileConfig.FileConfigDTO();
        fileConfig2.setPath(inputFilePath2);
        templateMakerFileConfig.setFiles(Arrays.asList(fileConfig1, fileConfig2));

        // 文件分组配置测试
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = new TemplateMakerFileConfig.FileGroupConfig();
        fileGroupConfig.setCondition("outputText");
        fileGroupConfig.setGroupKey("test2");
        fileGroupConfig.setGroupName("测试分组");
        templateMakerFileConfig.setFileGroupConfig(fileGroupConfig);

        // 模型参数配置
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();

        // - 模型组配置
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = new TemplateMakerModelConfig.ModelGroupConfig();
        modelGroupConfig.setGroupKey("mysql");
        modelGroupConfig.setGroupName("数据库配置");
        templateMakerModelConfig.setModelGroupConfig(modelGroupConfig);

        // - 模型配置
        TemplateMakerModelConfig.ModelConfigDTO modelInfoConfig1 = new TemplateMakerModelConfig.ModelConfigDTO();
        modelInfoConfig1.setFieldName("url");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setDefaultValue("jdbc:mysql://localhost:3306/my_db");
        modelInfoConfig1.setReplaceText("jdbc:mysql://localhost:3306/my_db");

        TemplateMakerModelConfig.ModelConfigDTO modelInfoConfig2 = new TemplateMakerModelConfig.ModelConfigDTO();
        modelInfoConfig2.setFieldName("username");
        modelInfoConfig2.setType("String");
        modelInfoConfig2.setDefaultValue("root");
        modelInfoConfig2.setReplaceText("root");

        List<TemplateMakerModelConfig.ModelConfigDTO> modelConfigDTOList = Arrays.asList(modelInfoConfig1, modelInfoConfig2);
        templateMakerModelConfig.setModels(modelConfigDTOList);

        long id = TemplateMaker.makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, 1L);
        System.out.println("id = " + id);
    }
}

