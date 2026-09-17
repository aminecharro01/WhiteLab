package ma.WhiteLab.repository.common;

import ma.WhiteLab.entities.base.BaseEntity;
import ma.WhiteLab.entities.cabinet.CabinetMedicale;
import ma.WhiteLab.entities.cabinet.Statistiques;
import ma.WhiteLab.entities.dossierMedical.*;
import ma.WhiteLab.entities.cabinet.Charges;
import ma.WhiteLab.entities.cabinet.Revenus;
import ma.WhiteLab.entities.dossierMedical.SituationFinanciere;
import ma.WhiteLab.entities.patient.*;
import ma.WhiteLab.entities.enums.*;
import ma.WhiteLab.entities.user.Admin;
import ma.WhiteLab.entities.user.Medecin;
import ma.WhiteLab.entities.user.Secretaire;
import ma.WhiteLab.entities.agenda.AgendaMensuel;
import ma.WhiteLab.entities.agenda.RendezVous;
import ma.WhiteLab.entities.user.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class RowMappers {

    private RowMappers() {}

    // ============================================================
    //  MÉTHODE GLOBALE POUR MAPPER BaseEntity
    // ============================================================
    public static <T extends BaseEntity> T mapBaseEntity(ResultSet rs, T entity) throws SQLException {
        entity.setId(rs.getLong("id"));

        Timestamp tsCreation = rs.getTimestamp("dateCreation");
        entity.setDateCreation(tsCreation != null ? tsCreation.toLocalDateTime() : null);

        Timestamp tsMaj = rs.getTimestamp("dateMiseAJour");
        entity.setDateMiseAJour(tsMaj != null ? tsMaj.toLocalDateTime() : null);

        entity.setCreePar(getStringOrNull(rs, "creePar"));
        entity.setModifierPar(getStringOrNull(rs, "modifierPar"));

        return entity;
    }

    private static String getStringOrNull(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return rs.wasNull() ? null : value;
    }

    public static <E extends Enum<E>> E mapEnum(ResultSet rs, String columnName, Class<E> enumClass) throws SQLException {
        String value = getStringOrNull(rs, columnName);
        if (value != null && !value.isBlank()) {
            try {
                return Enum.valueOf(enumClass, value);
            } catch (IllegalArgumentException e) {
                System.err.println("Valeur enum invalide pour " + columnName + " : " + value);
            }
        }
        return null;
    }

    // ============================================================
// UTILISATEUR : ROWMAPPER AVEC SINGLE TABLE INHERITANCE (STI)
// ============================================================
    public static Utilisateur mapUtilisateur(ResultSet rs) throws SQLException {

        // ---- Discriminant obligatoire ----
        String typeStr = getStringOrNull(rs, "type");

        if (typeStr == null) {
            throw new SQLException(
                    "La colonne discriminante 'type' est obligatoire pour mapper un Utilisateur."
            );
        }

        // ---- Instanciation selon le type ----
        Utilisateur utilisateur = switch (typeStr) {
            case "ADMIN" -> new Admin();
            case "MEDECIN" -> new Medecin();
            case "SECRETAIRE" -> new Secretaire();
            default -> throw new SQLException("Type d'utilisateur inconnu : " + typeStr);
        };

        // 🔥 IMPORTANT : setter du type métier
        utilisateur.setType(RoleR.valueOf(typeStr));

        // =========================================================
        // Champs communs (BaseEntity + Utilisateur)
        // =========================================================
        mapBaseEntity(rs, utilisateur);

        utilisateur.setNom(rs.getString("nom"));
        utilisateur.setPrenom(rs.getString("prenom"));
        utilisateur.setEmail(getStringOrNull(rs, "email"));
        utilisateur.setAdresse(getStringOrNull(rs, "adresse"));
        utilisateur.setCin(getStringOrNull(rs, "cin"));
        utilisateur.setTelephone(getStringOrNull(rs, "telephone"));

        Date dateNaissSql = rs.getDate("dateNaissance");
        utilisateur.setDateNaissance(
                dateNaissSql != null ? dateNaissSql.toLocalDate() : null
        );

        utilisateur.setSexe(mapEnum(rs, "sexe", Sexe.class));

        Timestamp lastLoginTs = rs.getTimestamp("lastLoginDate");
        utilisateur.setLastLoginDate(
                lastLoginTs != null ? lastLoginTs.toLocalDateTime() : null
        );

        utilisateur.setMotDePasse(getStringOrNull(rs, "motDePasse"));

        // =========================================================
        // Champs communs au STAFF (Médecin + Secrétaire)
        // =========================================================
        if (utilisateur instanceof Medecin || utilisateur instanceof Secretaire) {

            Double salaire = rs.getObject("salaire", Double.class);
            Double prime = rs.getObject("prime", Double.class);

            Date dateRecSql = rs.getDate("dateRecrutement");
            LocalDate dateRecrutement =
                    dateRecSql != null ? dateRecSql.toLocalDate() : null;

            int soldeConge = rs.getInt("soldeConge");
            if (rs.wasNull()) soldeConge = 30;

            Long cabinetId = rs.getObject("cabinetMedicale_id", Long.class);
            CabinetMedicale cabinet = null;
            if (cabinetId != null) {
                cabinet = new CabinetMedicale();
                cabinet.setId(cabinetId);
            }

            // =================== MÉDECIN ===================
            if (utilisateur instanceof Medecin medecin) {

                medecin.setSalaire(salaire);
                medecin.setPrime(prime);
                medecin.setDateRecrutement(dateRecrutement);
                medecin.setSoldeConge(soldeConge);
                medecin.setCabinetMedicale(cabinet);

                medecin.setSpecialite(getStringOrNull(rs, "specialite"));

                // Initialisation safe
                medecin.setAgendaDocteur(new AgendaMensuel());
                medecin.setDossiersMed(new ArrayList<>());

            }
            // =================== SECRÉTAIRE ===================
            else if (utilisateur instanceof Secretaire secretaire) {

                secretaire.setSalaire(salaire);
                secretaire.setPrime(prime);
                secretaire.setDateRecrutement(dateRecrutement);
                secretaire.setSoldeConge(soldeConge);
                secretaire.setCabinetMedicale(cabinet);

                secretaire.setNumCNS(getStringOrNull(rs, "numCNS"));
                secretaire.setCommission(rs.getObject("commission", Double.class));
            }
        }

        // =========================================================
        // Relations (chargées plus tard)
        // =========================================================
        utilisateur.setRoles(new ArrayList<>());
        utilisateur.setNotifications(new ArrayList<>());

        return utilisateur;
    }


    // ============================================================
    //  DOSSIER MEDICAL (corrigé : medecin_id au lieu de medecine_id)
    // ============================================================
    public static DossierMedical mapDossierMedical(ResultSet rs) throws SQLException {
        DossierMedical d = mapBaseEntity(rs, new DossierMedical());

        d.setHistorique(getStringOrNull(rs, "historique"));

        long patId = rs.getLong("pat_id");
        if (!rs.wasNull()) {
            Patient p = new Patient();
            p.setId(patId);
            d.setPat(p);
        }

        long medecinId = rs.getLong("medecin_id"); // ← CORRIGÉ
        if (!rs.wasNull()) {
            Medecin medecin = new Medecin();
            medecin.setId(medecinId);
            d.setMedecine(medecin); // ou d.setMedecin(...) selon votre getter/setter
        }

        return d;
    }

    // ============================================================
    //  AGENDA MENSUEL (utilise maintenant Utilisateur → on caste en Medecin)
    // ============================================================
    public static AgendaMensuel mapAgendaMensuel(ResultSet rs) throws SQLException {
        AgendaMensuel a = mapBaseEntity(rs, new AgendaMensuel());

        // Mapping du mois en tant qu'enum Mois
        a.setMois(mapEnum(rs, "mois", Mois.class));

        // Initialisation de la liste des jours non disponibles
        a.setJoursNonDisponible(new ArrayList<>());

        // Mapping du médecin responsable de l'agenda
        long medId = rs.getLong("medecin_id");
        if (!rs.wasNull()) {
            Medecin m = new Medecin();
            m.setId(medId);
            a.setMedecin(m);
        }

        return a;
    }

    // ============================================================
    //  PATIENT
    // ============================================================
    public static Patient mapPatient(ResultSet rs) throws SQLException {
        Patient patient = mapBaseEntity(rs, new Patient());

        patient.setNom(rs.getString("nom"));
        patient.setPrenom(rs.getString("prenom"));
        patient.setAdresse(getStringOrNull(rs, "adresse"));
        patient.setTelephone(getStringOrNull(rs, "telephone"));
        patient.setEmail(getStringOrNull(rs, "email"));

        Date dn = rs.getDate("dateNaissance");
        patient.setDateNaissance(dn != null ? dn.toLocalDate() : null);

        patient.setSexe(mapEnum(rs, "sexe", Sexe.class));
        patient.setAssurance(mapEnum(rs, "assurance", Assurance.class));
        patient.setAntecedents(new ArrayList<>());

        return patient;
    }


    // ============================================================
    //  DENT (odontogramme)
    // ============================================================
    public static Dent mapDent(ResultSet rs) throws SQLException {
        Dent d = mapBaseEntity(rs, new Dent());
        d.setPatientId(rs.getLong("patient_id"));
        d.setNumero(rs.getInt("numero"));
        d.setEtat(mapEnum(rs, "etat", EtatDent.class));
        d.setNote(getStringOrNull(rs, "note"));
        return d;
    }

    // ============================================================
    //  ANTECEDENT
    // ============================================================
    public static Antecedent mapAntecedent(ResultSet rs) throws SQLException {

        Antecedent a = mapBaseEntity(rs, new Antecedent());

        a.setNom(rs.getString("nom"));
        a.setDescription(rs.getString("description"));

        a.setCategorie(mapEnum(rs, "categorie", CategorieAntecedent.class));
        a.setNiveauDeRisk(mapEnum(rs, "niveauRisque", NiveauDeRisk.class));

        return a;
    }


    // ============================================================
    //  CERTIFICAT
    // ============================================================
    public static Certificat mapCertificat(ResultSet rs) throws SQLException {

        Certificat c = mapBaseEntity(rs, new Certificat());

        Date dDebut = rs.getDate("dateDebut");
        if (dDebut != null) c.setDateDebut(dDebut.toLocalDate());

        Date dFin = rs.getDate("dateFin");
        if (dFin != null) c.setDateFin(dFin.toLocalDate());

        c.setDureeRepos(rs.getInt("dureeRepos"));
        c.setContenu(rs.getString("contenu"));

        // Mappage des relations (IDs uniquement)
        long consultationId = rs.getLong("consultation_id");
        if (!rs.wasNull()) {
            Consultation consult = new Consultation();
            consult.setId(consultationId);
            c.setConsultation(consult);
        }

        long dossierMedId = rs.getLong("dossier_med_id");
        if (!rs.wasNull()) {
            DossierMedical dm = new DossierMedical();
            dm.setId(dossierMedId);
            c.setDossierMedical(dm);
        }

        return c;
    }

    // ============================================================
    //  NOTIFICATION
    // ============================================================
    public static Notification mapNotification(ResultSet rs) throws SQLException {

        Notification n = mapBaseEntity(rs, new Notification());

        // Mappage robuste des Enums
        n.setTitre(mapEnum(rs, "titre", TitreNotification.class));

        n.setMessage(rs.getString("message"));

        Date d = rs.getDate("date");
        if (d != null) n.setDate(d.toLocalDate());

        Time t = rs.getTime("time");
        if (t != null) n.setTime(t.toLocalTime());

        n.setType(mapEnum(rs, "type", TypeNotification.class));
        n.setPriorite(mapEnum(rs, "priorite", PrioriteNotification.class));

        return n;
    }

    public static Role mapRole(ResultSet rs, Connection c) throws SQLException {
        Role role = new Role();

        // Mapping des champs de BaseEntity
        role.setId(rs.getLong("id"));
        role.setDateCreation(getLocalDateTime(rs, "dateCreation"));
        role.setDateMiseAJour(getLocalDateTime(rs, "dateMiseAJour"));
        role.setCreePar(rs.getString("creePar"));
        role.setModifierPar(rs.getString("modifierPar"));

        // Mapping spécifique Role avec Enum RoleR
        role.setLibelle(mapEnum(rs, "libelle", RoleR.class));
        role.setDescription(rs.getString("description"));

        // Mapping des privilèges via Role_Privilege
        List<RolePrivilege> privileges = new ArrayList<>();
        String sqlPriv = "SELECT privilege FROM Role_Privilege WHERE role_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sqlPriv)) {
            ps.setLong(1, role.getId());
            try (ResultSet rsPriv = ps.executeQuery()) {
                while (rsPriv.next()) {
                    RolePrivilege rp = new RolePrivilege();
                    rp.setPrivilege(rsPriv.getString("privilege"));
                    rp.setRole(role);
                    privileges.add(rp);
                }
            }
        }
        role.setPrivileges(privileges);

        return role;
    }


    // Méthode utilitaire pour convertir DATETIME en LocalDateTime
    private static java.time.LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        if (ts != null) return ts.toLocalDateTime();
        return null;
    }

    public static CabinetMedicale mapCabinetMedical(ResultSet rs) throws SQLException {
        CabinetMedicale c = mapBaseEntity(rs, new CabinetMedicale());

        c.setNom(rs.getString("nom"));
        c.setEmail(getStringOrNull(rs, "email"));
        c.setLogo(getStringOrNull(rs, "logo"));
        c.setCategorie(getStringOrNull(rs, "categorie"));
        c.setTel1(getStringOrNull(rs, "tel1"));
        c.setTel2(getStringOrNull(rs, "tel2"));
        c.setSiteWeb(getStringOrNull(rs, "siteWeb"));
        c.setInstagram(getStringOrNull(rs, "instagram"));
        c.setFacebook(getStringOrNull(rs, "facebook"));
        c.setDescription(getStringOrNull(rs, "description"));

        c.setStatistiques(new ArrayList<>());
        c.setCharges(new ArrayList<>());
        c.setRevenus(new ArrayList<>());

        return c;
    }

    public static Consultation mapConsultation(ResultSet rs) throws SQLException {
        Consultation c = mapBaseEntity(rs, new Consultation());

        Timestamp dateConsult = rs.getTimestamp("date");
        c.setDate(dateConsult != null ? dateConsult.toLocalDateTime() : null);

        c.setStatus(mapEnum(rs, "status", StatusConsultation.class));
        c.setNotes(getStringOrNull(rs, "notes"));
        c.setObservationsMedecin(getStringOrNull(rs, "observations_medecin"));

        long dossierId = rs.getLong("dossier_medical_id");
        if (!rs.wasNull()) {
            DossierMedical dm = new DossierMedical();
            dm.setId(dossierId);
            c.setDossierMedical(dm);
        }

        c.setOrdonnances(new ArrayList<>());
        c.setCertificats(new Certificat()); // ← CORRIGÉ : Liste, pas un seul objet
        c.setIms(new ArrayList<>());
        c.setFactures(new ArrayList<>());
        c.setRendezVouses(new ArrayList<>());

        return c;
    }

    // ============================================================
    // Statistiques
    // ============================================================
    public static Statistiques mapStatistiques(ResultSet rs) throws SQLException {
        Statistiques s = mapBaseEntity(rs, new Statistiques());

        s.setNom(rs.getString("nom"));

        s.setCategorie(mapEnum(rs, "categorie", CategorieStatistique.class));

        // CORRIGÉ : Vérification du double
        double chiffre = rs.getDouble("chiffre");
        if (!rs.wasNull()) s.setChiffre(chiffre);

        java.sql.Date d = rs.getDate("dateCalcul");
        if (d != null) s.setDateCalcul(d.toLocalDate());

        // Chargement minimal de CabinetMedicale : juste l'ID
        long cabinetId = rs.getLong("cabinet_id");
        if (!rs.wasNull()) {
            CabinetMedicale c = new CabinetMedicale();
            c.setId(cabinetId);
            s.setCabinetMedicale(c);
        }

        return s;
    }

    // ============================================================
    // ACTE MEDICAL
    // ============================================================
    public static ActeMedical mapActeMedical(ResultSet rs) throws SQLException {
        ActeMedical acte = mapBaseEntity(rs, new ActeMedical());

        acte.setLibelle(rs.getString("libelle"));
        acte.setCategorie(rs.getString("categorie"));

        // CORRIGÉ : Vérification du float
        float prix = rs.getFloat("prixDeBase");
        if (!rs.wasNull()) acte.setPrixDeBase(prix);

        // La liste d'interventions sera chargée via le service
        acte.setInterventionMedecin(new ArrayList<>());

        return acte;
    }
    // ============================================================
    // INTERVENTION MEDECIN
    // ============================================================
    public static InterventionMedecin mapInterventionMedecin(ResultSet rs) throws SQLException {
        InterventionMedecin im = mapBaseEntity(rs, new InterventionMedecin());

        // CORRIGÉ : Vérification du double
        double prix = rs.getDouble("prixDePatient");
        if (!rs.wasNull()) im.setPrixDePatient(prix);

        // CORRIGÉ : Vérification de l'int
        int num = rs.getInt("numDent");
        if (!rs.wasNull()) im.setNumDent(num);

        // Mappage des relations (IDs uniquement)
        long consultationId = rs.getLong("consultation_id");
        if (!rs.wasNull()) {
            Consultation c = new Consultation();
            c.setId(consultationId);
            im.setConsultation(c);
        }

        long acteMedicalId = rs.getLong("acteMedical_id");
        if (!rs.wasNull()) {
            ActeMedical am = new ActeMedical();
            am.setId(acteMedicalId);
            im.setActeMedical(am);
        }

        return im;
    }

    // ============================================================
    // REVENUS
    // ============================================================
    public static Revenus mapRevenus(ResultSet rs) throws SQLException {
        Revenus r = mapBaseEntity(rs, new Revenus());

        r.setTitre(rs.getString("titre"));
        r.setDescription(rs.getString("description"));

        // CORRIGÉ : Vérification du double
        double montant = rs.getDouble("montant");
        if (!rs.wasNull()) r.setMontant(montant);

        Timestamp dt = rs.getTimestamp("date");
        if (dt != null) r.setDate(dt.toLocalDateTime());

        // Relation CabinetMedicale à charger via service
        long cabinetId = rs.getLong("cabinet_id");
        if (!rs.wasNull()) {
            CabinetMedicale cabinet = new CabinetMedicale();
            cabinet.setId(cabinetId);
            r.setCabinetMedicale(cabinet);
        }

        return r;
    }

    // ============================================================
    // FACTURE
    // ============================================================
    public static Facture mapFacture(ResultSet rs) throws SQLException {
        Facture f = mapBaseEntity(rs, new Facture());

        // CORRIGÉ : Vérification des floats
        float totalFact = rs.getFloat("totalFact");
        if (!rs.wasNull()) f.setTotalFact(totalFact);

        float totalPaye = rs.getFloat("totalPaye");
        if (!rs.wasNull()) f.setTotalPaye(totalPaye);

        float reste = rs.getFloat("reste");
        if (!rs.wasNull()) f.setReste(reste);

        Date d = rs.getDate("date");
        if (d != null) f.setDate(d.toLocalDate());

        f.setStatut(mapEnum(rs, "statut", StatutFacture.class));

        // Relations Consultation et SituationFinanciere à charger via service (IDs uniquement)
        long consultationId = rs.getLong("consultation_id");
        if (!rs.wasNull()) {
            Consultation c = new Consultation();
            c.setId(consultationId);
            f.setConsultation(c);
        }

        long sfId = rs.getLong("sf_id");
        if (!rs.wasNull()) {
            SituationFinanciere sf = new SituationFinanciere();
            sf.setId(sfId);
            f.setSf(sf);
        }

        return f;
    }

    // ============================================================
    // Situation Financiere
    // ============================================================
    public static SituationFinanciere mapSituationFinanciere(ResultSet rs) throws SQLException {
        SituationFinanciere sf = mapBaseEntity(rs, new SituationFinanciere());

        // CORRIGÉ : Vérification des floats
        float totalDesActes = rs.getFloat("totalDesActes");
        if (!rs.wasNull()) sf.setTotalDesActes(totalDesActes);

        float totalPaye = rs.getFloat("totalPaye");
        if (!rs.wasNull()) sf.setTotalPaye(totalPaye);

        float credit = rs.getFloat("credit");
        if (!rs.wasNull()) sf.setCredit(credit);

        sf.setEnPromo(mapEnum(rs, "enPromo", PromoStatus.class));
        sf.setStatus(mapEnum(rs, "status", Status.class));

        // Relations listeFactures et dossierMedical à charger via service (IDs uniquement)
        sf.setListeFactures(new ArrayList<>());

        long dossierMedicalId = rs.getLong("dossierMedical_id");
        if (!rs.wasNull()) {
            DossierMedical dm = new DossierMedical();
            dm.setId(dossierMedicalId);
            sf.setDossierMedical(dm);
        }

        return sf;
    }

    // ============================================================
    // RendezVous
    // ============================================================
    public static RendezVous mapRendezVous(ResultSet rs) throws SQLException {
        RendezVous r = mapBaseEntity(rs, new RendezVous());

        r.setDate(rs.getTimestamp("dateRDV") != null ? rs.getTimestamp("dateRDV").toLocalDateTime() : null);
        r.setTime(rs.getTime("heure_rdv") != null ? rs.getTime("heure_rdv").toLocalTime() : null);
        r.setMotif(rs.getString("motif"));
        r.setStatus(mapEnum(rs, "status", Status.class));
        r.setNoteMedecin(rs.getString("note_medecin"));

        // ============================
        // Dossier médical (d_id)
        // ============================
        Long dmid = rs.getObject("d_id", Long.class);
        if (dmid == null) {
            // Fallback for queries that don't use the 'd_id' alias
            dmid = rs.getObject("dossier_med_id", Long.class);
        }

        if (dmid != null) {
            DossierMedical dm = new DossierMedical();
            dm.setId(dmid);

            // ============================
            // Patient (p_id, p_nom, p_prenom)
            // ============================
            Long patId = rs.getObject("p_id", Long.class);
             if (patId == null) {
                // Fallback for queries that don't use the 'p_id' alias
                patId = rs.getObject("pat_id", Long.class);
            }

            if (patId != null) {
                Patient p = new Patient();
                p.setId(patId);
                String nom = rs.getString("p_nom");
                String prenom = rs.getString("p_prenom");
                p.setNom(nom);
                p.setPrenom(prenom);
                dm.setPat(p);

                System.out.println("[DEBUG] Mapped Patient: id=" + patId + ", nom=" + nom + ", prenom=" + prenom);
            } else {
                 System.out.println("[DEBUG] Patient ID not found in ResultSet for Dossier ID: " + dmid);
            }

            r.setDossierMed(dm);
        } else {
             System.out.println("[DEBUG] DossierMedical ID not found in ResultSet for RendezVous ID: " + r.getId());
        }
        
        Long consultationId = rs.getObject("consultation_id", Long.class);
        if (consultationId != null) {
            Consultation c = new Consultation();
            c.setId(consultationId);
            r.setConsultation(c);
        }

        return r;
    }

    public static RendezVous mapRendezVousWithMedecin(ResultSet rs) throws SQLException {
        RendezVous r = mapRendezVous(rs);
        if (r.getDossierMed() != null && r.getDossierMed().getMedecine() != null) {
            Medecin medecin = r.getDossierMed().getMedecine();
            medecin.setNom(rs.getString("medecin_nom"));
            medecin.setPrenom(rs.getString("medecin_prenom"));
        }
        return r;
    }


    // ============================================================
    // Medicament
    // ============================================================
    public static Medicament mapMedicament(ResultSet rs) throws SQLException {
        Medicament m = mapBaseEntity(rs, new Medicament()); // Utiliser mapBaseEntity

        m.setNom(rs.getString("nom"));
        m.setLabo(rs.getString("labo"));
        m.setType(rs.getString("type"));

        m.setForme(mapEnum(rs, "forme", Forme.class));

        // CORRIGÉ : Vérification du boolean (parfois getBoolean peut être unreliable)
        Boolean remboursable = (Boolean) rs.getObject("remboursable");
        m.setRemboursable(remboursable != null && remboursable);

        // CORRIGÉ : Vérification du double
        double prix = rs.getDouble("prix_unitaire");
        if (!rs.wasNull()) m.setPrixUnitaire(prix);

        m.setDescription(rs.getString("description"));

        // prescriptions NON CHARGEES ici (Lazy)
        m.setPrescriptions(new ArrayList<>());

        return m;
    }

    // ============================================================
    // Ordonnance
    // ============================================================
    public static Ordonnance mapOrdonnance(ResultSet rs) throws SQLException {
        Ordonnance o = mapBaseEntity(rs, new Ordonnance()); // Utiliser mapBaseEntity

        o.setDateOrdonnance(rs.getDate("date_ordonnance") != null ? rs.getDate("date_ordonnance").toLocalDate() : null);

        // Relations : consultation et dossierMedical
        long consultationId = rs.getLong("consultation_id");
        if (!rs.wasNull()) {
            Consultation c = new Consultation();
            c.setId(consultationId);
            o.setConsultation(c);
        }

        long dossierId = rs.getLong("dossier_med_id");
        if (!rs.wasNull()) {
            DossierMedical d = new DossierMedical();
            d.setId(dossierId);
            o.setDossierMedical(d);
        }

        // prescriptions NON CHARGEES ici (lazy)
        o.setPrescriptions(new ArrayList<>());

        return o;
    }

    // ============================================================
    // Prescription
    // ============================================================
    public static Prescription mapPrescription(ResultSet rs) throws SQLException {
        Prescription p = mapBaseEntity(rs, new Prescription()); // Utiliser mapBaseEntity

        // CORRIGÉ : Vérification des Int
        int qte = rs.getInt("qte");
        if (!rs.wasNull()) p.setQte(qte);

        p.setFrequence(rs.getString("frequence"));

        int duree = rs.getInt("duree");
        if (!rs.wasNull()) p.setDuree(duree);

        // Relations : ordonnance et medicament
        long ordonnanceId = rs.getLong("ordonnance_id");
        if (!rs.wasNull()) {
            Ordonnance o = new Ordonnance();
            o.setId(ordonnanceId);
            p.setOrdonnance(o);
        }

        long medicamentId = rs.getLong("medicament_id");
        if (!rs.wasNull()) {
            Medicament m = new Medicament();
            m.setId(medicamentId);
            p.setMedicament(m);
        }

        return p;
    }

    public static Charges mapCharges(ResultSet rs) throws SQLException {
        Charges c = mapBaseEntity(rs, new Charges());

        c.setTitre(getStringOrNull(rs, "titre"));
        c.setDescription(getStringOrNull(rs, "description"));

        // FIX: Use getDouble() with a null check to avoid ClassCastException
        if (rs.getObject("montant") != null) {
            c.setMontant(rs.getDouble("montant"));
        } else {
            c.setMontant(null);
        }

        Timestamp dt = rs.getTimestamp("date");
        c.setDate(dt != null ? dt.toLocalDateTime() : null);

        long cabinetId = rs.getLong("cabinet_id");
        if (!rs.wasNull()) {
            CabinetMedicale cabinet = new CabinetMedicale();
            cabinet.setId(cabinetId);
            c.setCabinetMedicale(cabinet);
        }

        return c;
    }

}