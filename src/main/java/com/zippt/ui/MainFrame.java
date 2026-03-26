package com.zippt.ui;

import com.zippt.enums.Role;
import com.zippt.model.User;
import com.zippt.service.*;
import com.zippt.ui.agent.AgentMainPanel;
import com.zippt.ui.buyer.BuyerMainPanel;
import com.zippt.ui.common.StyleConstants;
import com.zippt.ui.seller.SellerMainPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_BUYER = "BUYER";
    private static final String CARD_SELLER = "SELLER";
    private static final String CARD_AGENT = "AGENT";

    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private final UserService userService;
    private final PropertyService propertyService;
    private final ReservationService reservationService;
    private final AuctionService auctionService;
    private final ReviewService reviewService;

    public MainFrame(UserService userService, PropertyService propertyService,
                     ReservationService reservationService, AuctionService auctionService,
                     ReviewService reviewService) {
        this.userService = userService;
        this.propertyService = propertyService;
        this.reservationService = reservationService;
        this.auctionService = auctionService;
        this.reviewService = reviewService;

        setTitle("ZIP-PT | 집피티 — 부동산 매칭 플랫폼");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(StyleConstants.BACKGROUND);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                        MainFrame.this, "프로그램을 종료하시겠습니까?", "종료 확인",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        });

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        LoginPanel loginPanel = new LoginPanel(userService, this::onLogin);
        cardPanel.add(loginPanel, CARD_LOGIN);

        setContentPane(cardPanel);
    }

    private void onLogin(User user) {
        String cardName;
        JPanel dashPanel;

        if (user.getRole() == Role.BUYER) {
            cardName = CARD_BUYER;
            dashPanel = new BuyerMainPanel(user, userService, propertyService,
                    reservationService, reviewService, this::onLogout);
        } else if (user.getRole() == Role.SELLER) {
            cardName = CARD_SELLER;
            dashPanel = new SellerMainPanel(user, userService, propertyService,
                    auctionService, this::onLogout);
        } else {
            cardName = CARD_AGENT;
            dashPanel = new AgentMainPanel(user, userService, propertyService,
                    reservationService, auctionService, this::onLogout);
        }

        cardPanel.add(dashPanel, cardName);
        cardLayout.show(cardPanel, cardName);
    }

    private void onLogout() {
        for (Component c : cardPanel.getComponents()) {
            if (c instanceof BuyerMainPanel || c instanceof SellerMainPanel || c instanceof AgentMainPanel) {
                cardPanel.remove(c);
            }
        }
        cardLayout.show(cardPanel, CARD_LOGIN);
    }
}
