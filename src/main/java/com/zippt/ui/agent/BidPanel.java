package com.zippt.ui.agent;

import com.zippt.model.Auction;
import com.zippt.model.Bid;
import com.zippt.model.Property;
import com.zippt.model.User;
import com.zippt.service.AuctionService;
import com.zippt.service.PropertyService;
import com.zippt.ui.buyer.BuyerMainPanel;
import com.zippt.ui.common.ComponentFactory;
import com.zippt.ui.common.StyleConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BidPanel extends JPanel implements BuyerMainPanel.Refreshable {

    private final User currentUser;
    private final PropertyService propertyService;
    private final AuctionService auctionService;
    private JTable table;
    private DefaultTableModel tableModel;

    public BidPanel(User currentUser, PropertyService propertyService, AuctionService auctionService) {
        this.currentUser = currentUser;
        this.propertyService = propertyService;
        this.auctionService = auctionService;
        setLayout(new BorderLayout(0, 12));
        setBackground(StyleConstants.BACKGROUND);
        initUI();
    }

    private void initUI() {
        add(ComponentFactory.createTitleLabel("내 입찰 목록"), BorderLayout.NORTH);

        String[] columns = {"역경매 ID", "매물 주소", "수수료율(%)", "조건", "역경매 상태"};
        table = ComponentFactory.createStyledTable(columns);
        tableModel = (DefaultTableModel) table.getModel();
        add(ComponentFactory.wrapInScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    @Override
    public void refresh() {
        tableModel.setRowCount(0);
        List<Bid> bids = auctionService.findBidsByAgentId(currentUser.getId());
        for (Bid b : bids) {
            String auctionStatus = auctionService.findAuctionById(b.getAuctionId())
                    .map(a -> a.getStatus().getDisplayName()).orElse("-");
            String address = auctionService.findAuctionById(b.getAuctionId())
                    .flatMap(a -> propertyService.findById(a.getPropertyId()))
                    .map(Property::getAddress).orElse("-");
            tableModel.addRow(new Object[]{
                    b.getAuctionId(), address,
                    String.format("%.2f", b.getCommissionRate()),
                    b.getConditions() != null ? b.getConditions() : "-",
                    auctionStatus
            });
        }
    }
}
