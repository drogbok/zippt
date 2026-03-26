package com.zippt.data;

import com.zippt.enums.PropertyType;
import com.zippt.enums.Role;
import com.zippt.model.Property;
import com.zippt.model.User;
import com.zippt.repository.*;

/**
 * 테스트용 Mock 데이터를 생성하는 초기화 클래스.
 * 사용자 7명, 매물 8건을 자동 생성한다.
 */
public class MockDataInitializer {

    public static void init(UserRepository userRepo, PropertyRepository propertyRepo) {
        // === 매수자 ===
        User buyer1 = createUser("buyer1", "1234", "김매수", "010-1111-1111", Role.BUYER, null);
        User buyer2 = createUser("buyer2", "1234", "이매수", "010-2222-2222", Role.BUYER, null);

        // === 매도자 ===
        User seller1 = createUser("seller1", "1234", "박매도", "010-3333-3333", Role.SELLER, null);
        User seller2 = createUser("seller2", "1234", "최매도", "010-4444-4444", Role.SELLER, null);

        // === 중개사 ===
        User agent1 = createUser("agent1", "1234", "정중개", "010-5555-5555", Role.AGENT, "강남구");
        User agent2 = createUser("agent2", "1234", "한중개", "010-6666-6666", Role.AGENT, "서초구");
        User agent3 = createUser("agent3", "1234", "오중개", "010-7777-7777", Role.AGENT, "마포구");

        userRepo.save(buyer1);
        userRepo.save(buyer2);
        userRepo.save(seller1);
        userRepo.save(seller2);
        userRepo.save(agent1);
        userRepo.save(agent2);
        userRepo.save(agent3);

        // === 매물 (seller1 소유) ===
        propertyRepo.save(createProperty(seller1.getId(),
                "서울시 강남구 역삼동 123-4", "강남구", 84.5, 150000,
                PropertyType.APARTMENT, "역삼역 도보 5분, 남향"));
        propertyRepo.save(createProperty(seller1.getId(),
                "서울시 서초구 서초동 456-7", "서초구", 59.0, 110000,
                PropertyType.APARTMENT, "서초역 인근, 리모델링 완료"));
        propertyRepo.save(createProperty(seller1.getId(),
                "서울시 마포구 상수동 789-1", "마포구", 45.0, 50000,
                PropertyType.VILLA, "상수역 도보 3분, 조용한 주택가"));
        propertyRepo.save(createProperty(seller1.getId(),
                "서울시 강남구 삼성동 100-2", "강남구", 33.0, 70000,
                PropertyType.OFFICETEL, "삼성역 역세권, 풀옵션"));
        propertyRepo.save(createProperty(seller1.getId(),
                "서울시 서초구 방배동 200-3", "서초구", 120.0, 200000,
                PropertyType.HOUSE, "방배역 인근, 정원 보유"));

        // === 매물 (seller2 소유) ===
        propertyRepo.save(createProperty(seller2.getId(),
                "서울시 마포구 연남동 55-8", "마포구", 52.0, 60000,
                PropertyType.VILLA, "연남동 카페거리 인접"));
        propertyRepo.save(createProperty(seller2.getId(),
                "서울시 강남구 논현동 88-3", "강남구", 66.0, 250000,
                PropertyType.COMMERCIAL, "논현역 대로변 1층 상가"));
        propertyRepo.save(createProperty(seller2.getId(),
                "서울시 서초구 잠원동 300-1", "서초구", 102.0, 180000,
                PropertyType.APARTMENT, "한강 조망, 고층"));

        System.out.println("[시스템] Mock 데이터 초기화 완료 — 사용자 7명, 매물 8건 등록됨");
        System.out.println("[시스템] 테스트 계정: buyer1/1234, seller1/1234, agent1/1234 (비밀번호 모두 1234)");
        System.out.println();
    }

    private static User createUser(String username, String password, String name,
                                   String phone, Role role, String region) {
        User user = new User(username, password, name, phone, role);
        if (region != null) user.setRegion(region);
        return user;
    }

    private static Property createProperty(long sellerId, String address, String district,
                                           double areaSqm, long priceInWan,
                                           PropertyType type, String description) {
        Property p = new Property();
        p.setSellerId(sellerId);
        p.setAddress(address);
        p.setDistrict(district);
        p.setAreaSqm(areaSqm);
        p.setPriceInWan(priceInWan);
        p.setPropertyType(type);
        p.setDescription(description);
        return p;
    }
}
