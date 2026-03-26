package com.zippt.ui.agent;

import com.zippt.model.User;
import com.zippt.service.*;
import com.zippt.ui.buyer.BuyerMainPanel;
import com.zippt.ui.common.ComponentFactory;
import com.zippt.ui.common.StyleConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AgentMainPanel extends JPanel {

    private final CardLayout contentCardLayout;
    private final JPanel contentPanel;
    private final List<JButton> menuButtons = new ArrayList<>();

    public AgentMainPanel(User user, UserService userService, PropertyService propertyService,
                          ReservationService reservationService, AuctionService auctionService,
                          Runnable onLogout) {
        setLayout(new BorderLayout());

        JPanel sidebar = ComponentFactory.createSidebar(user.getName() + "님", "중개사");

        JButton btnReservation = ComponentFactory.createSidebarButton("  예약 관리");
        JButton btnAuction = ComponentFactory.createSidebarButton("  역경매 조회");
        JButton btnBid = ComponentFactory.createSidebarButton("  내 입찰");
        JButton btnLogout = ComponentFactory.createSidebarButton("  로그아웃");

        menuButtons.add(btnReservation);
        menuButtons.add(btnAuction);
        menuButtons.add(btnBid);

        sidebar.add(btnReservation);
        sidebar.add(btnAuction);
        sidebar.add(btnBid);
        sidebar.add(Box.createVerticalGlue());

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x334155));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(8));
        btnLogout.setForeground(StyleConstants.DANGER);
        sidebar.add(btnLogout);

        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(StyleConstants.BACKGROUND);

        ReservationManagePanel reservationPanel = new ReservationManagePanel(
                user, userService, propertyService, reservationService);
        AuctionListPanel auctionPanel = new AuctionListPanel(
                user, userService, propertyService, auctionService);
        BidPanel bidPanel = new BidPanel(user, propertyService, auctionService);

        contentPanel.add(reservationPanel, "RESERVATION");
        contentPanel.add(auctionPanel, "AUCTION");
        contentPanel.add(bidPanel, "BID");

        btnReservation.addActionListener(e -> switchTo("RESERVATION", btnReservation, reservationPanel));
        btnAuction.addActionListener(e -> switchTo("AUCTION", btnAuction, auctionPanel));
        btnBid.addActionListener(e -> switchTo("BID", btnBid, bidPanel));
        btnLogout.addActionListener(e -> onLogout.run());

        add(sidebar, BorderLayout.WEST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(StyleConstants.BACKGROUND);
        wrapper.setBorder(new EmptyBorder(StyleConstants.PADDING_LARGE, StyleConstants.PADDING_LARGE,
                StyleConstants.PADDING_LARGE, StyleConstants.PADDING_LARGE));
        wrapper.add(contentPanel, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        switchTo("RESERVATION", btnReservation, reservationPanel);
    }

    private void switchTo(String card, JButton activeBtn, JPanel panel) {
        for (JButton btn : menuButtons) {
            ComponentFactory.setSidebarActive(btn, false);
        }
        ComponentFactory.setSidebarActive(activeBtn, true);
        contentCardLayout.show(contentPanel, card);
        if (panel instanceof BuyerMainPanel.Refreshable) {
            ((BuyerMainPanel.Refreshable) panel).refresh();
        }
    }
}
