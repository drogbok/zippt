package com.zippt.model;

import com.zippt.enums.PropertyType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Property {
    private long id;
    private long sellerId;
    private String address;
    private String district;
    private double areaSqm;
    private long priceInWan;
    private PropertyType propertyType;
    private String description;
    private LocalDateTime createdAt;

    public Property() {
        this.createdAt = LocalDateTime.now();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getSellerId() { return sellerId; }
    public void setSellerId(long sellerId) { this.sellerId = sellerId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public double getAreaSqm() { return areaSqm; }
    public void setAreaSqm(double areaSqm) { this.areaSqm = areaSqm; }

    public long getPriceInWan() { return priceInWan; }
    public void setPriceInWan(long priceInWan) { this.priceInWan = priceInWan; }

    public PropertyType getPropertyType() { return propertyType; }
    public void setPropertyType(PropertyType propertyType) { this.propertyType = propertyType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String formatPrice() {
        if (priceInWan >= 10000) {
            long eok = priceInWan / 10000;
            long remainder = priceInWan % 10000;
            return remainder > 0
                    ? String.format("%d억 %,d만원", eok, remainder)
                    : String.format("%d억원", eok);
        }
        return String.format("%,d만원", priceInWan);
    }

    @Override
    public String toString() {
        return String.format("[매물#%d] %s | %s | %.1f㎡ | %s | %s | %s",
                id, address, district, areaSqm, formatPrice(),
                propertyType.getDisplayName(),
                createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }
}
