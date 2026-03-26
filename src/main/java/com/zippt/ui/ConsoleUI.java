package com.zippt.ui;

import com.zippt.enums.Role;
import com.zippt.model.User;
import com.zippt.service.*;

import java.util.Scanner;

public class ConsoleUI {
    private final Scanner scanner;
    private final UserService userService;
    private final PropertyService propertyService;
    private final ReservationService reservationService;
    private final AuctionService auctionService;
    private final ReviewService reviewService;

    private User currentUser;

    public ConsoleUI(Scanner scanner,
                     UserService userService,
                     PropertyService propertyService,
                     ReservationService reservationService,
                     AuctionService auctionService,
                     ReviewService reviewService) {
        this.scanner = scanner;
        this.userService = userService;
        this.propertyService = propertyService;
        this.reservationService = reservationService;
        this.auctionService = auctionService;
        this.reviewService = reviewService;
    }

    public void start() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║     집피티(ZIP-PT) v1.0              ║");
        System.out.println("║   AI 기반 부동산 매칭 플랫폼         ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();

        boolean running = true;
        while (running) {
            if (currentUser == null) {
                running = showMainMenu();
            } else {
                showRoleMenu();
            }
        }
        System.out.println("집피티(ZIP-PT)를 이용해주셔서 감사합니다.");
    }

    private boolean showMainMenu() {
        System.out.println("=== 메인 메뉴 ===");
        System.out.println("1. 회원가입");
        System.out.println("2. 로그인");
        System.out.println("0. 종료");
        System.out.print("> 선택: ");

        int choice = readInt();
        switch (choice) {
            case 1 -> handleRegister();
            case 2 -> handleLogin();
            case 0 -> { return false; }
            default -> System.out.println("[오류] 올바른 번호를 입력해주세요.\n");
        }
        return true;
    }

    private void handleRegister() {
        System.out.println("\n--- 회원가입 ---");
        System.out.print("아이디: ");
        String username = scanner.nextLine().trim();
        System.out.print("비밀번호: ");
        String password = scanner.nextLine().trim();
        System.out.print("이름: ");
        String name = scanner.nextLine().trim();
        System.out.print("전화번호: ");
        String phone = scanner.nextLine().trim();

        System.out.println("역할 선택:");
        System.out.println("  1. 매수자(Buyer)");
        System.out.println("  2. 매도자(Seller)");
        System.out.println("  3. 중개사(Agent)");
        System.out.print("> 선택: ");
        int roleChoice = readInt();

        Role role;
        switch (roleChoice) {
            case 1 -> role = Role.BUYER;
            case 2 -> role = Role.SELLER;
            case 3 -> role = Role.AGENT;
            default -> {
                System.out.println("[오류] 잘못된 역할 선택입니다.\n");
                return;
            }
        }

        String region = null;
        if (role == Role.AGENT) {
            System.out.print("담당 지역 (예: 강남구): ");
            region = scanner.nextLine().trim();
        }

        try {
            User user = userService.register(username, password, name, phone, role, region);
            System.out.println("[성공] 회원가입이 완료되었습니다. " + user);
        } catch (IllegalArgumentException e) {
            System.out.println("[오류] " + e.getMessage());
        }
        System.out.println();
    }

    private void handleLogin() {
        System.out.println("\n--- 로그인 ---");
        System.out.print("아이디: ");
        String username = scanner.nextLine().trim();
        System.out.print("비밀번호: ");
        String password = scanner.nextLine().trim();

        var userOpt = userService.login(username, password);
        if (userOpt.isPresent()) {
            currentUser = userOpt.get();
            System.out.println("[성공] 환영합니다, " + currentUser.getName()
                    + "님! (" + currentUser.getRole().getDisplayName() + ")");
        } else {
            System.out.println("[오류] 아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        System.out.println();
    }

    private void showRoleMenu() {
        switch (currentUser.getRole()) {
            case BUYER -> {
                BuyerMenu menu = new BuyerMenu(scanner, currentUser,
                        propertyService, reservationService, reviewService, userService);
                if (!menu.show()) currentUser = null;
            }
            case SELLER -> {
                SellerMenu menu = new SellerMenu(scanner, currentUser,
                        propertyService, auctionService, userService);
                if (!menu.show()) currentUser = null;
            }
            case AGENT -> {
                AgentMenu menu = new AgentMenu(scanner, currentUser,
                        reservationService, auctionService, propertyService, userService);
                if (!menu.show()) currentUser = null;
            }
        }
    }

    public int readInt() {
        try {
            String line = scanner.nextLine().trim();
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
