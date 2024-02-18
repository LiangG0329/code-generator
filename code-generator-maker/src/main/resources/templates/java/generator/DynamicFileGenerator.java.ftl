package ${basePackage}.generator;

import cn.hutool.core.io.FileUtil;
import ${basePackage}.model.DataModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 动态文件生成
 * @author @{author}
 * @create 2024/2/13
 */
public class DynamicFileGenerator {
    /**
     * 生成文件
     * @param inputPath  模板文件输入路径
     * @param outputPath  生成文件输出路径
     * @param model  数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerate(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // 创建Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

        // 指定模板文件所在的路径
        File templateDir = new File(inputPath).getParentFile();
        System.out.println("templateDir = " + templateDir);
        configuration.setDirectoryForTemplateLoading(templateDir);

        // 设置模板文件使用的字符集,数字格式
        configuration.setDefaultEncoding("utf-8");
        configuration.setNumberFormat("0.######");

        // 创建模板对象，加载指定模板
        String templateName = new File(inputPath).getName();
        // 指定字符集,解决中文乱码问题
        Template template = configuration.getTemplate(templateName,"utf-8");

        // 文件不存在则创建文件和父目录
        if (!FileUtil.exist(outputPath)) {
            FileUtil.touch(outputPath);
        }

        // 指定生成的文件路径和名称
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(outputPath)), StandardCharsets.UTF_8));

        // 调用template的process,处理并生成文件
        template.process(model, out);

        // 关闭输出
        out.close();
    }
}
