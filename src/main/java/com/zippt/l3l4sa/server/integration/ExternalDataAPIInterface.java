package com.zippt.l3l4sa.server.integration;

import com.zippt.l3l4sa.common.enums.PropertyType;
import com.zippt.l3l4sa.server.domain.MarketData;
import com.zippt.l3l4sa.server.domain.NewsData;
import java.util.List;

public class ExternalDataAPIInterface {
    public List<MarketData> fetchMarketData(String region, PropertyType propertyType) {
        return List.of();
    }

    public List<NewsData> fetchNewsData(String region) {
        return List.of();
    }
}


