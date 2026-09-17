package ma.WhiteLab.conf;

import ma.WhiteLab.common.consoleLog.ConsoleLogger;
import ma.WhiteLab.common.utils.FactoryUtils;
import ma.WhiteLab.common.utils.RepoFactory;

import ma.WhiteLab.mvc.controllers.modules.auth.api.AuthController;
import ma.WhiteLab.mvc.controllers.dashboardModule.api.DashboardController;
import ma.WhiteLab.mvc.controllers.modules.dossierMedicale.api.DossiersController;
import ma.WhiteLab.mvc.controllers.modules.dossierMedicale.impl.DossiersControllerImpl;
import ma.WhiteLab.mvc.controllers.modules.patient.api.PatientsController;
import ma.WhiteLab.mvc.controllers.modules.patient.impl.PatientsControllerImpl;
import ma.WhiteLab.mvc.controllers.otherModules.api.CabinetsController;
import ma.WhiteLab.mvc.controllers.otherModules.api.ParametrageController;
import ma.WhiteLab.mvc.controllers.otherModules.api.RolesController;
import ma.WhiteLab.mvc.controllers.otherModules.api.UsersController;
import ma.WhiteLab.mvc.controllers.otherModules.impl.CabinetsControllerImpl;
import ma.WhiteLab.mvc.controllers.otherModules.impl.ParametrageControllerImpl;
import ma.WhiteLab.mvc.controllers.otherModules.impl.RolesControllerImpl;
import ma.WhiteLab.mvc.controllers.profileModule.api.ProfileController;
import ma.WhiteLab.mvc.controllers.modules.agenda.api.AgendaController;           // ← AJOUTÉ
import ma.WhiteLab.mvc.controllers.modules.agenda.impl.AgendaControllerImpl;     // ← AJOUTÉ

import ma.WhiteLab.repository.modules.agenda.api.AgendaMensuelRepository;
import ma.WhiteLab.repository.modules.agenda.api.RendezVousRepository;
import ma.WhiteLab.repository.modules.cabinet.api.*;
import ma.WhiteLab.repository.modules.dossierMedical.api.*;
import ma.WhiteLab.repository.modules.notifications.api.NotificationRepository;
import ma.WhiteLab.repository.modules.patient.api.AntecedentRepository;
import ma.WhiteLab.repository.modules.patient.api.DentRepository;
import ma.WhiteLab.repository.modules.patient.api.PatientRepository;
import ma.WhiteLab.repository.modules.user.api.*;

import ma.WhiteLab.service.modules.auth.api.*;
import ma.WhiteLab.service.modules.cabinet.api.CabinetMedicalService;
import ma.WhiteLab.service.modules.cabinet.api.CabinetMedicalValidator;
import ma.WhiteLab.service.modules.cabinet.impl.CabinetMedicalServiceImpl;
import ma.WhiteLab.service.modules.cabinet.impl.CabinetMedicalValidatorImpl;
import ma.WhiteLab.service.modules.caisse.api.*;
import ma.WhiteLab.service.modules.caisse.impl.SituationFinanciereServiceImpl;
import ma.WhiteLab.service.modules.certificat.api.CertificatService;
import ma.WhiteLab.service.modules.certificat.impl.CertificatServiceImpl;
import ma.WhiteLab.service.modules.dossierMedicale.api.MedicamentService;
import ma.WhiteLab.service.modules.dossierMedicale.api.MedicamentValidator;
import ma.WhiteLab.service.modules.dossierMedicale.api.OrdonnanceService;
import ma.WhiteLab.service.modules.dossierMedicale.api.PrescriptionService;
import ma.WhiteLab.service.modules.dossierMedicale.impl.*;
import ma.WhiteLab.service.modules.patient.api.PatientService;
import ma.WhiteLab.service.modules.patient.impl.PatientServiceImpl;
import ma.WhiteLab.service.modules.profileService.api.*;
import ma.WhiteLab.service.modules.dossierMedicale.api.DossierMedicalService;
import ma.WhiteLab.service.modules.consultation.api.ConsultationService;
import ma.WhiteLab.service.modules.consultation.impl.ConsultationServiceImpl;
import ma.WhiteLab.service.modules.dossierMedicale.api.InterventionMedecinService;
import ma.WhiteLab.service.modules.dossierMedicale.impl.InterventionMedecinServiceImpl;
import ma.WhiteLab.service.modules.dossierMedicale.api.ActeMedicalService;
import ma.WhiteLab.service.modules.dossierMedicale.impl.ActeMedicalServiceImpl;
import ma.WhiteLab.service.modules.notifications.api.NotificationService;
import ma.WhiteLab.service.modules.notifications.impl.NotificationServiceImpl;
import ma.WhiteLab.service.modules.patient.api.AntecedentService;
import ma.WhiteLab.service.modules.patient.impl.AntecedentServiceImpl;
import ma.WhiteLab.service.modules.patient.api.DentService;
import ma.WhiteLab.service.modules.patient.impl.DentServiceImpl;
import ma.WhiteLab.service.modules.agendas.api.AgendaMensuelService;
import ma.WhiteLab.service.modules.agendas.impl.AgendaMensuelServiceImpl;
import ma.WhiteLab.service.modules.agendas.api.RendezVousService;
import ma.WhiteLab.service.modules.agendas.impl.RendezVousServiceImpl;

