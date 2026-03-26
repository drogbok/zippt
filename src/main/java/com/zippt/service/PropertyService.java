package com.zippt.service;

import com.zippt.enums.PropertyType;
import com.zippt.model.Property;
import com.zippt.repository.PropertyRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PropertyService {
    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public Property register(long sellerId, String address, String district,
                             double areaSqm, long priceInWan,
                             PropertyType type, String description) {
        Property property = new Property();
        property.setSellerId(sellerId);
        property.setAddress(address);
        property.setDistrict(district);
        property.setAreaSqm(areaSqm);
        property.setPriceInWan(priceInWan);
        property.setPropertyType(type);
        property.setDescription(description);
        return propertyRepository.save(property);
    }

    public boolean update(long propertyId, long sellerId, String address, String district,
                          double areaSqm, long priceInWan,
                          PropertyType type, String description) {
        Optional<Property> opt = propertyRepository.findById(propertyId);
        if (opt.isEmpty()) return false;

        Property property = opt.get();
        if (property.getSellerId() != sellerId) {
            throw new IllegalArgumentException("본인의 매물만 수정할 수 있습니다.");
        }

        property.setAddress(address);
        property.setDistrict(district);
        property.setAreaSqm(areaSqm);
        property.setPriceInWan(priceInWan);
        property.setPropertyType(type);
        property.setDescription(description);
        propertyRepository.save(property);
        return true;
    }

    public boolean delete(long propertyId, long sellerId) {
        Optional<Property> opt = propertyRepository.findById(propertyId);
        if (opt.isEmpty()) return false;

        Property property = opt.get();
        if (property.getSellerId() != sellerId) {
            throw new IllegalArgumentException("본인의 매물만 삭제할 수 있습니다.");
        }
        return propertyRepository.delete(propertyId);
    }

    public List<Property> search(String district, Long minPrice, Long maxPrice,
                                 Double minArea, Double maxArea, PropertyType type) {
        return propertyRepository.findAll().stream()
                .filter(p -> district == null || district.isBlank()
                        || p.getDistrict().contains(district))
                .filter(p -> minPrice == null || p.getPriceInWan() >= minPrice)
                .filter(p -> maxPrice == null || p.getPriceInWan() <= maxPrice)
                .filter(p -> minArea == null || p.getAreaSqm() >= minArea)
                .filter(p -> maxArea == null || p.getAreaSqm() <= maxArea)
                .filter(p -> type == null || p.getPropertyType() == type)
                .collect(Collectors.toList());
    }

    public Optional<Property> findById(long id) {
        return propertyRepository.findById(id);
    }

    public List<Property> findBySellerId(long sellerId) {
        return propertyRepository.findBySellerId(sellerId);
    }

    public List<Property> findAll() {
        return propertyRepository.findAll();
    }
}
