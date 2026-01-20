package com.ideaflow.parseshortvideo.parseshortvideo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonArrayExtractor {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern PATTERN = Pattern.compile("^\\d+\\s*:\\s*(.*)$");
    
    /**
     * 解析格式为"数字: []"的字符串，提取冒号后面的JSON数组并转换为JsonNode
     * @param input 输入字符串，格式应该为"数字: []"
     * @return JsonNode对象，如果解析失败则返回null
     */
    public static JsonNode extractJsonArray(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        
        Matcher matcher = PATTERN.matcher(input.trim());
        if (matcher.matches()) {
            String jsonArrayStr = matcher.group(1);
            
            try {
                return objectMapper.readTree(jsonArrayStr);
            } catch (JsonProcessingException e) {
                System.err.println("无法解析JSON数组: " + e.getMessage());
                return null;
            }
        } else {
            System.err.println("输入格式不正确，期望格式为 '数字: []'");
            return null;
        }
    }
    
    /**
     * 解析格式为"数字: []"的字符串，提取冒号后面的JSON数组并转换为JsonNode
     * @param input 输入字符串，格式应该为"数字: []"
     * @param objectMapper 自定义ObjectMapper实例
     * @return JsonNode对象，如果解析失败则返回null
     */
    public static JsonNode extractJsonArray(String input, ObjectMapper objectMapper) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        
        Matcher matcher = PATTERN.matcher(input.trim());
        if (matcher.matches()) {
            String jsonArrayStr = matcher.group(1);
            
            try {
                return objectMapper.readTree(jsonArrayStr);
            } catch (JsonProcessingException e) {
                System.err.println("无法解析JSON数组: " + e.getMessage());
                return null;
            }
        } else {
            System.err.println("输入格式不正确，期望格式为 '数字: []'");
            return null;
        }
    }
    
    /**
     * 验证输入字符串是否符合"数字: []"格式
     * @param input 输入字符串
     * @return 如果格式正确返回true，否则返回false
     */
    public static boolean isValidFormat(String input) {
        if (input == null) {
            return false;
        }
        
        Matcher matcher = PATTERN.matcher(input.trim());
        return matcher.matches();
    }
    
    /**
     * 解码URL中的编码字符，例如将 %7C 解码为 |
     * @param url 编码的URL字符串
     * @return 解码后的URL字符串
     */
    public static String decodeUrl(String url) {
        if (url == null) {
            return null;
        }
        
        return URLDecoder.decode(url, StandardCharsets.UTF_8);
    }

    public static String encodeUrl(String url) {
        if (url == null) {
            return null;
        }

        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }
}