package ma.WhiteLab.service.modules.dashboard_statistiques.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.cabinet.CabinetMedicale;
import ma.WhiteLab.entities.dossierMedical.*;
import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.entities.user.Utilisateur;
import ma.WhiteLab.entities.enums.Status;
import ma.WhiteLab.entities.agenda.RendezVous;
import ma.WhiteLab.entities.cabinet.Charges;
import ma.WhiteLab.entities.cabinet.Revenus;
import ma.WhiteLab.repository.modules.agenda.api.RendezVousRepository;
import ma.WhiteLab.repository.modules.cabinet.api.CabinetMedicaleRepository;
import ma.WhiteLab.repository.modules.cabinet.api.ChargesRepository;
import ma.WhiteLab.repository.modules.cabinet.api.RevenusRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.*;
import ma.WhiteLab.repository.modules.patient.api.PatientRepository;
import ma.WhiteLab.repository.modules.user.api.UtilisateurRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.dashboard_statistiques.api.StatistiquesService;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StatistiquesServiceImpl implements StatistiquesService {

    private final RepoFactory<RevenusRepository> revenusRepoFactory;
    private final RepoFactory<ChargesRepository> chargesRepoFactory;
    private final RepoFactory<FactureRepository> factureRepoFactory;
    private final RepoFactory<PatientRepository> patientRepoFactory;
    private final RepoFactory<ConsultationRepository> consultationRepoFactory;
    private final RepoFactory<RendezVousRepository> rendezVousRepoFactory;
    private final RepoFactory<PrescriptionRepository> prescriptionRepoFactory;
    private final RepoFactory<InterventionMedecinRepository> interventionRepoFactory;
    private final RepoFactory<ActeMedicalRepository> acteRepoFactory;
    private final RepoFactory<OrdonnanceRepository> ordonnanceRepoFactory;
    private final RepoFactory<DossierMedicalRepository> dossierRepoFactory;
    private final RepoFactory<UtilisateurRepository<Utilisateur>> userRepoFactory;
    private final RepoFactory<MedicamentRepository> medicamentRepoFactory;
    private final RepoFactory<CabinetMedicaleRepository> cabinetRepoFactory; // New dependency

    public StatistiquesServiceImpl(
            RepoFactory<RevenusRepository> revenusRepoFactory,
            RepoFactory<ChargesRepository> chargesRepoFactory,
            RepoFactory<FactureRepository> factureRepoFactory,
            RepoFactory<PatientRepository> patientRepoFactory,
            RepoFactory<ConsultationRepository> consultationRepoFactory,
            RepoFactory<RendezVousRepository> rendezVousRepoFactory,
            RepoFactory<PrescriptionRepository> prescriptionRepoFactory,
            RepoFactory<InterventionMedecinRepository> interventionRepoFactory,
            RepoFactory<ActeMedicalRepository> acteRepoFactory,
            RepoFactory<OrdonnanceRepository> ordonnanceRepoFactory,
            RepoFactory<DossierMedicalRepository> dossierRepoFactory,
            RepoFactory<UtilisateurRepository<Utilisateur>> userRepoFactory,
            RepoFactory<MedicamentRepository> medicamentRepoFactory,
            RepoFactory<CabinetMedicaleRepository> cabinetRepoFactory) {
        this.revenusRepoFactory = revenusRepoFactory;
        this.chargesRepoFactory = chargesRepoFactory;
        this.factureRepoFactory = factureRepoFactory;
        this.patientRepoFactory = patientRepoFactory;
        this.consultationRepoFactory = consultationRepoFactory;
        this.rendezVousRepoFactory = rendezVousRepoFactory;
        this.prescriptionRepoFactory = prescriptionRepoFactory;
        this.interventionRepoFactory = interventionRepoFactory;
        this.acteRepoFactory = acteRepoFactory;
        this.ordonnanceRepoFactory = ordonnanceRepoFactory;
        this.dossierRepoFactory = dossierRepoFactory;
        this.userRepoFactory = userRepoFactory;
        this.medicamentRepoFactory = medicamentRepoFactory;
        this.cabinetRepoFactory = cabinetRepoFactory;
    }


    @Override
    public List<TimeSeriesDTO> getRevenueVsExpenseTrend(LocalDate start, LocalDate end) {
        return Transaction.initTransaction(cnx -> {
            RevenusRepository revenusRepo = revenusRepoFactory.create(cnx);
            ChargesRepository chargesRepo = chargesRepoFactory.create(cnx);

            Map<LocalDate, Double> revenueData = revenusRepo.findAll().stream()
                    .filter(r -> r.getDate() != null && !r.getDate().toLocalDate().isBefore(start) && !r.getDate().toLocalDate().isAfter(end))
                    .collect(Collectors.groupingBy(r -> r.getDate().toLocalDate().withDayOfMonth(1), Collectors.summingDouble(r -> r.getMontant() != null ? r.getMontant() : 0.0)));

            Map<LocalDate, Double> expenseData = chargesRepo.findAll().stream()
                    .filter(c -> c.getDate() != null && !c.getDate().toLocalDate().isBefore(start) && !c.getDate().toLocalDate().isAfter(end))
                    .collect(Collectors.groupingBy(c -> c.getDate().toLocalDate().withDayOfMonth(1), Collectors.summingDouble(c -> c.getMontant() != null ? c.getMontant() : 0.0)));

            return List.of(new TimeSeriesDTO("Revenus", revenueData), new TimeSeriesDTO("Dépenses", expenseData));
        });
    }

    @Override
    public List<BreakdownDTO> getRevenueByActeCategory(LocalDate start, LocalDate end) {
        return Transaction.initTransaction(cnx -> {
            ConsultationRepository consultationRepo = consultationRepoFactory.create(cnx);
            ActeMedicalRepository acteRepo = acteRepoFactory.create(cnx);
            InterventionMedecinRepository interventionRepo = interventionRepoFactory.create(cnx);

            Map<Long, Consultation> consultationsById = consultationRepo.findAll().stream()
                    .filter(c -> c.getDate() != null && !c.getDate().toLocalDate().isBefore(start) && !c.getDate().toLocalDate().isAfter(end))
                    .collect(Collectors.toMap(Consultation::getId, c -> c));

            Map<Long, ActeMedical> actesById = acteRepo.findAll().stream()
                    .collect(Collectors.toMap(ActeMedical::getId, a -> a));

            return interventionRepo.findAll().stream()
                    .filter(intervention -> intervention.getConsultation() != null && consultationsById.containsKey(intervention.getConsultation().getId()))
                    .filter(intervention -> intervention.getActeMedical() != null && actesById.containsKey(intervention.getActeMedical().getId()))
                    .map(intervention -> {
                        ActeMedical a = actesById.get(intervention.getActeMedical().getId());
                        if (a.getCategorie() == null) return null;
                        return new BreakdownDTO(a.getCategorie(), intervention.getPrixDePatient());
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.groupingBy(BreakdownDTO::getCategory, Collectors.summingDouble(BreakdownDTO::getValue)))
                    .entrySet().stream()
                    .map(entry -> new BreakdownDTO(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<UnpaidInvoiceSummaryDTO> getUnpaidInvoicesReport() {
        return Transaction.initTransaction(cnx -> {
            FactureRepository factureRepo = factureRepoFactory.create(cnx);
            ConsultationRepository consultationRepo = consultationRepoFactory.create(cnx);
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            PatientRepository patientRepo = patientRepoFactory.create(cnx);

            // Fetch Data
            List<Facture> factures = factureRepo.findAll();
            Map<Long, Consultation> consultMap = consultationRepo.findAll().stream()
                    .collect(Collectors.toMap(Consultation::getId, Function.identity()));
            Map<Long, DossierMedical> dossierMap = dossierRepo.findAll().stream()
                    .collect(Collectors.toMap(DossierMedical::getId, Function.identity()));
            Map<Long, Patient> patientMap = patientRepo.findAll().stream()
                    .collect(Collectors.toMap(Patient::getId, Function.identity()));

            return factures.stream()
                    .filter(f -> f.getStatut() != ma.WhiteLab.entities.enums.StatutFacture.PAYEE)
                    .map(f -> {
                        String patientName = "N/A";
                        
                        // Manual Resolve: Facture -> Consultation -> Dossier -> Patient
                        if (f.getConsultation() != null && f.getConsultation().getId() != null) {
                            Consultation c = consultMap.get(f.getConsultation().getId());
                            if (c != null && c.getDossierMedical() != null && c.getDossierMedical().getId() != null) {
                                DossierMedical d = dossierMap.get(c.getDossierMedical().getId());
                                if (d != null && d.getPat() != null && d.getPat().getId() != null) {
                                    Patient p = patientMap.get(d.getPat().getId());
                                    if (p != null) {
                                        patientName = p.getNomComplet();
                                    }
                                }
                            }
                        }

                        return new UnpaidInvoiceSummaryDTO(
                                f.getId(),
                                patientName,
                                BigDecimal.valueOf(f.getReste()),
                                f.getDate(),
                                f.getDate() != null ? Period.between(f.getDate(), LocalDate.now()).getDays() : 0
                        );
                    })
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<BreakdownDTO> getPatientAgeDistribution() {
        return Transaction.initTransaction(cnx -> {
            PatientRepository patientRepo = patientRepoFactory.create(cnx);
            return patientRepo.findAll().stream()
                    .filter(p -> p.getDateNaissance() != null)
                    .map(p -> Period.between(p.getDateNaissance(), LocalDate.now()).getYears())
                    .collect(Collectors.groupingBy(age -> (age / 10) * 10, Collectors.counting()))
                    .entrySet().stream()
                    .map(entry -> {
                        String ageGroup = entry.getKey() + "-" + (entry.getKey() + 9);
                        return new BreakdownDTO(ageGroup, entry.getValue().doubleValue());
                    })
                    .collect(Collectors.toList());
        });
    }

    @Override
    public TimeSeriesDTO getNewPatientAcquisitionTrend(LocalDate start, LocalDate end) {
        return Transaction.initTransaction(cnx -> {
            PatientRepository patientRepo = patientRepoFactory.create(cnx);
            Map<LocalDate, Double> trend = patientRepo.findAll().stream()
                    .filter(p -> p.getDateCreation() != null && !p.getDateCreation().toLocalDate().isBefore(start) && !p.getDateCreation().toLocalDate().isAfter(end))
                    .collect(Collectors.groupingBy(p -> p.getDateCreation().toLocalDate().withDayOfMonth(1), Collectors.counting()))
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue()));
            return new TimeSeriesDTO("Nouveaux Patients", trend);
        });
    }

    @Override
    public List<BreakdownDTO> getConsultationWorkload(LocalDate start, LocalDate end) {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            UtilisateurRepository<Utilisateur> userRepo = userRepoFactory.create(cnx);
            ConsultationRepository consultationRepo = consultationRepoFactory.create(cnx);

            Map<Long, DossierMedical> dossiersById = dossierRepo.findAll().stream()
                    .collect(Collectors.toMap(DossierMedical::getId, dm -> dm));

            Map<Long, Utilisateur> usersById = userRepo.findAll().stream()
                    .collect(Collectors.toMap(Utilisateur::getId, u -> u));

            return consultationRepo.findAll().stream()
                    .filter(c -> c.getDate() != null && !c.getDate().toLocalDate().isBefore(start) && !c.getDate().toLocalDate().isAfter(end))
                    .map(consultation -> {
                        if (consultation.getDossierMedical() == null) return null;
                        DossierMedical dm = dossiersById.get(consultation.getDossierMedical().getId());
                        if (dm == null || dm.getMedecine() == null) return null;
                        Utilisateur medecin = usersById.get(dm.getMedecine().getId());
                        return (medecin != null) ? medecin.getPrenom() + " " + medecin.getNom() : null;
                    })
                    .filter(nom -> nom != null)
                    .collect(Collectors.groupingBy(nomComplet -> nomComplet, Collectors.counting()))
                    .entrySet().stream()
                    .map(entry -> new BreakdownDTO(entry.getKey(), entry.getValue().doubleValue()))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public AppointmentStatusDTO getAppointmentStatusAnalysis(LocalDate start, LocalDate end) {
        return Transaction.initTransaction(cnx -> {
            RendezVousRepository rendezVousRepo = rendezVousRepoFactory.create(cnx);
            List<RendezVous> appointments = rendezVousRepo.findAll().stream()
                    .filter(r -> r.getDate() != null && !r.getDate().toLocalDate().isBefore(start) && !r.getDate().toLocalDate().isAfter(end))
                    .collect(Collectors.toList());

            long total = appointments.size();
            if (total == 0) return new AppointmentStatusDTO(0, 0, 0, 0, 0.0);

            long active = appointments.stream().filter(r -> r.getStatus() == Status.ACTIVE).count();
            long inactive = appointments.stream().filter(r -> r.getStatus() == Status.INACTIVE).count();
            long other = appointments.stream().filter(r -> r.getStatus() == Status.AUCUNE).count();
            double cancellationRate = (total > 0) ? (double) other / total * 100 : 0.0;

            return new AppointmentStatusDTO(total, active, inactive, other, cancellationRate);
        });
    }

    @Override
    public List<BreakdownDTO> getTopPrescribedMedications(LocalDate start, LocalDate end, int limit) {
        return Transaction.initTransaction(cnx -> {
            OrdonnanceRepository ordonnanceRepo = ordonnanceRepoFactory.create(cnx);
            MedicamentRepository medicamentRepo = medicamentRepoFactory.create(cnx);
            PrescriptionRepository prescriptionRepo = prescriptionRepoFactory.create(cnx);

            Map<Long, Ordonnance> ordonnancesById = ordonnanceRepo.findAll().stream()
                    .filter(o -> o.getDateOrdonnance() != null && !o.getDateOrdonnance().isBefore(start) && !o.getDateOrdonnance().isAfter(end))
                    .collect(Collectors.toMap(Ordonnance::getId, o -> o));

            Map<Long, Medicament> medicamentsById = medicamentRepo.findAll().stream()
                    .collect(Collectors.toMap(Medicament::getId, m -> m));

            return prescriptionRepo.findAll().stream()
                    .filter(p -> p.getOrdonnance() != null && ordonnancesById.containsKey(p.getOrdonnance().getId()))
                    .filter(p -> p.getMedicament() != null && medicamentsById.containsKey(p.getMedicament().getId()))
                    .map(p -> {
                        Medicament med = medicamentsById.get(p.getMedicament().getId());
                        if (med == null || med.getNom() == null) return null;
                        return new AbstractMap.SimpleEntry<>(med.getNom(), p.getQte());
                    })
                    .filter(entry -> entry != null)
                    .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> new BreakdownDTO(entry.getKey(), entry.getValue().doubleValue()))
                    .collect(Collectors.toList());
        });
    }

    // ================= ADMIN STATS IMPL =================

    @Override
    public List<BreakdownDTO> getUsersByRoleDistribution() {
        return Transaction.initTransaction(cnx -> {
            UtilisateurRepository<Utilisateur> userRepo = userRepoFactory.create(cnx);
            return userRepo.findAll().stream()
                    .map(u -> u.getType() != null ? u.getType().name() : "INCONNU")
                    .collect(Collectors.groupingBy(role -> role, Collectors.counting()))
                    .entrySet().stream()
                    .map(entry -> new BreakdownDTO(entry.getKey(), entry.getValue().doubleValue()))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<BreakdownDTO> getCabinetsByCategoryDistribution() {
        return Transaction.initTransaction(cnx -> {
            CabinetMedicaleRepository cabinetRepo = cabinetRepoFactory.create(cnx);
            return cabinetRepo.findAll().stream()
                    .map(c -> c.getCategorie() != null && !c.getCategorie().isBlank() ? c.getCategorie() : "NON DÉFINI")
                    .collect(Collectors.groupingBy(cat -> cat, Collectors.counting()))
                    .entrySet().stream()
                    .map(entry -> new BreakdownDTO(entry.getKey(), entry.getValue().doubleValue()))
                    .collect(Collectors.toList());
        });
    }
}