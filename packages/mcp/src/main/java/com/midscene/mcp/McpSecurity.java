package com.midscene.mcp;

import com.midscene.core.exception.PlatformException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * MCP安全类
 * 处理MCP协议的安全相关功能，包括身份验证、加密和安全通信
 */
public class McpSecurity {
    private static final Logger logger = Logger.getLogger(McpSecurity.class.getName());
    
    // 加密相关常量
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    
    // 哈希算法
    private static final String HASH_ALGORITHM = "SHA-256";
    
    // 安全随机数生成器
    private final SecureRandom secureRandom;
    
    // 会话密钥管理
    private final Map<String, SessionSecurityInfo> sessionKeys = new ConcurrentHashMap<>();
    
    // API密钥管理
    private final Map<String, String> apiKeys = new ConcurrentHashMap<>();
    
    // 令牌有效期（毫秒）
    private long tokenExpirationMs = 3600000; // 默认1小时
    
    // 是否启用安全模式
    private boolean securityEnabled = true;
    
    // 是否需要身份验证
    private boolean authenticationRequired = true;
    
    /**
     * 构造函数
     */
    public McpSecurity() {
        this.secureRandom = new SecureRandom();
        logger.info("MCP security initialized");
    }
    
    /**
     * 设置安全模式
     * @param enabled 是否启用
     */
    public void setSecurityEnabled(boolean enabled) {
        this.securityEnabled = enabled;
        logger.info("MCP security mode set to: " + enabled);
    }
    
    /**
     * 设置是否需要身份验证
     * @param required 是否需要
     */
    public void setAuthenticationRequired(boolean required) {
        this.authenticationRequired = required;
        logger.info("MCP authentication requirement set to: " + required);
    }
    
    /**
     * 设置令牌有效期
     * @param milliseconds 有效期（毫秒）
     */
    public void setTokenExpiration(long milliseconds) {
        this.tokenExpirationMs = milliseconds;
        logger.info("MCP token expiration set to: " + milliseconds + "ms");
    }
    
    /**
     * 注册API密钥
     * @param apiKey API密钥
     * @param secretKey 密钥对应的密钥
     */
    public void registerApiKey(String apiKey, String secretKey) {
        if (apiKey == null || secretKey == null) {
            throw new IllegalArgumentException("API key and secret key cannot be null");
        }
        
        // 存储密钥的哈希值而不是明文
        String hashedSecret = hash(secretKey);
        apiKeys.put(apiKey, hashedSecret);
        logger.info("API key registered: " + maskApiKey(apiKey));
    }
    
    /**
     * 注销API密钥
     * @param apiKey API密钥
     * @return 是否成功注销
     */
    public boolean unregisterApiKey(String apiKey) {
        String removed = apiKeys.remove(apiKey);
        if (removed != null) {
            logger.info("API key unregistered: " + maskApiKey(apiKey));
            return true;
        }
        return false;
    }
    
    /**
     * 验证API密钥
     * @param apiKey API密钥
     * @param secretKey 密钥
     * @return 是否验证通过
     */
    public boolean validateApiKey(String apiKey, String secretKey) {
        if (!securityEnabled || !authenticationRequired) {
            logger.warning("Security validation skipped due to configuration");
            return true;
        }
        
        if (apiKey == null || secretKey == null) {
            return false;
        }
        
        String storedHash = apiKeys.get(apiKey);
        if (storedHash == null) {
            logger.warning("Unknown API key: " + maskApiKey(apiKey));
            return false;
        }
        
        String providedHash = hash(secretKey);
        boolean isValid = storedHash.equals(providedHash);
        
        if (!isValid) {
            logger.warning("Invalid secret key for API key: " + maskApiKey(apiKey));
        }
        
        return isValid;
    }
    