import ma.WhiteLab.entities.user.Medecin;
import ma.WhiteLab.service.modules.referentiel.api.ReferentielService;
import ma.WhiteLab.service.modules.referentiel.impl.ReferentielServiceImpl;
import ma.WhiteLab.service.modules.users.api.RoleManagementService;
import ma.WhiteLab.service.modules.users.api.UserManagementService;
import ma.WhiteLab.mvc.controllers.modules.notifications.api.NotificationsController;
import ma.WhiteLab.mvc.controllers.modules.notifications.impl.NotificationsControllerImpl;


import java.io.InputStream;
import java.sql.Connection;
import java.util.*;

public final class ApplicationContext {

    private static volatile ApplicationContext INSTANCE;
    private static final Map<Class<?>, Object> context = new LinkedHashMap<>();

    private Properties props;
    private final ClassLoader cl = Thread.currentThread().getContextClassLoader();

    // Repos
    private RepoFactory<UtilisateurRepository> utilisateurRepositoryFactory;
    private RepoFactory<RoleRepository> roleRepositoryFactory;
    private RepoFactory<PatientRepository> patientRepoFactory;
    private RepoFactory<CabinetMedicaleRepository> cabinetRepoFactory;
    private RepoFactory<DossierMedicalRepository> dossierMedicalRepoFactory;
    private RepoFactory<MedicamentRepository> medicamentRepoFactory;
    private RepoFactory<ActeMedicalRepository> acteRepoFactory;
    private RepoFactory<AntecedentRepository> antecedentRepoFactory;
    private RepoFactory<DentRepository> dentRepoFactory;
    private RepoFactory<ConsultationRepository> consultationRepoFactory;
    private RepoFactory<CertificatRepository> certificatRepoFactory;
    private RepoFactory<FactureRepository> factureRepoFactory;
    private RepoFactory<InterventionMedecinRepository> interventionRepoFactory;
    private RepoFactory<OrdonnanceRepository> ordonnanceRepoFactory;
    private RepoFactory<PrescriptionRepository> prescriptionRepoFactory;
    private RepoFactory<AgendaMensuelRepository> agendaMensuelRepoFactory;
    private RepoFactory<RendezVousRepository> rendezVousRepoFactory;
    private RepoFactory<NotificationRepository> notificationRepoFactory;
    private RepoFactory<ChargesRepository> chargesRepoFactory;
    private RepoFactory<RevenusRepository> revenusRepoFactory;
    private RepoFactory<StatistiquesRepository> statistiquesRepoFactory;
    private RepoFactory<SituationFinanciereRepository> situationFinanciereRepoFactory;

    private ApplicationContext() {
        try {
            loadPropertiesAndBeans();
            ConsoleLogger.info("ApplicationContext initialisé avec succès : " + context.size() + " beans chargés.");
            printAllBeans(); // ← très utile pour voir si AgendaController est chargé
        } catch (Exception e) {
            ConsoleLogger.error("ÉCHEC CRITIQUE : Impossible d'initialiser ApplicationContext", e);
            throw new RuntimeException("Erreur fatale lors de l'initialisation", e);
        }
    }

