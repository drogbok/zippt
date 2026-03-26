package com.zippt.ui.buyer;

import com.zippt.enums.PropertyType;
import com.zippt.model.Property;
import com.zippt.model.User;
import com.zippt.service.PropertyService;
import com.zippt.service.ReservationService;
import com.zippt.service.UserService;
import com.zippt.ui.common.ComponentFactory;
import com.zippt.ui.common.StyleConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PropertySearchPanel extends JPanel implements BuyerMainPanel.Refreshable {

    private final User currentUser;
    private final UserService userService;
    private final PropertyService propertyService;
    private final ReservationService reservationService;

    private JTextField districtField;
    private JTextField minPriceField;
    private JTextField maxPriceField;
    private JTextField minAreaField;
    private JTextField maxAreaField;
    private JComboBox<String> typeCombo;
    private JTable resultTable;
    private DefaultTableModel tableModel;

    public PropertySearchPanel(User currentUser, UserService userService,
                               PropertyService propertyService, ReservationService reservationService) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.propertyService = propertyService;
        this.reservationService = reservationService;
        setLayout(new BorderLayout(0, 16));
        setBackground(StyleConstants.BACKGROUND);
        initUI();
        refresh();
    }

    private void initUI() {
        JLabel title = ComponentFactory.createTitleLabel("매물 검색");
        add(title, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterPanel.setBackground(StyleConstants.SURFACE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.BORDER),
                new EmptyBorder(12, 16, 12, 16)));

        filterPanel.add(ComponentFactory.createLabel("지역"));
        districtField = ComponentFactory.createTextField();
        districtField.setPreferredSize(new Dimension(100, 30));
        filterPanel.add(districtField);

        filterPanel.add(ComponentFactory.createLabel("가격(만원)"));
        minPriceField = ComponentFactory.createTextField();
        minPriceField.setPreferredSize(new Dimension(80, 30));
        filterPanel.add(minPriceField);
        filterPanel.add(new JLabel("~"));
        maxPriceField = ComponentFactory.createTextField();
        maxPriceField.setPreferredSize(new Dimension(80, 30));
        filterPanel.add(maxPriceField);

        filterPanel.add(ComponentFactory.createLabel("면적(㎡)"));
        minAreaField = ComponentFactory.createTextField();
        minAreaField.setPreferredSize(new Dimension(60, 30));
        filterPanel.add(minAreaField);
        filterPanel.add(new JLabel("~"));
        maxAreaField = ComponentFactory.createTextField();
        maxAreaField.setPreferredSize(new Dimension(60, 30));
        filterPanel.add(maxAreaField);

        filterPanel.add(ComponentFactory.createLabel("유형"));
        String[] types = new String[]{"전체", "아파트", "빌라", "오피스텔", "단독주택", "상가"};
        typeCombo = ComponentFactory.createComboBox(types);
        typeCombo.setPreferredSize(new Dimension(100, 30));
        filterPanel.add(typeCombo);

        JButton searchBtn = ComponentFactory.createPrimaryButton("검색");
        filterPanel.add(searchBtn);
        JButton resetBtn = ComponentFactory.createOutlineButton("초기화");
        filterPanel.add(resetBtn);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(StyleConstants.BACKGROUND);
        centerPanel.add(filterPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "주소", "지역", "면적(㎡)", "가격", "유형"};
        resultTable = ComponentFactory.createStyledTable(columns);
        tableModel = (DefaultTableModel) resultTable.getModel();
        centerPanel.add(ComponentFactory.wrapInScrollPane(resultTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(StyleConstants.BACKGROUND);
        JButton detailBtn = ComponentFactory.createOutlineButton("상세 보기");
        btnPanel.add(detailBtn);
        centerPanel.add(btnPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        searchBtn.addActionListener(e -> doSearch());
        resetBtn.addActionListener(e -> {
            districtField.setText("");
            minPriceField.setText("");
            maxPriceField.setText("");
            minAreaField.setText("");
            maxAreaField.setText("");
            typeCombo.setSelectedIndex(0);
            refresh();
        });

        detailBtn.addActionListener(e -> showDetail());
        resultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) showDetail();
            }
        });
    }

    private void doSearch() {
        String district = districtField.getText().trim();
        Long minPrice = parseLong(minPriceField.getText().trim());
        Long maxPrice = parseLong(maxPriceField.getText().trim());
        Double minArea = parseDouble(minAreaField.getText().trim());
        Double maxArea = parseDouble(maxAreaField.getText().trim());
        PropertyType type = parseType((String) typeCombo.getSelectedItem());

        List<Property> results = propertyService.search(
                district.isEmpty() ? null : district,
                minPrice, maxPrice, minArea, maxArea, type);
        populateTable(results);
    }

    private void populateTable(List<Property> properties) {
        tableModel.setRowCount(0);
        for (Property p : properties) {
            tableModel.addRow(new Object[]{
                    p.getId(), p.getAddress(), p.getDistrict(),
                    String.format("%.1f", p.getAreaSqm()),
                    p.formatPrice(),
                    p.getPropertyType().getDisplayName()
            });
        }
    }

    private void showDetail() {
        int row = resultTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "매물을 선택해주세요.");
            return;
        }
        long propertyId = (long) tableModel.getValueAt(row, 0);
        propertyService.findById(propertyId).ifPresent(p -> {
            PropertyDetailDialog dialog = new PropertyDetailDialog(
                    SwingUtilities.getWindowAncestor(this),
                    p, currentUser, userService, reservationService);
            dialog.setVisible(true);
        });
    }

    @Override
    public void refresh() {
        populateTable(propertyService.findAll());
    }

    private Long parseLong(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    private Double parseDouble(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
    }

    private PropertyType parseType(String s) {
        if (s == null || "전체".equals(s)) return null;
        for (PropertyType t : PropertyType.values()) {
            if (t.getDisplayName().equals(s)) return t;
        }
        return null;
    }
}
