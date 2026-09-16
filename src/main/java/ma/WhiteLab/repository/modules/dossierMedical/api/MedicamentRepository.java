package ma.WhiteLab.repository.modules.dossierMedical.api;

import ma.WhiteLab.entities.dossierMedical.Medicament;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;

public interface MedicamentRepository extends CrudRepository<Medicament, Long> {

    // Recherche exacte
    List<Medicament> searchByName(String nom);

    // Recherche partielle (LIKE % %)
    List<Medicament> searchByNameLike(String text);

    // Filtrer par laboratoire
    List<Medicament> findByLabo(String labo);

    // Médicaments remboursables uniquement
    List<Medicament> findRemboursables();

    // Filtrer par forme
    List<Medicament> findByForme(String forme);

    // Filtrer par intervalle de prix
    List<Medicament> findByPrixBetween(double min, double max);

    long count();

    long countRemboursables();

    double getPrixMoyen();

    Medicament findTopByPrixDesc();

    Medicament findTopByPrixAsc();
}
