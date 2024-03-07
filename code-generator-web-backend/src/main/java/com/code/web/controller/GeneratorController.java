package com.code.web.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.maker.generator.main.GenerateTemplate;
import com.code.maker.generator.main.ZipGenerator;
import com.code.maker.meta.Meta;
import com.code.maker.meta.MetaValidator;
import com.code.web.annotation.AuthCheck;
import com.code.web.common.BaseResponse;
import com.code.web.common.DeleteRequest;
import com.code.web.common.ErrorCode;
import com.code.web.common.ResultUtils;
import com.code.web.constant.UserConstant;
import com.code.web.exception.BusinessException;
import com.code.web.exception.ThrowUtils;
import com.code.web.manager.CacheManager;
import com.code.web.manager.CosManager;
import com.code.web.model.dto.generator.*;
import com.code.web.model.entity.Generator;
import com.code.web.model.entity.User;
import com.code.web.model.vo.GeneratorVO;
import com.code.web.service.GeneratorService;
import com.code.web.service.UserService;
import com.code.web.utils.CacheUtils;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.code.web.utils.CacheUtils.getCacheFilePath;

/**
 * 生成器接口
 *
 * @author Liang
 * @from <a href="https://github.com/LiangG0329/code-generator">代码工坊</a>
 */
@RestController
@RequestMapping("/generator")
@Slf4j
public class GeneratorController {

    @Resource
    private GeneratorService generatorService;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private CacheManager cacheManager;

    // region 增删改查

    /**
     * 创建
     *
     * @param generatorAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addGenerator(@RequestBody GeneratorAddRequest generatorAddRequest, HttpServletRequest request) {
        if (generatorAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorAddRequest, generator);
        List<String> tags = generatorAddRequest.getTags();
        generator.setTags(JSONUtil.toJsonStr(tags));
        Meta.FileConfigDTO fileConfig = generatorAddRequest.getFileConfig();
        generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        Meta.ModelConfigDTO modelConfig = generatorAddRequest.getModelConfig();
        generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));

        // 参数校验
        generatorService.validGenerator(generator, true);
        User loginUser = userService.getLoginUser(request);
        generator.setUserId(loginUser.getId());
        generator.setAuthor(loginUser.getUserName());
        generator.setStatus(0);
        generator.setDownloadCount(0L);
        boolean result = generatorService.save(generator);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newGeneratorId = generator.getId();
        return ResultUtils.success(newGeneratorId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteGenerator(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldGenerator.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = generatorService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param generatorUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateGenerator(@RequestBody GeneratorUpdateRequest generatorUpdateRequest) {
        if (generatorUpdateRequest == null || generatorUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorUpdateRequest, generator);
        List<String> tags = generatorUpdateRequest.getTags();
        generator.setTags(JSONUtil.toJsonStr(tags));
        Meta.FileConfigDTO fileConfig = generatorUpdateRequest.getFileConfig();
        generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        Meta.ModelConfigDTO modelConfig = generatorUpdateRequest.getModelConfig();
        generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));

        // 参数校验
        generatorService.validGenerator(generator, false);
        long id = generatorUpdateRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = generatorService.updateById(generator);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<GeneratorVO> getGeneratorVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(generatorService.getGeneratorVO(generator, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param generatorQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Generator>> listGeneratorByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                 HttpServletRequest request) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }

    /**
     * 快速分页获取列表（封装类）
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo/fast")
    public BaseResponse<Page<GeneratorVO>> listGeneratorVOByPageFast(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                     HttpServletRequest request) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 优先从缓存获取数据
        String pageCacheKey = CacheUtils.getPageCacheKey(generatorQueryRequest);
        Object cacheValue = cacheManager.get(pageCacheKey);
        if (cacheValue != null) {
            return ResultUtils.success((Page<GeneratorVO>) cacheValue);
        }

        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 数据库查询
        QueryWrapper<Generator> queryWrapper = generatorService.getQueryWrapper(generatorQueryRequest);
        queryWrapper.select("id",
                "name",
                "description",
                "tags",
                "picture",
                "status",
                "downloadCount",
                "userId",
                "createTime",
                "updateTime"
        );
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size), queryWrapper);
        Page<GeneratorVO> generatorVOPage = generatorService.getGeneratorVOPage(generatorPage, request);

        // 精简字段
        generatorVOPage.getRecords().forEach(generatorVO -> {
            generatorVO.setFileConfig(null);
            generatorVO.setModelConfig(null);
        });

        // 写入缓存
        cacheManager.put(pageCacheKey, generatorVOPage);

        return ResultUtils.success(generatorVOPage);
    }

    /**
     * 分页获取当前用户创建的生成器资源列表
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listMyGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                   HttpServletRequest request) {
        if (generatorQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        generatorQueryRequest.setUserId(loginUser.getId());
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }

    // endregion


    /**
     * 编辑（生成器）
     *
     * @param generatorEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editGenerator(@RequestBody GeneratorEditRequest generatorEditRequest, HttpServletRequest request) {
        if (generatorEditRequest == null || generatorEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        long id = generatorEditRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldGenerator.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorEditRequest, generator);
        List<String> tags = generatorEditRequest.getTags();
        generator.setTags(JSONUtil.toJsonStr(tags));
        Meta.FileConfigDTO fileConfig = generatorEditRequest.getFileConfig();
        generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        Meta.ModelConfigDTO modelConfig = generatorEditRequest.getModelConfig();
        generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));

        // 参数校验
        generatorService.validGenerator(generator, false);

        boolean result = generatorService.updateById(generator);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 下载生成器
     *
     * @param id
     * @param request
     * @param response
     * @throws IOException
     */
    @GetMapping("/download")
    public void downloadGeneratorById(long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        String distPath = generator.getDistPath();
        if (StrUtil.isBlank(distPath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "产物包不存在");
        }

        // 日志记录,追踪事件
        log.info("userId = {} 下载了 {}", loginUser.getId(), distPath);

        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(distPath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + distPath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
            // 增加下载次数 更新到数据库
            generator.setDownloadCount(generator.getDownloadCount() + 1);
            generatorService.updateById(generator);

        } catch (Exception e) {
            log.error("file download error, filepath = " + distPath, e);
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }

    /**
     * 使用生成器
     *
     * @param generatorUseRequest
     * @param request
     * @param response
     * @throws IOException
     */
    @PostMapping("/use")
    public void useGenerator(@RequestBody GeneratorUseRequest generatorUseRequest,
                             HttpServletRequest request,
                             HttpServletResponse response) throws IOException {

        // 获取用户输入请求参数
        Long id = generatorUseRequest.getId();
        Map<String, Object> dataModel = generatorUseRequest.getDataModel();

        // 需要登录
        User loginUser = userService.getLoginUser(request);
        log.info("userId = {} 在线使用生成器 id={}", loginUser.getId(), id);

        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "生成器不存在");
        }

