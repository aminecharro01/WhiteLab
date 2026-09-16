package ma.WhiteLab.repository.modules.patient.api;

import ma.WhiteLab.entities.patient.Antecedent;
import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.entities.enums.CategorieAntecedent;
import ma.WhiteLab.entities.enums.NiveauDeRisk;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AntecedentRepository extends CrudRepository<Antecedent, Long> {

    // Rechercher un antécédent par son nom
    Optional<Antecedent> findByNom(String nom);

    // Filtrer par catégorie
    List<Antecedent> findByCategorie(CategorieAntecedent categorie);

    // Filtrer par niveau de risque
    List<Antecedent> findByNiveauRisque(NiveauDeRisk niveau);

    // Vérifier l'existence par ID
    boolean existsById(Long id);

    // Compter le nombre total d'antécédents
    long count();

    // Pagination
    List<Antecedent> findPage(int limit, int offset);

    // Récupérer les patients ayant cet antécédent
    List<Patient> getPatientsHavingAntecedent(Long antecedentId);

    List<Antecedent> getAntecedentsByPatientId(Long id);
}
