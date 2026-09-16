package ma.WhiteLab.mvc.controllers.otherModules.impl;

import javax.swing.JPanel;
import ma.WhiteLab.mvc.controllers.otherModules.api.ParametrageController;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.otherPages.ParametragePanel;
import ma.WhiteLab.service.modules.dossierMedicale.api.ActeMedicalService;
import ma.WhiteLab.service.modules.dossierMedicale.api.MedicamentService;
import ma.WhiteLab.service.modules.patient.api.AntecedentService;
import ma.WhiteLab.service.modules.referentiel.api.ReferentielService;

public class ParametrageControllerImpl implements ParametrageController {

    private final MedicamentService medicamentService;
    private final ActeMedicalService acteService;
    private final AntecedentService antecedentService;
    private final ReferentielService referentielService;
    
    private ParametragePanel cached;

    public ParametrageControllerImpl(MedicamentService medicamentService,
                                     ActeMedicalService acteService,
                                     AntecedentService antecedentService,
                                     ReferentielService referentielService) {
        this.medicamentService = medicamentService;
        this.acteService = acteService;
        this.antecedentService = antecedentService;
        this.referentielService = referentielService;
    }

    @Override
    public JPanel getView(UserPrincipal principal) {
        if (cached == null) {
            cached = new ParametragePanel(principal, medicamentService, acteService, antecedentService, referentielService);
        }
        return cached;
    }
}