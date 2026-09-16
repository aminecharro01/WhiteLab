package ma.WhiteLab.service.modules.dashboard_statistiques.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.agenda.RendezVous;
import ma.WhiteLab.entities.cabinet.Charges;
import ma.WhiteLab.entities.cabinet.Revenus;
import ma.WhiteLab.entities.dossierMedical.Consultation;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.entities.user.Utilisateur;
import ma.WhiteLab.entities.enums.Status;
import ma.WhiteLab.repository.modules.agenda.api.RendezVousRepository;
import ma.WhiteLab.repository.modules.cabinet.api.CabinetMedicaleRepository;
import ma.WhiteLab.repository.modules.cabinet.api.ChargesRepository;
import ma.WhiteLab.repository.modules.cabinet.api.RevenusRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.ConsultationRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.DossierMedicalRepository;
import ma.WhiteLab.repository.modules.patient.api.PatientRepository;
import ma.WhiteLab.repository.modules.user.api.UtilisateurRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.dashboard_statistiques.api.DashboardService;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.DashboardDataDTO;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.RendezVousDTO;
import ma.WhiteLab.service.modules.dashboard_statistiques.dto.UserSummaryDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DashboardServiceImpl implements DashboardService {

    private final RepoFactory<RevenusRepository> revenusRepoFactory;
    private final RepoFactory<ChargesRepository> chargesRepoFactory;
    private final RepoFactory<ConsultationRepository> consultationRepoFactory;
    private final RepoFactory<RendezVousRepository> rendezVousRepoFactory;
    private final RepoFactory<DossierMedicalRepository> dossierRepoFactory;
    private final RepoFactory<PatientRepository> patientRepoFactory;
    private final RepoFactory<UtilisateurRepository> utilisateurRepositoryFactory;
    private final RepoFactory<CabinetMedicaleRepository> cabinetRepoFactory;

    public DashboardServiceImpl(
            RepoFactory<RevenusRepository> revenusRepoFactory,
            RepoFactory<ChargesRepository> chargesRepoFactory,
            RepoFactory<ConsultationRepository> consultationRepoFactory,
            RepoFactory<RendezVousRepository> rendezVousRepoFactory,
            RepoFactory<DossierMedicalRepository> dossierRepoFactory,
            RepoFactory<PatientRepository> patientRepoFactory,
            RepoFactory<UtilisateurRepository> utilisateurRepositoryFactory,
            RepoFactory<CabinetMedicaleRepository> cabinetRepoFactory) {

        this.revenusRepoFactory = revenusRepoFactory;
        this.chargesRepoFactory = chargesRepoFactory;
        this.consultationRepoFactory = consultationRepoFactory;
        this.rendezVousRepoFactory = rendezVousRepoFactory;
        this.dossierRepoFactory = dossierRepoFactory;
        this.patientRepoFactory = patientRepoFactory;
        this.utilisateurRepositoryFactory = utilisateurRepositoryFactory;
        this.cabinetRepoFactory = cabinetRepoFactory;
    }

    @Override
    public DashboardDataDTO getDashboardData(Long cabinetId) {
        return Transaction.initTransaction(cnx -> {

            // === Repositories ===
            RevenusRepository revenusRepo = revenusRepoFactory.create(cnx);
            ChargesRepository chargesRepo = chargesRepoFactory.create(cnx);
            ConsultationRepository consultationRepo = consultationRepoFactory.create(cnx);
            RendezVousRepository rendezVousRepo = rendezVousRepoFactory.create(cnx);
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            PatientRepository patientRepo = patientRepoFactory.create(cnx);
            UtilisateurRepository<Utilisateur> userRepo = utilisateurRepositoryFactory.create(cnx);
            CabinetMedicaleRepository cabinetRepo = cabinetRepoFactory.create(cnx);

            LocalDate today = LocalDate.now();
            DashboardDataDTO dto = new DashboardDataDTO();

            // === Core Data ===
            List<Revenus> allRevenus = revenusRepo.findByCabinetId(cabinetId);
            List<Charges> allCharges = chargesRepo.findByCabinetId(cabinetId);
            List<Consultation> allConsultations = consultationRepo.findAll();
            List<RendezVous> allRendezVous = rendezVousRepo.findAll();
            List<Utilisateur> allUsers = userRepo.findAll();

            // === Lookups ===
            Map<Long, DossierMedical> dossierMap = dossierRepo.findAll().stream()
                    .collect(Collectors.toMap(DossierMedical::getId, Function.identity()));
            Map<Long, Patient> patientMap = patientRepo.findAll().stream()
                    .collect(Collectors.toMap(Patient::getId, Function.identity()));

            // === Stats Calculation ===
            dto.setRecetteDuJour(calculateTotal(allRevenus, today, today));
            dto.setRecetteDuMois(calculateTotal(allRevenus, today.withDayOfMonth(1), today));
            dto.setRecetteDeAnnee(calculateTotal(allRevenus, today.withDayOfYear(1), today));
            dto.setDepensesDuMois(calculateTotalCharges(allCharges, today.withDayOfMonth(1), today));

            dto.setNbrConsultationsDuJour(countConsultations(allConsultations, today, today));
            dto.setNbrConsultationsDuMois(countConsultations(allConsultations, today.withDayOfMonth(1), today));
            dto.setNbrConsultationsDeAnnee(countConsultations(allConsultations, today.withDayOfYear(1), today));

            // === Admin Stats ===
            dto.setTotalUsers(allUsers.size());
            try {
                dto.setTotalCabinets(cabinetRepo.count());
            } catch (Exception e) {
                dto.setTotalCabinets((long) cabinetRepo.findAll().size());
            }

            Map<String, Double> roles = allUsers.stream()
                    .map(u -> u.getType() != null ? u.getType().name() : "INCONNU")
                    .collect(Collectors.groupingBy(r -> r, Collectors.counting()))
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue()));
            dto.setUsersByRole(roles);

            List<UserSummaryDTO> latestUsers = allUsers.stream()
                    .sorted(Comparator.comparing(Utilisateur::getId).reversed())
                    .limit(10)
                    .map(u -> new UserSummaryDTO(
                            u.getId(),
                            (u.getNom() + " " + u.getPrenom()).trim(),
                            u.getEmail(),
                            u.getType() != null ? u.getType().name() : "-"
                    ))
                    .collect(Collectors.toList());
            dto.setLatestUsers(latestUsers);

            // === Rendez-Vous du jour ===
            List<RendezVous> rdvDuJour = allRendezVous.stream()
                    .filter(r -> r.getDate() != null && r.getDate().toLocalDate().isEqual(today))
                    .collect(Collectors.toList());
            dto.setRendezVousDuJour(mapToRendezVousDTO(rdvDuJour, dossierMap, patientMap));

            long fileAttenteCount = rdvDuJour.stream()
                    .filter(r -> r.getStatus() == Status.ACTIVE)
                    .count();
            dto.setFileAttenteCount(fileAttenteCount);

            return dto;
        });
    }

    // === Helpers ===

    private BigDecimal calculateTotal(List<Revenus> revenus, LocalDate start, LocalDate end) {
        return revenus.stream()
                .filter(r -> r.getDate() != null && !r.getDate().toLocalDate().isBefore(start) && !r.getDate().toLocalDate().isAfter(end))
                .map(r -> BigDecimal.valueOf(r.getMontant() != null ? r.getMontant() : 0.0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalCharges(List<Charges> charges, LocalDate start, LocalDate end) {
        return charges.stream()
                .filter(c -> c.getDate() != null && !c.getDate().toLocalDate().isBefore(start) && !c.getDate().toLocalDate().isAfter(end))
                .map(c -> BigDecimal.valueOf(c.getMontant() != null ? c.getMontant() : 0.0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long countConsultations(List<Consultation> consultations, LocalDate start, LocalDate end) {
        return consultations.stream()
                .filter(c -> c.getDate() != null && !c.getDate().toLocalDate().isBefore(start) && !c.getDate().toLocalDate().isAfter(end))
                .count();
    }

    private List<RendezVousDTO> mapToRendezVousDTO(List<RendezVous> rendezVousList, Map<Long, DossierMedical> dossierMap, Map<Long, Patient> patientMap) {
        return rendezVousList.stream()
                .map(rdv -> {
                    String patientName = "N/A";
                    if (rdv.getDossierMed() != null && rdv.getDossierMed().getId() != null) {
                        DossierMedical dm = dossierMap.get(rdv.getDossierMed().getId());
                        if (dm != null && dm.getPat() != null && dm.getPat().getId() != null) {
                            Patient p = patientMap.get(dm.getPat().getId());
                            if (p != null) patientName = p.getNomComplet();
                        }
                    }
                    return new RendezVousDTO(
                            rdv.getTime() != null ? rdv.getTime() : LocalTime.MIDNIGHT,
                            patientName,
                            rdv.getMotif(),
                            rdv.getStatus()
                    );
                })
                .collect(Collectors.toList());
    }
}
