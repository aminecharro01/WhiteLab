package ma.WhiteLab.service.modules.cabinet.api;

import ma.WhiteLab.entities.cabinet.CabinetMedicale;
import ma.WhiteLab.service.modules.cabinet.dto.CabinetSummaryDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service principal pour la gestion administrative des cabinets médicaux.
 * Utilisé pour la création, modification, suppression et recherche avancée de cabinets.
 * Typiquement utilisé par un super-admin ou dans une version multi-cabinets.
 */
public interface CabinetMedicalService {

    /* ========================= CRUD & RECHERCHE ========================= */

    List<CabinetMedicale> findAll();

    List<CabinetMedicale> findAllOrderByNom();

    /**
     * Retourne une liste légère de tous les cabinets (pour affichage dans tableaux)
     */
    List<CabinetSummaryDTO> getAllSummaries();

    Optional<CabinetMedicale> findById(Long id);

    Optional<CabinetMedicale> findByNom(String nom);

    Optional<CabinetMedicale> findByEmail(String email);

    List<CabinetMedicale> findByCategorie(String categorie);

    /**
     * Recherche multi-critères par mot-clé (nom, email, téléphone, ville...)
     */
    List<CabinetMedicale> search(String keyword);

    CabinetMedicale create(CabinetMedicale cabinet, String creePar);

    CabinetMedicale update(CabinetMedicale cabinet, String modifierPar);

    void deleteById(Long id);

    long count();

    /* ========================= STATISTIQUES LÉGÈRES ========================= */

}