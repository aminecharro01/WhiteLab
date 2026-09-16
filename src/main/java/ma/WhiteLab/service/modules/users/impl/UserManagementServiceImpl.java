package ma.WhiteLab.service.modules.users.impl;

import ma.WhiteLab.common.consoleLog.ConsoleLogger;
import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.entities.enums.TitreNotification;
import ma.WhiteLab.entities.enums.TypeNotification;
import ma.WhiteLab.entities.user.*;
import ma.WhiteLab.repository.modules.user.api.RoleRepository;
import ma.WhiteLab.repository.modules.user.api.UtilisateurRepository;
import ma.WhiteLab.service.common.Transaction;
import ma.WhiteLab.service.modules.auth.api.PasswordEncoder;
import ma.WhiteLab.service.modules.notifications.api.NotificationService;
import ma.WhiteLab.service.modules.users.api.UserManagementService;
import ma.WhiteLab.service.modules.users.dto.CreateUserRequest;
import ma.WhiteLab.service.modules.users.dto.UpdateUserRequest;
import ma.WhiteLab.service.modules.users.dto.UserDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserManagementServiceImpl implements UserManagementService {

    private final RepoFactory<UtilisateurRepository> userRepoFactory;
    private final RepoFactory<RoleRepository> roleRepoFactory;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;   // <- ADD


    public UserManagementServiceImpl(
            RepoFactory<UtilisateurRepository> userRepoFactory,
            RepoFactory<RoleRepository> roleRepoFactory,
            PasswordEncoder passwordEncoder, NotificationService notificationService) {
        this.userRepoFactory = userRepoFactory;
        this.roleRepoFactory = roleRepoFactory;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return (List<UserDto>) Transaction.initTransaction(cnx -> {
            UtilisateurRepository userRepo = userRepoFactory.create(cnx);
            RoleRepository roleRepo = roleRepoFactory.create(cnx);

            return userRepo.findAll().stream()
                    .map(u -> mapToDto((Utilisateur) u, roleRepo))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public UserDto getUserById(Long id) {
        return Transaction.initTransaction(cnx -> {
            UtilisateurRepository userRepo = userRepoFactory.create(cnx);
            RoleRepository roleRepo = roleRepoFactory.create(cnx);

            Utilisateur u = (Utilisateur) userRepo.findById(id);
            if (u == null) throw new IllegalArgumentException("User not found: " + id);

            return mapToDto(u, roleRepo);
        });
    }

    @Override
    public UserDto createUser(CreateUserRequest req, String creePar) {
        return Transaction.initTransaction(cnx -> {
            UtilisateurRepository<Utilisateur> userRepo = userRepoFactory.create(cnx);
            RoleRepository roleRepo = roleRepoFactory.create(cnx);

            // Vérifie si email existe
            if (userRepo.findByEmail(req.email()).isPresent()) {
                throw new IllegalArgumentException("Email déjà utilisé");
            }

            // Crée le bon type d'utilisateur selon le rôle principal
            Utilisateur u;
            switch (req.role()) {
                case ADMIN -> u = new Admin();
                case MEDECIN -> u = new Medecin();
                case SECRETAIRE -> u = new Secretaire();
                default -> throw new IllegalArgumentException("Rôle principal non supporté: " + req.role());
            }

            // Remplit les champs
            populateUser(u, req);
            u.setCreePar(creePar);
            u.setDateCreation(LocalDateTime.now());
            u.setMotDePasse(passwordEncoder.encode(req.motDePasse()));

            // Ajoute le rôle principal
            Role principalRole = roleRepo.findByLibelle(req.role())
                    .orElseThrow(() -> new IllegalArgumentException("Role non trouvé: " + req.role()));
            u.setRoles(new ArrayList<>());
            u.getRoles().add(principalRole);

            // Sauvegarde dans la base
            userRepo.create(u);
            notificationService.createNotificationForAllUsers(
                    TitreNotification.INFO.name(),        // <-- VALID
                    "Un nouvel utilisateur a été créé : " + u.getNom() + " " + u.getPrenom(),
                    TypeNotification.SYSTEME.name(),     // <-- VALID
                    creePar
            );


            return mapToDto(u, roleRepo);
        });
    }

    @Override
    public UserDto updateUser(UpdateUserRequest req, String modifierPar) {
        return Transaction.initTransaction(cnx -> {
            UtilisateurRepository<Utilisateur> userRepo = userRepoFactory.create(cnx);
            RoleRepository roleRepo = roleRepoFactory.create(cnx);

            Utilisateur u = userRepo.findById(req.id());
            if (u == null) throw new IllegalArgumentException("User not found");

            populateUser(u, req);
            u.setModifierPar(modifierPar);
            u.setDateMiseAJour(LocalDateTime.now());

            userRepo.update(u);

            return mapToDto(u, roleRepo);
        });
    }

    @Override
    public void deleteUser(Long id) {
        Transaction.initTransaction(cnx -> {
            UtilisateurRepository<Utilisateur> userRepo = userRepoFactory.create(cnx);
            userRepo.deleteById(id);
            notificationService.createNotificationForAllUsers(
                    TitreNotification.ALERTE.name(),
                    "Un utilisateur a été supprimé (ID=" + id + ")",
                    TypeNotification.SYSTEME.name(),
                    "Admin"
            );
            return null;
        });
    }

    @Override
    public void resetUserPassword(Long userId, String newPassword) {
        Transaction.initTransaction(cnx -> {
            UtilisateurRepository<Utilisateur> userRepo = userRepoFactory.create(cnx);
            userRepo.updatePassword(userId, passwordEncoder.encode(newPassword));
            return null;
        });
    }

    @Override
    public void toggleUserAccountStatus(Long userId, boolean enable) {
        ConsoleLogger.warn("toggleUserAccountStatus not implemented in DB schema for user " + userId);
    }

    @Override
    public List<LocalDateTime> getUserConnectionHistory(Long userId) {
        return Transaction.initTransaction(cnx -> {
            UtilisateurRepository<Utilisateur> userRepo = userRepoFactory.create(cnx);
            Utilisateur u = userRepo.findById(userId);
            if (u != null && u.getLastLoginDate() != null) {
                return List.of(u.getLastLoginDate());
            }
            return Collections.emptyList();
        });
    }

    // ================= Helpers =================

    private void populateUser(Utilisateur u, CreateUserRequest req) {
        u.setNom(req.nom());
        u.setPrenom(req.prenom());
        u.setEmail(req.email());
        u.setAdresse(req.adresse());
        u.setCin(req.cin());
        u.setTelephone(req.telephone());
        u.setSexe(req.sexe());
        u.setDateNaissance(req.dateNaissance());

        if (u instanceof Staff s) {
            s.setSalaire(req.salaire());
            s.setPrime(req.prime());
            s.setDateRecrutement(req.dateRecrutement());
            s.setSoldeConge(req.soldeConge());
        }
        if (u instanceof Medecin m) {
            m.setSpecialite(req.specialite());
        }
        if (u instanceof Secretaire s) {
            s.setNumCNS(req.numCNSS());
            s.setCommission(req.commission());
        }
    }

    private void populateUser(Utilisateur u, UpdateUserRequest req) {
        u.setNom(req.nom());
        u.setPrenom(req.prenom());
        u.setEmail(req.email());
        u.setAdresse(req.adresse());
        u.setCin(req.cin());
        u.setTelephone(req.telephone());
        u.setSexe(req.sexe());
        u.setDateNaissance(req.dateNaissance());

        if (u instanceof Staff s) {
            s.setSalaire(req.salaire());
            s.setPrime(req.prime());
            s.setDateRecrutement(req.dateRecrutement());
            s.setSoldeConge(req.soldeConge());
        }
        if (u instanceof Medecin m) {
            m.setSpecialite(req.specialite());
        }
        if (u instanceof Secretaire s) {
            s.setNumCNS(req.numCNSS());
            s.setCommission(req.commission());
        }
    }

    private UserDto mapToDto(Utilisateur u, RoleRepository roleRepo) {
        List<String> roles = roleRepo.findRolesByUserId(u.getId()).stream()
                .map(r -> r.getLibelle().name())
                .collect(Collectors.toList());

        Double salaire = null, prime = null, commission = null;
        Integer solde = null;
        LocalDate recrut = null;
        String spec = null, cnss = null;

        if (u instanceof Staff s) {
            salaire = s.getSalaire();
            prime = s.getPrime();
            solde = s.getSoldeConge();
            recrut = s.getDateRecrutement();
        }
        if (u instanceof Medecin m) spec = m.getSpecialite();
        if (u instanceof Secretaire s) {
            cnss = s.getNumCNS();
            commission = s.getCommission();
        }

        return new UserDto(
                u.getId(), u.getNom(), u.getPrenom(), u.getEmail(), u.getAdresse(), u.getCin(), u.getTelephone(),
                u.getSexe(), u.getDateNaissance(), u.getType(), u.getLastLoginDate(), true,
                salaire, prime, recrut, solde, spec, cnss, commission, roles
        );
    }
}
