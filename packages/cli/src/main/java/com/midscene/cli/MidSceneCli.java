package com.midscene.cli;

import com.midscene.cli.command.*;
import com.midscene.cli.config.CliConfig;
import com.midscene.cli.exception.CliException;
import com.midscene.cli.util.Logger;
import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.Map;

/**
 * MidScene CLI 主入口类
 * 提供命令行接口，支持各种操作命令
 */
public class MidSceneCli {
    private static final String VERSION = "1.0.0";
    private static final String APP_NAME = "midscene";
    
    private final CommandLineParser parser = new DefaultParser();
    private final HelpFormatter helpFormatter = new HelpFormatter();
    private final Map<String, Command> commands = new HashMap<>();
    private final CliConfig config;
    private final Logger logger;
    
    /**
     * 构造函数
     */
    public MidSceneCli() {
        this.config = CliConfig.load();
        this.logger = new Logger(config.isVerbose());
        initializeCommands();
    }
    
    /**
     * 初始化所有支持的命令
     */
    private void initializeCommands() {
        commands.put("run", new RunCommand(config, logger));
        commands.put("record", new RecordCommand(config, logger));
        commands.put("evaluate", new EvaluateCommand(config, logger));
        commands.put("config", new ConfigCommand(config, logger));
        commands.put("version", new VersionCommand(VERSION));
        commands.put("help", new HelpCommand(this));
        commands.put("init", new InitCommand(config, logger));
        commands.put("convert", new ConvertCommand(config, logger));
    }
    
    /**
     * 主入口方法
     */
    public int execute(String[] args) {
        try {
            if (args.length == 0) {
                printHelp();
                return 1;
            }
            
            // 解析全局选项
            Options globalOptions = createGlobalOptions();
            CommandLine cmd = parser.parse(globalOptions, args, true);
            
            // 处理全局选项
            if (cmd.hasOption("v")) {
                config.setVerbose(true);
                logger.setVerbose(true);
            }
            
            if (cmd.hasOption("h")) {
                printHelp();
                return 0;
            }
            
            if (cmd.hasOption("V")) {
                System.out.println(APP_NAME + " version " + VERSION);
                return 0;
            }
            
            // 获取子命令和其参数
            String[] remainingArgs = cmd.getArgs();
            if (remainingArgs.length == 0) {
                printHelp();
                return 1;
            }
            
            String commandName = remainingArgs[0].toLowerCase();
            String[] commandArgs = new String[remainingArgs.length - 1];
            System.arraycopy(remainingArgs, 1, commandArgs, 0, remainingArgs.length - 1);
            
            // 执行对应的命令
            Command command = commands.get(commandName);
            if (command != null) {
                return command.execute(commandArgs);
            } else {
                logger.error("未知命令: " + commandName);
                printHelp();
                return 1;
            }
            
        } catch (ParseException e) {
            logger.error("命令行解析错误: " + e.getMessage());
            printHelp();
            return 1;
        } catch (CliException e) {
            logger.error("执行错误: " + e.getMessage());
            if (e.getCause() != null && config.isVerbose()) {
                logger.debug("详细错误信息:");
                e.getCause().printStackTrace();
            }
            return 1;
        } catch (Exception e) {
            logger.error("发生未预期的错误: " + e.getMessage());
            if (config.isVerbose()) {
                e.printStackTrace();
            }
            return 2;
        }
    }
    
    /**
     * 创建全局选项
     */
    private Options createGlobalOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "显示帮助信息");
        options.addOption("v", "verbose", false, "启用详细日志");
        options.addOption("V", "version", false, "显示版本信息");
        return options;
    }
    
    /**
     * 打印帮助信息
     */
    public void printHelp() {
        System.out.println("MidScene CLI - AI驱动的自动化测试工具");
        System.out.println("版本: " + VERSION);
        System.out.println();
        System.out.println("使用方法: midscene [选项] <命令> [命令参数]");
        System.out.println();
        System.out.println("全局选项:");
        System.out.println("  -h, --help      显示帮助信息");
        System.out.println("  -v, --verbose   启用详细日志输出");
        System.out.println("  -V, --version   显示版本信息");
        System.out.println();
        System.out.println("可用命令:");
        System.out.println("  run          执行自动化测试任务");
        System.out.println("  record       录制用户操作流程");
        System.out.println("  evaluate     评估测试结果和性能");
        System.out.println("  config       配置CLI设置");
        System.out.println("  init         初始化项目配置");
        System.out.println("  convert      转换测试脚本格式");
        System.out.println("  help         显示命令帮助信息");
        System.out.println("  version      显示版本信息");
        System.out.println();
        System.out.println("使用 'midscene help <命令>' 查看特定命令的详细帮助");
    }
    
    /**
     * 获取命令帮助信息
     */
    public String getCommandHelp(String commandName) {
        Command command = commands.get(commandName);
        if (command != null) {
            return command.getHelp();
        }
        return "未知命令: " + commandName;
    }
    
    /**
     * 主方法
     */
    public static void main(String[] args) {
        MidSceneCli cli = new MidSceneCli();
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }
}