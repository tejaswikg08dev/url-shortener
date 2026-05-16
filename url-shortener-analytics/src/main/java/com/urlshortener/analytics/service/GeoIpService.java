package com.urlshortener.analytics.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
@Slf4j
public class GeoIpService {

    private DatabaseReader dbReader;

    @PostConstruct
    public void init() {
        try{
            InputStream db = getClass().getResourceAsStream("/GeoLite2-Country.mmdb");
            if(db != null){
                dbReader = new DatabaseReader.Builder(db).build();
                log.info("GeoIP database loaded successfully");
            } else {
                log.warn("GeoIP database not found - country lookup will return 'XX'");
            }
        } catch(Exception e){
            log.error("Failed to load GeoIP databse", e);
        }
    }

    public String lookupCountry(String ip){
        if(dbReader == null || ip == null) return "XX";
        try {
            InetAddress address = InetAddress.getByName(ip);
            CountryResponse response = dbReader.country(address);
            String code = response.getCountry().getIsoCode();
            return code != null ? code : "XX";
        } catch (Exception e) {
            return "XX";
        }
    }
}
