package ma.WhiteLab.service.modules.patient.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.enums.EtatDent;
import ma.WhiteLab.entities.patient.Dent;
import ma.WhiteLab.repository.modules.patient.api.DentRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.patient.api.DentService;
import ma.WhiteLab.service.modules.patient.dto.DentDTO;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DentServiceImpl implements DentService {

    /** Adult permanent dentition, FDI two-digit notation. */
    private static final int[] FDI_NUMBERS = {
            18, 17, 16, 15, 14, 13, 12, 11, 21, 22, 23, 24, 25, 26, 27, 28,
            48, 47, 46, 45, 44, 43, 42, 41, 31, 32, 33, 34, 35, 36, 37, 38
    };

    private final RepoFactory<DentRepository> dentRepoFactory;

    public DentServiceImpl(RepoFactory<DentRepository> dentRepoFactory) {
        this.dentRepoFactory = dentRepoFactory;
    }

    private DentDTO toDTO(Dent d) {
        return new DentDTO(d.getId(), d.getPatientId(), d.getNumero(),
                d.getEtat() != null ? d.getEtat().name() : EtatDent.SAIN.name(), d.getNote());
    }

    @Override
    public List<DentDTO> getOdontogramme(Long patientId) {
        return Transaction.initTransaction(cnx -> {
            DentRepository repo = dentRepoFactory.create(cnx);
            List<Dent> existing = repo.findByPatientId(patientId);

            Map<Integer, Dent> byNumero = new LinkedHashMap<>();
            for (Dent d : existing) byNumero.put(d.getNumero(), d);

            return java.util.Arrays.stream(FDI_NUMBERS)
                    .mapToObj(numero -> {
                        Dent d = byNumero.get(numero);
                        if (d == null) {
                            d = Dent.builder().patientId(patientId).numero(numero).etat(EtatDent.SAIN).build();
                        }
                        return toDTO(d);
                    })
                    .collect(Collectors.toList());
        });
    }

    @Override
    public void updateEtat(Long patientId, int numero, EtatDent etat, String note, String modifiePar) {
        Transaction.initTransaction(cnx -> {
            DentRepository repo = dentRepoFactory.create(cnx);
            Dent d = Dent.builder()
                    .patientId(patientId)
                    .numero(numero)
                    .etat(etat)
                    .note(note)
                    .creePar(modifiePar)
                    .modifierPar(modifiePar)
                    .build();
            repo.upsert(d);
            return null;
        });
    }
}
