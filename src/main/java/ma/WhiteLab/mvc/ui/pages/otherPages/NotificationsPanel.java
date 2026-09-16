package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.mvc.controllers.modules.notifications.api.NotificationsController;
import ma.WhiteLab.service.modules.notifications.api.NotificationService;
import ma.WhiteLab.service.modules.notifications.dto.NotificationDTO;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationsPanel extends JPanel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    // Palette
    private static final Color BG_COLOR = new Color(242, 245, 248);
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color TEXT_MAIN = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT = new Color(127, 140, 141);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color HOVER_COLOR = new Color(248, 250, 252);
    private static final Color DELETE_COLOR = new Color(231, 76, 60);
    private static final Color UNREAD_BG = new Color(250, 252, 255);

    private final UserPrincipal principal;
    private final NotificationsController controller;
    private final JPanel listContainer;

    public NotificationsPanel(NotificationsController controller, UserPrincipal principal) {
        this.principal = principal;
        this.controller = controller;


        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(218, 226, 234)),
                new EmptyBorder(20, 30, 20, 30)
        ));

        JLabel titleLabel = new JLabel("Mes Notifications", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_MAIN);
        header.add(titleLabel, BorderLayout.CENTER);

        JButton refreshBtn = createStyledButton("Actualiser", PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> loadContent());
        header.add(refreshBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Main content
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(BG_COLOR);
        listContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);

        add(scrollPane, BorderLayout.CENTER);

        // Initial load
        loadContent();
    }

    private void loadContent() {
        listContainer.removeAll();

        try {
            NotificationService service = ApplicationContext.getInstance().getBean(NotificationService.class);
            // Always get notifications for the current user
            List<NotificationDTO> notifications = service.getNotificationsForUser(principal.id());

            if (notifications.isEmpty()) {
                listContainer.add(createEmptyState());
            } else {
                for (NotificationDTO notif : notifications) {
                    listContainer.add(createNotificationCard(notif));
                    listContainer.add(Box.createVerticalStrut(15));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            listContainer.add(createErrorState(e.getMessage()));
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createNotificationCard(NotificationDTO notif) {
        // This assumes NotificationDTO has a isRead() method. 
        boolean isRead = notif.isRead();

        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(isRead ? CARD_BG : UNREAD_BG);
        card.setMaximumSize(new Dimension(1000, 100));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isRead ? new Color(230, 236, 241) : PRIMARY_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Priority bar
        JPanel priorityBar = new JPanel();
        priorityBar.setPreferredSize(new Dimension(6, 0));
        priorityBar.setBackground(getPriorityColor(notif.getPriorite()));
        card.add(priorityBar, BorderLayout.WEST);

        // Center content
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        centerPanel.setOpaque(false);

        String titlePrefix = isRead ? "" : "🔵 ";
        JLabel lblTitle = new JLabel("<html><b>" + titlePrefix + escapeHtml(notif.getTitre() != null ? notif.getTitre() : "Notification") + "</b></html>");
        lblTitle.setFont(new Font("Segoe UI", isRead ? Font.PLAIN : Font.BOLD, 14));
        lblTitle.setForeground(TEXT_MAIN);

        JLabel lblMessage = new JLabel("<html>" + escapeHtml(notif.getMessage() != null ? notif.getMessage() : "") + "</html>");
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMessage.setForeground(isRead ? TEXT_LIGHT : TEXT_MAIN);

        centerPanel.add(lblTitle);
        centerPanel.add(lblMessage);
        card.add(centerPanel, BorderLayout.CENTER);

        // Right side: Date and Delete button
        JPanel rightPanel = new JPanel(new BorderLayout(0, 5));
        rightPanel.setOpaque(false);

        String dateStr = "Inconnu";
        if (notif.getDate() != null) {
            try {
                dateStr = notif.getDate().format(DATE_FORMAT);
                if (notif.getTime() != null) {
                    dateStr += " à " + notif.getTime().format(TIME_FORMAT);
                }
            } catch (Exception ignored) {
                dateStr = notif.getDate().toString();
            }
        }

        JLabel lblDate = new JLabel(dateStr);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(TEXT_LIGHT);
        lblDate.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPanel.add(lblDate, BorderLayout.NORTH);

        // Delete button
        JButton deleteBtn = createStyledButton("Supprimer", DELETE_COLOR);
        deleteBtn.addActionListener(e -> deleteNotificationAction(notif));
        JPanel deleteButtonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        deleteButtonContainer.setOpaque(false);
        deleteButtonContainer.add(deleteBtn);
        rightPanel.add(deleteButtonContainer, BorderLayout.SOUTH);

        card.add(rightPanel, BorderLayout.EAST);

        // Hover and click to mark as read
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.setBackground(HOVER_COLOR);
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBackground(isRead ? CARD_BG : UNREAD_BG);
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Make sure the click was not on the delete button
                    Component source = (Component) e.getSource();
                    Point pt = e.getPoint();
                    Component clickedComponent = SwingUtilities.getDeepestComponentAt(source, pt.x, pt.y);
                    if (!(clickedComponent instanceof JButton)) {
                         if (!isRead) {
                            markAsReadAction(notif);
                        }
                    }
                }
            }
        });

        return card;
    }

    private void markAsReadAction(NotificationDTO notif) {
        try {
            NotificationService service = ApplicationContext.getInstance().getBean(NotificationService.class);
            service.markAsRead(principal.id(), notif.getId());
            loadContent();
        } catch (Exception ex) {
            System.err.println("Erreur marquage comme lu : " + ex.getMessage());
        }
    }
    
    private void deleteNotificationAction(NotificationDTO notif) {
        int response = JOptionPane.showConfirmDialog(
                this,
                "Êtes-vous sûr de vouloir supprimer cette notification ?\n" +
                        "Titre : " + notif.getTitre(),
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            controller.deleteNotification(notif.getId(), principal);
            // Refresh the list after deletion
            loadContent();
        }
    }

    private Color getPriorityColor(String priority) {
        if (priority == null) return TEXT_LIGHT;
        return switch (priority.toUpperCase()) {
            case "URGENT", "HIGH", "HAUTE" -> new Color(231, 76, 60);
            case "MOYEN", "MEDIUM" -> new Color(241, 196, 15);
            case "BAS", "LOW" -> new Color(46, 204, 113);
            default -> PRIMARY_COLOR;
        };
    }

    private JButton createStyledButton(String text, Color fg) {
        JButton btn = new JButton(text);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Component createEmptyState() {
        JPanel emptyPanel = new JPanel(new BorderLayout(0, 20));
        emptyPanel.setBackground(BG_COLOR);
        emptyPanel.setBorder(new EmptyBorder(120, 40, 120, 40));

        JLabel iconLabel = new JLabel("🔔", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 72));
        iconLabel.setForeground(TEXT_LIGHT);
        emptyPanel.add(iconLabel, BorderLayout.NORTH);

        JLabel mainText = new JLabel("Aucune notification pour le moment", SwingConstants.CENTER);
        mainText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainText.setForeground(TEXT_MAIN);
        emptyPanel.add(mainText, BorderLayout.CENTER);

        JLabel subText = new JLabel(
                "<html><div style='text-align:center; color:#7f8c8d; font-size:13px;'>" +
                        "Vous serez averti dès qu'il y aura du nouveau.<br>" +
                        "Restez connecté !" +
                        "</div></html>",
                SwingConstants.CENTER
        );
        subText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emptyPanel.add(subText, BorderLayout.SOUTH);

        return emptyPanel;
    }

    private Component createErrorState(String msg) {
        JPanel errorPanel = new JPanel(new BorderLayout(10, 10));
        errorPanel.setBackground(BG_COLOR);
        errorPanel.setBorder(new EmptyBorder(100, 20, 100, 20));

        JLabel iconLabel = new JLabel("⚠", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        iconLabel.setForeground(DELETE_COLOR);
        errorPanel.add(iconLabel, BorderLayout.NORTH);

        JLabel errorLabel = new JLabel(
                "<html><div style='text-align:center; color:#c0392b; font-size:14px;'>" +
                        "Erreur lors du chargement des notifications<br>" +
                        "<b>" + escapeHtml(msg) + "</b><br>" +
                        "<small>Veuillez réessayer ou contacter l'administrateur</small>" +
                        "</div></html>",
                SwingConstants.CENTER
        );
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        errorPanel.add(errorLabel, BorderLayout.CENTER);

        return errorPanel;
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}