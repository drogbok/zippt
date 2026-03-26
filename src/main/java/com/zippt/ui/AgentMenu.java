package com.zippt.ui;

import com.zippt.enums.ReservationStatus;
import com.zippt.model.*;
import com.zippt.service.*;

import java.util.List;
import java.util.Scanner;

public class AgentMenu {
    private final Scanner scanner;
    private final User currentUser;
    private final ReservationService reservationService;
    private final AuctionService auctionService;
    private final PropertyService propertyService;
    private final UserService userService;

    public AgentMenu(Scanner scanner, User currentUser,
                     ReservationService reservationService,
                     AuctionService auctionService,
                     PropertyService propertyService,
                     UserService userService) {
        this.scanner = scanner;
        this.currentUser = currentUser;
        this.reservationService = reservationService;
        this.auctionService = auctionService;
        this.propertyService = propertyService;
        this.userService = userService;
    }

    public boolean show() {
        System.out.println("\n=== 중개사 메뉴 [" + currentUser.getName() + "] ===");
        System.out.println("1. 내 예약 관리");
        System.out.println("2. 역경매 목록 조회");
        System.out.println("3. 입찰 참여");
        System.out.println("4. 내 입찰 목록");
        System.out.println("0. 로그아웃");
        System.out.print("> 선택: ");

        int choice = readInt();
        switch (choice) {
            case 1 -> manageReservations();
            case 2 -> viewOpenAuctions();
            case 3 -> placeBid();
            case 4 -> myBids();
            case 0 -> {
                System.out.println("[알림] 로그아웃되었습니다.\n");
                return false;
            }
            default -> System.out.println("[오류] 올바른 번호를 입력해주세요.");
        }
        return true;
    }

    private void manageReservations() {
        System.out.println("\n--- 내 예약 관리 ---");
        List<Reservation> reservations = reservationService.findByAgentId(currentUser.getId());
        if (reservations.isEmpty()) {
            System.out.println("[알림] 배정된 예약이 없습니다.\n");
            return;
        }

        for (Reservation r : reservations) {
            String buyerName = userService.findById(r.getBuyerId())
                    .map(User::getName).orElse("알 수 없음");
            String propAddr = propertyService.findById(r.getPropertyId())
                    .map(Property::getAddress).orElse("알 수 없음");
            System.out.printf("  %s | 매수자: %s | 매물: %s%n", r, buyerName, propAddr);
        }

        System.out.println("\n처리할 작업:");
        System.out.println("  1. 예약 확정 (Pending → Confirmed)");
        System.out.println("  2. 예약 거절 (Pending → Rejected)");
        System.out.println("  3. 방문 완료 처리 (Confirmed → Visited)");
        System.out.println("  0. 돌아가기");
        System.out.print("> 선택: ");

        int action = readInt();
        if (action == 0) return;

        System.out.print("예약 ID: ");
        long reservationId = readLong();

        try {
            switch (action) {
                case 1 -> {
                    reservationService.confirm(reservationId, currentUser.getId());
                    System.out.println("[성공] 예약이 확정되었습니다.");
                }
                case 2 -> {
                    reservationService.reject(reservationId, currentUser.getId());
                    System.out.println("[성공] 예약이 거절되었습니다.");
                }
                case 3 -> {
                    reservationService.markVisited(reservationId, currentUser.getId());
                    System.out.println("[성공] 방문 완료 처리되었습니다.");
                }
                default -> System.out.println("[오류] 올바른 번호를 입력해주세요.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[오류] " + e.getMessage());
        }
        System.out.println();
    }

    private void viewOpenAuctions() {
        System.out.println("\n--- 진행 중인 역경매 목록 ---");
        List<Auction> auctions = auctionService.findOpenOrActiveAuctions();
        if (auctions.isEmpty()) {
            System.out.println("[알림] 현재 진행 중인 역경매가 없습니다.");
        } else {
            for (Auction a : auctions) {
                String propInfo = propertyService.findById(a.getPropertyId())
                        .map(Property::toString).orElse("매물 정보 없음");
                String sellerName = userService.findById(a.getSellerId())
                        .map(User::getName).orElse("알 수 없음");
                System.out.println("  " + a);
                System.out.println("    └ 매물: " + propInfo);
                System.out.println("    └ 매도자: " + sellerName);

                List<Bid> bids = auctionService.findBidsByAuctionId(a.getId());
                System.out.println("    └ 현재 입찰 수: " + bids.size() + "건");
                System.out.println();
            }
        }
        System.out.println();
    }

    private void placeBid() {
        System.out.println("\n--- 입찰 참여 ---");
        List<Auction> auctions = auctionService.findOpenOrActiveAuctions();
        if (auctions.isEmpty()) {
            System.out.println("[알림] 입찰 가능한 역경매가 없습니다.\n");
            return;
        }

        System.out.println("입찰 가능한 경매:");
        auctions.forEach(a -> System.out.println("  " + a));

        System.out.print("경매 ID: ");
        long auctionId = readLong();

        System.out.print("제안 수수료율 (%): ");
        double rate = readDouble();
        if (rate <= 0) {
            System.out.println("[오류] 유효한 수수료율을 입력해주세요.\n");
            return;
        }

        System.out.print("입찰 조건 (선택사항): ");
        String conditions = scanner.nextLine().trim();

        try {
            Bid bid = auctionService.placeBid(auctionId, currentUser.getId(), rate,
                    conditions.isEmpty() ? null : conditions);
            System.out.println("[성공] 입찰이 완료되었습니다. " + bid);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("[오류] " + e.getMessage());
        }
        System.out.println();
    }

    private void myBids() {
        System.out.println("\n--- 내 입찰 목록 ---");
        List<Bid> bids = auctionService.findBidsByAgentId(currentUser.getId());
        if (bids.isEmpty()) {
            System.out.println("[알림] 입찰 내역이 없습니다.");
        } else {
            for (Bid b : bids) {
                var auctionOpt = auctionService.findAuctionById(b.getAuctionId());
                String auctionInfo = auctionOpt
                        .map(a -> "경매상태: " + a.getStatus().getDisplayName())
                        .orElse("");
                boolean isAwarded = auctionOpt
                        .map(a -> a.getAwardedBidId() != null && a.getAwardedBidId() == b.getId())
                        .orElse(false);
                System.out.println("  " + b + " | " + auctionInfo
                        + (isAwarded ? " ★ 낙찰됨" : ""));
            }
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
