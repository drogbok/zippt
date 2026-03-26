package com.zippt.ui.seller;

import com.zippt.enums.PropertyType;
import com.zippt.model.Property;
import com.zippt.model.User;
import com.zippt.service.PropertyService;
import com.zippt.ui.buyer.BuyerMainPanel;
import com.zippt.ui.common.ComponentFactory;
import com.zippt.ui.common.StyleConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PropertyManagePanel extends JPanel implements BuyerMainPanel.Refreshable {

    private final User currentUser;
    private final PropertyService propertyService;
    private JTable table;
    private DefaultTableModel tableModel;

    public PropertyManagePanel(User currentUser, PropertyService propertyService) {
        this.currentUser = currentUser;
        this.propertyService = propertyService;
        setLayout(new BorderLayout(0, 12));
        setBackground(StyleConstants.BACKGROUND);
        initUI();
    }

    private void initUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(StyleConstants.BACKGROUND);
        header.add(ComponentFactory.createTitleLabel("매물 관리"), BorderLayout.WEST);
        JButton addBtn = ComponentFactory.createPrimaryButton("+ 매물 등록");
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "주소", "지역", "면적(㎡)", "가격", "유형", "등록일"};
        table = ComponentFactory.createStyledTable(columns);
        tableModel = (DefaultTableModel) table.getModel();
        add(ComponentFactory.wrapInScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(StyleConstants.BACKGROUND);
        JButton editBtn = ComponentFactory.createOutlineButton("수정");
        JButton deleteBtn = ComponentFactory.createDangerButton("삭제");
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showPropertyDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "매물을 선택해주세요."); return; }
            long id = (long) tableModel.getValueAt(row, 0);
            propertyService.findById(id).ifPresent(p -> showPropertyDialog(p));
        });
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "매물을 선택해주세요."); return; }
            long id = (long) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?",
                    "삭제 확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    propertyService.delete(id, currentUser.getId());
                    refresh();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        refresh();
    }

    private void showPropertyDialog(Property existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                isEdit ? "매물 수정" : "매물 등록", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(440, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(StyleConstants.SURFACE);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField addressField = ComponentFactory.createTextField();
        JTextField districtField = ComponentFactory.createTextField();
        JTextField areaField = ComponentFactory.createTextField();
        JTextField priceField = ComponentFactory.createTextField();
        String[] types = {"아파트", "빌라", "오피스텔", "단독주택", "상가"};
        JComboBox<String> typeCombo = ComponentFactory.createComboBox(types);
        JTextArea descArea = ComponentFactory.createTextArea(3);

        if (isEdit) {
            addressField.setText(existing.getAddress());
            districtField.setText(existing.getDistrict());
            areaField.setText(String.valueOf(existing.getAreaSqm()));
            priceField.setText(String.valueOf(existing.getPriceInWan()));
            for (int i = 0; i < types.length; i++) {
                if (types[i].equals(existing.getPropertyType().getDisplayName())) {
                    typeCombo.setSelectedIndex(i); break;
                }
            }
            descArea.setText(existing.getDescription());
        }

        String[][] labels = {{"주소", ""}, {"지역", ""}, {"면적(㎡)", ""}, {"가격(만원)", ""}, {"유형", ""}, {"설명", ""}};
        JComponent[] fields = {addressField, districtField, areaField, priceField, typeCombo,
                new JScrollPane(descArea)};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            gbc.weightx = 0;
            panel.add(ComponentFactory.createLabel(labels[i][0]), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            if (fields[i] instanceof JScrollPane) {
                gbc.fill = GridBagConstraints.BOTH;
                gbc.weighty = 1;
            }
            panel.add(fields[i], gbc);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0;
        }

        JButton saveBtn = ComponentFactory.createPrimaryButton(isEdit ? "수정" : "등록");
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 6, 6, 6);
        panel.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                String address = addressField.getText().trim();
                String district = districtField.getText().trim();
                double area = Double.parseDouble(areaField.getText().trim());
                long price = Long.parseLong(priceField.getText().trim());
                PropertyType type = PropertyType.values()[typeCombo.getSelectedIndex()];
                String desc = descArea.getText().trim();

                if (isEdit) {
                    propertyService.update(existing.getId(), currentUser.getId(),
                            address, district, area, price, type, desc);
                } else {
                    propertyService.register(currentUser.getId(),
                            address, district, area, price, type, desc);
                }
                dialog.dispose();
                refresh();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "면적과 가격은 숫자로 입력해주세요.",
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
        List<Property> list = propertyService.findBySellerId(currentUser.getId());
        for (Property p : list) {
            tableModel.addRow(new Object[]{
                    p.getId(), p.getAddress(), p.getDistrict(),
                    String.format("%.1f", p.getAreaSqm()),
                    p.formatPrice(),
                    p.getPropertyType().getDisplayName(),
                    p.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            });
        }
    }
}
