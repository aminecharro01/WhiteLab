package ma.WhiteLab.mvc.ui.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.net.URL;

/**
 * A factory class for creating consistently styled Swing components for the application.
 * This ensures a modern and professional look and feel across all UI panels.
 *
 * @author Gemini
 * @version 1.0
 */
public final class UIFactory {

    // --- Colors ---
    public static final Color PRIMARY_COLOR = new Color(64, 120, 255);
    public static final Color PRIMARY_DARKER = new Color(50, 95, 204);
    public static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    public static final Color DANGER_COLOR = new Color(220, 53, 69);
    public static final Color WARNING_COLOR = new Color(255, 193, 7);
    public static final Color LIGHT_GRAY_COLOR = new Color(245, 247, 250);
    public static final Color BORDER_COLOR = new Color(220, 223, 230);
    public static final Color TEXT_COLOR = new Color(70, 70, 70);
    public static final Color LABEL_COLOR = new Color(100, 100, 100);

    // --- Fonts ---
    public static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    // --- Borders ---
    public static final Border COMPONENT_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
    );
    public static final Border SCROLL_PANE_BORDER = BorderFactory.createLineBorder(BORDER_COLOR);

    private UIFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a styled primary action button.
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        // A simple hover effect can be added with a MouseListener if desired
        return button;
    }

    /**
     * Creates a styled secondary action button (e.g., "Cancel").
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(new Color(230, 230, 230));
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    /**
     * Creates a styled text field.
     */
    public static JTextField createTextField(String placeholder) {
        JTextField textField = new JTextField();
        textField.setFont(MAIN_FONT);
        textField.setBorder(COMPONENT_BORDER);
        // Add placeholder text functionality
        addPlaceholder(textField, placeholder);
        return textField;
    }

     /**
     * Creates a styled text area, wrapped in a scroll pane.
     */
    public static JScrollPane createTextArea(JTextArea textArea, int rows) {
        textArea.setFont(MAIN_FONT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setRows(rows);
        textArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(COMPONENT_BORDER);
        return scrollPane;
    }

    /**
     * Styles a JTable with modern aesthetics (zebra striping, header style).
     */
    public static JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : LIGHT_GRAY_COLOR);
                }
                // Add padding to cells
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                }
                return c;
            }
        };
        table.setRowHeight(50);
        table.setFont(MAIN_FONT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(PRIMARY_COLOR.brighter());
        styleTableHeader(table.getTableHeader());
        return table;
    }

    /**
     * Applies a standard style to a JTable's header.
     */
    public static void styleTableHeader(JTableHeader header) {
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(new Color(238, 240, 245));
        header.setForeground(LABEL_COLOR);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    /**
     * Creates an icon-only button for use in tables or toolbars.
     */
    public static JButton createIconButton(String iconPath, String tooltip, int size) {
        JButton btn;
        URL url = UIFactory.class.getResource(iconPath);

        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            btn = new JButton(new ImageIcon(scaled));
        } else {
            // Fallback to text if icon not found
            String fallbackText = tooltip.substring(0, 1);
            btn = new JButton(fallbackText);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size / 2));
        }

        btn.setToolTipText(tooltip);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Adds placeholder text functionality to a JTextField.
     */
    public static void addPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(Color.GRAY);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_COLOR);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }
}
