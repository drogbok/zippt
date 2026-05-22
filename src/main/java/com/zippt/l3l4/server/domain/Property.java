package com.zippt.l3l4.server.domain;

import com.zippt.l3l4.common.enums.PropertyStatus;
import com.zippt.l3l4.common.enums.PropertyType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Property {
    private final String propertyId;
    private final String sellerId;
    private final String address;
    private final String region;
    private final BigDecimal areaSquareMeter;
    private final BigDecimal askingPrice;
    private final PropertyType propertyType;
    private final String description;
    private final LocalDateTime registeredAt;
    private PropertyStatus status;

    public Property(String propertyId, String sellerId, String address, String region,
                    BigDecimal areaSquareMeter, BigDecimal askingPrice,
                    PropertyType propertyType, String description) {
        this.propertyId = propertyId;
        this.sellerId = sellerId;
        this.address = address;
        this.region = region;
        this.areaSquareMeter = areaSquareMeter;
        this.askingPrice = askingPrice;
        this.propertyType = propertyType;
        this.description = description;
        this.registeredAt = LocalDateTime.now();
        this.status = PropertyStatus.REGISTERED;
    }

    public boolean isOwnedBy(String userId) {
        return sellerId.equals(userId);
    }

    public void markOnAuction() {
        status = PropertyStatus.ON_AUCTION;
    }

    public String getBasicInfo() {
        return address + " / " + areaSquareMeter + "㎡ / " + askingPrice;
    }

    public String getPropertyId() { return propertyId; }
    public String getSellerId() { return sellerId; }
    public String getAddress() { return address; }
    public String getRegion() { return region; }
    public BigDecimal getAreaSquareMeter() { return areaSquareMeter; }
    public BigDecimal getAskingPrice() { return askingPrice; }
    public PropertyType getPropertyType() { return propertyType; }
    public String getDescription() { return description; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public PropertyStatus getStatus() { return status; }
}

