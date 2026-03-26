package com.zippt.ui.buyer;

import com.zippt.enums.Role;
import com.zippt.model.Property;
import com.zippt.model.User;
import com.zippt.service.ReservationService;
import com.zippt.service.UserService;
import com.zippt.ui.common.ComponentFactory;
import com.zippt.ui.common.StyleConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class PropertyDetailDialog extends JDialog {

    public PropertyDetailDialog(Window owner, Property property, User currentUser,
                                UserService userService, ReservationService reservationService) {
        super(owner, "매물 상세", ModalityType.APPLICATION_MODAL);
        setSize(520, 560);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(StyleConstants.SURFACE);
        main.setBorder(new EmptyBorder(24, 30, 24, 30));

        JLabel title = ComponentFactory.createTitleLabel("매물 #" + property.getId());
        main.add(title, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(StyleConstants.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 12);
        gbc.anchor = GridBagConstraints.WEST;

        String sellerName = userService.findById(property.getSellerId())
                .map(User::getName).orElse("알 수 없음");

        String[][] rows = {
                {"주소", property.getAddress()},
                {"지역", property.getDistrict()},
                {"면적", String.format("%.1f ㎡", property.getAreaSqm())},
                {"가격", property.formatPrice()},
                {"유형", property.getPropertyType().getDisplayName()},
                {"설명", property.getDescription()},
                {"등록일", property.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))},
                {"매도자", sellerName}
        };

        for (int i = 0; i < rows.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            JLabel lbl = new JLabel(rows[i][0]);
            lbl.setFont(StyleConstants.BODY_BOLD_FONT);
            lbl.setForeground(StyleConstants.TEXT_SECONDARY);
            infoPanel.add(lbl, gbc);

            gbc.gridx = 1;
            JLabel val = new JLabel(rows[i][1]);
            val.setFont(StyleConstants.BODY_FONT);
            infoPanel.add(val, gbc);
        }

        main.add(infoPanel, BorderLayout.CENTER);

        JPanel reservePanel = new JPanel(new GridBagLayout());
        reservePanel.setBackground(new Color(0xF0F9FF));
        reservePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.BORDER),
                new EmptyBorder(16, 16, 16, 16)));

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel reserveTitle = new JLabel("방문 예약 신청");
        reserveTitle.setFont(StyleConstants.BODY_BOLD_FONT);
        reserveTitle.setForeground(StyleConstants.PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        reservePanel.add(reserveTitle, gbc);
        gbc.gridwidth = 1;

        List<User> agents = userService.findByRole(Role.AGENT).stream()
                .filter(a -> a.getRegion() != null && a.getRegion().equals(property.getDistrict()))
                .collect(Collectors.toList());

        if (agents.isEmpty()) {
            agents = userService.findByRole(Role.AGENT);
        }

        String[] agentNames = agents.stream()
                .map(a -> a.getId() + " - " + a.getName() + " (" + (a.getRegion() != null ? a.getRegion() : "") + ")")
                .toArray(String[]::new);
        JComboBox<String> agentCombo = ComponentFactory.createComboBox(agentNames);

        gbc.gridx = 0; gbc.gridy = 1;
        reservePanel.add(ComponentFactory.createLabel("중개사"), gbc);
        gbc.gridx = 1;
        reservePanel.add(agentCombo, gbc);

        JTextField dateField = ComponentFactory.createTextField();
        dateField.setText(LocalDateTime.now().plusDays(1).withHour(14).withMinute(0)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        gbc.gridx = 0; gbc.gridy = 2;
        reservePanel.add(ComponentFactory.createLabel("예약 일시"), gbc);
        gbc.gridx = 1;
        reservePanel.add(dateField, gbc);

        JLabel hint = new JLabel("형식: yyyy-MM-dd HH:mm");
        hint.setFont(StyleConstants.SMALL_FONT);
        hint.setForeground(StyleConstants.TEXT_SECONDARY);
        gbc.gridx = 1; gbc.gridy = 3;
        reservePanel.add(hint, gbc);

        JButton reserveBtn = ComponentFactory.createPrimaryButton("예약 신청");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 4, 4, 8);
        reservePanel.add(reserveBtn, gbc);

        List<User> finalAgents = agents;
        reserveBtn.addActionListener(e -> {
            if (agentCombo.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "선택 가능한 중개사가 없습니다.");
                return;
            }
            String selected = (String) agentCombo.getSelectedItem();
            long agentId = Long.parseLong(selected.split(" - ")[0].trim());

            try {
                LocalDateTime dt = LocalDateTime.parse(dateField.getText().trim(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                reservationService.create(currentUser.getId(), agentId, property.getId(), dt);
                JOptionPane.showMessageDialog(this, "예약이 신청되었습니다!", "성공",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd HH:mm)",
                        "오류", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        main.add(reservePanel, BorderLayout.SOUTH);
        setContentPane(main);
    }
}
