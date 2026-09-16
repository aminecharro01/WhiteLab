package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.otherPages.parametrage.ActeManagementPanel;
import ma.WhiteLab.mvc.ui.pages.otherPages.parametrage.AntecedentManagementPanel;
import ma.WhiteLab.mvc.ui.pages.otherPages.parametrage.AssuranceListPanel;
import ma.WhiteLab.mvc.ui.pages.otherPages.parametrage.MedicamentManagementPanel;
import ma.WhiteLab.service.modules.dossierMedicale.api.ActeMedicalService;
import ma.WhiteLab.service.modules.dossierMedicale.api.MedicamentService;
import ma.WhiteLab.service.modules.patient.api.AntecedentService;
import ma.WhiteLab.service.modules.referentiel.api.ReferentielService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ParametragePanel extends JPanel {

    public ParametragePanel(UserPrincipal principal,
                            MedicamentService medService,
                            ActeMedicalService acteService,
                            AntecedentService antService,
                            ReferentielService refService) {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel title = new JLabel("Paramétrage & Référentiels");
        title.setFont(new Font("Optima", Font.BOLD, 32));
        title.setForeground(new Color(44, 62, 80));
        add(title, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        tabs.addTab("Médicaments", new MedicamentManagementPanel(medService));
        tabs.addTab("Actes Médicaux", new ActeManagementPanel(acteService));
        tabs.addTab("Antécédents", new AntecedentManagementPanel(antService));
        tabs.addTab("Assurances", new AssuranceListPanel(refService));

        add(tabs, BorderLayout.CENTER);
    }
}