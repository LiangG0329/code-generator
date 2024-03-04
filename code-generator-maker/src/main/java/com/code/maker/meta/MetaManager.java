package com.code.maker.meta;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;

/**
 * 读取meta.json，生成 Meta 对象，使用双检锁单例模式，保证只有一个元信息对象，减少初始化开销
 * @author Liang
 * @create 2024/2/17
 */
public class MetaManager {

    private static volatile Meta meta;  // volatile关键字修饰，确保多线程环境下的内存可见性，对象一旦修改所有线程立即可见

    public static Meta getMeta() {
        if (meta == null) { // 已初始化，则直接返回(加锁开销较大)
            synchronized (MetaManager.class) {
                if (meta == null) { // 加锁后再次判空
                    meta = initMeta();
                }
            }
        }
        return meta;
    }

    private static Meta initMeta() {
//         String metaJson = ResourceUtil.readUtf8Str("meta.json");
//        String metaJson = ResourceUtil.readUtf8Str("springboot-init-meta.json");
        String metaJson = ResourceUtil.readUtf8Str("meta4.json");
        Meta metaBean = JSONUtil.toBean(metaJson, Meta.class);
        // 校验和处理默认值
        MetaValidator.doValidAndFill(metaBean);
        return metaBean;
    }

    /** 测试是否可以获得包含配置信息的meta对象 */
    public static void main(String[] args) {
        Meta meta = MetaManager.getMeta();
        System.out.println(meta);
    }
}
