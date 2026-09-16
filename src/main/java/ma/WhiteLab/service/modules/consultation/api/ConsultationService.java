package ma.WhiteLab.service.modules.consultation.api;

import ma.WhiteLab.entities.dossierMedical.ActeMedical;
import ma.WhiteLab.service.modules.consultation.dto.ConsultationDTO;
import ma.WhiteLab.common.exceptions.ValidationException;

import java.time.LocalDate;
import java.util.List;

public interface ConsultationService {

    /**
     * Récupère toutes les consultations.
     * @return Liste des DTOs des consultations.
     */
    List<ConsultationDTO> getAllConsultations();

    /**
     * Récupère une consultation par son ID.
     * @param id L'ID de la consultation.
     * @return Le DTO de la consultation, ou null si non trouvée.
     */
    ConsultationDTO getConsultationById(Long id);

    /**
     * Crée une nouvelle consultation.
     * @param dto Le DTO de la consultation à créer.
     * @return Le DTO de la consultation créée.
     * @throws ValidationException Si les données sont invalides.
     */
    ConsultationDTO createConsultation(ConsultationDTO dto) throws ValidationException;

    /**
     * Met à jour une consultation existante.
     * @param id L'ID de la consultation à mettre à jour.
     * @param dto Le DTO avec les nouvelles données.
     * @return Le DTO de la consultation mise à jour.
     * @throws ValidationException Si les données sont invalides ou si la consultation n'existe pas.
     */
    ConsultationDTO updateConsultation(Long id, ConsultationDTO dto) throws ValidationException;

    /**
     * Supprime une consultation par son ID.
     * @param id L'ID de la consultation à supprimer.
     * @throws ValidationException Si la consultation n'existe pas ou ne peut pas être supprimée.
     */
    void deleteConsultation(Long id) throws ValidationException;

    /**
     * Récupère les consultations par statut.
     * @param status Le statut à filtrer.
     * @return Liste des DTOs des consultations correspondantes.
     */
    List<ConsultationDTO> getConsultationsByStatus(String status);

    /**
     * Récupère les consultations associées à un dossier médical.
     * @param dossierId L'ID du dossier médical.
     * @return Liste des DTOs des consultations correspondantes.
     */
    List<ConsultationDTO> getConsultationsByDossierMedicalId(Long dossierId);

    /**
     * Compte les consultations par cabinet.
     * @param cabinetId L'ID du cabinet.
     * @return Le nombre de consultations.
     */
    long countByCabinet(Long cabinetId);

    /**
     * Compte les consultations par cabinet et mois.
     * @param cabinetId L'ID du cabinet.
     * @param year L'année.
     * @param month Le mois (1-12).
     * @return Le nombre de consultations.
     */
    long countByCabinetAndMonth(Long cabinetId, int year, int month);

    /**
     * Compte les consultations par date et cabinet.
     * @param date La date spécifique.
     * @param cabinetId L'ID du cabinet.
     * @return Le nombre de consultations.
     */
    long countByDateAndCabinet(LocalDate date, Long cabinetId);

    /**
     * Ajoute une intervention à une consultation.
     * @param consultationId L'ID de la consultation.
     * @param interventionDto Le DTO de l'intervention à ajouter.
     */
    void addIntervention(Long consultationId, ma.WhiteLab.service.modules.consultation.dto.InterventionDTO interventionDto);

    /**
     * Ajoute un acte à une consultation.
     * @param consultationId L'ID de la consultation.
     * @param acteDto Le DTO de l'acte à ajouter.
     */
    void addActe(Long consultationId, ma.WhiteLab.service.modules.consultation.dto.ActeDTO acteDto);

    List<ActeMedical> getActesDisponibles();
}