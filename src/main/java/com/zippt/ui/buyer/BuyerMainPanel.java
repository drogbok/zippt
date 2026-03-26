package com.zippt.ui.buyer;

import com.zippt.model.User;
import com.zippt.service.*;
import com.zippt.ui.common.ComponentFactory;
import com.zippt.ui.common.StyleConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BuyerMainPanel extends JPanel {

    private final CardLayout contentCardLayout;
    private final JPanel contentPanel;
    private final List<JButton> menuButtons = new ArrayList<>();

    public BuyerMainPanel(User user, UserService userService, PropertyService propertyService,
                          ReservationService reservationService, ReviewService reviewService,
                          Runnable onLogout) {
        setLayout(new BorderLayout());

        JPanel sidebar = ComponentFactory.createSidebar(user.getName() + "님", "매수자");

        JButton btnSearch = ComponentFactory.createSidebarButton("  매물 검색");
        JButton btnReservation = ComponentFactory.createSidebarButton("  내 예약");
        JButton btnReview = ComponentFactory.createSidebarButton("  후기 작성");
        JButton btnLogout = ComponentFactory.createSidebarButton("  로그아웃");

        menuButtons.add(btnSearch);
        menuButtons.add(btnReservation);
        menuButtons.add(btnReview);

        sidebar.add(btnSearch);
        sidebar.add(btnReservation);
        sidebar.add(btnReview);
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

        PropertySearchPanel searchPanel = new PropertySearchPanel(user, userService, propertyService, reservationService);
        ReservationPanel reservationPanel = new ReservationPanel(user, userService, propertyService, reservationService);
        ReviewPanel reviewPanel = new ReviewPanel(user, userService, propertyService, reservationService, reviewService);

        contentPanel.add(searchPanel, "SEARCH");
        contentPanel.add(reservationPanel, "RESERVATION");
        contentPanel.add(reviewPanel, "REVIEW");

        btnSearch.addActionListener(e -> switchTo("SEARCH", btnSearch, searchPanel));
        btnReservation.addActionListener(e -> switchTo("RESERVATION", btnReservation, reservationPanel));
        btnReview.addActionListener(e -> switchTo("REVIEW", btnReview, reviewPanel));
        btnLogout.addActionListener(e -> onLogout.run());

        add(sidebar, BorderLayout.WEST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(StyleConstants.BACKGROUND);
        wrapper.setBorder(new EmptyBorder(StyleConstants.PADDING_LARGE, StyleConstants.PADDING_LARGE,
                StyleConstants.PADDING_LARGE, StyleConstants.PADDING_LARGE));
        wrapper.add(contentPanel, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        switchTo("SEARCH", btnSearch, searchPanel);
    }

    private void switchTo(String card, JButton activeBtn, JPanel panel) {
        for (JButton btn : menuButtons) {
            ComponentFactory.setSidebarActive(btn, false);
        }
        ComponentFactory.setSidebarActive(activeBtn, true);
        contentCardLayout.show(contentPanel, card);
        if (panel instanceof Refreshable) {
            ((Refreshable) panel).refresh();
        }
    }

    public interface Refreshable {
        void refresh();
    }
}
