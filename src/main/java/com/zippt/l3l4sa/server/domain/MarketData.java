package com.zippt.l3l4sa.server.domain;

import com.zippt.l3l4sa.common.enums.PropertyType;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MarketData {
    private final String marketDataId;
    private final String region;
    private final PropertyType propertyType;
    private final BigDecimal transactionPrice;
    private final LocalDate transactionDate;
    private final String source;

    public MarketData(String marketDataId, String region, PropertyType propertyType,
                      BigDecimal transactionPrice, LocalDate transactionDate, String source) {
        this.marketDataId = marketDataId;
        this.region = region;
        this.propertyType = propertyType;
        this.transactionPrice = transactionPrice;
        this.transactionDate = transactionDate;
        this.source = source;
    }

    public BigDecimal getTransactionPrice() { return transactionPrice; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public String getMarketDataId() { return marketDataId; }
    public String getRegion() { return region; }
    public PropertyType getPropertyType() { return propertyType; }
    public String getSource() { return source; }
}


