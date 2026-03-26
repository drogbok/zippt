package com.zippt;

import com.zippt.data.MockDataInitializer;
import com.zippt.repository.*;
import com.zippt.service.*;
import com.zippt.ui.ConsoleUI;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Repository 초기화
        UserRepository userRepo = new UserRepository();
        PropertyRepository propertyRepo = new PropertyRepository();
        ReservationRepository reservationRepo = new ReservationRepository();
        AuctionRepository auctionRepo = new AuctionRepository();
        ReviewRepository reviewRepo = new ReviewRepository();

        // Service 초기화
        UserService userService = new UserService(userRepo);
        PropertyService propertyService = new PropertyService(propertyRepo);
        ReservationService reservationService = new ReservationService(reservationRepo);
        AuctionService auctionService = new AuctionService(auctionRepo);
        ReviewService reviewService = new ReviewService(reviewRepo, reservationRepo);

        // Mock 데이터 생성
        MockDataInitializer.init(userRepo, propertyRepo);

        // 콘솔 UI 시작
        Scanner scanner = new Scanner(System.in);
        ConsoleUI ui = new ConsoleUI(scanner, userService, propertyService,
                reservationService, auctionService, reviewService);
        ui.start();
        scanner.close();
    }
}
