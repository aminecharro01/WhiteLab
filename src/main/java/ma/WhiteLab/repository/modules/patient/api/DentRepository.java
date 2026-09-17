package ma.WhiteLab.repository.modules.patient.api;

import ma.WhiteLab.entities.patient.Dent;
import ma.WhiteLab.repository.common.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DentRepository extends CrudRepository<Dent, Long> {

    List<Dent> findByPatientId(Long patientId);

    Optional<Dent> findByPatientIdAndNumero(Long patientId, int numero);

    /** Insert if no row exists for (patientId, numero), otherwise update its état. */
    void upsert(Dent dent);
}
