package ma.WhiteLab.repository.modules.dossierMedical.api;

import ma.WhiteLab.entities.dossierMedical.Facture;
import ma.WhiteLab.repository.common.CrudRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public interface FactureRepository extends CrudRepository<Facture, Long> {

    // Récupérer toutes les factures d'une consultation spécifique
    List<Facture> findByConsultationId(Long consultationId);

    // Récupérer toutes les factures d'un certain statut
    List<Facture> findByStatut(String statut);

    // Récupérer toutes les factures entre deux dates
    List<Facture> findByDateBetween(LocalDate start, LocalDate end);


    double sumChiffreAffaireByDateAndCabinet(LocalDate date, Long cabinetId);

    double sumChiffreAffaireByCabinet(Long cabinetId);

    double sumChiffreAffaireByCabinetAndMonth(Long cabinetId, int year, int month);

    long countFacturesByCabinet(Long cabinetId);

}
