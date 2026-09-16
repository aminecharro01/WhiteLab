package ma.WhiteLab.service.modules.dossierMedicale.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.dossierMedical.Consultation;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.dossierMedical.Ordonnance;
import ma.WhiteLab.repository.modules.dossierMedical.api.ConsultationRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.DossierMedicalRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.dossierMedicale.api.OrdonnanceValidator;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class OrdonnanceValidatorImpl implements OrdonnanceValidator {

    private final RepoFactory<DossierMedicalRepository> dossierRepoFactory;
    private final RepoFactory<ConsultationRepository> consultationRepoFactory;

    public OrdonnanceValidatorImpl(
            RepoFactory<DossierMedicalRepository> dossierRepoFactory,
            RepoFactory<ConsultationRepository> consultationRepoFactory) {
        this.dossierRepoFactory = dossierRepoFactory;
        this.consultationRepoFactory = consultationRepoFactory;
    }

    @Override
    public Map<String, String> validateCreation(Ordonnance ordonnance) {
        return validateOrdonnanceCommon(ordonnance, true);
    }

    @Override
    public Map<String, String> validateUpdate(Ordonnance ordonnance) {
        Map<String, String> errors = validateOrdonnanceCommon(ordonnance, false);

        // Pour update, l'ID est obligatoire
        if (ordonnance.getId() == null) {
            errors.put("id", "L'ID de l'ordonnance est obligatoire pour une mise à jour.");
        }

        return errors;
    }

    @Override
    public Map<String, String> validateOrdonnance(Ordonnance ordonnance) {
        return validateOrdonnanceCommon(ordonnance, false);
    }

    /**
     * Méthode commune pour valider une ordonnance
     */
    private Map<String, String> validateOrdonnanceCommon(Ordonnance ordonnance, boolean isCreation) {
        Map<String, String> errors = new HashMap<>();

        if (ordonnance == null) {
            errors.put("ordonnance", "L'ordonnance ne peut pas être null.");
            return errors;
        }

        // 1. Date obligatoire et non future
        LocalDate date = ordonnance.getDateOrdonnance();
        if (date == null) {
            errors.put("dateOrdonnance", "La date de l'ordonnance est obligatoire.");
        } else if (date.isAfter(LocalDate.now())) {
            errors.put("dateOrdonnance", "La date de l'ordonnance ne peut pas être dans le futur.");
        }

        // 2. Au moins une référence (dossier OU consultation)
        DossierMedical dossier = ordonnance.getDossierMedical();
        Consultation consultation = ordonnance.getConsultation();

        if (dossier == null && consultation == null) {
            errors.put("references", "L'ordonnance doit être liée à un dossier médical ou à une consultation.");
        }

        // 3. Vérification existence du dossier médical (si fourni)
        if (dossier != null && dossier.getId() != null) {
            boolean dossierExists = Transaction.initTransaction(cnx -> {
                DossierMedicalRepository repo = dossierRepoFactory.create(cnx);
                return repo.findById(dossier.getId()) != null;
            });

            if (!dossierExists) {
                errors.put("dossierMedical", "Le dossier médical avec l'ID " + dossier.getId() + " n'existe pas.");
            }
        }

        // 4. Vérification existence de la consultation (si fournie)
        if (consultation != null && consultation.getId() != null) {
            boolean consultationExists = Transaction.initTransaction(cnx -> {
                ConsultationRepository repo = consultationRepoFactory.create(cnx);
                return repo.findById(consultation.getId()) != null;
            });

            if (!consultationExists) {
                errors.put("consultation", "La consultation avec l'ID " + consultation.getId() + " n'existe pas.");
            }
        }

        return errors;
    }
}