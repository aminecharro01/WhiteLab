package ma.WhiteLab.repository.modules.dashboard.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DashboardRepository {

    BigDecimal getTotalRevenus(Long cabinetId, LocalDate start, LocalDate end);

    BigDecimal getTotalCharges(Long cabinetId, LocalDate start, LocalDate end);

    int getTotalConsultations(LocalDate start, LocalDate end);

    int getTotalPatients(LocalDate start, LocalDate end);
}
