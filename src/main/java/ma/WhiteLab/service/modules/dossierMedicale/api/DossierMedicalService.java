package ma.WhiteLab.service.modules.dossierMedicale.api;

import ma.WhiteLab.entities.agenda.RendezVous;
import ma.WhiteLab.entities.dossierMedical.*;
import ma.WhiteLab.entities.patient.Antecedent;
import ma.WhiteLab.entities.patient.Patient;

import java.util.List;
import java.util.Optional;

public interface DossierMedicalService {

    // ====================== GESTION DU DOSSIER ======================
    DossierMedical createDossierForPatient(Long patientId, Long medecinId, String creePar);
    DossierMedical updateHistorique(Long dossierId, String nouveauHistorique, String modifierPar);
    void deleteDossier(Long patientId);

    Optional<DossierMedical> getDossierById(Long id);
    Optional<DossierMedical> getDossierByPatientId(Long patientId);
    List<DossierMedical> getDossiersByMedecinId(Long medecinId);
    List<DossierMedical> getAllDossiers();
    long countDossiers();

    // ====================== RECHERCHES ======================
    List<DossierMedical> searchByPatientName(String nomOrPrenom);
    List<DossierMedical> searchByHistorique(String keyword);
    List<DossierMedical> findRecentDossiers(int days);

    // ====================== LECTURE DES DONNÉES CLINIQUES (LECTURE SEULE) ======================
    Patient getPatient(Long dossierId);

    List<Consultation> getConsultations(Long dossierId);
    List<Ordonnance> getOrdonnances(Long dossierId);
    List<Certificat> getCertificats(Long dossierId);
    List<Antecedent> getAntecedents(Long dossierId);
    List<RendezVous> getRendezVous(Long dossierId);

    SituationFinanciere getSituationFinanciere(Long dossierId);

    // ====================== EXPORT ======================
    byte[] exportDossierToPdf(Long dossierId);
}