package com.code.web.job;

import cn.hutool.core.util.StrUtil;
import com.code.web.manager.CosManager;
import com.code.web.mapper.GeneratorMapper;
import com.code.web.model.entity.Generator;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 清理对象存储的定时任务处理器
 *
 * @author Liang
 */
@Component
@Slf4j
public class ClearCosJobHandler {

    @Resource
    private CosManager cosManager;

    @Resource
    private GeneratorMapper generatorMapper;

    /**
     * 每天执行 文件清理
     *
     * @throws InterruptedException
     */
    @XxlJob("clearCosJobHandler")
    public void clearCosJonHandler() throws InterruptedException {
        log.info("clearCosJobHandler start");

        // 清理cos业务逻辑

        // 1. 清除用户上传的临时模板文件 (generator_make_template)
        cosManager.deleteDir("/generator_make_template/");

        // 2. 删除 isDelete=1的代码生成器对应的产物包文件（generator_dist）
        // 获取已删除的代码生成器对应的产物包文件
        List<Generator> generators = generatorMapper.listDeletedGenerator();
        List<String> keyList = generators.stream()
                .map(Generator::getDistPath)
                .filter(StrUtil::isNotBlank)
                // 移除 '/' 前缀
                .map(distPath -> distPath.substring(1))
                .collect(Collectors.toList());

        cosManager.deleteObjects(keyList);
        log.info("clearCosJobHandler end");
    }

}
