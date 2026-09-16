package ma.WhiteLab.repository.modules.cabinet.api;

import ma.WhiteLab.entities.cabinet.Statistiques;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;

public interface StatistiquesRepository extends CrudRepository<Statistiques, Long> {

    // Méthode spécifique : toutes les statistiques d’un cabinet
    List<Statistiques> findByCabinetId(Long cabinetId);
}
