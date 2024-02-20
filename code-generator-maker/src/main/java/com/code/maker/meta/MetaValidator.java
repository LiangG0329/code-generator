package com.code.maker.meta;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.code.maker.meta.Meta.FileConfigDTO.FileDTO;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.code.maker.meta.Meta.FileConfigDTO;
import com.code.maker.meta.Meta.ModelConfigDTO;


/**
 * @author Liang
 * @create 2024/2/19
 */
public class MetaValidator {
    public static void doValidAndFill(Meta meta) {
        // 基础信息校验和默认值
        validAndFillMetaBasic(meta);

        // fileConfig校验和默认值
        validAndFillFileConfig(meta);

        // modelConfig校验和默认值
        validAndFillModelConfig(meta);
    }

    /**
     * 基础信息校验和默认值设置
     * @param meta 元信息对象
     */
    private static void validAndFillMetaBasic(Meta meta) {
        // 校验配置文件信息，没有则设置默认值
        String name = StrUtil.blankToDefault(meta.getName(), "code-generator");
        String description = StrUtil.emptyToDefault(meta.getDescription(), "代码生成器");
        String author = StrUtil.emptyToDefault(meta.getAuthor(), "Liang");
        String basePackage = StrUtil.blankToDefault(meta.getBasePackage(), "com.code");
        String version = StrUtil.emptyToDefault(meta.getVersion(), "1.0");
        String createTime = StrUtil.emptyToDefault(meta.getCreateTime(), DateUtil.now());

        meta.setName(name);
        meta.setDescription(description);
        meta.setAuthor(author);
        meta.setBasePackage(basePackage);
        meta.setVersion(version);
        meta.setCreateTime(createTime);
    }

    /**
     * fileConfig校验和默认值设置
     * @param meta 元信息对象
     */
    private static void validAndFillFileConfig(Meta meta) {
        FileConfigDTO fileConfig = meta.getFileConfig();
        if (fileConfig == null) {
            return;
        }
        // sourceRootPath: 必填(模板文件位置)
        String sourceRootPath = fileConfig.getSourceRootPath();
        if (StrUtil.isBlank(sourceRootPath)) {
            throw new MetaException("未填写 sourceRootPath");
        }

        // inputRootPath: 默认值: .source + sourceRootPath 的最后一个层级路径
        String inputRootPath = fileConfig.getInputRootPath();
        if (StrUtil.isEmpty(inputRootPath)) {
            String defaultInputRootPath = ".source" + System.getProperty("file.separator")+ FileUtil.getLastPathEle(Paths.get(sourceRootPath)).getFileName().toString();
            fileConfig.setInputRootPath(defaultInputRootPath);
        }

        // outputRootPath：默认值: 当前路径下的 generated
        String outputRootPath = fileConfig.getOutputRootPath();
        if (StrUtil.isEmpty(outputRootPath)) {
            String defaultOutRootPath = "generated";
            fileConfig.setOutputRootPath(defaultOutRootPath);
        }

        String fileConfigType = fileConfig.getType();
        String defaultType = "dir";
        if (StrUtil.isEmpty(fileConfigType)) {
            fileConfig.setType(defaultType);
        }

        // fileInfo默认值
        List<FileDTO> files = fileConfig.getFiles();
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        for (FileDTO file : files) {
            // inputPath 必填
            String inputPath = file.getInputPath();
            if (StrUtil.isBlank(inputPath)) {
                throw new MetaException("未填写 inputPath");
            }
            // outputPath: 默认值 等于inputPath
            String outputPath = file.getOutputPath();
            if (StrUtil.isEmpty(outputPath)) {
                file.setOutputPath(inputPath);
            }
            // type: 默认 inputPath 有文件后缀（如 .java）为 file，否则为 dir
            String fileType = file.getType();
            if (StrUtil.isBlank(fileType)) {
                // 无文件后缀
                if (StrUtil.isBlank(FileUtil.getSuffix(inputPath))) {
                    file.setType("dir");
                } else {
                    file.setType("file");
                }
            }
            // generateType: 如果文件结尾不为 .ftl, generateType 默认为 static, 否则为 dynamic
            String generateType = file.getGenerateType();
            if (StrUtil.isBlank(generateType)) {
                // .ftl文件 动态模板
                if (inputPath.endsWith(".ftl")) {
                    file.setGenerateType("dynamic");
                } else {
                    file.setGenerateType("static");
                }
            }
        }
    }

    /**
     * modelConfig校验和默认值设置
     * @param meta 元信息对象
     */
    private static void validAndFillModelConfig(Meta meta) {
        ModelConfigDTO modelConfig = meta.getModelConfig();
        if (modelConfig == null) {
            return;
        }
        List<ModelConfigDTO.ModelDTO> models = modelConfig.getModels();
        if (CollectionUtil.isEmpty(models)) {
            return;
        }
        for (ModelConfigDTO.ModelDTO model : models) {
            // 输出路径默认值
            String fieldName = model.getFieldName();
            if (StrUtil.isBlank(fieldName)) {
                throw new MetaException("未填写 fieldName");
            }

            String modelInfoType = model.getType();
            if (StrUtil.isEmpty(modelInfoType)) {
                model.setType("String");
            }
        }
    }
}
