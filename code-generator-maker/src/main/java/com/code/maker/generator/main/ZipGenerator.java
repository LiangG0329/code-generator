package com.code.maker.generator.main;

/**
 * 制作zip压缩包
 *
 * @author Liang
 * @create 2024/2/29
 */
public class ZipGenerator extends GenerateTemplate{
    @Override
    protected String buildDist(String outputPath, String sourceCopyPath, String jarPath, String shellOutputPath) {
        String distPath = super.buildDist(outputPath, sourceCopyPath, jarPath, shellOutputPath);
        String zipPath = super.buildZip(distPath);
        System.out.println("生成zip压缩包 zipPath = " + zipPath);
        return zipPath;
    }
}
