package ma.WhiteLab.mvc.controllers.otherModules.impl;

import javax.swing.JPanel;
import ma.WhiteLab.mvc.controllers.otherModules.api.CabinetsController;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.otherPages.CabinetsPanel;
import ma.WhiteLab.service.modules.cabinet.api.CabinetMedicalService;

public class CabinetsControllerImpl implements CabinetsController {

    private final CabinetMedicalService cabinetService;
    private CabinetsPanel cached;

    public CabinetsControllerImpl(CabinetMedicalService cabinetService) {
        this.cabinetService = cabinetService;
    }

    @Override
    public JPanel getView(UserPrincipal principal) {
        if (cached == null) {
            cached = new CabinetsPanel(principal, cabinetService);
        }
        return cached;
    }
}