    /**
     * 为会话创建密钥
     * @param sessionId 会话ID
     * @return 创建的会话密钥
     * @throws PlatformException 如果创建失败
     */
    public String createSessionKey(String sessionId) throws PlatformException {
        if (!securityEnabled) {
            return null;
        }
        
        try {
            // 生成随机密钥
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(AES_KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            
            // 编码密钥为Base64字符串
            String keyString = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            
            // 存储会话密钥信息
            SessionSecurityInfo info = new SessionSecurityInfo(secretKey, Instant.now().toEpochMilli());
            sessionKeys.put(sessionId, info);
            
            logger.info("Created session key for session: " + maskSessionId(sessionId));
            return keyString;
            
        } catch (NoSuchAlgorithmException e) {
            throw new PlatformException("Failed to create session key", e);
        }
    }
    
    /**
     * 获取会话密钥
     * @param sessionId 会话ID
     * @return 会话密钥
     */
    private SecretKey getSessionKey(String sessionId) {
        SessionSecurityInfo info = sessionKeys.get(sessionId);
        if (info == null) {
            logger.warning("Session key not found for: " + maskSessionId(sessionId));
            return null;
        }
        
        // 检查密钥是否过期
        if (isKeyExpired(info)) {
            logger.warning("Session key expired for: " + maskSessionId(sessionId));
            sessionKeys.remove(sessionId);
            return null;
        }
        
        // 更新最后使用时间
        info.updateLastUsed();
        return info.getKey();
    }
    
    /**
     * 移除会话密钥
     * @param sessionId 会话ID
     */
    public void removeSessionKey(String sessionId) {
        sessionKeys.remove(sessionId);
        logger.info("Removed session key for: " + maskSessionId(sessionId));
    }
    
    /**
     * 检查密钥是否过期
     * @param info 会话安全信息
     * @return 是否过期
     */
    private boolean isKeyExpired(SessionSecurityInfo info) {
        long currentTime = Instant.now().toEpochMilli();
        return (currentTime - info.getLastUsed()) > tokenExpirationMs;
    }
    
    /**
     * 加密消息
     * @param sessionId 会话ID
     * @param plaintext 明文消息
     * @return 加密后的消息（Base64编码）
     * @throws PlatformException 如果加密失败
     */
    public String encrypt(String sessionId, String plaintext) throws PlatformException {
        if (!securityEnabled) {
            return plaintext;
        }
        
        try {
            SecretKey secretKey = getSessionKey(sessionId);
            if (secretKey == null) {
                throw new PlatformException("No valid session key found");
            }
            
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            // 加密数据
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // 组合IV和密文
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            
            // 编码为Base64
            return Base64.getEncoder().encodeToString(byteBuffer.array());
            
        } catch (Exception e) {
            throw new PlatformException("Failed to encrypt message", e);
        }
    }
    
    /**
     * 解密消息
     * @param sessionId 会话ID
     * @param encryptedData 加密数据（Base64编码）
     * @return 解密后的明文
     * @throws PlatformException 如果解密失败
     */
    public String decrypt(String sessionId, String encryptedData) throws PlatformException {
        if (!securityEnabled) {
            return encryptedData;
        }
        
        try {
            SecretKey secretKey = getSessionKey(sessionId);
            if (secretKey == null) {
                throw new PlatformException("No valid session key found");
            }
            
            // 解码Base64数据
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            
            // 提取IV
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedBytes);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            
            // 提取密文
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            // 解密数据
            byte[] decryptedData = cipher.doFinal(ciphertext);
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            throw new PlatformException("Failed to decrypt message", e);
        }
    }
    
