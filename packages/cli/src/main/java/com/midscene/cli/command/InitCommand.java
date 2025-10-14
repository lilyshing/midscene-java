package com.midscene.cli.command;

import com.midscene.cli.config.CliConfig;
import com.midscene.cli.exception.CliException;
import com.midscene.cli.util.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 初始化项目配置命令
 */
public class InitCommand extends AbstractCommand {
    
    public InitCommand(CliConfig config, Logger logger) {
        super("init", "初始化项目配置", "[选项] [目录路径]", config, logger);
    }
    
    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        options.addOption(Option.builder("t")
                .longOpt("template")
                .hasArg()
                .argName("模板类型")
                .desc("指定项目模板(web/android/ios/all)")
                .build());
        
        options.addOption(Option.builder("n")
                .longOpt("name")
                .hasArg()
                .argName("项目名称")
                .desc("指定项目名称")
                .build());
        
        options.addOption(Option.builder("f")
                .longOpt("force")
                .desc("强制在非空目录初始化")
                .build());
    }
    
    @Override
    protected int doExecute(CommandLine cmd, String[] args) {
        // 确定初始化目录
        String directoryPath = args.length > 0 ? args[0] : ".";
        File directory = new File(directoryPath);
        
        // 验证目录
        if (!directory.exists()) {
            // 创建目录
            try {
                Files.createDirectories(directory.toPath());
                logger.info("已创建目录: " + directoryPath);
            } catch (IOException e) {
                throw new CliException("无法创建目录: " + e.getMessage(), e);
            }
        } else if (!directory.isDirectory()) {
            throw new CliException("指定的路径不是一个目录: " + directoryPath);
        }
        
        // 检查目录是否为空
        if (directory.list().length > 0 && !cmd.hasOption("f")) {
            throw new CliException("目录不为空，请使用 --force 选项强制初始化");
        }
        
        // 解析命令行选项
        String template = cmd.getOptionValue("t", "all");
        String projectName = cmd.getOptionValue("n", new File(directoryPath).getName());
        
        logger.info("开始初始化项目: " + projectName);
        logger.info("目录: " + directory.getAbsolutePath());
        logger.info("模板: " + template);
        
        try {
            // 创建项目结构
            createProjectStructure(directory, projectName, template);
            
            logger.info("项目初始化完成!");
            logger.info("\n接下来可以:");
            logger.info("1. 编辑配置文件: " + Paths.get(directoryPath, "midscene.config.json"));
            logger.info("2. 执行测试: midscene run tests/");
            logger.info("3. 录制操作: midscene record --platform web --url https://example.com recording.json");
            
            return 0;
        } catch (Exception e) {
            throw new CliException("项目初始化失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建项目结构
     */
    private void createProjectStructure(File baseDir, String projectName, String template) throws IOException {
        // 创建基本目录结构
        createDirectory(baseDir, "tests");
        createDirectory(baseDir, "recordings");
        createDirectory(baseDir, "reports");
        createDirectory(baseDir, "configs");
        
        // 创建示例配置文件
        createConfigFile(baseDir, projectName);
        
        // 根据模板创建示例文件
        if (template.equals("all") || template.equals("web")) {
            createWebExample(baseDir);
        }
        
        if (template.equals("all") || template.equals("android")) {
            createAndroidExample(baseDir);
        }
        
        if (template.equals("all") || template.equals("ios")) {
            createIosExample(baseDir);
        }
    }
    
    private void createDirectory(File baseDir, String name) throws IOException {
        Path dirPath = Paths.get(baseDir.getAbsolutePath(), name);
        Files.createDirectories(dirPath);
        logger.debug("创建目录: " + dirPath);
    }
    
    private void createConfigFile(File baseDir, String projectName) throws IOException {
        Path configPath = Paths.get(baseDir.getAbsolutePath(), "midscene.config.json");
        String configContent = "{
  \"projectName\": \"" + projectName + "\",
  \"defaultPlatform\": \"web\",
  \"defaultEnvironment\": \"dev\",
  \"environments\": {
    \"dev\": {
      \"web\": {
        \"baseUrl\": \"http://localhost:3000\"
      }
    },
    \"test\": {
      \"web\": {
        \"baseUrl\": \"https://test.example.com\"
      }
    },
    \"prod\": {
      \"web\": {
        \"baseUrl\": \"https://example.com\"
      }
    }
  },
  \"browser\": \"chrome\",
  \"timeout\": 30000,
  \"retryCount\": 1,
  \"reportFormat\": \"html\"
}";
        
        Files.write(configPath, configContent.getBytes());
        logger.debug("创建配置文件: " + configPath);
    }
    
    private void createWebExample(File baseDir) throws IOException {
        Path testPath = Paths.get(baseDir.getAbsolutePath(), "tests", "web_example.json");
        String testContent = "{
  \"name\": \"Web Example Test\",
  \"platform\": \"web\",
  \"browser\": \"chrome\",
  \"steps\": [
    {
      \"type\": \"navigate\",
      \"url\": \"https://example.com\"
    },
    {
      \"type\": \"wait\",
      \"seconds\": 2
    },
    {
      \"type\": \"assert\",
      \"condition\": \"titleContains\",
      \"value\": \"Example Domain\"
    }
  ]
}";
        
        Files.write(testPath, testContent.getBytes());
        logger.debug("创建Web示例: " + testPath);
    }
    
    private void createAndroidExample(File baseDir) throws IOException {
        Path testPath = Paths.get(baseDir.getAbsolutePath(), "tests", "android_example.json");
        String testContent = "{
  \"name\": \"Android Example Test\",
  \"platform\": \"android\",
  \"appPackage\": \"com.example.app\",
  \"steps\": [
    {
      \"type\": \"launchApp\"
    },
    {
      \"type\": \"waitForElement\",
      \"locator\": {\n        \"id\": \"com.example.app:id/login_button\"\n      }
    }
  ]
}";
        
        Files.write(testPath, testContent.getBytes());
        logger.debug("创建Android示例: " + testPath);
    }
    
    private void createIosExample(File baseDir) throws IOException {
        Path testPath = Paths.get(baseDir.getAbsolutePath(), "tests", "ios_example.json");
        String testContent = "{
  \"name\": \"iOS Example Test\",
  \"platform\": \"ios\",
  \"bundleId\": \"com.example.app\",
  \"steps\": [
    {
      \"type\": \"launchApp\"
    },
    {
      \"type\": \"waitForElement\",
      \"locator\": {\n        \"id\": \"loginButton\"\n      }
    }
  ]
}";
        
        Files.write(testPath, testContent.getBytes());
        logger.debug("创建iOS示例: " + testPath);
    }
}