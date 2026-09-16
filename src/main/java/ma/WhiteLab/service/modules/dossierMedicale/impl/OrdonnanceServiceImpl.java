package ma.WhiteLab.service.modules.dossierMedicale.impl;

import ma.WhiteLab.common.consoleLog.ConsoleLogger;
import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.entities.dossierMedical.Ordonnance;
import ma.WhiteLab.entities.dossierMedical.Prescription;
import ma.WhiteLab.repository.modules.dossierMedical.api.ConsultationRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.DossierMedicalRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.OrdonnanceRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.PrescriptionRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.dossierMedicale.api.OrdonnanceService;
import ma.WhiteLab.service.modules.dossierMedicale.api.OrdonnanceValidator;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class OrdonnanceServiceImpl implements OrdonnanceService {

    private final RepoFactory<OrdonnanceRepository> ordonnanceRepoFactory;
    private final RepoFactory<DossierMedicalRepository> dossierMedicalRepoFactory;
    private final RepoFactory<PrescriptionRepository> prescriptionRepoFactory;
    private final RepoFactory<ConsultationRepository> consultationRepoFactory;

    private final OrdonnanceValidator validator;

    public OrdonnanceServiceImpl(
            RepoFactory<OrdonnanceRepository> ordonnanceRepoFactory,
            RepoFactory<DossierMedicalRepository> dossierMedicalRepoFactory,
            RepoFactory<PrescriptionRepository> prescriptionRepoFactory,
            RepoFactory<ConsultationRepository> consultationRepoFactory) {

        this.ordonnanceRepoFactory = ordonnanceRepoFactory;
        this.dossierMedicalRepoFactory = dossierMedicalRepoFactory;
        this.prescriptionRepoFactory = prescriptionRepoFactory;
        this.consultationRepoFactory = consultationRepoFactory;

        // IMPORTANT : On passe les FACTORIES au validateur (pas les repositories)
        this.validator = new OrdonnanceValidatorImpl(
                dossierMedicalRepoFactory,
                consultationRepoFactory
        );
    }

    /* ================= CRUD AVEC VALIDATION ================= */

    @Override
    public void create(Ordonnance o) {
        Map<String, String> errors = validator.validateCreation(o);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Erreurs de validation de l'ordonnance : " + errors);
        }

        Transaction.initTransaction(cnx -> {
            OrdonnanceRepository ordRepo = ordonnanceRepoFactory.create(cnx);
            PrescriptionRepository prescRepo = prescriptionRepoFactory.create(cnx);

            ordRepo.create(o);

            if (o.getPrescriptions() != null && !o.getPrescriptions().isEmpty()) {
                for (Prescription p : o.getPrescriptions()) {
                    p.setOrdonnance(o);
                    prescRepo.create(p);
                }
            }
            return null;
        });
    }

    @Override
    public void update(Ordonnance o) {
        Map<String, String> errors = validator.validateUpdate(o);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Erreurs de validation de l'ordonnance : " + errors);
        }

        Transaction.initTransaction(cnx -> {
            OrdonnanceRepository ordRepo = ordonnanceRepoFactory.create(cnx);
            PrescriptionRepository prescRepo = prescriptionRepoFactory.create(cnx);

            ordRepo.update(o);

            if (o.getPrescriptions() != null) {
                for (Prescription p : o.getPrescriptions()) {
                    p.setOrdonnance(o);
                    if (p.getId() != null) {
                        prescRepo.update(p);
                    } else {
                        prescRepo.create(p);
                    }
                }
            }
            return null;
        });
    }

    @Override
    public List<Ordonnance> getAll() {
        return Transaction.initTransaction(cnx -> {
            OrdonnanceRepository ordRepo = ordonnanceRepoFactory.create(cnx);
            PrescriptionRepository prescRepo = prescriptionRepoFactory.create(cnx);

            List<Ordonnance> list = ordRepo.findAll();
            list.forEach(o -> o.setPrescriptions(prescRepo.findByOrdonnanceId(o.getId())));
            return list;
        });
    }

    @Override
    public Ordonnance getById(Long id) {
        return Transaction.initTransaction(cnx -> {
            OrdonnanceRepository ordRepo = ordonnanceRepoFactory.create(cnx);
            PrescriptionRepository prescRepo = prescriptionRepoFactory.create(cnx);

            Ordonnance o = ordRepo.findById(id);
            if (o != null) {
                o.setPrescriptions(prescRepo.findByOrdonnanceId(o.getId()));
            }
            return o;
        });
    }

    @Override
    public void delete(Long id) {
        Transaction.initTransaction(cnx -> {
            PrescriptionRepository prescRepo = prescriptionRepoFactory.create(cnx);
            OrdonnanceRepository ordRepo = ordonnanceRepoFactory.create(cnx);

            List<Prescription> prescriptions = prescRepo.findByOrdonnanceId(id);
            for (Prescription p : prescriptions) {
                prescRepo.deleteById(p.getId());
            }

            ordRepo.deleteById(id);
            return null;
        });
    }

    @Override
    public Integer count() {
        return Transaction.initTransaction(cnx -> {
            OrdonnanceRepository repo = ordonnanceRepoFactory.create(cnx);
            // À améliorer : implémenter une vraie méthode count() dans le repository
            return repo.findAll().size();
        });
    }

    /* ================= RECHERCHES SPÉCIFIQUES ================= */

    @Override
    public List<Ordonnance> findByDossierId(Long dossierId) {
        return Transaction.initTransaction(cnx -> {
            OrdonnanceRepository ordRepo = ordonnanceRepoFactory.create(cnx);
            PrescriptionRepository prescRepo = prescriptionRepoFactory.create(cnx);

            List<Ordonnance> list = ordRepo.findByDossierId(dossierId);
            list.forEach(o -> o.setPrescriptions(prescRepo.findByOrdonnanceId(o.getId())));
            return list;
        });
    }

    @Override
    public List<Ordonnance> findByConsultationId(Long consultationId) {
        return Transaction.initTransaction(cnx -> {
            OrdonnanceRepository ordRepo = ordonnanceRepoFactory.create(cnx);
            PrescriptionRepository prescRepo = prescriptionRepoFactory.create(cnx);

            List<Ordonnance> list = ordRepo.findByConsultationId(consultationId);
            list.forEach(o -> o.setPrescriptions(prescRepo.findByOrdonnanceId(o.getId())));
            return list;
        });
    }

    @Override
    public List<Ordonnance> findByDate(LocalDate date) {
        return Transaction.initTransaction(cnx -> {
            OrdonnanceRepository ordRepo = ordonnanceRepoFactory.create(cnx);
            PrescriptionRepository prescRepo = prescriptionRepoFactory.create(cnx);

            List<Ordonnance> list = ordRepo.findByDate(date);
            list.forEach(o -> o.setPrescriptions(prescRepo.findByOrdonnanceId(o.getId())));
            return list;
        });
    }

    @Override
    public List<Ordonnance> findBetweenDates(LocalDate start, LocalDate end) {
        return Transaction.initTransaction(cnx -> {
            OrdonnanceRepository ordRepo = ordonnanceRepoFactory.create(cnx);
            PrescriptionRepository prescRepo = prescriptionRepoFactory.create(cnx);

            List<Ordonnance> list = ordRepo.findBetweenDates(start, end);
            list.forEach(o -> o.setPrescriptions(prescRepo.findByOrdonnanceId(o.getId())));
            return list;
        });
    }

    @Override
    public List<Ordonnance> findByMedecinId(Long medecinId) {
        return getAll().stream()
                .filter(o -> o.getDossierMedical() != null &&
                        o.getDossierMedical().getMedecine() != null &&
                        medecinId.equals(o.getDossierMedical().getMedecine().getId()))
                .toList();
    }

    @Override
    public List<Ordonnance> findByPatientId(Long patientId) {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dmRepo = dossierMedicalRepoFactory.create(cnx);

            DossierMedical dossier = dmRepo.findByPatientId(patientId)
                    .orElse(null);

            if (dossier == null) {
                return List.of();
            }

            return findByDossierId(dossier.getId());
        });
    }

    @Override
    public List<Ordonnance> findRecent(int days) {
        LocalDate start = LocalDate.now().minusDays(days);
        return findBetweenDates(start, LocalDate.now());
    }

    /* ================= TÉLÉCHARGEMENT PDF ================= */

    @Override
    public byte[] downloadOrdonnance(Long ordonnanceId) {
        Ordonnance o = getById(ordonnanceId);
        if (o == null) {
            throw new IllegalArgumentException("Ordonnance introuvable avec l'ID : " + ordonnanceId);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("ORDONNANCE MÉDICALE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            document.add(new Paragraph("Date : " + o.getDateOrdonnance()));
            document.add(new Paragraph(" "));

            String patientName = (o.getDossierMedical() != null && o.getDossierMedical().getPat() != null)
                    ? o.getDossierMedical().getPat().getNom().toUpperCase() + " " + o.getDossierMedical().getPat().getPrenom()
                    : "Patient inconnu";
            document.add(new Paragraph("Patient : " + patientName));

            String medecinName = (o.getDossierMedical() != null && o.getDossierMedical().getMedecine() != null)
                    ? "Dr. " + o.getDossierMedical().getMedecine().getNom() + " " + o.getDossierMedical().getMedecine().getPrenom()
                    : "Médecin inconnu";
            document.add(new Paragraph("Médecin : " + medecinName));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("__________________________________________________________________"));
            document.add(new Paragraph(" "));

            Paragraph prescTitle = new Paragraph("Prescriptions", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            document.add(prescTitle);
            document.add(new Paragraph(" "));

            if (o.getPrescriptions() != null && !o.getPrescriptions().isEmpty()) {
                for (Prescription p : o.getPrescriptions()) {
                    String medic = p.getMedicament() != null ? p.getMedicament().getNom() : "Médicament inconnu";
                    document.add(new Paragraph("• " + medic));
                    document.add(new Paragraph("  Posologie : " + p.getFrequence() + " — Durée : " + p.getDuree() + " jours"));
                    document.add(new Paragraph("  Quantité : " + p.getQte() + " unités"));
                    document.add(new Paragraph(" "));
                }
            } else {
                document.add(new Paragraph("Aucune prescription associée."));
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF de l'ordonnance", e);
        }
    }
}