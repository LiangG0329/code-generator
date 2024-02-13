import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @create 2024/2/13
 */
public class FreeMakerTest {
    /**
     *  freeMaker模板测试
     */
    @Test
    public void test() throws IOException, TemplateException {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setNumberFormat("0.######");

        // 指定模板文件所在的路径
        configuration.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));

        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");

        // 创建模板对象，加载指定模板
        Template template = configuration.getTemplate("myweb.html.ftl");

        // 数据模型，填充模板
        Map<String, Object> dataModel = new HashMap<>();

        dataModel.put("currentYear", 2024);

        List<Map<String, Object>> menuItems = new ArrayList<>();
        Map<String, Object> menuItem1 = new HashMap<>();
        menuItem1.put("url", "https://github.com");
        menuItem1.put("label", "GitHub");
        Map<String, Object> menuItem2 = new HashMap<>();
        menuItem2.put("url", "https://github.com/LiangG0329");
        menuItem2.put("label", "myGitHub");

        menuItems.add(menuItem1);
        menuItems.add(menuItem2);

        dataModel.put("menuItems", menuItems);

        // 指定生成的文件路径和名称
        Writer out = new FileWriter("myweb.html");

        // 调用template的process,处理并生成文件
        template.process(dataModel, out);
        out.close();
    }
}
