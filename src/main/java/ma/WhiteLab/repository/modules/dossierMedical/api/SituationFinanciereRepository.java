package ma.WhiteLab.repository.modules.dossierMedical.api;

import ma.WhiteLab.entities.dossierMedical.SituationFinanciere;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;

public interface SituationFinanciereRepository extends CrudRepository<SituationFinanciere, Long> {

    // Récupérer le suivi financier d'un dossier médical spécifique
    List<SituationFinanciere> findByDossierId(Long dossierId);

    // Récupérer tous les suivis financiers actifs
    List<SituationFinanciere> findByStatus(String status);

    // Récupérer tous les suivis financiers en promo
    List<SituationFinanciere> findByPromo(String promoStatus);
}
