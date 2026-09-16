package ma.WhiteLab.repository.modules.dossierMedical.api;

import ma.WhiteLab.entities.dossierMedical.InterventionMedecin;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;

public interface InterventionMedecinRepository extends CrudRepository<InterventionMedecin, Long> {

    // Recherche par dent
    List<InterventionMedecin> findByNumDent(int numDent);

    // Récupérer toutes les interventions d’une consultation
    List<InterventionMedecin> findByConsultationId(Long consultationId);

    // Récupérer toutes les interventions pour un acte médical spécifique
    List<InterventionMedecin> findByActeMedicalId(Long acteMedicalId);
}
