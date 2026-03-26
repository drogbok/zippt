package com.zippt.ui;

import com.zippt.enums.Role;
import com.zippt.model.User;
import com.zippt.service.UserService;
import com.zippt.ui.common.ComponentFactory;
import com.zippt.ui.common.StyleConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {

    private final UserService userService;
    private final Consumer<User> onLoginSuccess;

    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JLabel loginErrorLabel;

    private JTextField regUsernameField;
    private JPasswordField regPasswordField;
    private JTextField regNameField;
    private JTextField regPhoneField;
    private JComboBox<String> regRoleCombo;
    private JTextField regRegionField;
    private JLabel regRegionLabel;
    private JLabel regMessageLabel;

    private JTabbedPane tabbedPane;

    public LoginPanel(UserService userService, Consumer<User> onLoginSuccess) {
        this.userService = userService;
        this.onLoginSuccess = onLoginSuccess;
        setLayout(new GridBagLayout());
        setBackground(StyleConstants.BACKGROUND);
        initUI();
    }

    private void initUI() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(StyleConstants.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.BORDER),
                new EmptyBorder(40, 50, 40, 50)));
        card.setMaximumSize(new Dimension(460, 600));

        JLabel logo = new JLabel("ZIP-PT", SwingConstants.CENTER);
        logo.setFont(StyleConstants.LOGO_FONT);
        logo.setForeground(StyleConstants.PRIMARY);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("부동산 매칭 플랫폼", SwingConstants.CENTER);
        subtitle.setFont(StyleConstants.BODY_FONT);
        subtitle.setForeground(StyleConstants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(logo);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(30));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(StyleConstants.BODY_BOLD_FONT);
        tabbedPane.addTab("로그인", createLoginTab());
        tabbedPane.addTab("회원가입", createRegisterTab());
        card.add(tabbedPane);

        add(card);
    }

    private JPanel createLoginTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(StyleConstants.SURFACE);
        panel.setBorder(new EmptyBorder(20, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(ComponentFactory.createLabel("아이디"), gbc);
        gbc.gridx = 1;
        loginUsernameField = ComponentFactory.createTextField();
        loginUsernameField.setPreferredSize(new Dimension(240, 34));
        panel.add(loginUsernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(ComponentFactory.createLabel("비밀번호"), gbc);
        gbc.gridx = 1;
        loginPasswordField = ComponentFactory.createPasswordField();
        loginPasswordField.setPreferredSize(new Dimension(240, 34));
        panel.add(loginPasswordField, gbc);

        loginErrorLabel = new JLabel(" ");
        loginErrorLabel.setFont(StyleConstants.SMALL_FONT);
        loginErrorLabel.setForeground(StyleConstants.DANGER);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(loginErrorLabel, gbc);

        JButton loginBtn = ComponentFactory.createPrimaryButton("로그인");
        loginBtn.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = 3;
        panel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> doLogin());
        loginPasswordField.addActionListener(e -> doLogin());

        return panel;
    }

    private JPanel createRegisterTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(StyleConstants.SURFACE);
        panel.setBorder(new EmptyBorder(20, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(ComponentFactory.createLabel("아이디"), gbc);
        gbc.gridx = 1;
        regUsernameField = ComponentFactory.createTextField();
        regUsernameField.setPreferredSize(new Dimension(240, 34));
        panel.add(regUsernameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(ComponentFactory.createLabel("비밀번호"), gbc);
        gbc.gridx = 1;
        regPasswordField = ComponentFactory.createPasswordField();
        regPasswordField.setPreferredSize(new Dimension(240, 34));
        panel.add(regPasswordField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(ComponentFactory.createLabel("이름"), gbc);
        gbc.gridx = 1;
        regNameField = ComponentFactory.createTextField();
        regNameField.setPreferredSize(new Dimension(240, 34));
        panel.add(regNameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(ComponentFactory.createLabel("전화번호"), gbc);
        gbc.gridx = 1;
        regPhoneField = ComponentFactory.createTextField();
        regPhoneField.setPreferredSize(new Dimension(240, 34));
        panel.add(regPhoneField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(ComponentFactory.createLabel("역할"), gbc);
        gbc.gridx = 1;
        regRoleCombo = ComponentFactory.createComboBox(new String[]{"매수자", "매도자", "중개사"});
        panel.add(regRoleCombo, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        regRegionLabel = ComponentFactory.createLabel("담당 지역");
        regRegionLabel.setVisible(false);
        panel.add(regRegionLabel, gbc);
        gbc.gridx = 1;
        regRegionField = ComponentFactory.createTextField();
        regRegionField.setPreferredSize(new Dimension(240, 34));
        regRegionField.setVisible(false);
        panel.add(regRegionField, gbc);

        regRoleCombo.addActionListener(e -> {
            boolean isAgent = "중개사".equals(regRoleCombo.getSelectedItem());
            regRegionLabel.setVisible(isAgent);
            regRegionField.setVisible(isAgent);
        });

        row++;
        regMessageLabel = new JLabel(" ");
        regMessageLabel.setFont(StyleConstants.SMALL_FONT);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(regMessageLabel, gbc);

        row++;
        JButton regBtn = ComponentFactory.createPrimaryButton("회원가입");
        regBtn.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = row;
        panel.add(regBtn, gbc);

        regBtn.addActionListener(e -> doRegister());

        return panel;
    }

    private void doLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            loginErrorLabel.setText("아이디와 비밀번호를 모두 입력해주세요.");
            return;
        }

        Optional<User> opt = userService.login(username, password);
        if (opt.isPresent()) {
            loginErrorLabel.setText(" ");
            loginUsernameField.setText("");
            loginPasswordField.setText("");
            onLoginSuccess.accept(opt.get());
        } else {
            loginErrorLabel.setText("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    private void doRegister() {
        String username = regUsernameField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String name = regNameField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String roleStr = (String) regRoleCombo.getSelectedItem();
        String region = regRegionField.getText().trim();

        Role role;
        switch (roleStr) {
            case "매도자": role = Role.SELLER; break;
            case "중개사": role = Role.AGENT; break;
            default: role = Role.BUYER;
        }

        try {
            userService.register(username, password, name, phone, role,
                    role == Role.AGENT ? region : null);
            regMessageLabel.setForeground(StyleConstants.SUCCESS);
            regMessageLabel.setText("회원가입 성공! 로그인 탭에서 로그인해주세요.");
            regUsernameField.setText("");
            regPasswordField.setText("");
            regNameField.setText("");
            regPhoneField.setText("");
            regRegionField.setText("");
            regRoleCombo.setSelectedIndex(0);

            Timer timer = new Timer(1500, e -> tabbedPane.setSelectedIndex(0));
            timer.setRepeats(false);
            timer.start();
        } catch (IllegalArgumentException ex) {
            regMessageLabel.setForeground(StyleConstants.DANGER);
            regMessageLabel.setText(ex.getMessage());
        }
    }
}
