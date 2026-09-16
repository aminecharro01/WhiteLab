package ma.WhiteLab.mvc.ui.pages.otherPages;

import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.otherPages.common.BasePlaceholderPanel;

public class CabinetPanel extends BasePlaceholderPanel {
    public CabinetPanel(UserPrincipal principal) {
        super("Cabinet courant", principal);
    }
}
