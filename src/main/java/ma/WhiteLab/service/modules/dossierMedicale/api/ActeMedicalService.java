package ma.WhiteLab.service.modules.dossierMedicale.api;

import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.service.modules.consultation.dto.ActeDTO;

import java.time.LocalDate;
import java.util.List;

public interface ActeMedicalService {

    /* ================= CRUD ================= */

    List<ActeDTO> getAllActes();

    ActeDTO getActeById(Long id);

    ActeDTO createActe(ActeDTO dto) throws ValidationException;

    ActeDTO updateActe(Long id, ActeDTO dto) throws ValidationException;

    void deleteActe(Long id) throws ValidationException;

    /* ================= Recherche ================= */

    ActeDTO getActeByLibelle(String libelle);

    /* ================= Statistiques ================= */

    String getMostFrequentActeNomCeMois(Long cabinetId);

    List<ActeDTO> getTop5ActesByPeriodeAndCabinet(
            LocalDate debutMois,
            LocalDate finMois,
            Long cabinetId
    );
}
