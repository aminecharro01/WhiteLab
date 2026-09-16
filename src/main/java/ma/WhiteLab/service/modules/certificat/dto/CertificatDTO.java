package ma.WhiteLab.service.modules.certificat.dto;

import lombok.Data;
import ma.WhiteLab.entities.dossierMedical.Certificat;
import ma.WhiteLab.entities.dossierMedical.Consultation;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CertificatDTO {

    private Long id;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int dureeRepos;
    private String contenu;
    private Long consultationId;
    private Long dossierMedicalId;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    private String creePar;
    private String modifierPar;

    // =================== Conversion Utility ===================
    public static CertificatDTO fromEntity(Certificat cert) {
        if (cert == null) return null;

        CertificatDTO dto = new CertificatDTO();
        dto.setId(cert.getId());
        dto.setDateDebut(cert.getDateDebut());
        dto.setDateFin(cert.getDateFin());
        dto.setDureeRepos(cert.getDureeRepos());
        dto.setContenu(cert.getContenu());
        dto.setConsultationId(cert.getConsultation() != null ? cert.getConsultation().getId() : null);
        dto.setDossierMedicalId(cert.getDossierMedical() != null ? cert.getDossierMedical().getId() : null);
        dto.setDateCreation(cert.getDateCreation());
        dto.setDateMiseAJour(cert.getDateMiseAJour());
        dto.setCreePar(cert.getCreePar());
        dto.setModifierPar(cert.getModifierPar());

        return dto;
    }

    public Certificat toEntity() {
        Certificat entity = new Certificat();

        entity.setId(this.id);
        entity.setDateDebut(this.dateDebut);
        entity.setDateFin(this.dateFin);
        entity.setDureeRepos(this.dureeRepos);
        entity.setContenu(this.contenu);

        // Les relations (Consultation et DossierMedical) ne sont PAS settées ici
        // → elles doivent être injectées manuellement dans le service ou le controller
        // (c'est la bonne pratique pour éviter de charger des entités complètes dans le DTO)
        // entity.setConsultation(...);  ← à faire dans le service
        // entity.setDossierMedical(...); ← à faire dans le service

        entity.setDateCreation(this.dateCreation);
        entity.setDateMiseAJour(this.dateMiseAJour);
        entity.setCreePar(this.creePar);
        entity.setModifierPar(this.modifierPar);

        return entity;
    }
}