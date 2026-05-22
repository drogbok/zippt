package com.zippt.l3l4.server.domain;

import java.time.LocalDateTime;

public class NewsData {
    private final String newsDataId;
    private final String region;
    private final String title;
    private final String contentSummary;
    private final LocalDateTime publishedAt;
    private final String source;

    public NewsData(String newsDataId, String region, String title,
                    String contentSummary, LocalDateTime publishedAt, String source) {
        this.newsDataId = newsDataId;
        this.region = region;
        this.title = title;
        this.contentSummary = contentSummary;
        this.publishedAt = publishedAt;
        this.source = source;
    }

    public String getSummary() { return contentSummary; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public String getNewsDataId() { return newsDataId; }
    public String getRegion() { return region; }
    public String getTitle() { return title; }
    public String getSource() { return source; }
}

