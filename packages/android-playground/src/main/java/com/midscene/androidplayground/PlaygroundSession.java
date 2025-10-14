package com.midscene.androidplayground;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 播放场会话，表示与单个设备的交互会话
 */
public class PlaygroundSession {
    private static final Logger logger = LoggerFactory.getLogger(PlaygroundSession.class);
    
    // 会话ID
    private final String sessionId;
    // 关联的设备
    private final AndroidDevice device;
    // 会话创建时间
    private final LocalDateTime createdAt;
    // 会话是否已关闭
    private final AtomicBoolean closed;
    // 最后活动时间
    private LocalDateTime lastActivityTime;
    
    /**
     * 创建新的播放场会话
     * @param sessionId 会话ID
     * @param device 设备对象
     */
    public PlaygroundSession(String sessionId, AndroidDevice device) {
        this.sessionId = sessionId;
        this.device = device;
        this.createdAt = LocalDateTime.now();
        this.lastActivityTime = LocalDateTime.now();
        this.closed = new AtomicBoolean(false);
        logger.debug("Created new PlaygroundSession: {} for device: {}", sessionId, device.getDeviceId());
    }
    
    /**
     * 发送命令到设备
     * @param command 要发送的命令
     * @return 命令执行结果
     * @throws PlaygroundException 当命令执行失败时抛出
     */
    public CommandResult executeCommand(String command) throws PlaygroundException {
        ensureSessionOpen();
        updateLastActivityTime();
        
        logger.debug("Executing command on device {}: {}", device.getDeviceId(), command);
        
        try {
            // 实际项目中，这里应该通过ADB或其他方式向设备发送命令
            // 这里只是模拟实现
            
            // 模拟命令执行
            Thread.sleep(100); // 模拟执行时间
            
            // 模拟命令结果
            CommandResult result = new CommandResult(command, 0, "Command executed successfully", "");
            logger.debug("Command executed successfully on device {}", device.getDeviceId());
            
            return result;
        } catch (Exception e) {
            logger.error("Failed to execute command on device {}: {}", device.getDeviceId(), command, e);
            throw new PlaygroundException("Failed to execute command: " + e.getMessage(), e);
        }
    }
    
    /**
     * 安装应用到设备
     * @param apkPath APK文件路径
     * @return 安装结果
     * @throws PlaygroundException 当安装失败时抛出
     */
    public InstallResult installApp(String apkPath) throws PlaygroundException {
        ensureSessionOpen();
        updateLastActivityTime();
        
        logger.info("Installing app on device {}: {}", device.getDeviceId(), apkPath);
        
        try {
            // 实际项目中，这里应该通过ADB命令安装应用
            // 这里只是模拟实现
            
            // 模拟安装过程
            Thread.sleep(2000); // 模拟安装时间
            
            // 模拟安装结果
            InstallResult result = new InstallResult(apkPath, true, "Successfully installed");
            logger.info("App installed successfully on device {}", device.getDeviceId());
            
            return result;
        } catch (Exception e) {
            logger.error("Failed to install app on device {}: {}", device.getDeviceId(), apkPath, e);
            throw new PlaygroundException("Failed to install app: " + e.getMessage(), e);
        }
    }
    
    /**
     * 启动应用
     * @param packageName 应用包名
     * @return 启动结果
     * @throws PlaygroundException 当启动失败时抛出
     */
    public boolean launchApp(String packageName) throws PlaygroundException {
        ensureSessionOpen();
        updateLastActivityTime();
        
        logger.info("Launching app on device {}: {}", device.getDeviceId(), packageName);
        
        try {
            // 实际项目中，这里应该通过ADB命令启动应用
            // 这里只是模拟实现
            
            // 模拟启动
            Thread.sleep(1000); // 模拟启动时间
            
            logger.info("App launched successfully on device {}", device.getDeviceId());
            return true;
        } catch (Exception e) {
            logger.error("Failed to launch app on device {}: {}", device.getDeviceId(), packageName, e);
            throw new PlaygroundException("Failed to launch app: " + e.getMessage(), e);
        }
    }
    
    /**
     * 关闭会话
     */
    public void close() {
        if (closed.compareAndSet(false, true)) {
            logger.info("Closing session: {} for device: {}", sessionId, device.getDeviceId());
            
            // 清理资源
            // 实际项目中，这里应该执行额外的清理工作
            
            logger.info("Session closed: {}", sessionId);
        }
    }
    
    /**
     * 确保会话是打开的
     * @throws IllegalStateException 当会话已关闭时抛出
     */
    private void ensureSessionOpen() {
        if (closed.get()) {
            throw new IllegalStateException("Session is closed: " + sessionId);
        }
    }
    
    /**
     * 更新最后活动时间
     */
    private void updateLastActivityTime() {
        this.lastActivityTime = LocalDateTime.now();
    }
    
    /**
     * 获取会话ID
     * @return 会话ID
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * 获取关联的设备
     * @return 设备对象
     */
    public AndroidDevice getDevice() {
        return device;
    }
    
    /**
     * 获取会话创建时间
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * 获取最后活动时间
     * @return 最后活动时间
     */
    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }
    
    /**
     * 检查会话是否已关闭
     * @return 如果会话已关闭，返回 true
     */
    public boolean isClosed() {
        return closed.get();
    }
    
    /**
     * 命令执行结果类
     */
    public static class CommandResult {
        private final String command;
        private final int exitCode;
        private final String stdout;
        private final String stderr;
        
        public CommandResult(String command, int exitCode, String stdout, String stderr) {
            this.command = command;
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }
        
        public String getCommand() {
            return command;
        }
        
        public int getExitCode() {
            return exitCode;
        }
        
        public String getStdout() {
            return stdout;
        }
        
        public String getStderr() {
            return stderr;
        }
        
        public boolean isSuccess() {
            return exitCode == 0;
        }
        
        @Override
        public String toString() {
            return "CommandResult{command='" + command + "', exitCode=" + exitCode + ", success=" + isSuccess() + "}";
        }
    }
    
    /**
     * 应用安装结果类
     */
    public static class InstallResult {
        private final String apkPath;
        private final boolean success;
        private final String message;
        
        public InstallResult(String apkPath, boolean success, String message) {
            this.apkPath = apkPath;
            this.success = success;
            this.message = message;
        }
        
        public String getApkPath() {
            return apkPath;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return "InstallResult{apkPath='" + apkPath + "', success=" + success + "}";
        }
    }
}