        // 生成器存储路径
        String distPath = generator.getDistPath();
        if (StrUtil.isBlank(distPath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "产物包不存在");
        }

        // 从对象存储下载生成器的压缩包
        String projectPath = System.getProperty("user.dir");
        // 定义独立的工作空间
        // 必须要用 userId 区分，否则可能会导致输入参数文件冲突
        String tempDirPath = String.format("%s/.temp/use/%s/%s", projectPath, id, loginUser.getId());
        String zipFilePath = tempDirPath + "/dist.zip";

        // 目录不存在则创建
        if (!FileUtil.exist(tempDirPath)) {
            FileUtil.mkdir(tempDirPath);
        }

        // 使用文件缓存，优先查找服务器本地缓存
        String cacheFilePath = CacheUtils.getCacheFilePath(id, distPath);
        Path cacheFilePathObj = Paths.get(cacheFilePath);
        Path zipFilePathObj = Paths.get(zipFilePath);

        if (!FileUtil.exist(zipFilePath)) {
            // 有缓存，复制文件
            if (FileUtil.exist(cacheFilePath)) {
                Files.copy(cacheFilePathObj, zipFilePathObj);
            } else {
                // 没有缓存，从对象存储下载文件
                FileUtil.touch(zipFilePath);
                try {
                    cosManager.download(distPath, zipFilePath);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成器下载失败");
                }
                // 写文件缓存
                File parentFile = cacheFilePathObj.toFile().getParentFile();
                if (!FileUtil.exist(parentFile)) {
                    FileUtil.mkdir(parentFile);
                }
                Files.copy(zipFilePathObj, cacheFilePathObj);
            }
        }

        // 解压压缩包，得到生成器产物包文件夹
        File unzipDistDir = ZipUtil.unzip(zipFilePath);

        // 将用户输入的参数写到 json 文件中
        String dataModelFilePath = tempDirPath + "/dataModel.json";
        String jsonStr = JSONUtil.toJsonStr(dataModel);
        FileUtil.writeUtf8String(jsonStr, dataModelFilePath);

        // 执行脚本
        // 遍历找到脚本文件 generator/generator.bat 所在路径
        // 要注意，如果不是 windows 系统，找 generator 文件而不是 bat
        String osName = System.getProperty("os.name");
        File scriptFile;
        if (osName.startsWith("Windows")) {
            scriptFile = FileUtil.loopFiles(unzipDistDir, 2, null)
                    .stream()
                    .filter(file -> file.isFile() && "generator.bat".equals(file.getName()))
                    .findFirst()
                    .orElseThrow(RuntimeException::new);
        } else {
            scriptFile = FileUtil.loopFiles(unzipDistDir, 2, null)
                    .stream()
                    .filter(file -> file.isFile() && "generator".equals(file.getName()))
                    .findFirst()
                    .orElseThrow(RuntimeException::new);
        }


