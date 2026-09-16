package ma.WhiteLab.mvc.ui.pages.panels.Agenda;

import ma.WhiteLab.entities.enums.Jour;
import ma.WhiteLab.service.modules.agendas.api.AgendaMensuelService;
import ma.WhiteLab.service.modules.agendas.dto.AgendaMensuelDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AgendaEditDialog extends JDialog {

    private final AgendaMensuelService agendaService;
    private final AgendaMensuelDTO agendaDTO;
    private final Consumer<AgendaMensuelDTO> onUpdateCallback;
    private final List<JCheckBox> dayCheckBoxes = new ArrayList<>();

    public AgendaEditDialog(Frame owner, AgendaMensuelDTO dto, AgendaMensuelService service, Consumer<AgendaMensuelDTO> callback) {
        super(owner, "Modifier l'Agenda Mensuel", true);
        this.agendaDTO = dto;
        this.agendaService = service;
        this.onUpdateCallback = callback;

        setSize(450, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        getRootPane().setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
    }

    private void initUI() {
        // Title
        JLabel titleLabel = new JLabel("Définir les jours non travaillés", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(64, 120, 255));
        add(titleLabel, BorderLayout.NORTH);

        // Checkboxes for days of the week
        JPanel checkPanel = new JPanel(new GridLayout(7, 1, 5, 5));
        checkPanel.setBackground(Color.WHITE);
        checkPanel.setBorder(new EmptyBorder(15, 40, 15, 40));

        Set<String> nonAvailableDays = Set.copyOf(agendaDTO.getJoursNonDisponible());
        for (Jour day : Jour.values()) {
            JCheckBox checkBox = new JCheckBox(day.getDisplayName());
            checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            checkBox.setBackground(Color.WHITE);
            checkBox.setSelected(nonAvailableDays.contains(day.name()));
            dayCheckBoxes.add(checkBox);
            checkPanel.add(checkBox);
        }
        add(checkPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = new JButton("Annuler");
        styleButton(cancelButton, new Color(108, 117, 125));
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new JButton("Sauvegarder");
        styleButton(saveButton, new Color(40, 167, 69));
        saveButton.addActionListener(e -> saveChanges());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void saveChanges() {
        List<String> selectedDays = dayCheckBoxes.stream()
                .filter(JCheckBox::isSelected)
                .map(cb -> {
                    for (Jour day : Jour.values()) {
                        if (day.getDisplayName().equals(cb.getText())) {
                            return day.name();
                        }
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        agendaDTO.setJoursNonDisponible(selectedDays);

        try {
            AgendaMensuelDTO resultingDto;
            // If ID is null, it's a new agenda, so we create it.
            if (agendaDTO.getId() == null) {
                resultingDto = agendaService.createAgendaMensuel(agendaDTO);
            } else {
                // Otherwise, we update the existing one.
                resultingDto = agendaService.updateAgendaMensuel(agendaDTO.getId(), agendaDTO);
            }

            if (onUpdateCallback != null) {
                onUpdateCallback.accept(resultingDto);
            }
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la sauvegarde : " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 25, 10, 25));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
    }
}
