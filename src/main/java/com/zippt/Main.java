package com.zippt;

import com.zippt.data.MockDataInitializer;
import com.zippt.repository.*;
import com.zippt.service.*;
import com.zippt.ui.ConsoleUI;
import com.zippt.ui.MainFrame;

import javax.swing.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UserRepository userRepo = new UserRepository();
        PropertyRepository propertyRepo = new PropertyRepository();
        ReservationRepository reservationRepo = new ReservationRepository();
        AuctionRepository auctionRepo = new AuctionRepository();
        ReviewRepository reviewRepo = new ReviewRepository();

        UserService userService = new UserService(userRepo);
        PropertyService propertyService = new PropertyService(propertyRepo);
        ReservationService reservationService = new ReservationService(reservationRepo);
        AuctionService auctionService = new AuctionService(auctionRepo);
        ReviewService reviewService = new ReviewService(reviewRepo, reservationRepo);

        MockDataInitializer.init(userRepo, propertyRepo);

        if (args.length > 0 && "--console".equals(args[0])) {
            Scanner scanner = new Scanner(System.in);
            ConsoleUI ui = new ConsoleUI(scanner, userService, propertyService,
                    reservationService, auctionService, reviewService);
            ui.start();
            scanner.close();
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}

                MainFrame frame = new MainFrame(userService, propertyService,
                        reservationService, auctionService, reviewService);
                frame.setVisible(true);
            });
        }
    }
}
