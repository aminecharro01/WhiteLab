package ma.WhiteLab.service.modules.dossierMedicale.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.WhiteLab.entities.dossierMedical.Medicament;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicamentPatientDTO {
    private Long patientId;
    private List<Medicament> medicamentsCompatibles;
    private List<Medicament> medicamentsContreIndiques;
}
