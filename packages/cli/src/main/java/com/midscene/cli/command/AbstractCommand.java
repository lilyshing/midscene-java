package com.midscene.cli.command;

import com.midscene.cli.config.CliConfig;
import com.midscene.cli.exception.CliException;
import com.midscene.cli.util.Logger;
import org.apache.commons.cli.*;

/**
 * 抽象命令类
 * 提供命令执行的通用功能
 */
public abstract class AbstractCommand implements Command {
    protected final Options options = new Options();
    protected final CommandLineParser parser = new DefaultParser();
    protected final HelpFormatter helpFormatter = new HelpFormatter();
    protected final CliConfig config;
    protected final Logger logger;
    protected final String name;
    protected final String description;
    protected final String usage;
    
    /**
     * 构造函数
     */
    public AbstractCommand(String name, String description, String usage, CliConfig config, Logger logger) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.config = config;
        this.logger = logger;
        initializeOptions();
    }
    
    /**
     * 初始化命令选项
     */
    protected void initializeOptions() {
        options.addOption("h", "help", false, "显示帮助信息");
    }
    
    @Override
    public int execute(String[] args) {
        try {
            CommandLine cmd = parser.parse(options, args);
            
            if (cmd.hasOption("h")) {
                System.out.println(getHelp());
                return 0;
            }
            
            return doExecute(cmd, cmd.getArgs());
        } catch (ParseException e) {
            logger.error("命令参数解析错误: " + e.getMessage());
            System.out.println(getHelp());
            return 1;
        } catch (CliException e) {
            throw e;
        } catch (Exception e) {
            throw new CliException("执行命令失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 实际执行命令的逻辑
     * 由具体命令类实现
     */
    protected abstract int doExecute(CommandLine cmd, String[] args);
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getHelp() {
        StringBuilder help = new StringBuilder();
        help.append("命令: " + name + " - " + description + "\n\n");
        help.append("使用方法: midscene " + name + " " + usage + "\n\n");
        help.append("选项:\n");
        
        // 构建选项描述
        for (Option option : options.getOptions()) {
            help.append(String.format("  -%s, --%s  %s\n", 
                option.getOpt(), 
                option.getLongOpt(), 
                option.getDescription()));
        }
        
        return help.toString();
    }
}