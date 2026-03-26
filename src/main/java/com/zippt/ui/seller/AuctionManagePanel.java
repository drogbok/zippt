package com.zippt.ui.seller;

import com.zippt.model.Auction;
import com.zippt.model.Property;
import com.zippt.model.User;
import com.zippt.service.AuctionService;
import com.zippt.service.PropertyService;
import com.zippt.service.UserService;
import com.zippt.ui.buyer.BuyerMainPanel;
import com.zippt.ui.common.ComponentFactory;
import com.zippt.ui.common.StyleConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AuctionManagePanel extends JPanel implements BuyerMainPanel.Refreshable {

    private final User currentUser;
    private final UserService userService;
    private final PropertyService propertyService;
    private final AuctionService auctionService;
    private JTable table;
    private DefaultTableModel tableModel;

    public AuctionManagePanel(User currentUser, UserService userService,
                              PropertyService propertyService, AuctionService auctionService) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.propertyService = propertyService;
        this.auctionService = auctionService;
        setLayout(new BorderLayout(0, 12));
        setBackground(StyleConstants.BACKGROUND);
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(StyleConstants.BACKGROUND);
        header.add(ComponentFactory.createTitleLabel("역경매 관리"), BorderLayout.WEST);
        JButton createBtn = ComponentFactory.createSecondaryButton("+ 역경매 생성");
        header.add(createBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "매물 주소", "상태", "입찰 수", "생성일"};
        table = ComponentFactory.createStyledTable(columns);
        tableModel = (DefaultTableModel) table.getModel();
        add(ComponentFactory.wrapInScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(StyleConstants.BACKGROUND);
        JButton bidReviewBtn = ComponentFactory.createOutlineButton("입찰 확인");
        btnPanel.add(bidReviewBtn);
        add(btnPanel, BorderLayout.SOUTH);

        createBtn.addActionListener(e -> showCreateDialog());
        bidReviewBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "역경매를 선택해주세요."); return; }
            long auctionId = (long) tableModel.getValueAt(row, 0);
            auctionService.findAuctionById(auctionId).ifPresent(a -> {
                BidReviewDialog dialog = new BidReviewDialog(
                        SwingUtilities.getWindowAncestor(this),
                        a, currentUser, userService, auctionService);
                dialog.setVisible(true);
                refresh();
            });
        });

        refresh();
    }

    private void showCreateDialog() {
        List<Property> myProperties = propertyService.findBySellerId(currentUser.getId());
        if (myProperties.isEmpty()) {
            JOptionPane.showMessageDialog(this, "등록된 매물이 없습니다. 먼저 매물을 등록해주세요.");
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "역경매 생성", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 260);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(StyleConstants.SURFACE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] propertyItems = myProperties.stream()
                .map(p -> p.getId() + " - " + p.getAddress())
                .toArray(String[]::new);
        JComboBox<String> propertyCombo = ComponentFactory.createComboBox(propertyItems);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(ComponentFactory.createLabel("매물"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(propertyCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(ComponentFactory.createLabel("요구사항"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        JTextArea reqArea = ComponentFactory.createTextArea(3);
        JScrollPane sp = new JScrollPane(reqArea);
        sp.setBorder(BorderFactory.createLineBorder(StyleConstants.BORDER));
        panel.add(sp, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0;
        gbc.insets = new Insets(12, 6, 6, 6);
        JButton saveBtn = ComponentFactory.createSecondaryButton("생성");
        panel.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            String selected = (String) propertyCombo.getSelectedItem();
            long propertyId = Long.parseLong(selected.split(" - ")[0].trim());
            String requirements = reqArea.getText().trim();
            try {
                auctionService.create(propertyId, currentUser.getId(), requirements);
                dialog.dispose();
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    @Override
    public void refresh() {
        tableModel.setRowCount(0);
        List<Auction> list = auctionService.findBySellerId(currentUser.getId());
        for (Auction a : list) {
            String address = propertyService.findById(a.getPropertyId())
                    .map(Property::getAddress).orElse("-");
            int bidCount = auctionService.findBidsByAuctionId(a.getId()).size();
            tableModel.addRow(new Object[]{
                    a.getId(), address, a.getStatus().getDisplayName(), bidCount,
                    a.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            });
        }
    }
}
