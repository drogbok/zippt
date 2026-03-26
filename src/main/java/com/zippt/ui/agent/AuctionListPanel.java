package com.zippt.ui.agent;

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
import java.util.List;

public class AuctionListPanel extends JPanel implements BuyerMainPanel.Refreshable {

    private final User currentUser;
    private final UserService userService;
    private final PropertyService propertyService;
    private final AuctionService auctionService;
    private JTable table;
    private DefaultTableModel tableModel;

    public AuctionListPanel(User currentUser, UserService userService,
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
        add(ComponentFactory.createTitleLabel("역경매 조회 (진행 중)"), BorderLayout.NORTH);

        String[] columns = {"ID", "매물 주소", "매도자", "상태", "요구사항"};
        table = ComponentFactory.createStyledTable(columns);
        tableModel = (DefaultTableModel) table.getModel();
        add(ComponentFactory.wrapInScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(StyleConstants.BACKGROUND);
        JButton bidBtn = ComponentFactory.createSecondaryButton("입찰 참여");
        btnPanel.add(bidBtn);
        add(btnPanel, BorderLayout.SOUTH);

        bidBtn.addActionListener(e -> showBidDialog());
        refresh();
    }

    private void showBidDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "역경매를 선택해주세요."); return; }
        long auctionId = (long) tableModel.getValueAt(row, 0);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "입찰 참여", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 240);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(StyleConstants.SURFACE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField rateField = ComponentFactory.createTextField();
        rateField.setPreferredSize(new Dimension(200, 34));
        JTextField condField = ComponentFactory.createTextField();
        condField.setPreferredSize(new Dimension(200, 34));

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(ComponentFactory.createLabel("수수료율(%)"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(rateField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(ComponentFactory.createLabel("조건"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(condField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 6, 6, 6);
        JButton submitBtn = ComponentFactory.createSecondaryButton("입찰 제출");
        panel.add(submitBtn, gbc);

        submitBtn.addActionListener(e -> {
            try {
                double rate = Double.parseDouble(rateField.getText().trim());
                String conditions = condField.getText().trim();
                auctionService.placeBid(auctionId, currentUser.getId(), rate, conditions);
                JOptionPane.showMessageDialog(dialog, "입찰이 완료되었습니다!", "성공",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refresh();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "수수료율을 숫자로 입력해주세요.",
                        "오류", JOptionPane.ERROR_MESSAGE);
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
        List<Auction> list = auctionService.findOpenOrActiveAuctions();
        for (Auction a : list) {
            String address = propertyService.findById(a.getPropertyId())
                    .map(Property::getAddress).orElse("-");
            String sellerName = userService.findById(a.getSellerId())
                    .map(User::getName).orElse("-");
            tableModel.addRow(new Object[]{
                    a.getId(), address, sellerName,
                    a.getStatus().getDisplayName(),
                    a.getRequirements() != null ? a.getRequirements() : "-"
            });
        }
    }
}
