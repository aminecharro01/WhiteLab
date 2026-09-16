package ma.WhiteLab.service.modules.dossierMedicale.impl;

import com.itextpdf.text.Paragraph;
import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.agenda.RendezVous;
import ma.WhiteLab.entities.dossierMedical.*;
import ma.WhiteLab.entities.patient.Antecedent;
import ma.WhiteLab.entities.patient.Patient;
import ma.WhiteLab.entities.user.Medecin;
import ma.WhiteLab.repository.modules.dossierMedical.api.*;
import ma.WhiteLab.repository.modules.patient.api.PatientRepository;
import ma.WhiteLab.repository.modules.user.api.UtilisateurRepository;
import ma.WhiteLab.service.modules.dossierMedicale.api.DossierMedicalService;
import ma.WhiteLab.service.modules.dossierMedicale.api.DossierMedicalValidator;
import ma.WhiteLab.service.common.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DossierMedicalServiceImpl implements DossierMedicalService {

    private final RepoFactory<DossierMedicalRepository> dossierRepoFactory;
    private final RepoFactory<ConsultationRepository> consultationRepoFactory;
    private final RepoFactory<OrdonnanceRepository> ordonnanceRepoFactory;
    private final RepoFactory<CertificatRepository> certificatRepoFactory;
    private final RepoFactory<SituationFinanciereRepository> situationRepoFactory;
    private final RepoFactory<PatientRepository> patientRepoFactory;
    private final RepoFactory<UtilisateurRepository<Medecin>> medecinRepoFactory;
    private final RepoFactory<PrescriptionRepository> prescriptionRepoFactory;  // ← ADD THIS LINE
    private final DossierMedicalValidator validator = new DossierMedicalValidatorImpl();

    public DossierMedicalServiceImpl(
            RepoFactory<DossierMedicalRepository> dossierRepoFactory,
            RepoFactory<ConsultationRepository> consultationRepoFactory,
            RepoFactory<OrdonnanceRepository> ordonnanceRepoFactory,
            RepoFactory<CertificatRepository> certificatRepoFactory,
            RepoFactory<SituationFinanciereRepository> situationRepoFactory,
            RepoFactory<PatientRepository> patientRepoFactory,
            RepoFactory<UtilisateurRepository<Medecin>> medecinRepoFactory, RepoFactory<PrescriptionRepository> prescriptionRepoFactory) {

        this.dossierRepoFactory = dossierRepoFactory;
        this.consultationRepoFactory = consultationRepoFactory;
        this.ordonnanceRepoFactory = ordonnanceRepoFactory;
        this.certificatRepoFactory = certificatRepoFactory;
        this.situationRepoFactory = situationRepoFactory;
        this.patientRepoFactory = patientRepoFactory;
        this.medecinRepoFactory = medecinRepoFactory;
        this.prescriptionRepoFactory = prescriptionRepoFactory;
    }

    @Override
    public DossierMedical createDossierForPatient(Long patientId, Long medecinId, String creePar) {
        Map<String, String> errors = validator.validateCreation(patientId, medecinId, creePar);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            PatientRepository patientRepo = patientRepoFactory.create(cnx);
            UtilisateurRepository<Medecin> medecinRepo = medecinRepoFactory.create(cnx);
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);

            Patient patient = patientRepo.findById(patientId);
            if (patient == null) {
                throw new IllegalArgumentException("Patient avec l'ID " + patientId + " n'existe pas.");
            }

            Medecin medecin = medecinRepo.findById(medecinId);
            if (medecin == null) {
                throw new IllegalArgumentException("Médecin avec l'ID " + medecinId + " n'existe pas.");
            }

            if (dossierRepo.findByPatientId(patientId).isPresent()) {
                throw new IllegalStateException("Un dossier médical existe déjà pour ce patient.");
            }

            DossierMedical dossier = DossierMedical.builder()
                    .pat(patient)
                    .medecine(medecin)
                    .historique("")
                    .creePar(creePar.trim())
                    .dateCreation(LocalDateTime.now())
                    .build();

            dossierRepo.create(dossier);
            return dossier;
        });
    }

    @Override
    public DossierMedical updateHistorique(Long dossierId, String nouveauHistorique, String modifierPar) {
        Map<String, String> errors = validator.validateUpdateHistorique(dossierId, nouveauHistorique, modifierPar);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);

            DossierMedical dossier = dossierRepo.findById(dossierId);
            if (dossier == null) {
                throw new IllegalArgumentException("Dossier médical avec l'ID " + dossierId + " n'existe pas.");
            }

            dossier.setHistorique(nouveauHistorique);
            dossier.setModifierPar(modifierPar.trim());
            dossier.setDateMiseAJour(LocalDateTime.now());

            dossierRepo.update(dossier);
            return dossier;
        });
    }

    // ====================== MÉTHODES DE LECTURE ======================

    @Override
    public Optional<DossierMedical> getDossierById(Long id) {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            UtilisateurRepository<Medecin> medecinRepo = medecinRepoFactory.create(cnx);
            PatientRepository patientRepo = patientRepoFactory.create(cnx); // optionnel mais utile

            DossierMedical dossier = dossierRepo.findById(id);
            if (dossier == null) {
                return Optional.empty();
            }

            // Chargement complet du médecin
            if (dossier.getMedecine() != null && dossier.getMedecine().getId() != null) {
                Medecin fullMedecin = medecinRepo.findById(dossier.getMedecine().getId());
                if (fullMedecin != null) {
                    dossier.setMedecine(fullMedecin);
                } else {
                    System.err.println("Médecin non trouvé pour ID: " + dossier.getMedecine().getId());
                }
            }

            // Chargement complet du patient (très recommandé pour l'affichage)
            if (dossier.getPat() != null && dossier.getPat().getId() != null) {
                Patient fullPatient = patientRepo.findById(dossier.getPat().getId());
                if (fullPatient != null) {
                    dossier.setPat(fullPatient);
                }
            }

            return Optional.of(dossier);
        });
    }

    @Override
    public Optional<DossierMedical> getDossierByPatientId(Long patientId) {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            UtilisateurRepository<Medecin> medecinRepo = medecinRepoFactory.create(cnx);
            PatientRepository patientRepo = patientRepoFactory.create(cnx);

            Optional<DossierMedical> optDossier = dossierRepo.findByPatientId(patientId);
            if (optDossier.isEmpty()) {
                return Optional.empty();
            }

            DossierMedical dossier = optDossier.get();

            // Chargement complet du médecin
            if (dossier.getMedecine() != null && dossier.getMedecine().getId() != null) {
                Medecin fullMedecin = medecinRepo.findById(dossier.getMedecine().getId());
                if (fullMedecin != null) {
                    dossier.setMedecine(fullMedecin);
                }
            }

            // Chargement complet du patient
            if (dossier.getPat() != null && dossier.getPat().getId() != null) {
                Patient fullPatient = patientRepo.findById(dossier.getPat().getId());
                if (fullPatient != null) {
                    dossier.setPat(fullPatient);
                }
            }

            return Optional.of(dossier);
        });
    }

    @Override
    public List<DossierMedical> getDossiersByMedecinId(Long medecinId) {
        if (medecinId == null) {
            return List.of();
        }

        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            PatientRepository patientRepo = patientRepoFactory.create(cnx);
            UtilisateurRepository<Medecin> medecinRepo = medecinRepoFactory.create(cnx);

            // 1. Récupère les dossiers bruts (avec seulement les IDs des relations)
            List<DossierMedical> dossiers = dossierRepo.findByMedecinId(medecinId);

            // 2. Pour chaque dossier, charge les entités complètes
            for (DossierMedical dossier : dossiers) {
                // Charge le patient complet
                if (dossier.getPat() != null && dossier.getPat().getId() != null) {
                    Patient fullPatient = patientRepo.findById(dossier.getPat().getId());
                    if (fullPatient != null) {
                        dossier.setPat(fullPatient);
                    }
                }

                // Charge le médecin complet (le connecté)
                Medecin fullMedecin = medecinRepo.findById(medecinId);
                if (fullMedecin != null) {
                    dossier.setMedecine(fullMedecin);
                }
            }

            // Optionnel : filtre les dossiers où patient ou médecin est manquant
            return dossiers.stream()
                    .filter(d -> d.getPat() != null && d.getPat().getNom() != null)
                    .filter(d -> d.getMedecine() != null && d.getMedecine().getNom() != null)
                    .toList();
        });
    }

    @Override
    public List<DossierMedical> getAllDossiers() {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            return dossierRepo.findAll();
        });
    }

    @Override
    public long countDossiers() {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            return dossierRepo.count();
        });
    }

    @Override
    public List<DossierMedical> searchByPatientName(String nomOrPrenom) {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            return dossierRepo.findByPatientName(nomOrPrenom);
        });
    }

    @Override
    public List<DossierMedical> searchByHistorique(String keyword) {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            return dossierRepo.searchByHistorique(keyword);
        });
    }

    @Override
    public List<DossierMedical> findRecentDossiers(int days) {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            return dossierRepo.findRecent(days);
        });
    }

    @Override
    public Patient getPatient(Long dossierId) {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            DossierMedical dossier = dossierRepo.findById(dossierId);
            return dossier != null ? dossier.getPat() : null;
        });
    }

    @Override
    public List<Consultation> getConsultations(Long dossierId) {
        return Transaction.initTransaction(cnx -> {
            ConsultationRepository consultationRepo = consultationRepoFactory.create(cnx);
            return consultationRepo.findByDossierMedicalId(dossierId);
        });
    }


    @Override
    public List<Ordonnance> getOrdonnances(Long dossierId) {
        if (dossierId == null) {
            return List.of();
        }

        return Transaction.initTransaction(cnx -> {
            OrdonnanceRepository ordonnanceRepo = ordonnanceRepoFactory.create(cnx);
            PrescriptionRepository prescriptionRepo = prescriptionRepoFactory.create(cnx);  // ← À ajouter !

            // 1. Récupère toutes les ordonnances du dossier
            List<Ordonnance> ordonnances = ordonnanceRepo.findByDossierId(dossierId);

            // 2. Charge explicitement les prescriptions pour CHAQUE ordonnance
            //    (c'est ce qui manquait !)
            for (Ordonnance ord : ordonnances) {
                List<Prescription> prescriptions = prescriptionRepo.findByOrdonnanceId(ord.getId());
                ord.setPrescriptions(prescriptions != null ? prescriptions : List.of());
            }

            return ordonnances;
        });
    }


    @Override
    public List<Certificat> getCertificats(Long dossierId) {
        return Transaction.initTransaction(cnx -> {
            CertificatRepository certificatRepo = certificatRepoFactory.create(cnx);
            return certificatRepo.findByDossierMedicalId(dossierId);
        });
    }

    @Override
    public List<Antecedent> getAntecedents(Long dossierId) {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            DossierMedical dossier = dossierRepo.findById(dossierId);
            return dossier != null
                    ? dossier.getPat().getAntecedents()
                    : List.of();
        });
    }

    @Override
    public List<RendezVous> getRendezVous(Long dossierId) {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            DossierMedical dossier = dossierRepo.findById(dossierId);
            return dossier != null ? dossier.getRendezVous() : List.of();
        });
    }

    @Override
    public SituationFinanciere getSituationFinanciere(Long dossierId) {
        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            DossierMedical dossier = dossierRepo.findById(dossierId);
            return dossier != null ? dossier.getSituationFinanciere() : null;
        });
    }
    @Override
    public void deleteDossier(Long dossierId) {
        if (dossierId == null) {
            return;
        }

        Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            DossierMedical dossier = dossierRepo.findById(dossierId);
            if (dossier != null) {
                dossierRepo.deleteById(dossierId);  // ← utilise la méthode existante du repository
            }
            return null;
        });
    }

    @Override
    public byte[] exportDossierToPdf(Long dossierId) {
        if (dossierId == null) return new byte[0];

        return Transaction.initTransaction(cnx -> {
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);
            ConsultationRepository consultationRepo = consultationRepoFactory.create(cnx);
            OrdonnanceRepository ordonnanceRepo = ordonnanceRepoFactory.create(cnx);
            PrescriptionRepository prescriptionRepo = prescriptionRepoFactory.create(cnx);
            CertificatRepository certificatRepo = certificatRepoFactory.create(cnx);
            UtilisateurRepository<Medecin> medecinRepo = medecinRepoFactory.create(cnx);
            PatientRepository patientRepo = patientRepoFactory.create(cnx);

            DossierMedical dossier = dossierRepo.findById(dossierId);
            if (dossier == null) return new byte[0];

            // Charge les entités complètes
            if (dossier.getMedecine() != null && dossier.getMedecine().getId() != null) {
                dossier.setMedecine(medecinRepo.findById(dossier.getMedecine().getId()));
            }
            if (dossier.getPat() != null && dossier.getPat().getId() != null) {
                dossier.setPat(patientRepo.findById(dossier.getPat().getId()));
            }

            try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
                document.open();

                // Titre
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                document.add(new com.itextpdf.text.Paragraph("Dossier Médical", titleFont));
                document.add(new com.itextpdf.text.Paragraph("\n"));

                // Patient & médecin
                document.add(new com.itextpdf.text.Paragraph("Patient: " + dossier.getPat().getNom() + " " + dossier.getPat().getPrenom()));
                document.add(new com.itextpdf.text.Paragraph("Médecin: " + dossier.getMedecine().getNom() + " " + dossier.getMedecine().getPrenom()));
                document.add(new com.itextpdf.text.Paragraph("Créé le: " + dossier.getDateCreation()));
                document.add(new com.itextpdf.text.Paragraph("Historique:"));
                document.add(new com.itextpdf.text.Paragraph(dossier.getHistorique()));
                document.add(new com.itextpdf.text.Paragraph("\n"));

                // Consultations
                List<Consultation> consultations = consultationRepo.findByDossierMedicalId(dossierId);
                document.add(new com.itextpdf.text.Paragraph("Consultations:"));
                if (consultations.isEmpty()) {
                    document.add(new com.itextpdf.text.Paragraph("Aucune consultation."));
                } else {
                    for (Consultation c : consultations) {
                        document.add(new com.itextpdf.text.Paragraph("- " + c.getDate() + " : " + c.getObservationsMedecin()));
                    }
                }
                document.add(new com.itextpdf.text.Paragraph("\n"));

                // Ordonnances et prescriptions
                List<Ordonnance> ordonnances = ordonnanceRepo.findByDossierId(dossierId);
                document.add(new com.itextpdf.text.Paragraph("Ordonnances:"));
                if (ordonnances.isEmpty()) {
                    document.add(new com.itextpdf.text.Paragraph("Aucune ordonnance."));
                } else {
                    for (Ordonnance o : ordonnances) {
                        document.add(new com.itextpdf.text.Paragraph("- " + o.getDateOrdonnance()));
                        List<Prescription> prescriptions = prescriptionRepo.findByOrdonnanceId(o.getId());
                        if (!prescriptions.isEmpty()) {
                            for (Prescription p : prescriptions) {
                                Medicament med = p.getMedicament();
                                String medInfo = med != null ? med.getNom() : "Inconnu";
                                document.add(new Paragraph("    * " + medInfo ));
                            }
                        }
                    }
                }
                document.add(new com.itextpdf.text.Paragraph("\n"));

                // Certificats
                List<Certificat> certificats = certificatRepo.findByDossierMedicalId(dossierId);
                document.add(new com.itextpdf.text.Paragraph("Certificats:"));
                if (certificats.isEmpty()) {
                    document.add(new com.itextpdf.text.Paragraph("Aucun certificat."));
                } else {
                    for (Certificat cert : certificats) {
                        document.add(new com.itextpdf.text.Paragraph("- " + cert.getContenu() + " : " + cert.getDureeRepos()));
                    }
                }

                document.close();
                return baos.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
                return new byte[0];
            }
        });
    }


}