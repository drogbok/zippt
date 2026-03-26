package com.zippt.ui;

import com.zippt.enums.PropertyType;
import com.zippt.enums.ReservationStatus;
import com.zippt.enums.Role;
import com.zippt.model.*;
import com.zippt.service.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class BuyerMenu {
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Scanner scanner;
    private final User currentUser;
    private final PropertyService propertyService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;
    private final UserService userService;

    public BuyerMenu(Scanner scanner, User currentUser,
                     PropertyService propertyService,
                     ReservationService reservationService,
                     ReviewService reviewService,
                     UserService userService) {
        this.scanner = scanner;
        this.currentUser = currentUser;
        this.propertyService = propertyService;
        this.reservationService = reservationService;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    /**
     * @return true면 계속 로그인 상태, false면 로그아웃
     */
    public boolean show() {
        System.out.println("\n=== 매수자 메뉴 [" + currentUser.getName() + "] ===");
        System.out.println("1. 매물 검색");
        System.out.println("2. 매물 상세 조회");
        System.out.println("3. 방문 예약 신청");
        System.out.println("4. 내 예약 목록");
        System.out.println("5. 후기 작성");
        System.out.println("0. 로그아웃");
        System.out.print("> 선택: ");

        int choice = readInt();
        switch (choice) {
            case 1 -> searchProperties();
            case 2 -> viewPropertyDetail();
            case 3 -> makeReservation();
            case 4 -> myReservations();
            case 5 -> writeReview();
            case 0 -> {
                System.out.println("[알림] 로그아웃되었습니다.\n");
                return false;
            }
            default -> System.out.println("[오류] 올바른 번호를 입력해주세요.");
        }
        return true;
    }

    private void searchProperties() {
        System.out.println("\n--- 매물 검색 ---");
        System.out.print("지역 (빈 값이면 전체): ");
        String district = scanner.nextLine().trim();

        System.out.print("최소 가격(만원, 빈 값이면 무제한): ");
        Long minPrice = readLongOrNull();

        System.out.print("최대 가격(만원, 빈 값이면 무제한): ");
        Long maxPrice = readLongOrNull();

        System.out.print("최소 면적(㎡, 빈 값이면 무제한): ");
        Double minArea = readDoubleOrNull();

        System.out.print("최대 면적(㎡, 빈 값이면 무제한): ");
        Double maxArea = readDoubleOrNull();

        System.out.println("매물 유형 (빈 값이면 전체):");
        PropertyType[] types = PropertyType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.println("  " + (i + 1) + ". " + types[i].getDisplayName());
        }
        System.out.print("> 선택: ");
        String typeInput = scanner.nextLine().trim();
        PropertyType type = null;
        if (!typeInput.isEmpty()) {
            try {
                int idx = Integer.parseInt(typeInput) - 1;
                if (idx >= 0 && idx < types.length) {
                    type = types[idx];
                }
            } catch (NumberFormatException ignored) {}
        }

        List<Property> results = propertyService.search(
                district.isEmpty() ? null : district, minPrice, maxPrice, minArea, maxArea, type);

        if (results.isEmpty()) {
            System.out.println("\n[결과] 조건에 맞는 매물이 없습니다.");
        } else {
            System.out.println("\n[결과] " + results.size() + "건의 매물이 검색되었습니다.");
            System.out.println("-".repeat(70));
            results.forEach(p -> System.out.println("  " + p));
            System.out.println("-".repeat(70));
        }
        System.out.println();
    }

    private void viewPropertyDetail() {
        System.out.println("\n--- 매물 상세 조회 ---");
        System.out.print("매물 ID: ");
        long id = readLong();
        if (id <= 0) {
            System.out.println("[오류] 올바른 ID를 입력해주세요.\n");
            return;
        }

        var opt = propertyService.findById(id);
        if (opt.isEmpty()) {
            System.out.println("[오류] 해당 매물을 찾을 수 없습니다.\n");
            return;
        }

        Property p = opt.get();
        System.out.println("\n" + "=".repeat(50));
        System.out.println("  매물 번호 : #" + p.getId());
        System.out.println("  주소      : " + p.getAddress());
        System.out.println("  지역      : " + p.getDistrict());
        System.out.println("  면적      : " + p.getAreaSqm() + "㎡");
        System.out.println("  가격      : " + p.formatPrice());
        System.out.println("  유형      : " + p.getPropertyType().getDisplayName());
        System.out.println("  설명      : " + (p.getDescription() != null ? p.getDescription() : "-"));

        userService.findById(p.getSellerId())
                .ifPresent(seller -> System.out.println("  매도자    : " + seller.getName()));

        List<Review> reviews = reviewService.findByPropertyId(p.getId());
        if (!reviews.isEmpty()) {
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
            System.out.printf("  평균 별점  : %.1f / 5.0 (%d건)%n", avg, reviews.size());
            reviews.forEach(r -> System.out.println("    " + r));
        }
        System.out.println("=".repeat(50) + "\n");
    }

    private void makeReservation() {
        System.out.println("\n--- 방문 예약 신청 ---");
        System.out.print("매물 ID: ");
        long propertyId = readLong();
        if (propertyId <= 0 || propertyService.findById(propertyId).isEmpty()) {
            System.out.println("[오류] 유효한 매물 ID를 입력해주세요.\n");
            return;
        }

        List<User> agents = userService.findByRole(Role.AGENT);
        if (agents.isEmpty()) {
            System.out.println("[알림] 등록된 중개사가 없습니다.\n");
            return;
        }
        System.out.println("중개사 목록:");
        agents.forEach(a -> System.out.println("  " + a));
        System.out.print("중개사 ID: ");
        long agentId = readLong();

        if (agentId <= 0 || userService.findById(agentId)
                .filter(u -> u.getRole() == Role.AGENT).isEmpty()) {
            System.out.println("[오류] 유효한 중개사 ID를 입력해주세요.\n");
            return;
        }

        System.out.print("방문 희망 일시 (yyyy-MM-dd HH:mm): ");
        String dtStr = scanner.nextLine().trim();
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(dtStr, DT_FMT);
        } catch (DateTimeParseException e) {
            System.out.println("[오류] 날짜 형식이 올바르지 않습니다. (예: 2026-04-01 14:00)\n");
            return;
        }

        try {
            Reservation r = reservationService.create(currentUser.getId(), agentId, propertyId, dateTime);
            System.out.println("[성공] 예약이 신청되었습니다. " + r);
        } catch (IllegalStateException e) {
            System.out.println("[오류] " + e.getMessage());
        }
        System.out.println();
    }

    private void myReservations() {
        System.out.println("\n--- 내 예약 목록 ---");
        List<Reservation> list = reservationService.findByBuyerId(currentUser.getId());
        if (list.isEmpty()) {
            System.out.println("[알림] 예약 내역이 없습니다.");
        } else {
            list.forEach(r -> {
                String line = "  " + r;
                userService.findById(r.getAgentId())
                        .ifPresent(a -> System.out.println(line + " | 중개사: " + a.getName()));
            });
        }
        System.out.println();
    }

    private void writeReview() {
        System.out.println("\n--- 후기 작성 ---");
        List<Reservation> visitedList = reservationService.findByBuyerId(currentUser.getId())
                .stream()
                .filter(r -> r.getStatus() == ReservationStatus.VISITED)
                .toList();

        if (visitedList.isEmpty()) {
            System.out.println("[알림] 후기 작성 가능한 예약(방문 완료 상태)이 없습니다.\n");
            return;
        }

        System.out.println("후기 작성 가능한 예약:");
        visitedList.forEach(r -> System.out.println("  " + r));

        System.out.print("예약 ID: ");
        long reservationId = readLong();

        System.out.print("별점 (1~5): ");
        int rating = readInt();

        System.out.print("후기 내용: ");
        String content = scanner.nextLine().trim();

        try {
            Review review = reviewService.create(reservationId, currentUser.getId(), rating, content);
            System.out.println("[성공] 후기가 등록되었습니다. " + review);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[오류] " + e.getMessage());
        }
        System.out.println();
    }

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private long readLong() {
        try {
            return Long.parseLong(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Long readLongOrNull() {
        String s = scanner.nextLine().trim();
        if (s.isEmpty()) return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double readDoubleOrNull() {
        String s = scanner.nextLine().trim();
        if (s.isEmpty()) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
