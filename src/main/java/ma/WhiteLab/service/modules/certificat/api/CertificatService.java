package ma.WhiteLab.service.modules.certificat.api;

import ma.WhiteLab.service.modules.certificat.dto.CertificatDTO;
import ma.WhiteLab.common.exceptions.ValidationException;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

public interface CertificatService {

    /**
     * Récupère tous les certificats.
     * @return Liste des DTOs des certificats.
     */
    List<CertificatDTO> getAllCertificats();

    /**
     * Récupère un certificat par son ID.
     * @param id L'ID du certificat.
     * @return Le DTO du certificat, ou null si non trouvé.
     */
    CertificatDTO getCertificatById(Long id);

    /**
     * Crée un nouveau certificat.
     * @param dto Le DTO du certificat à créer.
     * @return Le DTO du certificat créé.
     * @throws ValidationException Si les données sont invalides.
     */
    CertificatDTO createCertificat(CertificatDTO dto) throws ValidationException;

    /**
     * Met à jour un certificat existant.
     * @param id L'ID du certificat à mettre à jour.
     * @param dto Le DTO avec les nouvelles données.
     * @return Le DTO du certificat mis à jour.
     * @throws ValidationException Si les données sont invalides ou si le certificat n'existe pas.
     */
    CertificatDTO updateCertificat(Long id, CertificatDTO dto) throws ValidationException;

    /**
     * Supprime un certificat par son ID.
     * @param id L'ID du certificat à supprimer.
     * @throws ValidationException Si le certificat n'existe pas ou ne peut pas être supprimé.
     */
    void deleteCertificat(Long id) throws ValidationException;

    /**
     * Récupère les certificats dans une plage de dates de début.
     * @param start Date de début de la plage.
     * @param end Date de fin de la plage.
     * @return Liste des DTOs des certificats correspondants.
     */
    List<CertificatDTO> getCertificatsByDateDebutRange(LocalDate start, LocalDate end);

    /**
     * Récupère les certificats par durée de repos.
     * @param duree La durée de repos en jours.
     * @return Liste des DTOs des certificats correspondants.
     */
    List<CertificatDTO> getCertificatsByDureeRepos(int duree);

    /**
     * Récupère les certificats associés à un dossier médical.
     * @param dossierId L'ID du dossier médical.
     * @return Liste des DTOs des certificats correspondants.
     */
    List<CertificatDTO> getCertificatsByDossierMedicalId(Long dossierId);

    /**
     * Récupère les certificats associés à une consultation.
     * @param consultationId L'ID de la consultation.
     * @return Liste des DTOs des certificats correspondants.
     */
    List<CertificatDTO> getCertificatsByConsultationId(Long consultationId);

    /**
     * Génère un PDF pour un certificat.
     * @param id L'ID du certificat.
     * @return Un flux d'octets représentant le PDF généré.
     * @throws ValidationException Si le certificat n'existe pas ou si la génération échoue.
     */
    ByteArrayInputStream generateCertificatPdf(Long id) throws ValidationException;
}