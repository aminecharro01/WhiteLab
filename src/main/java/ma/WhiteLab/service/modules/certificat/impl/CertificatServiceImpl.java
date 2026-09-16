package ma.WhiteLab.service.modules.certificat.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.dossierMedical.Certificat;
import ma.WhiteLab.entities.dossierMedical.Consultation;
import ma.WhiteLab.entities.dossierMedical.DossierMedical;
import ma.WhiteLab.repository.modules.dossierMedical.api.CertificatRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.ConsultationRepository;
import ma.WhiteLab.repository.modules.dossierMedical.api.DossierMedicalRepository;
import ma.WhiteLab.service.modules.certificat.api.CertificatService;
import ma.WhiteLab.service.modules.certificat.api.CertificatValidator;
import ma.WhiteLab.service.modules.certificat.dto.CertificatDTO;
import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.service.common.Transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CertificatServiceImpl implements CertificatService {

    private final RepoFactory<CertificatRepository> certificatRepoFactory;
    private final RepoFactory<ConsultationRepository> consultationRepoFactory;
    private final RepoFactory<DossierMedicalRepository> dossierRepoFactory;
    private final CertificatValidator validator = new CertificatValidatorImpl();

    public CertificatServiceImpl(
            RepoFactory<CertificatRepository> certificatRepoFactory,
            RepoFactory<ConsultationRepository> consultationRepoFactory,
            RepoFactory<DossierMedicalRepository> dossierRepoFactory) {

        this.certificatRepoFactory = certificatRepoFactory;
        this.consultationRepoFactory = consultationRepoFactory;
        this.dossierRepoFactory = dossierRepoFactory;
    }

    private CertificatDTO toDTO(Certificat entity) {
        if (entity == null) {
            return null;
        }
        CertificatDTO dto = new CertificatDTO();
        dto.setId(entity.getId());
        dto.setDateDebut(entity.getDateDebut());
        dto.setDateFin(entity.getDateFin());
        dto.setDureeRepos(entity.getDureeRepos());
        dto.setContenu(entity.getContenu());
        dto.setConsultationId(entity.getConsultation() != null ? entity.getConsultation().getId() : null);
        dto.setDossierMedicalId(entity.getDossierMedical() != null ? entity.getDossierMedical().getId() : null);
        dto.setDateCreation(entity.getDateCreation());
        dto.setDateMiseAJour(entity.getDateMiseAJour());
        dto.setCreePar(entity.getCreePar());
        dto.setModifierPar(entity.getModifierPar());
        return dto;
    }

    private Certificat toEntity(CertificatDTO dto) {
        if (dto == null) {
            return null;
        }
        Certificat entity = new Certificat();
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        entity.setDureeRepos(dto.getDureeRepos());
        entity.setContenu(dto.getContenu());
        entity.setDateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : LocalDateTime.now());
        entity.setDateMiseAJour(dto.getDateMiseAJour());
        entity.setCreePar(dto.getCreePar());
        entity.setModifierPar(dto.getModifierPar());
        return entity;
    }

    @Override
    public List<CertificatDTO> getAllCertificats() {
        return Transaction.initTransaction(cnx -> {
            CertificatRepository repo = certificatRepoFactory.create(cnx);
            List<Certificat> entities = repo.findAll();
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public CertificatDTO getCertificatById(Long id) {
        return Transaction.initTransaction(cnx -> {
            CertificatRepository repo = certificatRepoFactory.create(cnx);
            Certificat entity = repo.findById(id);
            return entity != null ? toDTO(entity) : null;
        });
    }

    @Override
    public CertificatDTO createCertificat(CertificatDTO dto) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            CertificatRepository repo = certificatRepoFactory.create(cnx);
            ConsultationRepository consultRepo = consultationRepoFactory.create(cnx);
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);

            Certificat entity = toEntity(dto);

            if (dto.getConsultationId() != null) {
                Consultation consultation = consultRepo.findById(dto.getConsultationId());
                if (consultation == null) {
                    throw new IllegalArgumentException("Consultation avec l'ID " + dto.getConsultationId() + " n'existe pas.");
                }
                entity.setConsultation(consultation);
            }

            if (dto.getDossierMedicalId() != null) {
                DossierMedical dossier = dossierRepo.findById(dto.getDossierMedicalId());
                if (dossier == null) {
                    throw new IllegalArgumentException("Dossier médical avec l'ID " + dto.getDossierMedicalId() + " n'existe pas.");
                }
                entity.setDossierMedical(dossier);
            }

            repo.create(entity);
            dto.setId(entity.getId());
            return dto;
        });
    }

    @Override
    public CertificatDTO updateCertificat(Long id, CertificatDTO dto) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            CertificatRepository repo = certificatRepoFactory.create(cnx);
            ConsultationRepository consultRepo = consultationRepoFactory.create(cnx);
            DossierMedicalRepository dossierRepo = dossierRepoFactory.create(cnx);

            Certificat existing = repo.findById(id);
            if (existing == null) {
                throw new IllegalArgumentException("Certificat avec l'ID " + id + " n'existe pas.");
            }

            Certificat entity = toEntity(dto);
            entity.setId(id);

            if (dto.getConsultationId() != null) {
                Consultation consultation = consultRepo.findById(dto.getConsultationId());
                if (consultation == null) {
                    throw new IllegalArgumentException("Consultation avec l'ID " + dto.getConsultationId() + " n'existe pas.");
                }
                entity.setConsultation(consultation);
            } else {
                entity.setConsultation(null);
            }

            if (dto.getDossierMedicalId() != null) {
                DossierMedical dossier = dossierRepo.findById(dto.getDossierMedicalId());
                if (dossier == null) {
                    throw new IllegalArgumentException("Dossier médical avec l'ID " + dto.getDossierMedicalId() + " n'existe pas.");
                }
                entity.setDossierMedical(dossier);
            } else {
                entity.setDossierMedical(null);
            }

            repo.update(entity);
            return toDTO(entity);
        });
    }

    @Override
    public void deleteCertificat(Long id) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            CertificatRepository repo = certificatRepoFactory.create(cnx);
            Certificat entity = repo.findById(id);
            if (entity == null) {
                throw new ValidationException("Certificat avec l'ID " + id + " n'existe pas.");
            }
            repo.deleteById(id);
            return null;
        });
    }

    @Override
    public List<CertificatDTO> getCertificatsByDateDebutRange(LocalDate start, LocalDate end) {
        return Transaction.initTransaction(cnx -> {
            CertificatRepository repo = certificatRepoFactory.create(cnx);
            List<Certificat> entities = repo.findByDateDebutRange(start, end);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<CertificatDTO> getCertificatsByDureeRepos(int duree) {
        return Transaction.initTransaction(cnx -> {
            CertificatRepository repo = certificatRepoFactory.create(cnx);
            List<Certificat> entities = repo.findByDureeRepos(duree);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<CertificatDTO> getCertificatsByDossierMedicalId(Long dossierId) {
        return Transaction.initTransaction(cnx -> {
            CertificatRepository repo = certificatRepoFactory.create(cnx);
            List<Certificat> entities = repo.findByDossierMedicalId(dossierId);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<CertificatDTO> getCertificatsByConsultationId(Long consultationId) {
        return Transaction.initTransaction(cnx -> {
            CertificatRepository repo = certificatRepoFactory.create(cnx);
            List<Certificat> entities = repo.findByConsultationId(consultationId);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public ByteArrayInputStream generateCertificatPdf(Long id) throws ValidationException {
        return Transaction.initTransaction(cnx -> {
            CertificatRepository repo = certificatRepoFactory.create(cnx);
            Certificat entity = repo.findById(id);
            if (entity == null) {
                throw new ValidationException("Certificat avec l'ID " + id + " n'existe pas.");
            }
            // Implémentation de génération PDF (ex: utilisant iText ou similaire)
            // Pour cet exemple, un placeholder simple
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Logique de génération PDF ici...
            baos.write(("Certificat ID: " + id + " - Contenu: " + entity.getContenu()).getBytes());
            return new ByteArrayInputStream(baos.toByteArray());
        });
    }
}