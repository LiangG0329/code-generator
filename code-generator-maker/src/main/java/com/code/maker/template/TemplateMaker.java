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
import java.util.Arrays;
import java.util.List;
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
     * @param fileInputPathList 输入文件路径列表,支持输入多个路径
     * @param modelDTO 模型信息
     * @param searchStr 替换字段
     * @param id 工作空间 id
     * @return 生成的模板对应的工作空间id.若指定的id不存在或未指定,则自动生成id; 否则返回id等于指定id
     */
    public static long makeTemplate(Meta meta, String originProjectPath, List<String> fileInputPathList, Meta.ModelConfigDTO.ModelDTO modelDTO, String searchStr,Long id) {
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
        // sourceRootPath 工作空间内复制项目的路径
        String sourceRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString();
        // 注意 win 系统需要对路径进行转义
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");

        // 3.输入模型参数信息由 modelDTO 参数提供

        // 二.使用字符串替换，生成模板文件
        List<Meta.FileConfigDTO.FileDTO> fileDTOList = new ArrayList<>();
        // 支持输入多个路径,遍历文件路径列表,多次执行生成模板
        for (String fileInputPath: fileInputPathList) {
            String fileInputAbsolutePath = sourceRootPath + File.separator + fileInputPath;
            // 输入路径 fileInputAbsolutePath 为目录,递归遍历并获取目录下的所有文件列表,支持批量制作模板文件
            if (FileUtil.isDirectory(fileInputAbsolutePath)) {
                List<File> files = FileUtil.loopFiles(fileInputAbsolutePath);
                for (File file: files) {
                    Meta.FileConfigDTO.FileDTO fileDTO = makeFileTemplate(modelDTO, searchStr, sourceRootPath, file);
                    fileDTOList.add(fileDTO);
                }
            } else {
                // 输入路径是文件,当个文件制作模板
                Meta.FileConfigDTO.FileDTO fileDTO = makeFileTemplate(modelDTO, searchStr, sourceRootPath, new File(fileInputAbsolutePath));
                fileDTOList.add(fileDTO);
            }
        }


        // 三.生成配置文件 meta.json
        String metaOutputPath = sourceRootPath + File.separator + "meta.json";
        // 1.配置参数 fileConfig modelConfig

        // 支持多次制作: 如果已有 meta.json 文件，说明不是第一次制作,则在 meta 基础上进行修改,追加新配置参数;否则执行生成
        if (FileUtil.exist(metaOutputPath)) {
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);  // 读取文件为String,再转化为对象
            // 追加配置参数
            // fileConfig
            List<Meta.FileConfigDTO.FileDTO> oldFiles = oldMeta.getFileConfig().getFiles();
            oldFiles.addAll(fileDTOList);
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
            List<Meta.FileConfigDTO.FileDTO> newFileDTOList = new ArrayList<>();
            newFileDTOList.addAll(fileDTOList);
            fileConfigDTO.setFiles(newFileDTOList);
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
     * 制作单个模板文件
     * @param modelDTO  模型信息
     * @param searchStr  替换字段
     * @param sourceRootPath 工作空间内复制项目的路径
     * @param inputFile 需制作模板的文件对象
     * @return fileDTO配置文件信息对象
     */
    public static Meta.FileConfigDTO.FileDTO makeFileTemplate(Meta.ModelConfigDTO.ModelDTO modelDTO, String searchStr, String sourceRootPath, File inputFile) {
        // 一.输入信息
        // 制作模板的原始文件的绝对路径（用于制作模板）
        // 注意 win 系统需要对路径进行转义
        String fileInputAbsolutePath = inputFile.getAbsolutePath().replaceAll("\\\\", "/");
        String fileOutputAbsolutePath = fileInputAbsolutePath + ".ftl";

        // 文件输入输出相对路径（用于生成配置）
        String fileInputPath = fileInputAbsolutePath.replace(sourceRootPath + "/", "");
        String fileOutputPath = fileInputPath + ".ftl";

        // 3.输入模型参数信息由 modelDTO 参数提供

        // 二.使用字符串替换，生成模板文件
        String fileContent;
        // 支持多次制作: 如果已有 .ftl模板文件，说明不是第一次制作，则在已生成的模板基础上再次修改模板
        if (FileUtil.exist(fileOutputAbsolutePath)) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);  // 读取已生成的模板文件内容
        } else {
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);  // 读取原始文件作为模板原型
        }
        String replacement = String.format("${%s}", modelDTO.getFieldName());
        String newFileContent = StrUtil.replace(fileContent, searchStr, replacement);

        // 三.生成配置文件信息对象,并返回
        // fileDTO
        Meta.FileConfigDTO.FileDTO fileDTO = new Meta.FileConfigDTO.FileDTO();
        fileDTO.setInputPath(fileInputPath);
        fileDTO.setOutputPath(fileOutputPath);
        fileDTO.setType(FileTypeEnum.FILE.getValue());

        // 生成文件和原文件内容一致且没有同名.ftl文件，则当前文件未制作模板，则为static静态生成;若内容一致但存在同名.ftl模板文件,则此次生成未修改旧模板,类型未dynamic
        if (newFileContent.equals(fileContent)) {
            if (!FileUtil.exist(fileOutputAbsolutePath)) {
                // 静态生成,meta.json中 输出路径=输入路径(即输出文件不是.ftl文件),generateType=static
                fileDTO.setOutputPath(fileInputPath);
                fileDTO.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            } else {
                fileDTO.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
            }
        } else  {
            fileDTO.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
            // 生成 .ftl 模板文件
            FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
        }


        return fileDTO;
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
        String originProjectPath = new File(projectPath).getParent() + File.separator + "code-generator-demo-projects/springboot-init";
        String inputFilePath1 = "src/main/java/com/code/springbootinit/common";
        String inputFilePath2 = "src/main/java/com/code/springbootinit/controller";
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

        String searchStr = "BaseResponse";

        long id = TemplateMaker.makeTemplate(meta, originProjectPath, fileInputPathList, modelDTO, searchStr, 1760851793752944640L);
        System.out.println("id = " + id);
    }
}

