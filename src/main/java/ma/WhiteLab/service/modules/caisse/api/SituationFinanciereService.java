package ma.WhiteLab.service.modules.caisse.api;

import ma.WhiteLab.entities.dossierMedical.SituationFinanciere;
import ma.WhiteLab.entities.enums.PromoStatus;
import ma.WhiteLab.entities.enums.Status;

import java.util.List;

public interface SituationFinanciereService {

    List<SituationFinanciere> findAll();

    SituationFinanciere findById(Long id);

    void create(SituationFinanciere situationFinanciere);

    void update(SituationFinanciere situationFinanciere);

    void delete(Long id);

    List<SituationFinanciere> findByDossierId(Long dossierId);

    List<SituationFinanciere> findByStatus(Status status);

    List<SituationFinanciere> findByPromo(PromoStatus promoStatus);

    // Business methods
    void calculateCredit(SituationFinanciere sf);

    void recalculateForDossier(Long dossierId);

    SituationFinanciere findByDossierMedicalId(Long dossierId);
}
