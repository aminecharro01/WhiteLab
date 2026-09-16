package ma.WhiteLab.service.modules.referentiel.api;

import ma.WhiteLab.entities.enums.Assurance;
import java.util.List;

public interface ReferentielService {
    /**
     * Retrieve all available Insurance types defined in the system.
     * Since Assurance is an Enum, this is a read-only catalog.
     */
    List<Assurance> getAllAssurances();
}
