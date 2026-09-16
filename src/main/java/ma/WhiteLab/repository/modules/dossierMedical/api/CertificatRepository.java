package ma.WhiteLab.repository.modules.dossierMedical.api;

import ma.WhiteLab.entities.dossierMedical.Certificat;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;

public interface CertificatRepository extends CrudRepository<Certificat, Long> {

    // Récupérer les certificats par plage de dates
    List<Certificat> findByDateDebutRange(java.time.LocalDate start, java.time.LocalDate end);

    // Récupérer les certificats par durée de repos
    List<Certificat> findByDureeRepos(int duree);

    // Récupérer les certificats par dossier médical
    List<Certificat> findByDossierMedicalId(Long dossierId);
    // Bonus utile
    List<Certificat> findByConsultationId(Long consultationId);
}
