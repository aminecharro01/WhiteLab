package ma.WhiteLab.service.modules.certificat.api;

import ma.WhiteLab.service.modules.certificat.dto.CertificatDTO;

import java.util.Map;

public interface CertificatValidator {

    /**
     * Valide les données d'un DTO de certificat.
     * @param dto Le DTO à valider.
     * @return Une map des erreurs de validation (clé: champ, valeur: message d'erreur). Vide si valide.
     */
    Map<String, String> validate(CertificatDTO dto);
}