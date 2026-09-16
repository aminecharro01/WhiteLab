package ma.WhiteLab.security;


public final class Privileges {
    private Privileges() {}

    // ---- Administration
    public static final String USERS_ACCESS   = "GESTION_UTILISATEURS";
    public static final String CABINET_ACCESS = "GESTION_CABINET";

    // ---- Métier
    public static final String PATIENT_ACCESS = "GESTION_PATIENTS";
    public static final String DOSSIER_ACCESS = "GERER_DOSSIERS";
    public static final String RDV_ACCESS     = "GERER_RDV";
    public static final String CAISSE_ACCESS  = "GESTION_CAISSE";
    public static final String AGENDA_ACCESS  = "GERER_AGENDA_MEDECIN";
    public static final String PARAMETRAGE_ACCESS = "GESTION_PARAMETRAGES";


}