        // 添加可执行权限
        if (!osName.startsWith("Windows")) {
            try {
                Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
                Files.setPosixFilePermissions(scriptFile.toPath(), permissions);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // 构造命令
        // 脚本文件所在文件夹
        File scriptDir = scriptFile.getParentFile();
        // 脚本文件绝对路径
        String scriptAbsolutePath = scriptFile.getAbsolutePath();
        if (osName.startsWith("Windows")) {
            scriptAbsolutePath = scriptAbsolutePath.replace("\\", "/");
        }
        String[] commands = new String[]{scriptAbsolutePath, "json-generate", "--file=" + dataModelFilePath};

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(scriptDir);

        // 执行命令
        try {
            Process process = processBuilder.start();

            // 创建一个新线程来读取标准输出
            new Thread(() -> {
                try (BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = stderr.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // 创建一个新线程来读取错误输出
            new Thread(() -> {
                try (BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = stdout.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // 等待命令输出完成
            int exitCode = process.waitFor();
            System.out.println("运行脚本命令执行结束，退出码：" + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "执行生成器脚本错误");
        }

        // 压缩得到的生成结果,并返回给前端
        String generatedPath = scriptDir.getAbsolutePath() + "/generated";
        String resultPath = tempDirPath + "/result.zip";
        File resultZipFile = ZipUtil.zip(generatedPath, resultPath);

        // 设置响应头
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + resultZipFile.getName());
        Files.copy(resultZipFile.toPath(), response.getOutputStream());

        // 异步清理文件,防止占用服务器空间
        CompletableFuture.runAsync(() -> {
            FileUtil.del(tempDirPath);
        });
    }

    /**
     * 制作生成器
     *
     * @param generatorMakeRequest
     * @param request
     * @param response
     * @throws IOException
     */
    @PostMapping("/make")
    public void makeGenerator(@RequestBody GeneratorMakeRequest generatorMakeRequest,
                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        // 输入参数
        Meta meta = generatorMakeRequest.getMeta();
        String zipFilePath = generatorMakeRequest.getZipFilePath();
        if (StrUtil.isBlank(zipFilePath)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模板文件不能为空");
        }

        // 需要用户登录
        User loginUser = userService.getLoginUser(request);
        // 日志记录
        log.info("userId = {} 在线制作生成器", loginUser.getId());

        // 创建工作空间，下载压缩包到本地/服务器
        String projectPath = System.getProperty("user.dir");
        // 随机id
        String id = IdUtil.getSnowflakeNextId() + RandomUtil.randomString(6);
        String tempDirPath = String.format("%s/.temp/make/%s", projectPath, id);
        String localZipFilePath = tempDirPath + "/project.zip";

        if (!FileUtil.exist(localZipFilePath)) {
            FileUtil.touch(localZipFilePath);
        }

        // 下载文件
        try {
            cosManager.download(zipFilePath, localZipFilePath);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "模版文件压缩包下载失败");
        }

        // 解压，得到项目模板文件
        File unzipDistDir = ZipUtil.unzip(localZipFilePath);

        // 构造 meta 对象和生成器的输出路径
        String sourceRootPath = unzipDistDir.getAbsolutePath();
        meta.getFileConfig().setSourceRootPath(sourceRootPath);
        // 校验和默认值处理
        MetaValidator.doValidAndFill(meta);
        String outputPath = tempDirPath + "/generated/" + meta.getName();

        // 调用 maker工具方法制作生成器
        GenerateTemplate generateTemplate = new ZipGenerator();
        try {
            generateTemplate.doGenerate(meta, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成器制作失败");
        }

        // 下载制作好的生成器压缩包
        String suffix = "-dist.zip";
        String zipFileName = meta.getName() + suffix;
        // 生成器压缩包的绝对路径
        String distZipFilePath = outputPath + suffix;
        // 下载文件
        // 设置响应头
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + zipFileName);
        // 写入响应
        Files.copy(Paths.get(distZipFilePath), response.getOutputStream());

        // 清理工作空间的文件
        CompletableFuture.runAsync(() -> {
            FileUtil.del(tempDirPath);
        });
    }

    /**
     * 缓存代码生成器
     *
     * @param generatorCacheRequest
     * @return
     */
    @PostMapping("/cache")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void cacheGenerator(@RequestBody GeneratorCacheRequest generatorCacheRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (generatorCacheRequest == null || generatorCacheRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long id = generatorCacheRequest.getId();
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        String distPath = generator.getDistPath();
        if (StrUtil.isBlank(distPath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "产物包不存在");
        }

        String zipFilePath = getCacheFilePath(id, distPath);

        try {
            cosManager.download(distPath, zipFilePath);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成器下载失败");
        }
    }

    /**
     * 下载模板制作工具
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @GetMapping("/download/template")
    public void downloadTemplateTool(HttpServletRequest request, HttpServletResponse response) throws IOException {

        User loginUser = userService.getLoginUser(request);
        log.info("userId = {} 下载了模板制作工具", loginUser.getId());;

        String toolKey = "/template_tool/templateTool.zip";

        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(toolKey);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + toolKey);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();

        } catch (Exception e) {
            log.error("tool download error ", e);
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "模板工具下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }
}
