package ma.WhiteLab.mvc.ui.pages.pagesNames;

public enum ApplicationPages {
    DASHBOARD,
    STATISTICS,

    // Général
    PROFILE,
    NOTIFICATIONS,

    // Métier
    AGENDA,        // <--- Ajouté ici
    PATIENTS,
    DOSSIERS_MEDICAUX,
    CAISSE,

    // Administration & Paramétrage
    CABINET,       // Infos du cabinet courant
    CABINETS,      // Gestion multi-cabinets (Admin)
    USERS,
    USER,
    ROLES,
    PARAMETRAGE;

    // 🔥 SAFE resolver (DO NOT REMOVE)
    public static ApplicationPages from(String raw) {
        if (raw == null) return null;

        // Normalisation : majuscules, suppression des espaces et caractères spéciaux invisibles
        String normalized = raw
                .trim()
                .toUpperCase()
                .replaceAll("[^A-Z_]", "");

        for (ApplicationPages p : values()) {
            if (p.name().equals(normalized)) {
                return p;
            }
        }

        throw new IllegalArgumentException(
                "Page inconnue dans ApplicationPages : '" + raw + "'"
        );
    }
}