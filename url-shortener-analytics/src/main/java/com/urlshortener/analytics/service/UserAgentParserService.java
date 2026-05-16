package com.urlshortener.analytics.service;

import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

@Service
public class UserAgentParserService {

    private final Parser uaParser = new Parser();

    public String parseDeviceType(String userAgent){
        if (userAgent == null || userAgent.isBlank()) return "unknown";

        try {
            Client client = uaParser.parse(userAgent);
            String device = client.device.family.toLowerCase();

            if (device.contains("spider") || device.contains("bot")) return "bot";
            if (device.contains("mobile") || device.contains("phone")) return "mobile";
            if (device.contains("tablet") || device.contains("ipad")) return "tablet";
            return "desktop";
        } catch (Exception e) {
            return "unknown";
        }
    }
}
