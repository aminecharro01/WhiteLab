package ma.WhiteLab.service.modules.agendas.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class AgendaMensuelDTO {

    private Long id;
    private String mois; // String representation of Mois enum
    private Long medecinId;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    private String creePar;
    private String modifierPar;
    private List<String> joursNonDisponible; // List of Jour enum names as strings

    // Getters and Setters

}