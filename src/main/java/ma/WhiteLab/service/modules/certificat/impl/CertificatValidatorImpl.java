package ma.WhiteLab.service.modules.certificat.impl;

import ma.WhiteLab.service.modules.certificat.api.CertificatValidator;
import ma.WhiteLab.service.modules.certificat.dto.CertificatDTO;
import ma.WhiteLab.common.exceptions.ValidationException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class CertificatValidatorImpl implements CertificatValidator {

    @Override
    public Map<String, String> validate(CertificatDTO dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto == null) {
            errors.put("global", "Le DTO du certificat est null");
            return errors;
        }

        // Validation dateDebut
        if (dto.getDateDebut() == null) {
            errors.put("dateDebut", "La date de début est requise");
        }

        // Validation dateFin
        if (dto.getDateFin() == null) {
            errors.put("dateFin", "La date de fin est requise");
        } else if (dto.getDateDebut() != null && dto.getDateFin().isBefore(dto.getDateDebut())) {
            errors.put("dateFin", "La date de fin doit être après la date de début");
        }

        // Validation dureeRepos
        if (dto.getDureeRepos() <= 0) {
            errors.put("dureeRepos", "La durée de repos doit être positive");
        }

        // Validation contenu
        if (dto.getContenu() == null || dto.getContenu().trim().isEmpty()) {
            errors.put("contenu", "Le contenu est requis et ne peut pas être vide");
        } else if (dto.getContenu().length() > 2000) { // Exemple de limite
            errors.put("contenu", "Le contenu est trop long (max 2000 caractères)");
        }

        // Validation IDs (optionnels mais doivent être positifs si présents)
        if (dto.getConsultationId() != null && dto.getConsultationId() <= 0) {
            errors.put("consultationId", "L'ID de consultation doit être positif");
        }

        if (dto.getDossierMedicalId() != null && dto.getDossierMedicalId() <= 0) {
            errors.put("dossierMedicalId", "L'ID du dossier médical doit être positif");
        }

        // Validation dates création/modification (gérées par le système, pas validées ici)

        return errors;
    }
}