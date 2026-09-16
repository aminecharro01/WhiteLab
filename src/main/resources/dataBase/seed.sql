-- 1. Insert Roles (Matching your 'Role' table)
INSERT INTO Role (id, libelle, description, creePar)
VALUES
    (1, 'ROLE_ADMIN', 'Administrateur système', 'SYSTEM'),
    (2, 'ROLE_MEDECIN', 'Médecin / Dentiste', 'SYSTEM'),
    (3, 'ROLE_SECRETAIRE', 'Secrétariat et accueil', 'SYSTEM');

-- 2. Insert Privileges (Matching your 'Role_Privilege' table)
INSERT INTO Role_Privilege (role_id, privilege) VALUES
                                                    (1, 'GESTION_UTILISATEURS'),
                                                    (1, 'GESTION_CABINET'),
                                                    (2, 'GESTION_PATIENTS'),
                                                    (2, 'GERER_DOSSIERS'),
                                                    (2, 'GERER_RDV'),
                                                    (3, 'GESTION_PATIENTS'),
                                                    (3, 'GESTION_CAISSE'),
                                                    (3, 'GERER_AGENDA_MEDECIN'),
                                                    (3, 'GERER_RDV');

-- 3. Insert Users into the 'Utilisateur' table (Single Table Inheritance)
-- Note: 'avatar' and 'login' were not in your CREATE TABLE,
-- I have mapped them to existing fields or omitted them.

-- ADMIN
INSERT INTO Utilisateur
(id, nom, prenom, email, adresse, cin, telephone, sexe, motDePasse, dateNaissance, type, creePar)
VALUES
    (1, 'El Midaoui', 'Omar', 'admin.omar@dentaltech.ma', 'Rabat Agdal', 'X1234567', '0612345678', 'HOMME',
     '$2a$10$JzjdZK8fGupihk4/yfJLWeK1auTzwiFQg2E0g94MOxyPhg0e/rilC', '1994-04-10', 'ADMIN', 'SYSTEM');

-- MEDECIN
INSERT INTO Utilisateur
(id, nom, prenom, email, adresse, cin, telephone, sexe, motDePasse, dateNaissance, type, specialite, salaire, prime, dateRecrutement, creePar)
VALUES
    (2, 'Alami', 'Wafae', 'med.wafae@dentaltech.ma', 'Rabat Hassan', 'X7654321', '0678901234', 'FEMME',
     '$2a$10$XYTetaH42NaC52Uknz.0leKegAdoJXb8lXDV5aEEhUz2XaxeHmBvC', '1994-04-10', 'MEDECIN', 'Chirurgie dentaire – Implantologie', 25000.00, 2500.00, '2021-02-15', 'SYSTEM');

-- SECRETAIRE
INSERT INTO Utilisateur
(id, nom, prenom, email, adresse, cin, telephone, sexe, motDePasse, dateNaissance, type, numCNS, commission, salaire, prime, dateRecrutement, creePar)
VALUES
    (3, 'Ben Ali', 'Fatima Zahra', 'fz.secretariat@dentaltech.ma', 'Salé Tabriquet', 'C998877', '0654321098', 'FEMME',
     '$2a$10$nPuXpxsbMyVBJVnheD/diOM0pw/8mm3S6moHBRMOM7p9V.4dCNG0O', '1998-02-20', 'SECRETAIRE', 'CNSS009988', 5.0, 8000.00, 300.00, '2022-10-01', 'SYSTEM');

-- 4. Associate Users with Roles
INSERT INTO Utilisateur_Role (utilisateur_id, role_id) VALUES
                                                           (1, 1),
                                                           (2, 2),
                                                           (3, 3);