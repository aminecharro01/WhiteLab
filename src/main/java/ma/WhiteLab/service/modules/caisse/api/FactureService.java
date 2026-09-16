package ma.WhiteLab.service.modules.caisse.api;

import ma.WhiteLab.entities.dossierMedical.Facture;
import ma.WhiteLab.entities.enums.StatutFacture;

import java.time.LocalDate;
import java.util.List;

public interface FactureService {

    List<Facture> findAll();

    Facture findById(Long id);

    void create(Facture facture);

    void update(Facture facture);

    void delete(Long id);

    List<Facture> findByConsultationId(Long consultationId);

    List<Facture> findByStatut(StatutFacture statut);

    List<Facture> findByDateBetween(LocalDate start, LocalDate end);

    // Business methods
    void markAsPaid(Long factureId);

    void calculateReste(Facture facture);
}
