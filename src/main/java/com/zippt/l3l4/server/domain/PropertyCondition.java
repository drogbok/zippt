package com.zippt.l3l4.server.domain;

import com.zippt.l3l4.common.enums.PropertyType;
import java.math.BigDecimal;

public class PropertyCondition {
    private final String conditionId;
    private final String region;
    private final BigDecimal priceMin;
    private final BigDecimal priceMax;
    private final BigDecimal areaMin;
    private final BigDecimal areaMax;
    private final String naturalLanguageQuery;
    private final PropertyType propertyType;

    public PropertyCondition(String conditionId, String region, BigDecimal priceMin, BigDecimal priceMax,
                             BigDecimal areaMin, BigDecimal areaMax, String naturalLanguageQuery,
                             PropertyType propertyType) {
        this.conditionId = conditionId;
        this.region = region;
        this.priceMin = priceMin;
        this.priceMax = priceMax;
        this.areaMin = areaMin;
        this.areaMax = areaMax;
        this.naturalLanguageQuery = naturalLanguageQuery;
        this.propertyType = propertyType;
    }

    public boolean matches(Property property) {
        boolean regionMatches = region == null || region.equals(property.getRegion());
        boolean typeMatches = propertyType == null || propertyType == property.getPropertyType();
        boolean priceMatches = (priceMin == null || property.getAskingPrice().compareTo(priceMin) >= 0)
                && (priceMax == null || property.getAskingPrice().compareTo(priceMax) <= 0);
        return regionMatches && typeMatches && priceMatches;
    }

    public String getConditionId() { return conditionId; }
    public String getNaturalLanguageQuery() { return naturalLanguageQuery; }
}

