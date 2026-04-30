package com.douyin.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String baseUrl = "http://192.168.31.105:8080";
    private String storageRoot = "./storage";
    private String ffmpegPath = "ffmpeg";
    private double coverFrameSecond = 1.0;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getStorageRoot() {
        return storageRoot;
    }

    public void setStorageRoot(String storageRoot) {
        this.storageRoot = storageRoot;
    }

    public String getFfmpegPath() {
        return ffmpegPath;
    }

    public void setFfmpegPath(String ffmpegPath) {
        this.ffmpegPath = ffmpegPath;
    }

    public double getCoverFrameSecond() {
        return coverFrameSecond;
    }

    public void setCoverFrameSecond(double coverFrameSecond) {
        this.coverFrameSecond = coverFrameSecond;
    }
}
