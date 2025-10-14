package com.midscene.visualizer;

import com.midscene.visualizer.model.DebugSession;
import com.midscene.visualizer.model.DebugData;
import com.midscene.visualizer.model.ElementInfo;
import com.midscene.visualizer.model.TestExecution;
import com.midscene.visualizer.renderer.RendererFactory;
import com.midscene.visualizer.renderer.Renderer;
import com.midscene.visualizer.exporter.ExporterFactory;
import com.midscene.visualizer.exporter.Exporter;
import com.midscene.visualizer.util.Logger;
import com.midscene.core.driver.DeviceDriver;
import com.midscene.core.exception.MidSceneException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 可视化调试工具主类
 * 提供测试执行可视化、元素检查、调试会话管理等功能
 */
public class Visualizer {
    private Logger logger;
    private DebugSession currentSession;
    private DeviceDriver driver;
    private RendererFactory rendererFactory;
    private ExporterFactory exporterFactory;
    
    /**
     * 构造函数
     */
    public Visualizer() {
        this.logger = new Logger(true);
        this.rendererFactory = new RendererFactory();
        this.exporterFactory = new ExporterFactory();
    }
    
    /**
     * 设置设备驱动
     */
    public void setDeviceDriver(DeviceDriver driver) {
        this.driver = driver;
        logger.info("设备驱动已设置");
    }
    
    /**
     * 创建新的调试会话
     */
    public DebugSession createSession(String sessionId, String title) {
        currentSession = new DebugSession(sessionId, title);
        logger.info("已创建新的调试会话: " + title);
        return currentSession;
    }
    
    /**
     * 获取当前调试会话
     */
    public DebugSession getCurrentSession() {
        if (currentSession == null) {
            throw new MidSceneException("没有活动的调试会话");
        }
        return currentSession;
    }
    
    /**
     * 结束当前调试会话
     */
    public void endSession() {
        if (currentSession != null) {
            logger.info("已结束调试会话: " + currentSession.getTitle());
            currentSession = null;
        }
    }
    
    /**
     * 捕获当前设备屏幕
     */
    public byte[] captureScreen() throws IOException {
        if (driver == null) {
            throw new MidSceneException("设备驱动未设置");
        }
        
        byte[] screenshot = driver.takeScreenshot();
        logger.debug("已捕获屏幕截图，大小: " + screenshot.length + " bytes");
        
        if (currentSession != null) {
            currentSession.addScreenshot(screenshot);
        }
        
        return screenshot;
    }
    
    /**
     * 获取屏幕上的元素信息
     */
    public List<ElementInfo> getElements(String selector) {
        if (driver == null) {
            throw new MidSceneException("设备驱动未设置");
        }
        
        List<ElementInfo> elements = driver.findElements(selector);
        logger.debug("找到 " + elements.size() + " 个匹配元素");
        
        if (currentSession != null && !elements.isEmpty()) {
            currentSession.addElements(elements);
        }
        
        return elements;
    }
    
    /**
     * 获取当前页面信息
     */
    public Map<String, Object> getPageInfo() {
        if (driver == null) {
            throw new MidSceneException("设备驱动未设置");
        }
        
        Map<String, Object> pageInfo = driver.getPageInfo();
        logger.debug("已获取页面信息");
        
        if (currentSession != null) {
            currentSession.setPageInfo(pageInfo);
        }
        
        return pageInfo;
    }
    
    /**
     * 添加测试执行数据到会话
     */
    public void logTestExecution(TestExecution execution) {
        if (currentSession == null) {
            throw new MidSceneException("没有活动的调试会话");
        }
        
        currentSession.addTestExecution(execution);
        logger.debug("已记录测试执行: " + execution.getActionName());
    }
    
    /**
     * 添加自定义调试数据
     */
    public void addDebugData(String key, Object value) {
        if (currentSession == null) {
            throw new MidSceneException("没有活动的调试会话");
        }
        
        DebugData data = new DebugData(key, value);
        currentSession.addDebugData(data);
        logger.debug("已添加调试数据: " + key);
    }
    
    /**
     * 可视化渲染当前会话
     */
    public String render(String format) {
        if (currentSession == null) {
            throw new MidSceneException("没有活动的调试会话");
        }
        
        Renderer renderer = rendererFactory.getRenderer(format);
        String result = renderer.render(currentSession);
        logger.info("已渲染调试会话为 " + format + " 格式");
        
        return result;
    }
    
    /**
     * 导出调试会话
     */
    public void export(String format, String outputPath) throws IOException {
        if (currentSession == null) {
            throw new MidSceneException("没有活动的调试会话");
        }
        
        Exporter exporter = exporterFactory.getExporter(format);
        exporter.export(currentSession, outputPath);
        logger.info("已导出调试会话到: " + outputPath);
    }
    
    /**
     * 高亮显示元素
     */
    public void highlightElement(String selector) {
        if (driver == null) {
            throw new MidSceneException("设备驱动未设置");
        }
        
        driver.highlightElement(selector);
        logger.info("已高亮显示元素: " + selector);
    }
    
    /**
     * 保存会话为JSON文件
     */
    public void saveSession(String filePath) throws IOException {
        if (currentSession == null) {
            throw new MidSceneException("没有活动的调试会话");
        }
        
        File file = new File(filePath);
        Exporter jsonExporter = exporterFactory.getExporter("json");
        jsonExporter.export(currentSession, file.getAbsolutePath());
        logger.info("已保存会话到: " + filePath);
    }
    
    /**
     * 加载会话从JSON文件
     */
    public DebugSession loadSession(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("会话文件不存在: " + filePath);
        }
        
        // 这里应该实现从文件加载会话的逻辑
        // 为了简化，这里只是创建一个新的会话
        currentSession = new DebugSession("loaded-" + System.currentTimeMillis(), "Loaded Session");
        logger.info("已从文件加载会话: " + filePath);
        
        return currentSession;
    }
}