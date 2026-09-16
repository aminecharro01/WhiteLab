package ma.WhiteLab.service.modules.agendas.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.agenda.AgendaMensuel;
import ma.WhiteLab.entities.enums.Jour;
import ma.WhiteLab.entities.enums.Mois;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.entities.user.Medecin;
import ma.WhiteLab.entities.user.Utilisateur;
import ma.WhiteLab.mvc.dto.auth.UserPrincipal;
import ma.WhiteLab.repository.modules.agenda.api.AgendaMensuelRepository;
import ma.WhiteLab.repository.modules.user.api.UtilisateurRepository;
import ma.WhiteLab.service.modules.agendas.api.AgendaMensuelService;
import ma.WhiteLab.service.modules.agendas.api.AgendaMensuelValidator;
import ma.WhiteLab.service.modules.agendas.dto.AgendaMensuelDTO;
import ma.WhiteLab.common.exceptions.ValidationException;
import ma.WhiteLab.service.common.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AgendaMensuelServiceImpl implements AgendaMensuelService {

    private final RepoFactory<AgendaMensuelRepository> agendaRepoFactory;
    private final RepoFactory<UtilisateurRepository> medecinRepoFactory;
    private final AgendaMensuelValidator validator = new AgendaMensuelValidatorImpl();

    public AgendaMensuelServiceImpl(
            RepoFactory<AgendaMensuelRepository> agendaRepoFactory,
            RepoFactory<UtilisateurRepository> medecinRepoFactory) {

        this.agendaRepoFactory = agendaRepoFactory;
        this.medecinRepoFactory = medecinRepoFactory;
    }

    private AgendaMensuelDTO toDTO(AgendaMensuel entity) {
        if (entity == null) {
            return null;
        }
        AgendaMensuelDTO dto = new AgendaMensuelDTO();
        dto.setId(entity.getId());
        dto.setMois(entity.getMois() != null ? entity.getMois().name() : null);
        dto.setMedecinId(entity.getMedecin() != null ? entity.getMedecin().getId() : null);
        dto.setDateCreation(entity.getDateCreation());
        dto.setDateMiseAJour(entity.getDateMiseAJour());
        dto.setCreePar(entity.getCreePar());
        dto.setModifierPar(entity.getModifierPar());
        dto.setJoursNonDisponible(entity.getJoursNonDisponible() != null ?
                entity.getJoursNonDisponible().stream().map(Jour::name).collect(Collectors.toList()) : null);
        return dto;
    }

    private AgendaMensuel toEntity(AgendaMensuelDTO dto) {
        if (dto == null) {
            return null;
        }
        AgendaMensuel entity = new AgendaMensuel();

        // The DTO now provides the month as an enum name (e.g., "JANVIER")
        if (dto.getMois() != null && !dto.getMois().isBlank()) {
             try {
                entity.setMois(Mois.valueOf(dto.getMois().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("La valeur du mois '" + dto.getMois() + "' n'est pas un nom de mois valide.");
            }
        }

        entity.setDateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : LocalDateTime.now());
        entity.setDateMiseAJour(dto.getDateMiseAJour());
        entity.setCreePar(dto.getCreePar());
        entity.setModifierPar(dto.getModifierPar());
        entity.setJoursNonDisponible(dto.getJoursNonDisponible() != null ?
                dto.getJoursNonDisponible().stream().map(Jour::fromString).collect(Collectors.toList()) : null);
        return entity;
    }

    @Override
    public List<AgendaMensuelDTO> getAllAgendasMensuels() {
        return Transaction.initTransaction(cnx -> {
            AgendaMensuelRepository repo = agendaRepoFactory.create(cnx);
            List<AgendaMensuel> entities = repo.findAll();
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public AgendaMensuelDTO getAgendaMensuelById(Long id) {
        return Transaction.initTransaction(cnx -> {
            AgendaMensuelRepository repo = agendaRepoFactory.create(cnx);
            AgendaMensuel entity = repo.findById(id);
            return entity != null ? toDTO(entity) : null;
        });
    }

    @Override
    public AgendaMensuelDTO createAgendaMensuel(AgendaMensuelDTO dto) throws ValidationException {
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            AgendaMensuelRepository repo = agendaRepoFactory.create(cnx);
            UtilisateurRepository medecinRepo = medecinRepoFactory.create(cnx);

            AgendaMensuel entity = toEntity(dto);

            if (dto.getMedecinId() != null) {
                Utilisateur u = (Utilisateur) medecinRepo.findById(dto.getMedecinId());
                if (u == null) {
                    throw new IllegalArgumentException("Utilisateur avec l'ID " + dto.getMedecinId() + " n'existe pas.");
                }
                if (!(u instanceof Medecin)) {
                    throw new IllegalArgumentException("L'utilisateur avec l'ID " + dto.getMedecinId() + " n'est pas un médecin.");
                }
                Medecin medecin = (Medecin) u;
                entity.setMedecin(medecin);
            }

            repo.create(entity);
            dto.setId(entity.getId());
            return dto;
        });
    }

    @Override
    public AgendaMensuelDTO updateAgendaMensuel(Long id, AgendaMensuelDTO dto) throws ValidationException {
        if (id == null) {
            throw new IllegalArgumentException("Impossible de mettre à jour un agenda sans ID.");
        }
        Map<String, String> errors = validator.validate(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException("Erreurs de validation : " + errors);
        }

        return Transaction.initTransaction(cnx -> {
            AgendaMensuelRepository repo = agendaRepoFactory.create(cnx);
            UtilisateurRepository medecinRepo = medecinRepoFactory.create(cnx);

            AgendaMensuel existing = repo.findById(id);
            if (existing == null) {
                throw new IllegalArgumentException("AgendaMensuel avec l'ID " + id + " n'existe pas.");
            }

            AgendaMensuel entity = toEntity(dto);
            entity.setId(id);

            if (dto.getMedecinId() != null) {
                Utilisateur u = (Utilisateur) medecinRepo.findById(dto.getMedecinId());
                if (u == null) {
                    throw new IllegalArgumentException("Utilisateur avec l'ID " + dto.getMedecinId() + " n'existe pas.");
                }
                if (!(u instanceof Medecin)) {
                    throw new IllegalArgumentException("L'utilisateur avec l'ID " + dto.getMedecinId() + " n'est pas un médecin.");
                }
                Medecin medecin = (Medecin) u;
                entity.setMedecin(medecin);
            } else {
                entity.setMedecin(null);
            }

            repo.update(entity);
            return toDTO(entity);
        });
    }

    @Override
    public void deleteAgendaMensuel(Long id) throws ValidationException {
        Transaction.initTransaction(cnx -> {
            AgendaMensuelRepository repo = agendaRepoFactory.create(cnx);
            AgendaMensuel entity = repo.findById(id);
            if (entity == null) {
                throw new ValidationException("AgendaMensuel avec l'ID " + id + " n'existe pas.");
            }
            repo.deleteById(id);
            return null;
        });
    }

    @Override
    public List<AgendaMensuelDTO> getAgendasMensuelsByMedecinId(Long medecinId) {
        return Transaction.initTransaction(cnx -> {
            AgendaMensuelRepository repo = agendaRepoFactory.create(cnx);
            List<AgendaMensuel> entities = repo.findByMedecinId(medecinId);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<AgendaMensuelDTO> getAgendasMensuelsByMedecinIdAndMois(Long medecinId, String mois) {
        return Transaction.initTransaction(cnx -> {
            AgendaMensuelRepository repo = agendaRepoFactory.create(cnx);
            List<AgendaMensuel> entities = repo.findByMedecinIdAndMois(medecinId, mois);
            return entities.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        });
    }

    @Override
    public List<UserPrincipal> getAllMedecins() {
        return Transaction.initTransaction(cnx -> {
            UtilisateurRepository medecinRepo = medecinRepoFactory.create(cnx);

            List<Utilisateur> medecins = medecinRepo.findAllMedecins();

            return medecins.stream().map(m -> new UserPrincipal(
                    m.getId(),
                    m.getEmail(),
                    m.getPrenom() + " " + m.getNom(), // fullName
                    m.getEmail(),                      // login = email ici
                    RoleR.MEDECIN,                     // rôle principal
                    Set.of(RoleR.MEDECIN),             // tous les rôles
                    Set.of()                            // aucun privilège pour l'instant
            )).collect(Collectors.toList());
        });
    }

}