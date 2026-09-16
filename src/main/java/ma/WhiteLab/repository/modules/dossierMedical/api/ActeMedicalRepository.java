package ma.WhiteLab.repository.modules.dossierMedical.api;

import ma.WhiteLab.entities.dossierMedical.ActeMedical;
import ma.WhiteLab.repository.common.CrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActeMedicalRepository extends CrudRepository<ActeMedical, Long> {

    Optional<ActeMedical> findByLibelle(String libelle);

    // Retourne le nom de l'acte le plus fréquent ce mois-ci pour un cabinet
    String findMostFrequentActeNomCeMois(Long cabinetId);

    // Retourne les 5 actes les plus utilisés pour une période et un cabinet
    List<ActeMedical> findTop5ActesByPeriodeAndCabinet(LocalDate debutMois, LocalDate finMois, Long cabinetId);
}
