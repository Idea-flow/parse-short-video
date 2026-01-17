package com.ideaflow.parseshortvideo.parseshortvideo.util;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * User-Agent生成工具类
 */
@Component
public class UserAgentHelper {
    private final Random random = new Random();

    private static final List<String> IOS_USER_AGENTS = List.of(
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 15_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.6 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.3 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1"
    );

    private static final List<String> WINDOWS_USER_AGENTS = List.of(
           "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0",
           "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0 Trailer/93.3.3695.30",
           "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:123.0) Gecko/20100101 Firefox/123.0",
           "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0 Unique/97.7.7286.70"
    );

    /**
     * 获取随机iOS User-Agent
     */
    public String getRandomIosUserAgent() {
        return IOS_USER_AGENTS.get(random.nextInt(IOS_USER_AGENTS.size()));
    }

    /**
     * 获取随机Windows User-Agent
     */
    public String getRandomWindowsUserAgent() {
        return WINDOWS_USER_AGENTS.get(random.nextInt(WINDOWS_USER_AGENTS.size()));
    }

    /**
     * 获取默认User-Agent（iOS）
     */
    public String getDefaultUserAgent() {
        return getRandomIosUserAgent();
    }
}
