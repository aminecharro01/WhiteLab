package ma.WhiteLab.repository.modules.cabinet.api;

import ma.WhiteLab.entities.cabinet.CabinetMedicale;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CabinetMedicaleRepository extends CrudRepository<CabinetMedicale, Long> {
    // Recherche par nom exact
    Optional<CabinetMedicale> findByNom(String nom);

    // Recherche par catégorie
    List<CabinetMedicale> findByCategorie(String categorie);

    // Recherche par email
    Optional<CabinetMedicale> findByEmail(String email);

    // Liste tous les cabinets triés par nom
    List<CabinetMedicale> findAllOrderByNom();

    long count();
}
