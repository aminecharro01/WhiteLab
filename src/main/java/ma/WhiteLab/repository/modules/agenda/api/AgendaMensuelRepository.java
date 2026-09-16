package ma.WhiteLab.repository.modules.agenda.api;

import ma.WhiteLab.entities.agenda.AgendaMensuel;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;

public interface AgendaMensuelRepository extends CrudRepository<AgendaMensuel, Long> {

    // Recherche par médecin
    List<AgendaMensuel> findByMedecinId(Long medecinId);

    // Recherche par mois pour un médecin
    List<AgendaMensuel> findByMedecinIdAndMois(Long medecinId, String mois);


}
