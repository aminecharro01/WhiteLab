package ma.WhiteLab.service.modules.caisse.dto;

import lombok.Data;

@Data
public class SituationFinanciereDTO {
    private Float totalDesActes;
    private Float totalPaye;
    private Float credit;
    private String status;
    private String enPromo;
    private Long dossierId;
}