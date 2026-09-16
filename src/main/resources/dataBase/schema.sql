CREATE DATABASE IF NOT EXISTS WhiteLabDB;
USE WhiteLabDB;

-- =========================
-- Table Acte_Medical
-- =========================
CREATE TABLE IF NOT EXISTS Acte_Medical (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    libelle VARCHAR(255) NOT NULL,
    categorie VARCHAR(100),
    prixDeBase DECIMAL(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Table CabinetMedicale
-- =========================
CREATE TABLE IF NOT EXISTS CabinetMedicale (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    logo VARCHAR(255),
    categorie VARCHAR(100),
    tel1 VARCHAR(50),
    tel2 VARCHAR(50),
    siteWeb VARCHAR(255),
    instagram VARCHAR(255),
    facebook VARCHAR(255),
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Table Utilisateur (Single Table Inheritance : Admin, Médecin, Secrétaire)
-- =========================
CREATE TABLE Utilisateur (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),

    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    adresse VARCHAR(255),
    cin VARCHAR(50),
    telephone VARCHAR(50),
    dateNaissance DATE,
    sexe ENUM('HOMME','FEMME'),
    lastLoginDate DATETIME,
    motDePasse VARCHAR(255) NOT NULL,

    -- Discriminateur
    type ENUM('ADMIN','MEDECIN','SECRETAIRE') NOT NULL,

    -- Champs communs Staff (NULL pour ADMIN)
    salaire DECIMAL(10,2),
    prime DECIMAL(10,2),
    dateRecrutement DATE,
    soldeConge INT DEFAULT 30,
    cabinetMedicale_id BIGINT,

    -- Champs spécifiques Médecin
    specialite VARCHAR(255),

    -- Champs spécifiques Secrétaire
    numCNS VARCHAR(50),
    commission DECIMAL(10,2),

    CONSTRAINT fk_user_cabinet
        FOREIGN KEY (cabinetMedicale_id)
        REFERENCES CabinetMedicale(id)
        ON DELETE SET NULL,

    INDEX idx_type (type),
    INDEX idx_email (email),
    INDEX idx_nom_prenom (nom, prenom),
    INDEX idx_cabinet (cabinetMedicale_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Table Patient
-- =========================
CREATE TABLE IF NOT EXISTS Patient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    sexe VARCHAR(10),
    email VARCHAR(255),
    dateNaissance DATE,
    adresse VARCHAR(255),
    telephone VARCHAR(50),
    assurance VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Table DossierMedical
-- =========================
CREATE TABLE IF NOT EXISTS DossierMedical (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    historique TEXT,
    pat_id BIGINT,
    medecin_id BIGINT,
    CONSTRAINT fk_dossier_patient FOREIGN KEY (pat_id) REFERENCES Patient(id) ON DELETE SET NULL,
    CONSTRAINT fk_dossier_medecin FOREIGN KEY (medecin_id) REFERENCES Utilisateur(id) ON DELETE SET NULL,
    INDEX idx_patient (pat_id),
    INDEX idx_medecin (medecin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Antécédents
-- =========================
CREATE TABLE IF NOT EXISTS Antecedent (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    categorie VARCHAR(50),
    niveauRisque VARCHAR(50),
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS Patient_Antecedent (
    patient_id BIGINT NOT NULL,
    antecedent_id BIGINT NOT NULL,
    PRIMARY KEY (patient_id, antecedent_id),
    CONSTRAINT fk_pa_patient FOREIGN KEY (patient_id) REFERENCES Patient(id) ON DELETE CASCADE,
    CONSTRAINT fk_pa_antecedent FOREIGN KEY (antecedent_id) REFERENCES Antecedent(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Table Consultation
-- =========================
CREATE TABLE IF NOT EXISTS Consultation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    date DATETIME NOT NULL,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    observations_medecin TEXT,
    dossier_medical_id BIGINT,
    CONSTRAINT fk_consultation_dossier FOREIGN KEY (dossier_medical_id) REFERENCES DossierMedical(id) ON DELETE SET NULL,
    INDEX idx_dossier_medical (dossier_medical_id),
    INDEX idx_date (date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- InterventionMedecin
-- =========================
CREATE TABLE IF NOT EXISTS InterventionMedecin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    prixDePatient DECIMAL(10,2),
    numDent INT,
    consultation_id BIGINT,
    acteMedical_id BIGINT,
    FOREIGN KEY (consultation_id) REFERENCES Consultation(id) ON DELETE CASCADE,
    FOREIGN KEY (acteMedical_id) REFERENCES Acte_Medical(id) ON DELETE CASCADE,
    INDEX idx_consultation (consultation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Certificat
-- =========================
CREATE TABLE IF NOT EXISTS Certificat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    dateDebut DATE,
    dateFin DATE,
    dureeRepos INT,
    contenu TEXT,
    consultation_id BIGINT,
    dossier_med_id BIGINT,
    FOREIGN KEY (consultation_id) REFERENCES Consultation(id) ON DELETE CASCADE,
    FOREIGN KEY (dossier_med_id) REFERENCES DossierMedical(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- AgendaMensuel & AgendaMensuel_Jour
-- =========================
CREATE TABLE IF NOT EXISTS AgendaMensuel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    mois VARCHAR(20),
    medecin_id BIGINT,
    CONSTRAINT fk_agenda_medecin FOREIGN KEY (medecin_id) REFERENCES Utilisateur(id) ON DELETE CASCADE,
    INDEX idx_medecin (medecin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS AgendaMensuel_Jour (
    agenda_id BIGINT NOT NULL,
    jour_nom VARCHAR(20) NOT NULL,
    PRIMARY KEY (agenda_id, jour_nom),
    CONSTRAINT fk_agenda_jours FOREIGN KEY (agenda_id) REFERENCES AgendaMensuel(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- RendezVous
-- =========================
CREATE TABLE IF NOT EXISTS RendezVous (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    dateRDV DATETIME,
    heure_rdv TIME,
    motif VARCHAR(255),
    status VARCHAR(50),
    note_medecin TEXT,
    dossier_med_id BIGINT,
    consultation_id BIGINT,
    FOREIGN KEY (dossier_med_id) REFERENCES DossierMedical(id) ON DELETE CASCADE,
    FOREIGN KEY (consultation_id) REFERENCES Consultation(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Medicament
-- =========================
CREATE TABLE IF NOT EXISTS Medicament (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    nom VARCHAR(255),
    labo VARCHAR(255),
    type VARCHAR(50),
    forme VARCHAR(50),
    remboursable BOOLEAN,
    prix_unitaire DECIMAL(10,2),
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medicament_antecedent (
    medicament_id BIGINT NOT NULL,
    antecedent_id BIGINT NOT NULL,
    PRIMARY KEY (medicament_id, antecedent_id),
    FOREIGN KEY (medicament_id) REFERENCES medicament(id) ON DELETE CASCADE,
    FOREIGN KEY (antecedent_id) REFERENCES antecedent(id) ON DELETE CASCADE
);


-- =========================
-- Ordonnance
-- =========================
CREATE TABLE IF NOT EXISTS Ordonnance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    date_ordonnance DATE,
    consultation_id BIGINT,
    dossier_med_id BIGINT,
    FOREIGN KEY (consultation_id) REFERENCES Consultation(id) ON DELETE CASCADE,
    FOREIGN KEY (dossier_med_id) REFERENCES DossierMedical(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Prescription
-- =========================
CREATE TABLE IF NOT EXISTS Prescription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    qte INT,
    frequence VARCHAR(50),
    duree INT,
    ordonnance_id BIGINT,
    medicament_id BIGINT,
    FOREIGN KEY (ordonnance_id) REFERENCES Ordonnance(id) ON DELETE CASCADE,
    FOREIGN KEY (medicament_id) REFERENCES Medicament(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Role
-- =========================
CREATE TABLE IF NOT EXISTS Role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Role_Privilege (gestion granulaire des permissions)
-- =========================
CREATE TABLE IF NOT EXISTS Role_Privilege (
    role_id BIGINT NOT NULL,
    privilege VARCHAR(100) NOT NULL,
    PRIMARY KEY (role_id, privilege),
    CONSTRAINT fk_role_privilege_role
        FOREIGN KEY (role_id) REFERENCES Role(id) ON DELETE CASCADE,
    INDEX idx_privilege (privilege)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Notification
-- =========================
CREATE TABLE IF NOT EXISTS Notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    date DATE,
    time TIME,
    type VARCHAR(50) NOT NULL,
    priorite VARCHAR(50) NOT NULL,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(100),
    modifierPar VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Liaison Utilisateur <-> Role
-- =========================
CREATE TABLE IF NOT EXISTS Utilisateur_Role (
    utilisateur_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (utilisateur_id, role_id),
    FOREIGN KEY (utilisateur_id) REFERENCES Utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES Role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Liaison Utilisateur <-> Notification
-- =========================
CREATE TABLE IF NOT EXISTS Utilisateur_Notification (
    utilisateur_id BIGINT NOT NULL,
    notification_id BIGINT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    deleted_by_user BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (utilisateur_id, notification_id),
    FOREIGN KEY (utilisateur_id) REFERENCES Utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (notification_id) REFERENCES Notification(id) ON DELETE CASCADE,
    INDEX idx_user_notification_read (utilisateur_id, is_read),
    INDEX idx_user_notification_deleted (utilisateur_id, deleted_by_user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Statistiques
-- =========================
CREATE TABLE IF NOT EXISTS Statistiques (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    nom VARCHAR(255) NOT NULL,
    categorie VARCHAR(50),
    chiffre DECIMAL(12,2),
    dateCalcul DATE,
    cabinet_id BIGINT,
    FOREIGN KEY (cabinet_id) REFERENCES CabinetMedicale(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Charges
-- =========================
CREATE TABLE IF NOT EXISTS charges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255),
    description TEXT,
    montant DECIMAL(12,2),
    date DATETIME,
    cabinet_id BIGINT,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    CONSTRAINT fk_charges_cabinet FOREIGN KEY (cabinet_id) REFERENCES CabinetMedicale(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Revenus
-- =========================
CREATE TABLE IF NOT EXISTS revenus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255),
    description TEXT,
    montant DECIMAL(12,2),
    date DATETIME,
    cabinet_id BIGINT,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    CONSTRAINT fk_revenus_cabinet FOREIGN KEY (cabinet_id) REFERENCES CabinetMedicale(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- SituationFinanciere
-- =========================
CREATE TABLE IF NOT EXISTS SituationFinanciere (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    totalDesActes DECIMAL(12,2),
    totalPaye DECIMAL(12,2),
    credit DECIMAL(12,2),
    enPromo VARCHAR(50),
    status VARCHAR(50),
    dossierMedical_id BIGINT,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    CONSTRAINT fk_situation_dossier FOREIGN KEY (dossierMedical_id) REFERENCES DossierMedical(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================
-- Facture
-- =========================
CREATE TABLE IF NOT EXISTS facture (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    totalFact DECIMAL(12,2),
    totalPaye DECIMAL(12,2),
    reste DECIMAL(12,2),
    date DATE,
    statut VARCHAR(50),
    consultation_id BIGINT,
    sf_id BIGINT,
    dateCreation DATETIME DEFAULT CURRENT_TIMESTAMP,
    dateMiseAJour DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    creePar VARCHAR(255),
    modifierPar VARCHAR(255),
    CONSTRAINT fk_facture_consultation FOREIGN KEY (consultation_id) REFERENCES Consultation(id) ON DELETE SET NULL,
    CONSTRAINT fk_facture_situation_financiere FOREIGN KEY (sf_id) REFERENCES SituationFinanciere(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;