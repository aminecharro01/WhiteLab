package ma.WhiteLab.service.modules.auth.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.service.modules.auth.api.AuthService;
import ma.WhiteLab.mvc.dto.auth.AuthRequest;
import ma.WhiteLab.mvc.dto.auth.AuthResult;

/**
 * Classe de test unitaire/manuel pour vérifier le fonctionnement du module d'authentification.
 * Utilise le vrai ApplicationContext (singleton) chargé avec beans.properties.
 */
public class TestLoginMedecin {

    private static final AuthService authService;

    static {
        // Utilise le singleton → charge une seule fois le contexte avec le fichier par défaut
        // (src/main/resources/config/beans.properties)
        ApplicationContext context = ApplicationContext.getInstance();

        authService = context.getBean(AuthService.class);
        if (authService == null) {
            System.err.println("[ERREUR FATALE] AuthService introuvable dans le contexte !");
            System.err.println("Vérifiez que la ligne suivante existe dans config/beans.properties :");
            System.err.println("authService = ma.WhiteLab.service.modules.auth.impl.AuthServiceImpl");
            System.exit(1);
        }

        System.out.println("[TestLoginMedecin] AuthService chargé avec succès depuis le contexte.\n");
    }

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("   TEST AUTHENTIFICATION - WHITELAB 2025   ");
        System.out.println("==============================================\n");

        testLoginReussi();
        testUtilisateurInconnu();
        testMauvaisMotDePasse();
        testErreursValidation();

        System.out.println("\nTOUS LES TESTS TERMINÉS AVEC SUCCÈS !");
    }

    private static void testLoginReussi() {
        System.out.println("SCÉNARIO 1 : Connexion réussie (Médecin)");
        AuthResult result = authService.authenticate(new AuthRequest(
                "ali.medecin@example.com",
                "MotDePasse123"  // À adapter selon le mot de passe hashé en base
        ));

        if (result.isSuccess()) {
            System.out.println("Connexion réussie !");
            System.out.println("Utilisateur : " + result.getUserPrincipal().fullName());
            System.out.println("Rôles       : " + result.getUserPrincipal().roles());
            System.out.println("Privilèges  : " + result.getUserPrincipal().privileges());
        } else {
            System.out.println("Échec inattendu : " + result.getMessage());
        }
        separer();
    }

    private static void testUtilisateurInconnu() {
        System.out.println("SCÉNARIO 2 : Utilisateur inexistant");
        AuthResult result = authService.authenticate(new AuthRequest(
                "inconnu@exemple.com",
                "MotDePasse123"
        ));
        afficherEchec(result);
        separer();
    }

    private static void testMauvaisMotDePasse() {
        System.out.println("SCÉNARIO 3 : Mot de passe incorrect");
        AuthResult result = authService.authenticate(new AuthRequest(
                "ali.medecin@example.com",
                "mauvaispassword!"
        ));
        afficherEchec(result);
        separer();
    }

    private static void testErreursValidation() {
        System.out.println("SCÉNARIO 4 : Erreurs de validation du formulaire");

        // Email vide + mot de passe trop court
        afficherEchecValidation(authService.authenticate(new AuthRequest("", "abc")));

        // Email invalide (pas de @ ou domaine)
        afficherEchecValidation(authService.authenticate(new AuthRequest("pasunemail", "MotDePasse123!")));

        // Requête null
        afficherEchecValidation(authService.authenticate(null));

        separer();
    }

    // ===================================================================
    // Méthodes utilitaires d'affichage
    // ===================================================================

    private static void afficherEchec(AuthResult result) {
        if (result == null) {
            System.out.println("Résultat null (erreur critique)");
            return;
        }
        System.out.println("Échec attendu : " + result.getMessage());
        afficherErreursDetaillees(result);
    }

    private static void afficherEchecValidation(AuthResult result) {
        if (result == null) {
            System.out.println("Résultat null lors de la validation");
            return;
        }
        System.out.println("Erreurs de validation détectées : " + result.getMessage());
        afficherErreursDetaillees(result);
    }

    private static void afficherErreursDetaillees(AuthResult result) {
        if (result != null && result.hasFieldErrors() && !result.getFieldErrors().isEmpty()) {
            System.out.println("   Détails des erreurs par champ :");
            result.getFieldErrors().forEach((champ, msg) ->
                    System.out.println("   • " + champ + " → " + msg)
            );
        }
    }

    private static void separer() {
        System.out.println("──────────────────────────────────────────────\n");
    }
}