    /**
     * 生成哈希值
     * @param input 输入字符串
     * @return 哈希值（Base64编码）
     */
    public String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Failed to hash input", e);
            throw new RuntimeException("Failed to hash input", e);
        }
    }
    
    /**
     * 验证哈希值
     * @param input 输入字符串
     * @param hash 哈希值（Base64编码）
     * @return 是否匹配
     */
    public boolean verifyHash(String input, String hash) {
        String computedHash = hash(input);
        return computedHash.equals(hash);
    }
    
    /**
     * 生成安全随机字符串
     * @param length 字符串长度
     * @return 随机字符串
     */
    public String generateSecureRandomString(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).substring(0, length);
    }
    
    /**
     * 清理过期的会话密钥
     */
    public void cleanupExpiredKeys() {
        long currentTime = Instant.now().toEpochMilli();
        int removedCount = 0;
        
        for (Map.Entry<String, SessionSecurityInfo> entry : sessionKeys.entrySet()) {
            if ((currentTime - entry.getValue().getLastUsed()) > tokenExpirationMs) {
                sessionKeys.remove(entry.getKey());
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            logger.info("Cleaned up " + removedCount + " expired session keys");
        }
    }
    
    /**
     * 生成API密钥和密钥对
     * @return 包含API密钥和密钥的数组 [apiKey, secretKey]
     */
    public String[] generateApiKeyPair() {
        String apiKey = "MCP-" + generateSecureRandomString(24);
        String secretKey = generateSecureRandomString(32);
        return new String[]{apiKey, secretKey};
    }
    
    /**
     * 掩码API密钥用于日志记录
     * @param apiKey API密钥
     * @return 掩码后的密钥
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "[masked]";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
    
    /**
     * 掩码会话ID用于日志记录
     * @param sessionId 会话ID
     * @return 掩码后的会话ID
     */
    private String maskSessionId(String sessionId) {
        if (sessionId == null || sessionId.length() < 8) {
            return "[masked]";
        }
        return sessionId.substring(0, 4) + "..." + sessionId.substring(sessionId.length() - 4);
    }
    
    /**
     * 获取会话密钥数量
     * @return 密钥数量
     */
    public int getSessionKeyCount() {
        return sessionKeys.size();
    }
    
    /**
     * 获取API密钥数量
     * @return 密钥数量
     */
    public int getApiKeyCount() {
        return apiKeys.size();
    }
    
    /**
     * 导出密钥（用于备份）
     * @return 密钥映射
     */
    public Map<String, String> exportApiKeys() {
        // 返回一个新的映射，不直接暴露内部状态
        return new ConcurrentHashMap<>(apiKeys);
    }
    
    /**
     * 导入API密钥（用于恢复）
     * @param keys 密钥映射
     */
    public void importApiKeys(Map<String, String> keys) {
        if (keys != null) {
            apiKeys.clear();
            apiKeys.putAll(keys);
            logger.info("Imported " + keys.size() + " API keys");
        }
    }
    
    /**
     * 检查会话是否安全（有有效的密钥）
     * @param sessionId 会话ID
     * @return 是否安全
     */
    public boolean isSessionSecure(String sessionId) {
        if (!securityEnabled) {
            return true;
        }
        
        SessionSecurityInfo info = sessionKeys.get(sessionId);
        return info != null && !isKeyExpired(info);
    }
    
    /**
     * 生成签名
     * @param data 要签名的数据
     * @param secretKey 密钥
     * @return 签名（Base64编码）
     */
    public String sign(String data, String secretKey) {
        try {
            // 使用HMAC-SHA256生成签名
            String message = data + secretKey;
            return hash(message);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to sign data", e);
            throw new RuntimeException("Failed to sign data", e);
        }
    }
    
    /**
     * 验证签名
     * @param data 原始数据
     * @param signature 签名（Base64编码）
     * @param secretKey 密钥
     * @return 是否有效
     */
    public boolean verifySignature(String data, String signature, String secretKey) {
        String computedSignature = sign(data, secretKey);
        return computedSignature.equals(signature);
    }
    
    /**
     * 会话安全信息内部类
     */
    private static class SessionSecurityInfo {
        private final SecretKey key;
        private long lastUsed;
        
        public SessionSecurityInfo(SecretKey key, long lastUsed) {
            this.key = key;
            this.lastUsed = lastUsed;
        }
        
        public SecretKey getKey() {
            return key;
        }
        
        public long getLastUsed() {
            return lastUsed;
        }
        
        public void updateLastUsed() {
            this.lastUsed = Instant.now().toEpochMilli();
        }
    }
    
    /**
     * 创建默认的安全实例
     * @return McpSecurity实例
     */
    public static McpSecurity createDefault() {
        McpSecurity security = new McpSecurity();
        // 设置默认配置
        security.setTokenExpiration(3600000); // 1小时
        security.setAuthenticationRequired(true);
        security.setSecurityEnabled(true);
        return security;
    }
}