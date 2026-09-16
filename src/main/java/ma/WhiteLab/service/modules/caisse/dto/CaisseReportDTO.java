package ma.WhiteLab.service.modules.caisse.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CaisseReportDTO {
    private BigDecimal totalRevenus;
    private BigDecimal totalCharges;
    private BigDecimal solde;
    private Integer month; // null si global
    private Integer year;  // null si global
}