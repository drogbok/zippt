package com.zippt.ui.agent;

import com.zippt.enums.ReservationStatus;
import com.zippt.model.Property;
import com.zippt.model.Reservation;
import com.zippt.model.User;
import com.zippt.service.PropertyService;
import com.zippt.service.ReservationService;
import com.zippt.service.UserService;
import com.zippt.ui.buyer.BuyerMainPanel;
import com.zippt.ui.common.ComponentFactory;
import com.zippt.ui.common.StyleConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservationManagePanel extends JPanel implements BuyerMainPanel.Refreshable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final User currentUser;
    private final UserService userService;
    private final PropertyService propertyService;
    private final ReservationService reservationService;
    private JTable table;
    private DefaultTableModel tableModel;

    public ReservationManagePanel(User currentUser, UserService userService,
                                  PropertyService propertyService, ReservationService reservationService) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.propertyService = propertyService;
        this.reservationService = reservationService;
        setLayout(new BorderLayout(0, 12));
        setBackground(StyleConstants.BACKGROUND);
        initUI();
    }

    private void initUI() {
        add(ComponentFactory.createTitleLabel("예약 관리"), BorderLayout.NORTH);

        String[] columns = {"예약 ID", "매수자", "매물 주소", "예약 일시", "상태"};
        table = ComponentFactory.createStyledTable(columns);
        tableModel = (DefaultTableModel) table.getModel();

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font(StyleConstants.FONT_FAMILY, Font.BOLD, 11));
                String status = value != null ? value.toString() : "";
                Color fg;
                switch (status) {
                    case "대기": fg = StyleConstants.SECONDARY; break;
                    case "확정": fg = StyleConstants.PRIMARY; break;
                    case "거절": fg = StyleConstants.DANGER; break;
                    case "방문 완료": fg = StyleConstants.SUCCESS; break;
                    default: fg = StyleConstants.TEXT_SECONDARY;
                }
                label.setForeground(fg);
                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? StyleConstants.TABLE_ROW_ODD : StyleConstants.TABLE_ROW_EVEN);
                }
                return label;
            }
        });

        add(ComponentFactory.wrapInScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(StyleConstants.BACKGROUND);
        JButton confirmBtn = ComponentFactory.createPrimaryButton("확정");
        JButton rejectBtn = ComponentFactory.createDangerButton("거절");
        JButton visitedBtn = ComponentFactory.createOutlineButton("방문 완료");
        btnPanel.add(confirmBtn);
        btnPanel.add(rejectBtn);
        btnPanel.add(visitedBtn);
        add(btnPanel, BorderLayout.SOUTH);

        confirmBtn.addActionListener(e -> handleAction("confirm"));
        rejectBtn.addActionListener(e -> handleAction("reject"));
        visitedBtn.addActionListener(e -> handleAction("visited"));

        refresh();
    }

    private void handleAction(String action) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "예약을 선택해주세요."); return; }
        long reservationId = (long) tableModel.getValueAt(row, 0);

        try {
            switch (action) {
                case "confirm":
                    reservationService.confirm(reservationId, currentUser.getId());
                    break;
                case "reject":
                    reservationService.reject(reservationId, currentUser.getId());
                    break;
                case "visited":
                    reservationService.markVisited(reservationId, currentUser.getId());
                    break;
            }
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refresh() {
        tableModel.setRowCount(0);
        List<Reservation> list = reservationService.findByAgentId(currentUser.getId());
        for (Reservation r : list) {
            String buyerName = userService.findById(r.getBuyerId())
                    .map(User::getName).orElse("-");
            String address = propertyService.findById(r.getPropertyId())
                    .map(Property::getAddress).orElse("-");
            String dateStr = r.getReservationDateTime() != null
                    ? r.getReservationDateTime().format(FMT) : "-";
            tableModel.addRow(new Object[]{
                    r.getId(), buyerName, address, dateStr,
                    r.getStatus().getDisplayName()
            });
        }
    }
}
