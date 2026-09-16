package ma.WhiteLab.service.modules.agendas.dto;

import lombok.Getter;
import lombok.Setter;
import ma.WhiteLab.entities.agenda.RendezVous;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Setter
@Getter
public class RendezVousDTO {

    // ===================== Getters & Setters =====================
    private Long id;
    private LocalDateTime date; // Date et heure complète
    private LocalTime time; // Si séparé
    private String motif;
    private String status; // String representation of StatusRendezVous enum
    private String noteMedecin;
    private Long dossierMedId;
    private Long consultationId;

    // ===================== Conversion Utility =====================
    public static RendezVousDTO fromEntity(RendezVous rdv) {
        if (rdv == null) return null;

        RendezVousDTO dto = new RendezVousDTO();
        dto.setId(rdv.getId());
        dto.setDate(rdv.getDate());
        dto.setTime(rdv.getTime());
        dto.setMotif(rdv.getMotif());
        dto.setStatus(rdv.getStatus() != null ? rdv.getStatus().name() : null);
        dto.setNoteMedecin(rdv.getNoteMedecin());
        dto.setDossierMedId(rdv.getDossierMed() != null ? rdv.getDossierMed().getId() : null);
        dto.setConsultationId(rdv.getConsultation() != null ? rdv.getConsultation().getId() : null);

        return dto;
    }
}
