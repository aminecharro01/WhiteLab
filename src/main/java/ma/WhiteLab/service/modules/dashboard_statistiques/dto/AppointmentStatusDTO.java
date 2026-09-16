package ma.WhiteLab.service.modules.dashboard_statistiques.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import ma.WhiteLab.entities.enums.Status;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentStatusDTO {
    private long totalAppointments;
    private long activeCount;
    private long inactiveCount;
    private long otherCount;
    private double cancellationRate;
}