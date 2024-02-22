package com.code.maker.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.code.maker.meta.Meta;
import com.code.maker.meta.enums.FileGenerateTypeEnum;
import com.code.maker.meta.enums.FileTypeEnum;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模板制作工具
 * @author Liang
 * @create 2024/2/22
 */
public class TemplateMaker {

    public static long makeTemplate(Meta meta, String originProjectPath, String fileInputPath, Meta.ModelConfigDTO.ModelDTO modelDTO, String searchStr,Long id) {
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
        String templatePath = tempDirPath + File.separator + id;
        // 是否为首次制作模板
        // 支持多次制作: 目录不存在，则是首次制作;否则非首次,不再执行复制
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originProjectPath, templatePath, true);
            System.out.println("未指定id或id指定的工作空间不存在,生成工作空间,制作新模板. templatePath = " + templatePath);
        } else {
            System.out.println("已指定工作空间,修改已有模板. templatePath = " + templatePath);
        }

        // 一.输入信息
        // 1.输入项目基本信息由参数 meta 提供

        // 2.输入文件信息,输入文件路径由 fileInputPath 参数提供
        String sourceRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString();
        // 注意 win 系统需要对路径进行转义
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");
        // 指定生成.ftl模板文件路径
        String fileOutputPath = fileInputPath + ".ftl";

        // 3.输入模型参数信息由 modelDTO 参数提供

        // 二.使用字符串替换，生成模板文件
        String fileInputAbsolutePath = sourceRootPath + File.separator + fileInputPath;
        String fileOutputAbsolutePath = sourceRootPath + File.separator + fileOutputPath;
        String fileContent = null;
        // 支持多次制作: 如果已有 .ftl模板文件，说明不是第一次制作，则在已生成的模板基础上再次修改模板
        if (FileUtil.exist(fileInputAbsolutePath)) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);  // 读取已生成的模板文件内容
        } else {
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);  // 读取原始文件作为模板原型
        }
        String replacement = String.format("${%s}", modelDTO.getFieldName());
        String newFileContent = StrUtil.replace(fileContent, searchStr, replacement);

        // 输出模板 .ftl
        FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);

        // 三.生成配置文件 meta.json
        String metaOutputPath = sourceRootPath + File.separator + "meta.json";
        // 1.构造配置参数
        // fileDTO
        Meta.FileConfigDTO.FileDTO fileDTO = new Meta.FileConfigDTO.FileDTO();
        fileDTO.setInputPath(fileInputPath);
        fileDTO.setOutputPath(fileOutputPath);
        fileDTO.setType(FileTypeEnum.FILE.getValue());
        fileDTO.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        // 支持多次制作: 如果已有 meta.json 文件，说明不是第一次制作,则在 meta 基础上进行修改,追加新配置参数;否则执行生成
        if (FileUtil.exist(metaOutputPath)) {
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);  // 读取文件为String,再转化为对象
            // 追加配置参数
            // fileConfig
            List<Meta.FileConfigDTO.FileDTO> oldFiles = oldMeta.getFileConfig().getFiles();
            oldFiles.add(fileDTO);
            // modelConfig
            List<Meta.ModelConfigDTO.ModelDTO> oldModels = oldMeta.getModelConfig().getModels();
            oldModels.add(modelDTO);

            // 配置去重
            oldMeta.getFileConfig().setFiles(distinctFiles(oldFiles));
            oldMeta.getModelConfig().setModels(distinctModels(oldModels));

            // 2.更新元信息文件
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(oldMeta), metaOutputPath);
        } else {
            // fileConfig
            Meta.FileConfigDTO fileConfigDTO = new Meta.FileConfigDTO();
            fileConfigDTO.setSourceRootPath(sourceRootPath);
            List<Meta.FileConfigDTO.FileDTO> fileDTOList = new ArrayList<>();
            fileDTOList.add(fileDTO);
            fileConfigDTO.setFiles(fileDTOList);
            meta.setFileConfig(fileConfigDTO);

            // modelConfig
            Meta.ModelConfigDTO modelConfigDTO = new Meta.ModelConfigDTO();
            List<Meta.ModelConfigDTO.ModelDTO> modelDTOList = new ArrayList<>();
            modelDTOList.add(modelDTO);
            modelConfigDTO.setModels(modelDTOList);
            meta.setModelConfig(modelConfigDTO);

            // 2.输出元信息文件 meta.json
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(meta), metaOutputPath);  // 使用JSONUtil将对象转为String,再写文件
        }
        return id;
    }

    /**
     * 文件去重
     * @param fileDTOList fileDTO列表
     * @return 去重的 fileDTO 列表
     */
    public static List<Meta.FileConfigDTO.FileDTO> distinctFiles(List<Meta.FileConfigDTO.FileDTO> fileDTOList) {
        List<Meta.FileConfigDTO.FileDTO> newFileDTOList = new ArrayList<>(fileDTOList.stream()
                .collect(Collectors.toMap(Meta.FileConfigDTO.FileDTO::getInputPath, o -> o, (e, r) -> r)
                ).values());

        return newFileDTOList;
    }

    /**
     * 模型去重
     * @param modelDTOList modelDTO列表
     * @return 去重的 modelDTO 列表
     */
    public static List<Meta.ModelConfigDTO.ModelDTO> distinctModels(List<Meta.ModelConfigDTO.ModelDTO> modelDTOList) {
        List<Meta.ModelConfigDTO.ModelDTO> newModelDTOList = new ArrayList<>(modelDTOList.stream()
                .collect(Collectors.toMap(Meta.ModelConfigDTO.ModelDTO::getFieldName, o -> o, (e, r) -> r)
                ).values());

        return newModelDTOList;
    }

    /** 测试 */
    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir");
        String originProjectPath = new File(projectPath).getParent() + File.separator + "code-generator-demo-projects/acm-template";
        String fileInputPath = "src/com/code/acm/MainTemplate.java";

        Meta meta = new Meta();
        String name = "acm-template-generator";
        String description = "ACM 示例模板生成器";
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

        String searchStr = "MainTemplate";

        TemplateMaker.makeTemplate(meta, originProjectPath, fileInputPath, modelDTO, searchStr,1L);
    }
}

