package ma.WhiteLab.service.modules.caisse.api;

import ma.WhiteLab.entities.cabinet.Charges;

import java.time.LocalDateTime;
import java.util.List;

public interface ChargesService {

    List<Charges> findAll();

    Charges findById(Long id);

    void create(Charges charges);

    void update(Charges charges);

    void delete(Long id);

    List<Charges> findByCabinetId(Long cabinetId);

    List<Charges> findByDateBetween(LocalDateTime start, LocalDateTime end);

    // Business methods
    double calculateTotalCharges(Long cabinetId);
}
