package ma.WhiteLab.mvc.controllers.modules.agenda.impl;

import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.mvc.controllers.modules.agenda.api.AgendaController;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.otherPages.AgendaMensuelPanel;
import ma.WhiteLab.service.modules.agendas.api.AgendaMensuelService;
import ma.WhiteLab.service.modules.agendas.api.RendezVousService;
import ma.WhiteLab.service.modules.agendas.dto.AgendaMensuelDTO;
import ma.WhiteLab.service.modules.patient.api.PatientService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la gestion des agendas mensuels
 */
public class AgendaControllerImpl implements AgendaController {

    private final AgendaMensuelService agendaMensuelService;
    private final RendezVousService rendezVousService;
    private final PatientService patientService;

    private AgendaMensuelPanel currentPanel;

    public AgendaControllerImpl(
            AgendaMensuelService agendaMensuelService,
            RendezVousService rendezVousService,
            PatientService patientService) {

        if (agendaMensuelService == null || rendezVousService == null || patientService == null) {
            throw new IllegalArgumentException("Tous les services (AgendaMensuel, RendezVous, Patient) sont obligatoires");
        }

        this.agendaMensuelService = agendaMensuelService;
        this.rendezVousService = rendezVousService;
        this.patientService = patientService;
    }

    @Override
    public JPanel getView(UserPrincipal principal) {
        if (principal == null) {
            return createErrorPanel("Utilisateur non authentifié");
        }

        RoleR rolePrincipal = principal.rolePrincipal();
        Long restrictedMedecinId = null;
        List<UserPrincipal> availableMedecins = List.of();

        // Médecin → voit uniquement son agenda
        if (rolePrincipal == RoleR.MEDECIN) {
            restrictedMedecinId = principal.id();
        }
        // Secrétaire → voit la liste des médecins
        else if (rolePrincipal == RoleR.SECRETAIRE) {
            availableMedecins = agendaMensuelService.getAllMedecins(); // liste de tous les médecins
            if (!availableMedecins.isEmpty()) {
                restrictedMedecinId = availableMedecins.get(0).id(); // par défaut le 1er médecin
            }
        }
        // Accès refusé si ce n'est ni médecin ni secrétaire
        else {
            return createErrorPanel("Accès réservé aux médecins et secrétaires");
        }

        // Crée le panneau
        currentPanel = new AgendaMensuelPanel(
                agendaMensuelService,
                rendezVousService,
                patientService,
                restrictedMedecinId, // id du médecin connecté
                this::onAgendaChanged,
                principal
        );

        currentPanel.loadData();
        return currentPanel;
    }

    private void onAgendaChanged(AgendaMensuelDTO updatedDto) {
        SwingUtilities.invokeLater(() -> {
            if (currentPanel != null) {
                currentPanel.refreshData();
                JOptionPane.showMessageDialog(
                        currentPanel,
                        "Agenda mis à jour avec succès !",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
    }

    @Override
    public List<AgendaMensuelDTO> getVisibleAgendas(UserPrincipal principal) {
        if (principal == null) return List.of();

        RoleR role = principal.rolePrincipal();

        if (role == RoleR.MEDECIN) {
            return agendaMensuelService.getAgendasMensuelsByMedecinId(principal.id());
        }

        if (role == RoleR.SECRETAIRE) {
            return agendaMensuelService.getAllAgendasMensuels(); // voir tous les agendas
        }

        return List.of();
    }

    @Override
    public void refreshCurrentView() {
        if (currentPanel != null) {
            currentPanel.refreshData();
        }
    }

    private JPanel createErrorPanel(String message) {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(80, 50, 80, 50));
        panel.setBackground(new Color(255, 245, 245));

        JLabel lbl = new JLabel(message, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setForeground(new Color(200, 40, 40));
        panel.add(lbl, BorderLayout.CENTER);

        return panel;
    }

    public List<LocalDate> getDaysWithAppointmentsForMonth(Long medecinId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return rendezVousService.getDaysWithAppointments(medecinId, start, end);
    }

    public Map<LocalDate, Long> getRdvCountPerDayForMonth(Long medecinId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return rendezVousService.getAppointmentCountPerDay(medecinId, start, end);
    }

    public AgendaMensuelPanel getCurrentPanel() {
        return currentPanel;
    }
}
