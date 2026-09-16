package ma.WhiteLab.repository.modules.dossierMedical.api;

import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DossierMedicalRepository extends CrudRepository<DossierMedical, Long> {

    // Récupérer un dossier médical par ID du patient
    Optional<DossierMedical> findByPatientId(Long patientId);
    List<DossierMedical> findByPatientName(String nomOrPrenom);

    List<DossierMedical> searchByHistorique(String keyword);
    List<DossierMedical> findRecent(int days);

    // Récupérer les dossiers médicaux par ID du médecin
    List<DossierMedical> findByMedecinId(Long medecinId);

    // Compter le nombre total de dossiers médicaux
    long count();
}
