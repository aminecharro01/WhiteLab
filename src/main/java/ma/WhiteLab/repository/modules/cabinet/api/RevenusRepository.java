package ma.WhiteLab.repository.modules.cabinet.api;

import ma.WhiteLab.entities.cabinet.Revenus;
import ma.WhiteLab.repository.common.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RevenusRepository extends CrudRepository<Revenus, Long> {

    // Récupérer tous les revenus d'un cabinet spécifique
    List<Revenus> findByCabinetId(Long cabinetId);

    // Récupérer les revenus entre deux dates
    List<Revenus> findByDateBetween(LocalDateTime start, LocalDateTime end);

    // Récupérer les revenus selon un titre
    List<Revenus> findByTitre(String titre);
}
