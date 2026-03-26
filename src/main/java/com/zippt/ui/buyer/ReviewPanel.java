package com.zippt.ui.buyer;

import com.zippt.enums.ReservationStatus;
import com.zippt.model.Property;
import com.zippt.model.Reservation;
import com.zippt.model.User;
import com.zippt.service.*;
import com.zippt.ui.common.ComponentFactory;
import com.zippt.ui.common.StyleConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ReviewPanel extends JPanel implements BuyerMainPanel.Refreshable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final User currentUser;
    private final UserService userService;
    private final PropertyService propertyService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> ratingCombo;
    private JTextArea contentArea;

    public ReviewPanel(User currentUser, UserService userService, PropertyService propertyService,
                       ReservationService reservationService, ReviewService reviewService) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.propertyService = propertyService;
        this.reservationService = reservationService;
        this.reviewService = reviewService;
        setLayout(new BorderLayout(0, 16));
        setBackground(StyleConstants.BACKGROUND);
        initUI();
    }

    private void initUI() {
        add(ComponentFactory.createTitleLabel("후기 작성"), BorderLayout.NORTH);

        String[] columns = {"예약 ID", "매물 주소", "중개사", "방문 일시"};
        table = ComponentFactory.createStyledTable(columns);
        tableModel = (DefaultTableModel) table.getModel();

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(StyleConstants.BACKGROUND);
        JLabel info = ComponentFactory.createLabel("방문 완료된 예약에 한해 후기를 작성할 수 있습니다.");
        info.setForeground(StyleConstants.TEXT_SECONDARY);
        topSection.add(info, BorderLayout.NORTH);
        topSection.add(ComponentFactory.wrapInScrollPane(table), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(StyleConstants.SURFACE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.BORDER),
                new EmptyBorder(16, 20, 16, 20)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(ComponentFactory.createLabel("별점"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        ratingCombo = ComponentFactory.createComboBox(new String[]{"★★★★★ (5)", "★★★★☆ (4)", "★★★☆☆ (3)", "★★☆☆☆ (2)", "★☆☆☆☆ (1)"});
        formPanel.add(ratingCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(ComponentFactory.createLabel("내용"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        contentArea = ComponentFactory.createTextArea(4);
        JScrollPane sp = new JScrollPane(contentArea);
        sp.setBorder(BorderFactory.createLineBorder(StyleConstants.BORDER));
        formPanel.add(sp, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0; gbc.weighty = 0;
        JButton submitBtn = ComponentFactory.createPrimaryButton("후기 작성");
        formPanel.add(submitBtn, gbc);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSection, formPanel);
        splitPane.setDividerLocation(280);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);

        submitBtn.addActionListener(e -> doSubmit());
    }

    private void doSubmit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "후기를 작성할 예약을 선택해주세요.");
            return;
        }

        long reservationId = (long) tableModel.getValueAt(row, 0);
        int rating = 5 - ratingCombo.getSelectedIndex();
        String content = contentArea.getText().trim();

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "후기 내용을 입력해주세요.");
            return;
        }

        try {
            reviewService.create(reservationId, currentUser.getId(), rating, content);
            JOptionPane.showMessageDialog(this, "후기가 작성되었습니다!", "성공", JOptionPane.INFORMATION_MESSAGE);
            contentArea.setText("");
            ratingCombo.setSelectedIndex(0);
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        tableModel.setRowCount(0);
        List<Reservation> visited = reservationService.findByBuyerId(currentUser.getId()).stream()
                .filter(r -> r.getStatus() == ReservationStatus.VISITED)
                .collect(Collectors.toList());
        for (Reservation r : visited) {
            String address = propertyService.findById(r.getPropertyId())
                    .map(Property::getAddress).orElse("-");
            String agentName = userService.findById(r.getAgentId())
                    .map(User::getName).orElse("-");
            String dateStr = r.getReservationDateTime() != null
                    ? r.getReservationDateTime().format(FMT) : "-";
            tableModel.addRow(new Object[]{r.getId(), address, agentName, dateStr});
        }
    }
}
