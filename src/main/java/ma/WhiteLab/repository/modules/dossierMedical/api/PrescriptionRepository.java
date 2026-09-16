package ma.WhiteLab.repository.modules.dossierMedical.api;

import ma.WhiteLab.entities.dossierMedical.Prescription;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;

public interface PrescriptionRepository extends CrudRepository<Prescription, Long> {

    // Trouver toutes les prescriptions d'une ordonnance
    List<Prescription> findByOrdonnanceId(Long ordonnanceId);

    // Trouver toutes les prescriptions d'un médicament
    List<Prescription> findByMedicamentId(Long medicamentId);

    // Chercher par fréquence (optionnel)
    List<Prescription> findByFrequence(String frequence);

    // Vérifier si une prescription existe déjà pour éviter doublons
    boolean exists(Long ordonnanceId, Long medicamentId);
}
