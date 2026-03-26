package com.zippt.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class ComponentFactory {

    private ComponentFactory() {}

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, StyleConstants.PRIMARY, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, StyleConstants.SECONDARY, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createStyledButton(text, StyleConstants.DANGER, Color.WHITE);
    }

    public static JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(StyleConstants.BUTTON_FONT);
        btn.setForeground(StyleConstants.PRIMARY);
        btn.setBackground(StyleConstants.SURFACE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.PRIMARY, 1),
                new EmptyBorder(8, 20, 8, 20)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0xEFF6FF));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(StyleConstants.SURFACE);
            }
        });
        return btn;
    }

    private static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(StyleConstants.BUTTON_FONT);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Color hover = bg.darker();
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    public static JTable createStyledTable(String[] columnNames) {
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? StyleConstants.TABLE_ROW_ODD : StyleConstants.TABLE_ROW_EVEN);
                }
                return c;
            }
        };

        table.setFont(StyleConstants.BODY_FONT);
        table.setRowHeight(32);
        table.setSelectionBackground(new Color(0xDBEAFE));
        table.setSelectionForeground(StyleConstants.TEXT_PRIMARY);
        table.setGridColor(StyleConstants.BORDER);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setFont(StyleConstants.BODY_BOLD_FONT);
        header.setBackground(StyleConstants.TABLE_HEADER_BG);
        header.setForeground(StyleConstants.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, StyleConstants.BORDER));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        return table;
    }

    public static JScrollPane wrapInScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(StyleConstants.BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(StyleConstants.BODY_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.BORDER),
                new EmptyBorder(6, 10, 6, 10)));
        field.setPreferredSize(new Dimension(200, 34));
        return field;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(StyleConstants.BODY_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleConstants.BORDER),
                new EmptyBorder(6, 10, 6, 10)));
        field.setPreferredSize(new Dimension(200, 34));
        return field;
    }

    public static JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(StyleConstants.BODY_FONT);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(200, 34));
        return combo;
    }

    public static JTextArea createTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 30);
        area.setFont(StyleConstants.BODY_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(6, 10, 6, 10));
        return area;
    }

    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(StyleConstants.BODY_FONT);
        label.setForeground(StyleConstants.TEXT_PRIMARY);
        return label;
    }

    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(StyleConstants.SUBTITLE_FONT);
        label.setForeground(StyleConstants.TEXT_PRIMARY);
        return label;
    }

    public static JPanel createSidebar(String title, String subtitle) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(StyleConstants.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(StyleConstants.SIDEBAR_WIDTH, 0));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(StyleConstants.FONT_FAMILY, Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel subtitleLabel = new JLabel(subtitle, SwingConstants.CENTER);
        subtitleLabel.setFont(StyleConstants.SMALL_FONT);
        subtitleLabel.setForeground(StyleConstants.SIDEBAR_TEXT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        sidebar.add(titleLabel);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(subtitleLabel);
        sidebar.add(Box.createVerticalStrut(30));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x334155));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(10));

        return sidebar;
    }

    public static JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(StyleConstants.SIDEBAR_FONT);
        btn.setForeground(StyleConstants.SIDEBAR_TEXT);
        btn.setBackground(StyleConstants.SIDEBAR_BG);
        btn.setBorder(new EmptyBorder(12, 24, 12, 24));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorderPainted(false);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!Boolean.TRUE.equals(btn.getClientProperty("active"))) {
                    btn.setBackground(StyleConstants.SIDEBAR_HOVER);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!Boolean.TRUE.equals(btn.getClientProperty("active"))) {
                    btn.setBackground(StyleConstants.SIDEBAR_BG);
                }
            }
        });
        return btn;
    }

    public static void setSidebarActive(JButton btn, boolean active) {
        btn.putClientProperty("active", active);
        if (active) {
            btn.setBackground(StyleConstants.SIDEBAR_ACTIVE);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(StyleConstants.SIDEBAR_BG);
            btn.setForeground(StyleConstants.SIDEBAR_TEXT);
        }
    }

    public static JLabel createStatusBadge(String text, Color bg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(new Font(StyleConstants.FONT_FAMILY, Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setBorder(new EmptyBorder(2, 8, 2, 8));
        return badge;
    }

    public static void showToast(JPanel parent, String message, boolean success) {
        JLabel toast = new JLabel(message, SwingConstants.CENTER);
        toast.setFont(StyleConstants.BODY_BOLD_FONT);
        toast.setForeground(Color.WHITE);
        toast.setOpaque(true);
        toast.setBackground(success ? StyleConstants.SUCCESS : StyleConstants.DANGER);
        toast.setBorder(new EmptyBorder(8, 20, 8, 20));

        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        dialog.add(toast);
        dialog.pack();

        Window window = SwingUtilities.getWindowAncestor(parent);
        if (window != null) {
            int x = window.getX() + (window.getWidth() - dialog.getWidth()) / 2;
            int y = window.getY() + 60;
            dialog.setLocation(x, y);
        }
        dialog.setVisible(true);

        Timer timer = new Timer(2500, e -> dialog.dispose());
        timer.setRepeats(false);
        timer.start();
    }
}
