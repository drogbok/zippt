package com.zippt.ui;

import com.zippt.enums.AuctionStatus;
import com.zippt.enums.PropertyType;
import com.zippt.model.*;
import com.zippt.service.*;

import java.util.List;
import java.util.Scanner;

public class SellerMenu {
    private final Scanner scanner;
    private final User currentUser;
    private final PropertyService propertyService;
    private final AuctionService auctionService;
    private final UserService userService;

    public SellerMenu(Scanner scanner, User currentUser,
                      PropertyService propertyService,
                      AuctionService auctionService,
                      UserService userService) {
        this.scanner = scanner;
        this.currentUser = currentUser;
        this.propertyService = propertyService;
        this.auctionService = auctionService;
        this.userService = userService;
    }

    public boolean show() {
        System.out.println("\n=== 매도자 메뉴 [" + currentUser.getName() + "] ===");
        System.out.println("1. 매물 등록");
        System.out.println("2. 내 매물 목록");
        System.out.println("3. 매물 수정");
        System.out.println("4. 매물 삭제");
        System.out.println("5. 역경매 생성");
        System.out.println("6. 내 역경매 목록");
        System.out.println("7. 입찰 확인 및 낙찰");
        System.out.println("0. 로그아웃");
        System.out.print("> 선택: ");

        int choice = readInt();
        switch (choice) {
            case 1 -> registerProperty();
            case 2 -> myProperties();
            case 3 -> editProperty();
            case 4 -> deleteProperty();
            case 5 -> createAuction();
            case 6 -> myAuctions();
            case 7 -> viewBidsAndAward();
            case 0 -> {
                System.out.println("[알림] 로그아웃되었습니다.\n");
                return false;
            }
            default -> System.out.println("[오류] 올바른 번호를 입력해주세요.");
        }
        return true;
    }

    private void registerProperty() {
        System.out.println("\n--- 매물 등록 ---");
        System.out.print("주소: ");
        String address = scanner.nextLine().trim();
        System.out.print("지역구 (예: 강남구): ");
        String district = scanner.nextLine().trim();
        System.out.print("면적 (㎡): ");
        double area = readDouble();
        System.out.print("가격 (만원): ");
        long price = readLong();

        System.out.println("매물 유형:");
        PropertyType[] types = PropertyType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.println("  " + (i + 1) + ". " + types[i].getDisplayName());
        }
        System.out.print("> 선택: ");
        int typeIdx = readInt() - 1;
        if (typeIdx < 0 || typeIdx >= types.length) {
            System.out.println("[오류] 잘못된 유형 선택입니다.\n");
            return;
        }
        PropertyType type = types[typeIdx];

        System.out.print("설명 (선택사항): ");
        String desc = scanner.nextLine().trim();