    public static ApplicationContext getInstance() {
        if (INSTANCE == null) {
            synchronized (ApplicationContext.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ApplicationContext();
                }
            }
        }
        return INSTANCE;
    }

    private void loadPropertiesAndBeans() throws Exception {
        InputStream input = cl.getResourceAsStream("config/beans.properties");
        if (input == null) {
            throw new IllegalStateException("Fichier beans.properties introuvable dans le classpath");
        }
        props = new Properties();
        props.load(input);
        input.close();
        context.put(Properties.class, props);

        ConsoleLogger.info("Chargement des beans depuis beans.properties...");

        // ==== REPOS ====
        utilisateurRepositoryFactory   = buildRepoFactory("utilisateurRepository", UtilisateurRepository.class);
        roleRepositoryFactory          = buildRepoFactory("roleRepo", RoleRepository.class);
        patientRepoFactory             = buildRepoFactory("patientRepo", PatientRepository.class);
        cabinetRepoFactory             = buildRepoFactory("cabinetRepo", CabinetMedicaleRepository.class);
        dossierMedicalRepoFactory      = buildRepoFactory("dossierMedicalRepo", DossierMedicalRepository.class);
        medicamentRepoFactory          = buildRepoFactory("medicamentRepo", MedicamentRepository.class);
        acteRepoFactory                = buildRepoFactory("acteRepo", ActeMedicalRepository.class);
        antecedentRepoFactory          = buildRepoFactory("antecedentRepo", AntecedentRepository.class);
        dentRepoFactory                = buildRepoFactory("dentRepo", DentRepository.class);
        consultationRepoFactory        = buildRepoFactory("consultationRepo", ConsultationRepository.class);
        certificatRepoFactory          = buildRepoFactory("certificatRepo", CertificatRepository.class);
        factureRepoFactory             = buildRepoFactory("factureRepo", FactureRepository.class);
        interventionRepoFactory        = buildRepoFactory("interventionRepo", InterventionMedecinRepository.class);
        ordonnanceRepoFactory          = buildRepoFactory("ordonnanceRepo", OrdonnanceRepository.class);
        prescriptionRepoFactory        = buildRepoFactory("prescriptionRepo", PrescriptionRepository.class);
        agendaMensuelRepoFactory       = buildRepoFactory("agendaMensuelRepo", AgendaMensuelRepository.class);
        rendezVousRepoFactory          = buildRepoFactory("rendezVousRepo", RendezVousRepository.class);
        notificationRepoFactory        = buildRepoFactory("notificationRepo", NotificationRepository.class);
        chargesRepoFactory             = buildRepoFactory("chargesRepo", ChargesRepository.class);
        revenusRepoFactory             = buildRepoFactory("revenusRepo", RevenusRepository.class);
        statistiquesRepoFactory        = buildRepoFactory("statRepo", StatistiquesRepository.class);
        situationFinanciereRepoFactory = buildRepoFactory("suiviFinancierRepo", SituationFinanciereRepository.class);

        // Factory spécialisée pour Medecin (tu l'avais déjà, on la garde)
        RepoFactory<UtilisateurRepository<Medecin>> medecinRepoFactory = c -> {
            String implClassName = props.getProperty("utilisateurRepository");
            try {
                Class<?> implClass = Class.forName(implClassName.trim());
                @SuppressWarnings("unchecked")
                UtilisateurRepository<Medecin> repo = (UtilisateurRepository<Medecin>) implClass
                        .getDeclaredConstructor(Connection.class)
                        .newInstance(c);
                return repo;
            } catch (Exception e) {
                throw new RuntimeException("Impossible d'instancier le repository pour Medecin", e);
            }
        };

        // ==== VALIDATORS & ENCODERS ====
        var credentialsValidator    = FactoryUtils.buildImplInstance(props, "credentialsValidator", CredentialsValidator.class);
        var passwordEncoder         = FactoryUtils.buildImplInstance(props, "passwordEncoder", PasswordEncoder.class);
        var profileValidator        = FactoryUtils.buildImplInstance(props, "profileValidator", ProfileValidator.class);
        var changePasswordValidator = FactoryUtils.buildImplInstance(props, "changePasswordValidator", ChangePasswordValidator.class);
        var medicamentValidator     = FactoryUtils.buildImplInstance(props, "medicamentValidator", MedicamentValidator.class);

        var cabinetValidator        = new CabinetMedicalValidatorImpl(cabinetRepoFactory);

        context.put(CabinetMedicalValidator.class, cabinetValidator);
        context.put(CredentialsValidator.class, credentialsValidator);
        context.put(PasswordEncoder.class, passwordEncoder);
        context.put(ProfileValidator.class, profileValidator);
        context.put(ChangePasswordValidator.class, changePasswordValidator);
        context.put(MedicamentValidator.class, medicamentValidator);

        // ==== SERVICES ====
        var authService = FactoryUtils.buildImplInstance(props, "authService", AuthService.class,
                utilisateurRepositoryFactory, roleRepositoryFactory, credentialsValidator, passwordEncoder);

        var authorizationService = FactoryUtils.buildImplInstance(props, "authorizationService", AuthorizationService.class);

        var profileService = FactoryUtils.buildImplInstance(props, "profileService", ProfileService.class,
                utilisateurRepositoryFactory, profileValidator, changePasswordValidator, passwordEncoder);

        var patientService = new PatientServiceImpl(patientRepoFactory, antecedentRepoFactory);

        var referentielService = new ReferentielServiceImpl();
        context.put(ReferentielService.class, referentielService);

        var dossierMedicalService = new DossierMedicalServiceImpl(
                dossierMedicalRepoFactory,
                consultationRepoFactory,
                ordonnanceRepoFactory,
                certificatRepoFactory,
                situationFinanciereRepoFactory,
                patientRepoFactory,
                medecinRepoFactory,
                prescriptionRepoFactory
        );

        var ordonnanceService = new OrdonnanceServiceImpl(
                ordonnanceRepoFactory,
                dossierMedicalRepoFactory,
                prescriptionRepoFactory,
                consultationRepoFactory
        );

        var prescriptionService = new PrescriptionServiceImpl(
                prescriptionRepoFactory,
                ordonnanceRepoFactory,
                medicamentRepoFactory
        );

        var consultationService = new ConsultationServiceImpl(
                consultationRepoFactory,
                dossierMedicalRepoFactory,
                interventionRepoFactory,
                acteRepoFactory
        );

        var situationFinanciereService = new SituationFinanciereServiceImpl(
                situationFinanciereRepoFactory,
                interventionRepoFactory,
                consultationRepoFactory,
                dossierMedicalRepoFactory
        );
        var interventionService = new InterventionMedecinServiceImpl(
                interventionRepoFactory,
                consultationRepoFactory,
                acteRepoFactory,
                situationFinanciereService
        );

        var acteService = new ActeMedicalServiceImpl(acteRepoFactory);

        var notificationService = new NotificationServiceImpl(
                notificationRepoFactory,
                utilisateurRepositoryFactory
        );

        var antecedentService = new AntecedentServiceImpl(antecedentRepoFactory);

        var dentService = new DentServiceImpl(dentRepoFactory);

        var agendaMensuelService = new AgendaMensuelServiceImpl(
                agendaMensuelRepoFactory,
                utilisateurRepositoryFactory
        );

        var rendezVousService = new RendezVousServiceImpl(
                rendezVousRepoFactory,
                dossierMedicalRepoFactory,
                consultationRepoFactory
        );

        var medicamentService = new MedicamentServiceImpl(
                medicamentRepoFactory,
                medicamentValidator,
                dossierMedicalRepoFactory,
                antecedentRepoFactory
        );

        var certificatService = new CertificatServiceImpl(
                certificatRepoFactory,
                consultationRepoFactory,
                dossierMedicalRepoFactory
        );


        var dashboardService = new ma.WhiteLab.service.modules.dashboard_statistiques.impl.DashboardServiceImpl(
                revenusRepoFactory,
                chargesRepoFactory,
                consultationRepoFactory,
                rendezVousRepoFactory,
                dossierMedicalRepoFactory,
                patientRepoFactory,
                utilisateurRepositoryFactory,
                cabinetRepoFactory
        );
        var cabinetService = new CabinetMedicalServiceImpl(
                getCabinetRepoFactory(),
                cabinetValidator
        );
        UserManagementService userService = new ma.WhiteLab.service.modules.users.impl.UserManagementServiceImpl(
                getUtilisateurRepositoryFactory(),  // RepoFactory<UtilisateurRepository>
                getRoleRepositoryFactory(),         // RepoFactory<RoleRepository>
                passwordEncoder,                     // PasswordEncoder
                notificationService
        );

        RoleManagementService roleService = new ma.WhiteLab.service.modules.users.impl.RoleManagementServiceImpl(
                getRoleRepositoryFactory(),         // RepoFactory<RoleRepository>
                getUtilisateurRepositoryFactory()   // RepoFactory<UtilisateurRepository>
        );

        context.put(CabinetMedicalService.class, cabinetService);
        context.put(ma.WhiteLab.service.modules.dashboard_statistiques.api.DashboardService.class, dashboardService);
        context.put(CertificatService.class, certificatService);
        context.put(SituationFinanciereService.class, situationFinanciereService);
        context.put(AuthService.class, authService);
        context.put(AuthorizationService.class, authorizationService);
        context.put(ProfileService.class, profileService);
        context.put(PatientService.class, patientService);
        context.put(DossierMedicalService.class, dossierMedicalService);
        context.put(OrdonnanceService.class, ordonnanceService);
        context.put(PrescriptionService.class, prescriptionService);
        context.put(ConsultationService.class, consultationService);
        context.put(InterventionMedecinService.class, interventionService);
        context.put(ActeMedicalService.class, acteService);
        context.put(NotificationService.class, notificationService);
        context.put(AntecedentService.class, antecedentService);
        context.put(DentService.class, dentService);
        context.put(AgendaMensuelService.class, agendaMensuelService);
        context.put(RendezVousService.class, rendezVousService);
        context.put(MedicamentService.class, medicamentService);

        // ==== CONTROLLERS ====
        var authController       = FactoryUtils.buildImplInstance(props, "authController", AuthController.class, authService);
        var profileController    = FactoryUtils.buildImplInstance(props, "profileController", ProfileController.class, profileService);
        var dashboardController  = FactoryUtils.buildImplInstance(props, "dashboardController", DashboardController.class, authorizationService, authController);

        var rolesController = new RolesControllerImpl(roleService);
        context.put(RolesController.class, rolesController);

        var dossiersController = new DossiersControllerImpl(
                dossierMedicalService,
                ordonnanceService,
                prescriptionService,
                certificatService,
                antecedentService,
                rendezVousService,
                medicamentService,
                patientService,
                consultationService,
                interventionService,
                situationFinanciereService
        );

        var patientsController   = new PatientsControllerImpl(patientService, dossiersController, dossierMedicalService);

        // IMPORTANT : Chargement explicite du AgendaController
        var agendaController = new AgendaControllerImpl(
                agendaMensuelService,
                rendezVousService,
                patientService// ← le 2ème argument manquant !
        );
        var cabinetsController = new CabinetsControllerImpl(cabinetService);

        var usersController = FactoryUtils.buildImplInstance(
                props,
                "usersController",
                UsersController.class,
                userService,
                roleService
        );
        var parametrageController = new ParametrageControllerImpl(
                medicamentService,
                acteService,
                antecedentService,
                referentielService
        );

        var notificationsController = new NotificationsControllerImpl();
        context.put(NotificationsController.class, notificationsController);




        var revenusService = new ma.WhiteLab.service.modules.caisse.impl.RevenusServiceImpl(
                revenusRepoFactory
        );

        var chargesService = new ma.WhiteLab.service.modules.caisse.impl.ChargesServiceImpl(
                chargesRepoFactory
        );

        var factureService = new ma.WhiteLab.service.modules.caisse.impl.FactureServiceImpl(
                factureRepoFactory
        );

        var caisseService = new ma.WhiteLab.service.modules.caisse.impl.CaisseServiceImpl(
                revenusService,
                chargesService
        );

        var caisseController = new ma.WhiteLab.mvc.controllers.otherModules.impl.CaisseControllerImpl(
                caisseService,
                revenusService,
                chargesService,
                factureService,
                situationFinanciereService,
                consultationService,
                interventionService
        );

        context.put(CaisseService.class, caisseService);
        context.put(RevenusService.class, revenusService);
        context.put(ChargesService.class, chargesService);
        context.put(FactureService.class, factureService);

        context.put(ma.WhiteLab.mvc.controllers.otherModules.api.CaisseController.class, caisseController);
        context.put(ParametrageController.class, parametrageController);

        context.put(UsersController.class, usersController);
        context.put(CabinetsController.class, cabinetsController);
        context.put(AuthController.class, authController);
        context.put(ProfileController.class, profileController);
        context.put(DashboardController.class, dashboardController);
        context.put(DossiersController.class, dossiersController);
        context.put(PatientsController.class, patientsController);
        context.put(AgendaController.class, agendaController); // ← AJOUTÉ ICI !

        // Log de confirmation pour debug
        ConsoleLogger.info("AgendaController chargé : " + (agendaController != null ? "OK" : "ÉCHEC"));
    }

    public static <T> T getBean(Class<T> beanClass) {
        Object bean = context.get(beanClass);
        if (bean == null) {
            ConsoleLogger.error("Bean non trouvé dans le contexte : " + beanClass.getSimpleName());
            throw new IllegalStateException("Bean manquant : " + beanClass.getSimpleName());
        }
        return beanClass.cast(bean);
    }

    private <T> RepoFactory<T> buildRepoFactory(String propertyKey, Class<T> apiType) {
        String implClassName = props.getProperty(propertyKey);
        if (implClassName == null || implClassName.isBlank()) {
            throw new IllegalArgumentException("Propriété manquante dans beans.properties : " + propertyKey);
        }

        return (Connection c) -> {
            try {
                Class<?> implClass = Class.forName(implClassName.trim());
                return apiType.cast(implClass.getDeclaredConstructor(Connection.class).newInstance(c));
            } catch (Exception e) {
                throw new RuntimeException("Impossible d'instancier " + implClassName, e);
            }
        };
    }

    // ==== GETTERS REPO FACTORIES ====
    public RepoFactory<CabinetMedicaleRepository> getCabinetRepoFactory() { return cabinetRepoFactory; }
    public RepoFactory<UtilisateurRepository> getUtilisateurRepositoryFactory() { return utilisateurRepositoryFactory; }
    public RepoFactory<RoleRepository> getRoleRepositoryFactory() { return roleRepositoryFactory; }
    public RepoFactory<PatientRepository> getPatientRepoFactory() { return patientRepoFactory; }
    public RepoFactory<DossierMedicalRepository> getDossierMedicalRepoFactory() { return dossierMedicalRepoFactory; }
    public RepoFactory<MedicamentRepository> getMedicamentRepoFactory() { return medicamentRepoFactory; }
    public RepoFactory<ActeMedicalRepository> getActeRepoFactory() { return acteRepoFactory; }
    public RepoFactory<AntecedentRepository> getAntecedentRepoFactory() { return antecedentRepoFactory; }
    public RepoFactory<ConsultationRepository> getConsultationRepoFactory() { return consultationRepoFactory; }
    public RepoFactory<CertificatRepository> getCertificatRepoFactory() { return certificatRepoFactory; }
    public RepoFactory<FactureRepository> getFactureRepoFactory() { return factureRepoFactory; }
    public RepoFactory<InterventionMedecinRepository> getInterventionRepoFactory() { return interventionRepoFactory; }
    public RepoFactory<OrdonnanceRepository> getOrdonnanceRepoFactory() { return ordonnanceRepoFactory; }
    public RepoFactory<PrescriptionRepository> getPrescriptionRepoFactory() { return prescriptionRepoFactory; }
    public RepoFactory<AgendaMensuelRepository> getAgendaMensuelRepoFactory() { return agendaMensuelRepoFactory; }
    public RepoFactory<RendezVousRepository> getRendezVousRepoFactory() { return rendezVousRepoFactory; }
    public RepoFactory<NotificationRepository> getNotificationRepoFactory() { return notificationRepoFactory; }
    public RepoFactory<ChargesRepository> getChargesRepoFactory() { return chargesRepoFactory; }
    public RepoFactory<RevenusRepository> getRevenusRepoFactory() { return revenusRepoFactory; }
    public RepoFactory<StatistiquesRepository> getStatistiquesRepoFactory() { return statistiquesRepoFactory; }
    public RepoFactory<SituationFinanciereRepository> getSituationFinanciereRepoFactory() { return situationFinanciereRepoFactory; }

    // ==== UTILITIES ====
    public String getProperty(String key, String defaultValue) {
        String value = props.getProperty(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    public String getProperty(String key) { return getProperty(key, null); }

    public static String splitCamelCase(String s) {
        if (s == null || s.isBlank()) return s;
        return s.replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2");
    }

    public void printAllBeans() {
        System.out.println("\n=== Beans chargés dans ApplicationContext ===");
        context.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Class::getSimpleName)))
                .forEach(e -> System.out.println(splitCamelCase(e.getKey().getSimpleName()) + " → " + e.getValue().getClass().getName()));
        System.out.println("Total : " + context.size() + " beans\n");
    }
}