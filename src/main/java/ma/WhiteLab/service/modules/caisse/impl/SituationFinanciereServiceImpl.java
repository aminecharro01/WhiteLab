package ma.WhiteLab.service.modules.caisse.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.dossierMedical.Consultation;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.dossierMedical.InterventionMedecin;
import ma.WhiteLab.entities.dossierMedical.SituationFinanciere;
import ma.WhiteLab.entities.enums.PromoStatus;
import ma.WhiteLab.entities.enums.Status;
import ma.WhiteLab.repository.modules.dossierMedical.api.ConsultationRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.DossierMedicalRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.InterventionMedecinRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.SituationFinanciereRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.caisse.api.SituationFinanciereService;

import java.util.List;
import java.util.Optional;

public class SituationFinanciereServiceImpl implements SituationFinanciereService {

    private final RepoFactory<SituationFinanciereRepository> sfRepoFactory;
    private final RepoFactory<InterventionMedecinRepository> interventionRepoFactory;
    private final RepoFactory<ConsultationRepository> consultationRepoFactory;
    private final RepoFactory<DossierMedicalRepository> dossierRepoFactory;

    public SituationFinanciereServiceImpl(RepoFactory<SituationFinanciereRepository> sfRepoFactory, RepoFactory<InterventionMedecinRepository> interventionRepoFactory, RepoFactory<ConsultationRepository> consultationRepoFactory, RepoFactory<DossierMedicalRepository> dossierRepoFactory) {
        this.sfRepoFactory = sfRepoFactory;
        this.interventionRepoFactory = interventionRepoFactory;
        this.consultationRepoFactory = consultationRepoFactory;
        this.dossierRepoFactory = dossierRepoFactory;
    }

    @Override
    public List<SituationFinanciere> findAll() {
        return Transaction.initTransaction(cnx -> {
            SituationFinanciereRepository repo = sfRepoFactory.create(cnx);
            return repo.findAll();
        });
    }

    @Override
    public SituationFinanciere findById(Long id) {
        if (id == null) return null;
        return Transaction.initTransaction(cnx -> {
            SituationFinanciereRepository repo = sfRepoFactory.create(cnx);
            return repo.findById(id);
        });
    }

    @Override
    public void create(SituationFinanciere sf) {
        if (sf == null) throw new IllegalArgumentException("SituationFinanciere invalide");
        Transaction.initTransaction(cnx -> {
            SituationFinanciereRepository repo = sfRepoFactory.create(cnx);
            repo.create(sf);
            return null;
        });
    }

    @Override
    public void update(SituationFinanciere sf) {
        if (sf == null || sf.getId() == null) throw new IllegalArgumentException("SituationFinanciere invalide");
        Transaction.initTransaction(cnx -> {
            SituationFinanciereRepository repo = sfRepoFactory.create(cnx);
            repo.update(sf);
            return null;
        });
    }

    @Override
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("ID invalide");
        Transaction.initTransaction(cnx -> {
            SituationFinanciereRepository repo = sfRepoFactory.create(cnx);
            repo.deleteById(id);
            return null;
        });
    }

    @Override
    public List<SituationFinanciere> findByDossierId(Long dossierId) {
        if (dossierId == null) return List.of();
        return Transaction.initTransaction(cnx -> {
            SituationFinanciereRepository repo = sfRepoFactory.create(cnx);
            return repo.findByDossierId(dossierId);
        });
    }

    @Override
    public List<SituationFinanciere> findByStatus(Status status) {
        if (status == null) return List.of();
        return Transaction.initTransaction(cnx -> {
            SituationFinanciereRepository repo = sfRepoFactory.create(cnx);
            return repo.findByStatus(status.name());
        });
    }

    @Override
    public List<SituationFinanciere> findByPromo(PromoStatus promoStatus) {
        if (promoStatus == null) return List.of();
        return Transaction.initTransaction(cnx -> {
            SituationFinanciereRepository repo = sfRepoFactory.create(cnx);
            return repo.findByPromo(promoStatus.name());
        });
    }

    @Override
    public void calculateCredit(SituationFinanciere sf) {
        if (sf == null) return;
        Float totalDesActes = Optional.ofNullable(sf.getTotalDesActes()).orElse(0f);
        Float totalPaye = Optional.ofNullable(sf.getTotalPaye()).orElse(0f);
        sf.setCredit(totalDesActes - totalPaye);
    }

    @Override
    public void recalculateForDossier(Long dossierId) {
        if (dossierId == null) return;

        Transaction.initTransaction(cnx -> {
            SituationFinanciereRepository sfRepo = sfRepoFactory.create(cnx);
            ConsultationRepository consultationRepo = consultationRepoFactory.create(cnx);
            InterventionMedecinRepository interventionRepo = interventionRepoFactory.create(cnx);

            List<Consultation> consultations = consultationRepo.findByDossierMedicalId(dossierId);
            double totalActes = 0.0;
            if (consultations != null) {
                for (Consultation consultation : consultations) {
                    List<InterventionMedecin> interventions = interventionRepo.findByConsultationId(consultation.getId());
                    if (interventions != null) {
                        totalActes += interventions.stream()
                                .mapToDouble(InterventionMedecin::getPrixDePatient)
                                .sum();
                    }
                }
            }

            List<SituationFinanciere> sfs = sfRepo.findByDossierId(dossierId);
            SituationFinanciere sf = sfs.isEmpty() ? null : sfs.get(0);

            if (sf == null) {
                sf = new SituationFinanciere();
                DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
                DossierMedical dossier = dossierRepo.findById(dossierId);
                if (dossier == null) {
                    throw new IllegalStateException("Le dossier avec l'ID " + dossierId + " n'a pas été trouvé pour le recalcul.");
                }
                sf.setDossierMedical(dossier);
                sf.setTotalPaye(0.0f);
                sf.setStatus(Status.ACTIVE);
                sf.setEnPromo(PromoStatus.AUCUNE);
                sf.setTotalDesActes((float) totalActes);
                calculateCredit(sf);
                sfRepo.create(sf);
            } else {
                sf.setTotalDesActes((float) totalActes);
                calculateCredit(sf);
                sfRepo.update(sf);
            }

            return null;
        });
    }

    @Override
    public SituationFinanciere findByDossierMedicalId(Long dossierId) {
        List<SituationFinanciere> list = findByDossierId(dossierId);
        return list.isEmpty() ? null : list.get(0);
    }
}