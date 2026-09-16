package ma.WhiteLab.service.modules.dashboard_statistiques.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.enums.Status;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RendezVousDTO {
    private LocalTime heure;
    private String nomPatient;
    private String motif;
    private Status status;

    @Override
    public String toString() {
        return heure + " - " + nomPatient + " (" + motif + ") - Status: " + status;
    }
}