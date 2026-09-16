package ma.WhiteLab.service.modules.cabinet.api;

import ma.WhiteLab.entities.cabinet.CabinetMedicale;

/**
 * Service dédié à la gestion des paramètres du cabinet actuel.
 * Utilisé dans 99% des cas : l'application fonctionne avec un seul cabinet connecté.
 * Interface simple et intuitive pour le médecin/secrétaire.
 */
public interface ParametrageService {

    /**
     * Récupère le cabinet actuel de l'application.
     * Si plusieurs existent, retourne le premier ou celui marqué comme principal.
     * Lance une exception si aucun cabinet n'est configuré.
     */
    CabinetMedicale getCabinetActuel();

    /**
     * Met à jour les informations générales du cabinet actuel
     */
    CabinetMedicale updateParametresCabinet(CabinetMedicale cabinet, String modifierPar);

    /**
     * Met à jour uniquement le logo (chemin ou bytes)
     */
    CabinetMedicale updateLogo(String logoPath, String modifierPar);

    /**
     * Supprime le logo du cabinet actuel
     */
    void removeLogo(String modifierPar);

    /**
     * Vérifie si un cabinet est déjà configuré dans l'application
     */
    boolean isCabinetConfigured();

    /**
     * Utilisé au premier lancement : crée et configure le cabinet initial
     */
    CabinetMedicale initialiserCabinet(CabinetMedicale cabinet, String creePar);

    /**
     * Retourne vrai si l'app est en mode mono-cabinet (cas standard)
     */
    boolean isMonoCabinet();
}