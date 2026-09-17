package ma.WhiteLab.mvc.ui.palette.sidebarBuilder;

import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.mvc.ui.pages.pagesNames.ApplicationPages;

import java.util.ArrayList;
import java.util.List;

import static ma.WhiteLab.security.Privileges.*;

public final class NavigationSpecs {

    // Sections constantes pour éviter les fautes de frappe
    private static final String SEC_GENERAL = "Général";
    private static final String SEC_CABINET = "Cabinet";
    private static final String SEC_ADMIN = "Administration";
    private static final String SEC_SYSTEME = "Système";

    private NavigationSpecs() {}

    public static List<NavSpec> forPrincipal(UserPrincipal principal) {
        List<NavSpec> items = new ArrayList<>();
        if (principal == null || principal.roles() == null) return items;

        boolean isAdmin = principal.roles().contains(RoleR.ADMIN);
        boolean isMedecin = principal.roles().contains(RoleR.MEDECIN);
        boolean isSecretaire = principal.roles().contains(RoleR.SECRETAIRE);

        // --- SECTION : GÉNÉRAL (Commun à tous) ---
        items.add(item(SEC_GENERAL, "Tableau de bord", "ikon:TACHOMETER_ALT", ApplicationPages.DASHBOARD, null));
        items.add(item(SEC_GENERAL, "Mon Profil", "ikon:USER", ApplicationPages.PROFILE, null));
        items.add(item(SEC_GENERAL, "Notifications", "ikon:BELL", ApplicationPages.NOTIFICATIONS, null));

        // --- SECTION : MÉTIER / CABINET ---
        if (isMedecin) {
        }

        if (isMedecin || isSecretaire) {
            items.add(item(SEC_CABINET, "Gestion Patients", "ikon:TOOTH", ApplicationPages.PATIENTS, "GESTION_PATIENTS"));
            items.add(item(SEC_CABINET,"Caisse", "ikon:CASH_REGISTER", ApplicationPages.CAISSE, "GESTION_CAISSE"));


        }

        if (isSecretaire) {
            items.add(item(SEC_CABINET, "Agenda Medecine", "ikon:CALENDAR_ALT", ApplicationPages.AGENDA, "GERER_AGENDA_MEDECIN"));

        }

        // --- SECTION : ADMINISTRATION (Admin seulement) ---
        if (isAdmin) {
            items.add(item(SEC_ADMIN, "Gestion Cabinets", "ikon:HOSPITAL", ApplicationPages.CABINETS, CABINET_ACCESS));
            items.add(item(SEC_ADMIN, "Utilisateurs", "ikon:USERS", ApplicationPages.USERS, USERS_ACCESS));
            items.add(item(SEC_ADMIN,"Rôles & Droits",  "ikon:USER_SHIELD", ApplicationPages.ROLES, USERS_ACCESS));
            items.add(item(SEC_ADMIN,"Données Référentielles",  "ikon:DATABASE", ApplicationPages.PARAMETRAGE, USERS_ACCESS));

        }

        // --- SECTION : SYSTÈME / CONFIG ---
        // On affiche "Paramètres" pour tout le monde, mais le contenu changera selon les droits
        items.add(item(SEC_SYSTEME, "Paramètres", "ikon:COG", ApplicationPages.PARAMETRAGE, null));

        return items;
    }

    private static NavSpec item(String section, String label, String iconPath,
                                ApplicationPages page, String privilegeOrNull) {
        return new NavSpec(section, label, iconPath, page.name(), privilegeOrNull);
    }
}