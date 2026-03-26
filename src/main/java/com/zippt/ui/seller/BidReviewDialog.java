package com.zippt.ui.seller;

import com.zippt.model.Auction;
import com.zippt.model.Bid;
import com.zippt.model.User;
import com.zippt.service.AuctionService;
import com.zippt.service.UserService;
import com.zippt.ui.common.ComponentFactory;
import com.zippt.ui.common.StyleConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BidReviewDialog extends JDialog {

    public BidReviewDialog(Window owner, Auction auction, User currentUser,
                           UserService userService, AuctionService auctionService) {
        super(owner, "입찰 확인 — 역경매 #" + auction.getId(), ModalityType.APPLICATION_MODAL);
        setSize(560, 400);
        setLocationRelativeTo(owner);

        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBackground(StyleConstants.SURFACE);
        main.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = ComponentFactory.createTitleLabel("입찰 목록");
        JLabel statusLabel = ComponentFactory.createLabel("상태: " + auction.getStatus().getDisplayName());
        statusLabel.setForeground(StyleConstants.PRIMARY);
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(StyleConstants.SURFACE);
        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        main.add(headerPanel, BorderLayout.NORTH);

        String[] columns = {"입찰 ID", "중개사", "수수료율(%)", "조건"};
        JTable table = ComponentFactory.createStyledTable(columns);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        List<Bid> bids = auctionService.findBidsByAuctionId(auction.getId());
        for (Bid b : bids) {
            String agentName = userService.findById(b.getAgentId())
                    .map(User::getName).orElse("-");
            model.addRow(new Object[]{
                    b.getId(), agentName,
                    String.format("%.2f", b.getCommissionRate()),
                    b.getConditions() != null ? b.getConditions() : "-"
            });
        }

        main.add(ComponentFactory.wrapInScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(StyleConstants.SURFACE);
        JButton awardBtn = ComponentFactory.createSecondaryButton("낙찰");
        btnPanel.add(awardBtn);
        main.add(btnPanel, BorderLayout.SOUTH);

        awardBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "낙찰할 입찰을 선택해주세요."); return; }
            long bidId = (long) model.getValueAt(row, 0);
            try {
                auctionService.awardBid(auction.getId(), bidId, currentUser.getId());
                JOptionPane.showMessageDialog(this, "낙찰이 완료되었습니다!", "성공",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        setContentPane(main);
    }
}
