package ma.WhiteLab.service.modules.dashboard_statistiques.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnpaidInvoiceSummaryDTO {
    private Long invoiceId;
    private String patientName;
    private BigDecimal amountDue;
    private LocalDate invoiceDate;
    private long daysOutstanding;
}