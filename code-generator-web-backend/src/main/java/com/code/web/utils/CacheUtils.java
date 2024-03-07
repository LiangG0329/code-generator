package com.code.web.utils;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.json.JSONUtil;
import com.code.web.model.dto.generator.GeneratorQueryRequest;

/**
 * 缓存工具
 *
 * @author Liang
 * @create 2024/3/4
 */
public class CacheUtils {
    /**
     * 获取分页缓存key
     *
     * @param generatorQueryRequest 查询请求参数
     * @return key
     */
    public static String getPageCacheKey(GeneratorQueryRequest generatorQueryRequest) {
        String jsonStr = JSONUtil.toJsonStr(generatorQueryRequest);
        // 对请求参数字符串进行编码 base64编码
        String base64 = Base64Encoder.encode(jsonStr);
        // 规则  generator:page:请求参数
        String key = "generator:page:" + base64;
        return key;
    }

    /**
     * 获取缓存文件路径
     *
     * @param id
     * @param distPath
     * @return
     */
    public static String getCacheFilePath(long id, String distPath) {
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = String.format("%s/.temp/cache/%s", projectPath, id);
        String zipFilePath = tempDirPath + "/" + distPath;
        return zipFilePath;
    }
}
