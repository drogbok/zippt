package com.zippt.ui.buyer;

import com.zippt.enums.ReservationStatus;
import com.zippt.model.Property;
import com.zippt.model.Reservation;
import com.zippt.model.User;
import com.zippt.service.PropertyService;
import com.zippt.service.ReservationService;
import com.zippt.service.UserService;
import com.zippt.ui.common.ComponentFactory;
import com.zippt.ui.common.StyleConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservationPanel extends JPanel implements BuyerMainPanel.Refreshable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final User currentUser;
    private final UserService userService;
    private final PropertyService propertyService;
    private final ReservationService reservationService;
    private JTable table;
    private DefaultTableModel tableModel;

    public ReservationPanel(User currentUser, UserService userService,
                            PropertyService propertyService, ReservationService reservationService) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.propertyService = propertyService;
        this.reservationService = reservationService;
        setLayout(new BorderLayout(0, 16));
        setBackground(StyleConstants.BACKGROUND);
        initUI();
    }

    private void initUI() {
        add(ComponentFactory.createTitleLabel("내 예약 목록"), BorderLayout.NORTH);

        String[] columns = {"예약 ID", "매물 주소", "중개사", "예약 일시", "상태"};
        table = ComponentFactory.createStyledTable(columns);
        tableModel = (DefaultTableModel) table.getModel();

        table.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());

        add(ComponentFactory.wrapInScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        tableModel.setRowCount(0);
        List<Reservation> reservations = reservationService.findByBuyerId(currentUser.getId());
        for (Reservation r : reservations) {
            String address = propertyService.findById(r.getPropertyId())
                    .map(Property::getAddress).orElse("-");
            String agentName = userService.findById(r.getAgentId())
                    .map(User::getName).orElse("-");
            String dateStr = r.getReservationDateTime() != null
                    ? r.getReservationDateTime().format(FMT) : "-";

            tableModel.addRow(new Object[]{
                    r.getId(), address, agentName, dateStr,
                    r.getStatus().getDisplayName()
            });
        }
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font(StyleConstants.FONT_FAMILY, Font.BOLD, 11));
            label.setOpaque(true);

            String status = value != null ? value.toString() : "";
            Color bg;
            switch (status) {
                case "대기": bg = StyleConstants.TEXT_SECONDARY; break;
                case "확정": bg = StyleConstants.PRIMARY; break;
                case "거절": bg = StyleConstants.DANGER; break;
                case "방문 완료": bg = StyleConstants.SUCCESS; break;
                case "후기 완료": bg = StyleConstants.SECONDARY; break;
                default: bg = StyleConstants.TEXT_SECONDARY;
            }

            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(bg);
            } else {
                label.setBackground(row % 2 == 0 ? StyleConstants.TABLE_ROW_ODD : StyleConstants.TABLE_ROW_EVEN);
                label.setForeground(bg);
            }
            return label;
        }
    }
}
