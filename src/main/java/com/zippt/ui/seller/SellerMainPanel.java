package com.zippt.ui.seller;

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

public class SellerMainPanel extends JPanel {

    private final CardLayout contentCardLayout;
    private final JPanel contentPanel;
    private final List<JButton> menuButtons = new ArrayList<>();

    public SellerMainPanel(User user, UserService userService, PropertyService propertyService,
                           AuctionService auctionService, Runnable onLogout) {
        setLayout(new BorderLayout());

        JPanel sidebar = ComponentFactory.createSidebar(user.getName() + "님", "매도자");

        JButton btnProperty = ComponentFactory.createSidebarButton("  매물 관리");
        JButton btnAuction = ComponentFactory.createSidebarButton("  역경매 관리");
        JButton btnLogout = ComponentFactory.createSidebarButton("  로그아웃");

        menuButtons.add(btnProperty);
        menuButtons.add(btnAuction);

        sidebar.add(btnProperty);
        sidebar.add(btnAuction);
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

        PropertyManagePanel propertyPanel = new PropertyManagePanel(user, propertyService);
        AuctionManagePanel auctionPanel = new AuctionManagePanel(user, userService, propertyService, auctionService);

        contentPanel.add(propertyPanel, "PROPERTY");
        contentPanel.add(auctionPanel, "AUCTION");

        btnProperty.addActionListener(e -> switchTo("PROPERTY", btnProperty, propertyPanel));
        btnAuction.addActionListener(e -> switchTo("AUCTION", btnAuction, auctionPanel));
        btnLogout.addActionListener(e -> onLogout.run());

        add(sidebar, BorderLayout.WEST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(StyleConstants.BACKGROUND);
        wrapper.setBorder(new EmptyBorder(StyleConstants.PADDING_LARGE, StyleConstants.PADDING_LARGE,
                StyleConstants.PADDING_LARGE, StyleConstants.PADDING_LARGE));
        wrapper.add(contentPanel, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        switchTo("PROPERTY", btnProperty, propertyPanel);
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
