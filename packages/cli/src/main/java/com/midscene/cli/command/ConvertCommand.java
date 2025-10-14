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
 * 转换测试脚本格式命令
 */
public class ConvertCommand extends AbstractCommand {
    
    public ConvertCommand(CliConfig config, Logger logger) {
        super("convert", "转换测试脚本格式", "[选项] <源文件路径> <目标文件路径>", config, logger);
    }
    
    @Override
    protected void initializeOptions() {
        super.initializeOptions();
        options.addOption(Option.builder("f")
                .longOpt("from")
                .hasArg()
                .argName("源格式")
                .desc("指定源文件格式(json/yaml/java/csv)")
                .build());
        
        options.addOption(Option.builder("t")
                .longOpt("to")
                .hasArg()
                .argName("目标格式")
                .desc("指定目标文件格式(json/yaml/java/csv)")
                .build());
        
        options.addOption(Option.builder("r")
                .longOpt("recursive")
                .desc("递归转换目录下的所有文件")
                .build());
        
        options.addOption(Option.builder("o")
                .longOpt("overwrite")
                .desc("覆盖已存在的目标文件")
                .build());
        
        options.addOption(Option.builder("p")
                .longOpt("platform")
                .hasArg()
                .argName("平台类型")
                .desc("指定目标平台(web/android/ios)")
                .build());
    }
    
    @Override
    protected int doExecute(CommandLine cmd, String[] args) {
        if (args.length < 2) {
            throw new CliException("请指定源文件路径和目标文件路径");
        }
        
        String sourcePath = args[0];
        String targetPath = args[1];
        
        boolean recursive = cmd.hasOption("r");
        boolean overwrite = cmd.hasOption("o");
        String fromFormat = cmd.getOptionValue("f", null);
        String toFormat = cmd.getOptionValue("t", null);
        String platform = cmd.getOptionValue("p", null);
        
        File source = new File(sourcePath);
        
        if (!source.exists()) {
            throw new CliException("源文件或目录不存在: " + sourcePath);
        }
        
        try {
            if (source.isDirectory()) {
                if (!recursive) {
                    throw new CliException("源是目录，请使用 --recursive 选项递归转换");
                }
                
                // 创建目标目录
                File targetDir = new File(targetPath);
                if (!targetDir.exists()) {
                    Files.createDirectories(targetDir.toPath());
                } else if (!targetDir.isDirectory()) {
                    throw new CliException("目标不是一个目录: " + targetPath);
                }
                
                // 递归转换目录
                int convertedCount = convertDirectory(source, targetDir, fromFormat, toFormat, platform, overwrite);
                logger.info("成功转换 " + convertedCount + " 个文件");
            } else {
                // 转换单个文件
                convertFile(source, new File(targetPath), fromFormat, toFormat, platform, overwrite);
                logger.info("文件转换成功: " + targetPath);
            }
            
            return 0;
        } catch (IOException e) {
            throw new CliException("文件操作失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CliException("转换失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 转换单个文件
     */
    private void convertFile(File sourceFile, File targetFile, String fromFormat, 
                           String toFormat, String platform, boolean overwrite) throws IOException {
        // 检查目标文件是否存在
        if (targetFile.exists() && !overwrite) {
            throw new CliException("目标文件已存在: " + targetFile.getPath());
        }
        
        // 确定文件格式
        if (fromFormat == null) {
            fromFormat = getFormatFromExtension(sourceFile.getName());
        }
        
        if (toFormat == null) {
            toFormat = getFormatFromExtension(targetFile.getName());
        }
        
        validateFormat(fromFormat);
        validateFormat(toFormat);
        
        logger.debug("转换文件: " + sourceFile.getPath());
        logger.debug("从格式: " + fromFormat + " 到格式: " + toFormat);
        
        // 确保目标目录存在
        if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
            Files.createDirectories(targetFile.getParentFile().toPath());
        }
        
        // 这里将实现实际的文件转换逻辑
        // 目前作为示例，我们只是简单地复制文件内容
        byte[] content = Files.readAllBytes(sourceFile.toPath());
        Files.write(targetFile.toPath(), content);
        
        logger.debug("转换完成: " + targetFile.getPath());
    }
    
    /**
     * 递归转换目录
     */
    private int convertDirectory(File sourceDir, File targetDir, String fromFormat, 
                               String toFormat, String platform, boolean overwrite) throws IOException {
        int convertedCount = 0;
        
        File[] files = sourceDir.listFiles();
        if (files == null) {
            return 0;
        }
        
        for (File file : files) {
            String relativePath = getRelativePath(sourceDir, file);
            Path targetPath = Paths.get(targetDir.getAbsolutePath(), relativePath);
            
            if (file.isDirectory()) {
                // 递归处理子目录
                convertedCount += convertDirectory(file, targetPath.toFile(), 
                                                 fromFormat, toFormat, platform, overwrite);
            } else {
                // 检查文件是否需要转换
                String fileFormat = getFormatFromExtension(file.getName());
                if (fromFormat == null || fromFormat.equalsIgnoreCase(fileFormat)) {
                    // 确定目标文件路径和格式
                    String newExtension = "." + toFormat.toLowerCase();
                    String newFileName = file.getName().replaceAll("\\.[^.]+$", newExtension);
                    File targetFile = new File(targetPath.toString().replace(file.getName(), newFileName));
                    
                    convertFile(file, targetFile, fileFormat, toFormat, platform, overwrite);
                    convertedCount++;
                }
            }
        }
        
        return convertedCount;
    }
    
    /**
     * 获取相对路径
     */
    private String getRelativePath(File baseDir, File file) {
        return baseDir.toURI().relativize(file.toURI()).getPath();
    }
    
    /**
     * 从文件名获取格式
     */
    private String getFormatFromExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        throw new CliException("无法从文件名确定格式: " + fileName);
    }
    
    /**
     * 验证格式是否支持
     */
    private void validateFormat(String format) {
        if (!format.equals("json") && !format.equals("yaml") && !format.equals("yml") && 
            !format.equals("java") && !format.equals("csv")) {
            throw new CliException("不支持的格式: " + format);
        }
    }
}