package ma.WhiteLab.service.modules.referentiel.impl;

import ma.WhiteLab.entities.enums.Assurance;
import ma.WhiteLab.service.modules.referentiel.api.ReferentielService;

import java.util.Arrays;
import java.util.List;

public class ReferentielServiceImpl implements ReferentielService {

    @Override
    public List<Assurance> getAllAssurances() {
        // Return all enum constants as a list
        return Arrays.asList(Assurance.values());
    }
}
