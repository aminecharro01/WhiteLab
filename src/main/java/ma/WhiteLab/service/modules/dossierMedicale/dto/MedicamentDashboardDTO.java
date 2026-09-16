// MedicamentDashboardDTO.java
package ma.WhiteLab.service.modules.dossierMedicale.dto;

import lombok.Data;
import ma.WhiteLab.entities.dossierMedical.Medicament;

@Data
public class MedicamentDashboardDTO {
    private long total;
    private long remboursables;
    private long nonRemboursables;
    private double prixMoyen;
    private Medicament plusCher;
    private Medicament moinsCher;
}