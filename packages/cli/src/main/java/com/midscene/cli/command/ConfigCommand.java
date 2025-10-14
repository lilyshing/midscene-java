package com.midscene.cli.command;

import com.midscene.cli.config.CliConfig;
import com.midscene.cli.exception.CliException;
import com.midscene.cli.util.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.IOException;

/**
 * 配置CLI设置命令
 */
public class ConfigCommand extends AbstractCommand {
    
    public ConfigCommand(CliConfig config, Logger logger) {
        super("config", "配置CLI设置", "[选项] [key=value...]", config, logger);
    }
    
    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        options.addOption(Option.builder("l")
                .longOpt("list")
                .desc("列出所有配置项")
                .build());
        
        options.addOption(Option.builder("g")
                .longOpt("get")
                .hasArg()
                .argName("配置键")
                .desc("获取指定配置项的值")
                .build());
        
        options.addOption(Option.builder("s")
                .longOpt("set")
                .hasArg()
                .argName("key=value")
                .desc("设置配置项")
                .build());
        
        options.addOption(Option.builder("r")
                .longOpt("remove")
                .hasArg()
                .argName("配置键")
                .desc("删除配置项")
                .build());
        
        options.addOption(Option.builder("r")
                .longOpt("reset")
                .desc("重置所有配置为默认值")
                .build());
        
        options.addOption(Option.builder("p")
                .longOpt("path")
                .desc("显示配置文件路径")
                .build());
    }
    
    @Override
    protected int doExecute(CommandLine cmd, String[] args) {
        try {
            if (cmd.hasOption("l")) {
                // 列出所有配置
                System.out.println("当前配置:");
                for (String key : config.getPropertyNames()) {
                    System.out.printf("%s = %s\n", key, config.getProperty(key));
                }
                return 0;
            }
            
            if (cmd.hasOption("g")) {
                // 获取指定配置
                String key = cmd.getOptionValue("g");
                String value = config.getProperty(key);
                if (value != null) {
                    System.out.println(value);
                } else {
                    logger.warn("配置项不存在: " + key);
                    return 1;
                }
                return 0;
            }
            
            if (cmd.hasOption("s")) {
                // 设置配置项
                String keyValue = cmd.getOptionValue("s");
                int equalsIndex = keyValue.indexOf('=');
                if (equalsIndex > 0 && equalsIndex < keyValue.length() - 1) {
                    String key = keyValue.substring(0, equalsIndex).trim();
                    String value = keyValue.substring(equalsIndex + 1).trim();
                    config.setProperty(key, value);
                    config.save();
                    logger.info("已设置: " + key + " = " + value);
                    return 0;
                } else {
                    throw new CliException("无效的格式，使用: key=value");
                }
            }
            
            if (cmd.hasOption("r")) {
                // 删除配置项
                String key = cmd.getOptionValue("r");
                if (config.hasProperty(key)) {
                    config.removeProperty(key);
                    config.save();
                    logger.info("已删除配置项: " + key);
                    return 0;
                } else {
                    logger.warn("配置项不存在: " + key);
                    return 1;
                }
            }
            
            if (cmd.hasOption("reset")) {
                // 重置所有配置
                config.resetToDefaults();
                config.save();
                logger.info("已重置所有配置为默认值");
                return 0;
            }
            
            if (cmd.hasOption("p")) {
                // 显示配置文件路径
                System.out.println("配置文件路径: " + config.getConfigFilePath());
                return 0;
            }
            
            // 处理直接指定的key=value对
            if (args.length > 0) {
                for (String arg : args) {
                    int equalsIndex = arg.indexOf('=');
                    if (equalsIndex > 0 && equalsIndex < arg.length() - 1) {
                        String key = arg.substring(0, equalsIndex).trim();
                        String value = arg.substring(equalsIndex + 1).trim();
                        config.setProperty(key, value);
                        logger.info("已设置: " + key + " = " + value);
                    } else {
                        logger.warn("无效的格式，忽略: " + arg);
                    }
                }
                config.save();
                return 0;
            }
            
            // 默认列出所有配置
            System.out.println("当前配置:");
            for (String key : config.getPropertyNames()) {
                System.out.printf("%s = %s\n", key, config.getProperty(key));
            }
            System.out.println();
            System.out.println("使用 'midscene config help' 查看使用帮助");
            
            return 0;
        } catch (IOException e) {
            throw new CliException("保存配置失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CliException("配置操作失败: " + e.getMessage(), e);
        }
    }
}