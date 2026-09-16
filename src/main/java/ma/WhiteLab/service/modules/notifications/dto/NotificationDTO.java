package ma.WhiteLab.service.modules.notifications.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
public class NotificationDTO {

    private Long id;
    private String titre;
    private String message;
    private LocalDate date;
    private LocalTime time;
    private String type;
    private String priorite;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    private String creePar;
    private String modifierPar;
    private boolean isRead;

}