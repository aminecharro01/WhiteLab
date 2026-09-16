package ma.WhiteLab.repository.modules.cabinet.api;

import ma.WhiteLab.entities.cabinet.Charges;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;

public interface ChargesRepository extends CrudRepository<Charges, Long> {

    // Récupérer toutes les charges pour un cabinet spécifique
    List<Charges> findByCabinetId(Long cabinetId);

    // Récupérer toutes les charges dans une période donnée
    List<Charges> findByDateBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
