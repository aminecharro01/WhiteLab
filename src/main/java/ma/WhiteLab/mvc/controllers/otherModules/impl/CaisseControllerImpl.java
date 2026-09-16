package ma.WhiteLab.mvc.controllers.otherModules.impl;

import javax.swing.JPanel;
import ma.WhiteLab.mvc.controllers.otherModules.api.CaisseController;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.otherPages.CaissePanel;
import ma.WhiteLab.service.modules.caisse.api.CaisseService;
import ma.WhiteLab.service.modules.caisse.api.ChargesService;
import ma.WhiteLab.service.modules.caisse.api.FactureService;
import ma.WhiteLab.service.modules.caisse.api.RevenusService;

import ma.WhiteLab.service.modules.caisse.api.SituationFinanciereService;
import ma.WhiteLab.service.modules.consultation.api.ConsultationService;
import ma.WhiteLab.service.modules.dossierMedicale.api.InterventionMedecinService;

public class CaisseControllerImpl implements CaisseController {

    private final CaisseService caisseService;
    private final RevenusService revenusService;
    private final ChargesService chargesService;
    private final FactureService factureService;
    private final SituationFinanciereService sfService;
    private final ConsultationService consultationService;
    private final InterventionMedecinService interventionService;

    private CaissePanel cached;

    public CaisseControllerImpl(CaisseService caisseService,
                                RevenusService revenusService,
                                ChargesService chargesService,
                                FactureService factureService,
                                SituationFinanciereService sfService,
                                ConsultationService consultationService,
                                InterventionMedecinService interventionService) {
        this.caisseService = caisseService;
        this.revenusService = revenusService;
        this.chargesService = chargesService;
        this.factureService = factureService;
        this.sfService = sfService;
        this.consultationService = consultationService;
        this.interventionService = interventionService;
    }

    @Override
    public JPanel getView(UserPrincipal principal) {
        if (cached == null) {
            cached = new CaissePanel(
                    principal,
                    caisseService,
                    revenusService,
                    chargesService,
                    factureService,
                    sfService,
                    consultationService,
                    interventionService
            );
        }
        return cached;
    }
}