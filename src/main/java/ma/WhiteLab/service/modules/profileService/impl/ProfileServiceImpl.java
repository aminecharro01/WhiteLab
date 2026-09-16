package ma.WhiteLab.service.modules.profileService.impl;

import ma.WhiteLab.common.utils.RepoFactory;
import ma.WhiteLab.entities.enums.RoleR;
import ma.WhiteLab.entities.user.*;
import ma.WhiteLab.mvc.dto.profileDtos.*;
import ma.WhiteLab.repository.modules.user.api.UtilisateurRepository;
import ma.WhiteLab.service.modules.auth.api.PasswordEncoder;
import ma.WhiteLab.service.modules.profileService.api.ChangePasswordValidator;
import ma.WhiteLab.service.modules.profileService.api.ProfileService;
import ma.WhiteLab.service.modules.profileService.api.ProfileValidator;
import ma.WhiteLab.service.common.Transaction;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileServiceImpl implements ProfileService {

    private final RepoFactory<UtilisateurRepository<Utilisateur>> userRepoFactory;
    private final ProfileValidator validator;
    private final ChangePasswordValidator changePasswordValidator;
    private final PasswordEncoder passwordEncoder;

    public ProfileServiceImpl(
            RepoFactory<UtilisateurRepository<Utilisateur>> userRepoFactory,
            ProfileValidator validator,
            ChangePasswordValidator changePasswordValidator,
            PasswordEncoder passwordEncoder) {

        this.userRepoFactory = userRepoFactory;
        this.validator = validator;
        this.changePasswordValidator = changePasswordValidator;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ProfileData loadByUserId(Long userId) {
        if (userId == null) return null;

        return Transaction.initTransaction(cnx -> {
            UtilisateurRepository<Utilisateur> userRepo = userRepoFactory.create(cnx);

            Utilisateur user = userRepo.findById(userId);
            if (user == null) return null;

            // Rôle principal = type de l'utilisateur (puisque tu utilises un champ type ou via instanceof)
            RoleR rolePrincipal = determineRolePrincipal(user);

            return mapToProfileData(user, rolePrincipal);
        });
    }

    @Override
    public ProfileUpdateResult update(ProfileUpdateRequest req) {
        Map<String, String> errors = validator.validate(req);
        if (!errors.isEmpty()) {
            return ProfileUpdateResult.failure("Formulaire invalide", errors);
        }

        return Transaction.initTransaction(cnx -> {
            UtilisateurRepository<Utilisateur> userRepo = userRepoFactory.create(cnx);

            // Vérification unicité email
            Map<String, String> uniqErrors = validateUniqueness(userRepo, req);
            if (!uniqErrors.isEmpty()) {
                return ProfileUpdateResult.failure("Conflit d'unicité", uniqErrors);
            }

            Utilisateur user = userRepo.findById(req.id());
            if (user == null) {
                return ProfileUpdateResult.failure("Utilisateur introuvable", Map.of("_global", "Utilisateur introuvable"));
            }

            RoleR rolePrincipal = determineRolePrincipal(user);

            // Mise à jour des champs communs
            applyCommonFields(user, req);

            // Mise à jour des champs spécifiques selon le type
            if (user instanceof Staff staff) {
                applyStaffFields(staff, req);

                if (user instanceof Medecin medecin) {
                    applyMedecinFields(medecin, req);
                } else if (user instanceof Secretaire secretaire) {
                    applySecretaireFields(secretaire, req);
                }
            }

            // Persistance
            userRepo.update(user);

            // Retour du profil mis à jour
            ProfileData updated = mapToProfileData(user, rolePrincipal);
            return ProfileUpdateResult.success("Profil mis à jour avec succès", updated);
        });
    }

    @Override
    public ChangePasswordResult changePassword(ChangePasswordRequest req) {
        Map<String, String> errors = changePasswordValidator.validate(req);
        if (!errors.isEmpty()) {
            return ChangePasswordResult.failure("Formulaire invalide", errors);
        }

        return Transaction.initTransaction(cnx -> {
            UtilisateurRepository<Utilisateur> userRepo = userRepoFactory.create(cnx);

            Utilisateur user = userRepo.findById(req.userId());
            if (user == null) {
                return ChangePasswordResult.failure("Utilisateur introuvable", Map.of("_global", "Utilisateur introuvable"));
            }

            if (!passwordEncoder.matches(req.currentPassword(), user.getMotDePasse())) {
                return ChangePasswordResult.failure("Mot de passe actuel incorrect",
                        Map.of("currentPassword", "Mot de passe actuel incorrect"));
            }

            String encoded = passwordEncoder.encode(req.newPassword());
            userRepo.updatePassword(req.userId(), encoded);

            return ChangePasswordResult.success();
        });
    }

    // ======================== Helpers ========================

    private RoleR determineRolePrincipal(Utilisateur user) {
        if (user instanceof Admin) return RoleR.ADMIN;
        if (user instanceof Medecin) return RoleR.MEDECIN;
        if (user instanceof Secretaire) return RoleR.SECRETAIRE;
        // Ajoute d'autres rôles si besoin
        return RoleR.UTILISATEUR; // fallback
    }

    private Map<String, String> validateUniqueness(UtilisateurRepository<Utilisateur> userRepo, ProfileUpdateRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();

        String email = trim(req.email());
        if (email != null) {
            userRepo.findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(req.id())) {
                    errors.put("email", "Cet email est déjà utilisé par un autre compte.");
                }
            });
        }

        // Tu peux ajouter téléphone, CIN, etc. si tu as les méthodes dans le repo

        return errors;
    }

    private void applyCommonFields(Utilisateur u, ProfileUpdateRequest req) {
        u.setPrenom(trim(req.prenom()));
        u.setNom(trim(req.nom()));
        u.setEmail(trim(req.email()));
        u.setAdresse(trim(req.adresse()));
        u.setCin(trim(req.cin()));
        u.setTelephone(trim(req.tel()));
        u.setSexe(req.sexe());
        u.setDateNaissance(req.dateNaissance());
    }

    private void applyStaffFields(Staff s, ProfileUpdateRequest req) {
        s.setSalaire(req.salaire());
        s.setPrime(req.prime());
        s.setDateRecrutement(req.dateRecrutement());
        s.setSoldeConge(req.soldeConge());
    }

    private void applyMedecinFields(Medecin m, ProfileUpdateRequest req) {
        m.setSpecialite(trim(req.specialite()));
    }

    private void applySecretaireFields(Secretaire s, ProfileUpdateRequest req) {
        s.setNumCNS(trim(req.numCNSS()));
        s.setCommission(req.commission());
    }

    private ProfileData mapToProfileData(Utilisateur u, RoleR rolePrincipal) {
        ProfileData.ProfileDataBuilder builder = ProfileData.builder()
                .id(u.getId())
                .rolePrincipal(rolePrincipal)
                .prenom(u.getPrenom())
                .nom(u.getNom())
                .email(u.getEmail())
                .adresse(u.getAdresse())
                .cin(u.getCin())
                .tel(u.getTelephone())
                .sexe(u.getSexe())
                .dateNaissance(u.getDateNaissance());

        if (u instanceof Staff staff) {
            builder.salaire(staff.getSalaire())
                    .prime(staff.getPrime())
                    .dateRecrutement(staff.getDateRecrutement())
                    .soldeConge(staff.getSoldeConge())
                    .cabinetId(staff.getCabinetMedicale() != null ? staff.getCabinetMedicale().getId() : null);
        }

        if (u instanceof Medecin medecin) {
            builder.specialite(medecin.getSpecialite());
        }

        if (u instanceof Secretaire secretaire) {
            builder.numCNSS(secretaire.getNumCNS())
                    .commission(secretaire.getCommission());
        }

        return builder.build();
    }

    private String trim(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}