package ma.WhiteLab.service.modules.caisse.api;

import ma.WhiteLab.entities.cabinet.Revenus;

import java.time.LocalDateTime;
import java.util.List;

public interface RevenusService {

    List<Revenus> findAll();

    Revenus findById(Long id);

    void create(Revenus revenus);

    void update(Revenus revenus);

    void delete(Long id);

    List<Revenus> findByCabinetId(Long cabinetId);

    List<Revenus> findByDateBetween(LocalDateTime start, LocalDateTime end);

    List<Revenus> findByTitre(String titre);

    // Business methods
    double calculateTotalRevenus(Long cabinetId);
}