        Property p = propertyService.register(currentUser.getId(), address, district,
                area, price, type, desc.isEmpty() ? null : desc);
        System.out.println("[성공] 매물이 등록되었습니다. " + p);
        System.out.println();
    }

    private void myProperties() {
        System.out.println("\n--- 내 매물 목록 ---");
        List<Property> list = propertyService.findBySellerId(currentUser.getId());
        if (list.isEmpty()) {
            System.out.println("[알림] 등록된 매물이 없습니다.");
        } else {
            list.forEach(p -> System.out.println("  " + p));
        }
        System.out.println();
    }

    private void editProperty() {
        System.out.println("\n--- 매물 수정 ---");
        myProperties();
        System.out.print("수정할 매물 ID: ");
        long id = readLong();

        var opt = propertyService.findById(id);
        if (opt.isEmpty() || opt.get().getSellerId() != currentUser.getId()) {
            System.out.println("[오류] 본인의 매물만 수정할 수 있습니다.\n");
            return;
        }

        Property existing = opt.get();
        System.out.println("(빈 값을 입력하면 기존 값이 유지됩니다)");

        System.out.print("주소 [" + existing.getAddress() + "]: ");
        String address = scanner.nextLine().trim();
        if (address.isEmpty()) address = existing.getAddress();

        System.out.print("지역구 [" + existing.getDistrict() + "]: ");
        String district = scanner.nextLine().trim();
        if (district.isEmpty()) district = existing.getDistrict();

        System.out.print("면적(㎡) [" + existing.getAreaSqm() + "]: ");
        String areaStr = scanner.nextLine().trim();
        double area = areaStr.isEmpty() ? existing.getAreaSqm() : Double.parseDouble(areaStr);

        System.out.print("가격(만원) [" + existing.getPriceInWan() + "]: ");
        String priceStr = scanner.nextLine().trim();
        long price = priceStr.isEmpty() ? existing.getPriceInWan() : Long.parseLong(priceStr);

        System.out.println("매물 유형 [" + existing.getPropertyType().getDisplayName() + "]:");
        PropertyType[] types = PropertyType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.println("  " + (i + 1) + ". " + types[i].getDisplayName());
        }
        System.out.print("> 선택 (빈 값이면 유지): ");
        String typeStr = scanner.nextLine().trim();
        PropertyType type = existing.getPropertyType();
        if (!typeStr.isEmpty()) {
            int idx = Integer.parseInt(typeStr) - 1;
            if (idx >= 0 && idx < types.length) type = types[idx];
        }

        System.out.print("설명 [" + (existing.getDescription() != null ? existing.getDescription() : "") + "]: ");
        String desc = scanner.nextLine().trim();
        if (desc.isEmpty()) desc = existing.getDescription();

        try {
            propertyService.update(id, currentUser.getId(), address, district, area, price, type, desc);
            System.out.println("[성공] 매물 정보가 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("[오류] " + e.getMessage());
        }
        System.out.println();
    }

    private void deleteProperty() {
        System.out.println("\n--- 매물 삭제 ---");
        myProperties();
        System.out.print("삭제할 매물 ID: ");
        long id = readLong();

        try {
            if (propertyService.delete(id, currentUser.getId())) {
                System.out.println("[성공] 매물이 삭제되었습니다.");
            } else {
                System.out.println("[오류] 해당 매물을 찾을 수 없습니다.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("[오류] " + e.getMessage());
        }
        System.out.println();
    }

    private void createAuction() {
        System.out.println("\n--- 역경매 생성 ---");
        List<Property> myProps = propertyService.findBySellerId(currentUser.getId());
        if (myProps.isEmpty()) {
            System.out.println("[알림] 먼저 매물을 등록해주세요.\n");
            return;
        }
        System.out.println("내 매물 목록:");
        myProps.forEach(p -> System.out.println("  " + p));

        System.out.print("역경매할 매물 ID: ");
        long propertyId = readLong();
        if (propertyService.findById(propertyId)
                .filter(p -> p.getSellerId() == currentUser.getId()).isEmpty()) {
            System.out.println("[오류] 본인의 매물만 역경매에 등록할 수 있습니다.\n");
            return;
        }

        System.out.print("요구사항 (예: 빠른 거래 희망, 수수료 1% 이하 등): ");
        String requirements = scanner.nextLine().trim();

        Auction auction = auctionService.create(propertyId, currentUser.getId(),
                requirements.isEmpty() ? null : requirements);
        System.out.println("[성공] 역경매가 생성되었습니다. " + auction);
        System.out.println();
    }

    private void myAuctions() {
        System.out.println("\n--- 내 역경매 목록 ---");
        List<Auction> list = auctionService.findBySellerId(currentUser.getId());
        if (list.isEmpty()) {
            System.out.println("[알림] 생성된 역경매가 없습니다.");
        } else {
            for (Auction a : list) {
                System.out.println("  " + a);
                List<Bid> bids = auctionService.findBidsByAuctionId(a.getId());
                if (!bids.isEmpty()) {
                    System.out.println("    └ 입찰 " + bids.size() + "건");
                }
            }
        }
        System.out.println();
    }

    private void viewBidsAndAward() {
        System.out.println("\n--- 입찰 확인 및 낙찰 ---");
        List<Auction> auctions = auctionService.findBySellerId(currentUser.getId()).stream()
                .filter(a -> a.getStatus() == AuctionStatus.ACTIVE || a.getStatus() == AuctionStatus.OPEN)
                .toList();

        if (auctions.isEmpty()) {
            System.out.println("[알림] 낙찰 가능한 경매가 없습니다.\n");
            return;
        }

        System.out.println("진행 중인 경매:");
        auctions.forEach(a -> System.out.println("  " + a));

        System.out.print("경매 ID: ");
        long auctionId = readLong();

        List<Bid> bids = auctionService.findBidsByAuctionId(auctionId);
        if (bids.isEmpty()) {
            System.out.println("[알림] 아직 입찰이 없습니다.\n");
            return;
        }

        System.out.println("\n입찰 목록:");
        for (Bid b : bids) {
            String agentName = userService.findById(b.getAgentId())
                    .map(User::getName).orElse("알 수 없음");
            System.out.printf("  %s | 중개사: %s%n", b, agentName);
        }

        System.out.print("\n낙찰할 입찰 ID (0: 취소): ");
        long bidId = readLong();
        if (bidId == 0) return;

        try {
            auctionService.awardBid(auctionId, bidId, currentUser.getId());
            System.out.println("[성공] 낙찰이 완료되었습니다!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[오류] " + e.getMessage());
        }
        System.out.println();
    }

    private int readInt() {
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    private long readLong() {
        try { return Long.parseLong(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    private double readDouble() {
        try { return Double.parseDouble(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }
}
