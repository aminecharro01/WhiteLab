package ma.WhiteLab.service.modules.dashboard_statistiques.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.dossierMedical.InterventionMedecin;
import ma.WhiteLab.repository.modules.agenda.api.RendezVousRepository;
import ma.WhiteLab.repository.modules.cabinet.api.RevenusRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.InterventionMedecinRepository;
import ma.WhiteLab.repository.modules.patient.api.PatientRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.dashboard_statistiques.api.DashboardStatistiquesService;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.ChiffreAffairesParMoisDTO;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.DashboardGlobalDTO;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.TopActeDTO;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Refactored implementation that calculates statistics directly from source repositories.
 */
public class DashboardStatistiquesServiceImpl implements DashboardStatistiquesService {

    private final RepoFactory<RevenusRepository> revenusRepoFactory;
    private final RepoFactory<PatientRepository> patientRepoFactory;
    private final RepoFactory<RendezVousRepository> rendezVousRepoFactory;
    private final RepoFactory<InterventionMedecinRepository> interventionRepoFactory;

    public DashboardStatistiquesServiceImpl(
            RepoFactory<RevenusRepository> revenusRepoFactory,
            RepoFactory<PatientRepository> patientRepoFactory,
            RepoFactory<RendezVousRepository> rendezVousRepoFactory,
            RepoFactory<InterventionMedecinRepository> interventionRepoFactory) {
        this.revenusRepoFactory = revenusRepoFactory;
        this.patientRepoFactory = patientRepoFactory;
        this.rendezVousRepoFactory = rendezVousRepoFactory;
        this.interventionRepoFactory = interventionRepoFactory;
    }

    @Override
    public DashboardGlobalDTO getStatistiquesGlobales(Long cabinetId, LocalDate dateDebut, LocalDate dateFin) {
        return Transaction.initTransaction(cnx -> {
            RevenusRepository revenusRepo = revenusRepoFactory.create(cnx);
            PatientRepository patientRepo = patientRepoFactory.create(cnx);
            RendezVousRepository rendezVousRepo = rendezVousRepoFactory.create(cnx);
            InterventionMedecinRepository interventionRepo = interventionRepoFactory.create(cnx);

            // Note: The cabinetId is not used as repositories don't filter by it yet. This can be added later.
            double caTotal = revenusRepo.findAll().stream()
                    .filter(r -> !r.getDate().toLocalDate().isBefore(dateDebut) && !r.getDate().toLocalDate().isAfter(dateFin))
                    .mapToDouble(r -> r.getMontant())
                    .sum();

            long nombrePatients = patientRepo.findAll().stream()
                    .filter(p -> p.getDateCreation() != null && !p.getDateCreation().toLocalDate().isBefore(dateDebut) && !p.getDateCreation().toLocalDate().isAfter(dateFin))
                    .count();

            long nombreRendezVous = rendezVousRepo.findAll().stream()
                    .filter(r -> !r.getDate().toLocalDate().isBefore(dateDebut) && !r.getDate().toLocalDate().isAfter(dateFin))
                    .count();
            
            long nombreActes = interventionRepo.findAll().stream()
                    .filter(i -> i.getConsultation() != null && !i.getConsultation().getDate().toLocalDate().isBefore(dateDebut) && !i.getConsultation().getDate().toLocalDate().isAfter(dateFin))
                    .count();

            return new DashboardGlobalDTO(caTotal, nombrePatients, nombreRendezVous, nombreActes);
        });
    }

    @Override
    public List<ChiffreAffairesParMoisDTO> getChiffreAffairesParMois(Long cabinetId, int annee) {
        return Transaction.initTransaction(cnx -> {
            RevenusRepository revenusRepo = revenusRepoFactory.create(cnx);
            return revenusRepo.findAll().stream()
                    .filter(r -> r.getDate().toLocalDate().getYear() == annee)
                    .collect(Collectors.groupingBy(r -> r.getDate().toLocalDate().getMonthValue(), Collectors.summingDouble(r -> r.getMontant())))
                    .entrySet().stream()
                    .map(entry -> new ChiffreAffairesParMoisDTO(entry.getKey(), entry.getValue()))
                    .sorted(Comparator.comparingInt(ChiffreAffairesParMoisDTO::getMois))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<TopActeDTO> getTopActes(Long cabinetId, LocalDate dateDebut, LocalDate dateFin, int limit) {
        return Transaction.initTransaction(cnx -> {
            InterventionMedecinRepository interventionRepo = interventionRepoFactory.create(cnx);
            
            Map<String, Double> revenueByActe = interventionRepo.findAll().stream()
                .filter(i -> i.getConsultation() != null && !i.getConsultation().getDate().toLocalDate().isBefore(dateDebut) && !i.getConsultation().getDate().toLocalDate().isAfter(dateFin))
                .filter(i -> i.getActeMedical() != null)
                .collect(Collectors.groupingBy(i -> i.getActeMedical().getLibelle(), Collectors.summingDouble(InterventionMedecin::getPrixDePatient)));

            Map<String, Long> countByActe = interventionRepo.findAll().stream()
                .filter(i -> i.getConsultation() != null && !i.getConsultation().getDate().toLocalDate().isBefore(dateDebut) && !i.getConsultation().getDate().toLocalDate().isAfter(dateFin))
                .filter(i -> i.getActeMedical() != null)
                .collect(Collectors.groupingBy(i -> i.getActeMedical().getLibelle(), Collectors.counting()));
                
            return countByActe.entrySet().stream()
                    .map(entry -> new TopActeDTO(
                        entry.getKey(),
                        entry.getValue(),
                        revenueByActe.getOrDefault(entry.getKey(), 0.0)
                    ))
                    .sorted(Comparator.comparingLong(TopActeDTO::getNombre).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        });
    }
}
