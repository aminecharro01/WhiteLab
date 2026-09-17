![WhiteLab preview](docs/mockup.jpg)

# WhiteLab - Système de Gestion de Cabinet Médical

WhiteLab est une application de bureau complète développée en Java pour faciliter la gestion quotidienne d'un cabinet médical. Elle offre une interface moderne et intuitive pour gérer les patients, les dossiers médicaux, les rendez-vous et l'administration du cabinet.

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Java%20Swing%20%2B%20FlatLaf-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)

## 🚀 Fonctionnalités Clés

L'application est structurée autour de plusieurs modules métiers essentiels :

*   **Gestion des Patients** : Création, modification et suivi des dossiers administratifs des patients.
*   **Dossier Médical Informatisé** : Historique médical, consultations, prescriptions, et antécédents centralisés.
*   **Agenda et Rendez-vous** : Planification des consultations, gestion des créneaux horaires et suivi des rendez-vous.
*   **Gestion du Cabinet** : Configuration des informations du cabinet médical.
*   **Gestion des Utilisateurs** : Administration des comptes d'accès (Médecins, Secrétaires, Administrateurs) avec gestion des rôles.
*   **Authentification Sécurisée** : Système de connexion sécurisé pour protéger les données sensibles.
*   **Interface Moderne** : Utilisation de **FlatLaf** pour une expérience utilisateur fluide et esthétique.

## 🛠️ Stack Technique

Le projet repose sur une architecture robuste et des technologies éprouvées :

*   **Langage** : Java SE 21+
*   **Architecture** : MVC (Modèle-Vue-Contrôleur) pour une séparation claire des responsabilités.
*   **Interface Graphique (GUI)** : Java Swing avec la librairie [FlatLaf](https://www.formdev.com/flatlaf/) pour le Look & Feel.
*   **Base de Données** : MySQL 8.0+.
*   **Accès aux Données** : JDBC avec le driver `mysql-connector-j`.
*   **Build & Dépendances** : Apache Maven.
*   **Utilitaires** :
    *   `Lombok` pour réduire le code boilerplate.
    *   `JCalendar` pour les composants de date.
    *   `BCrypt` pour le hachage sécurisé des mots de passe.
    *   `iText PDF` pour la génération de documents/rapports.

## 📋 Prérequis

Avant de lancer le projet, assurez-vous d'avoir installé :

*   [Java Development Kit (JDK) 21](https://www.oracle.com/java/technologies/downloads/) ou supérieur.
*   [Apache Maven](https://maven.apache.org/download.cgi).
*   [MySQL Server](https://dev.mysql.com/downloads/mysql/).
*   Un IDE Java (IntelliJ IDEA, Eclipse, NetBeans, ou VS Code).

## ⚙️ Installation et Configuration

1.  **Cloner le dépôt**
    ```bash
    git clone https://github.com/votre-utilisateur/WhiteLab.git
    cd WhiteLab
    ```

2.  **Configuration de la Base de Données**
    *   Créez une base de données MySQL vide (par exemple `whitelab_db`).
    *   Importez le script SQL d'initialisation s'il est fourni (généralement dans un dossier `sql` ou `db`), ou laissez l'application générer les tables (si configuré ainsi).
    *   Vérifiez le fichier de configuration de la base de données (ex: `src/main/resources/database.properties` ou `application.properties`) et mettez à jour vos identifiants :
        ```properties
        db.url=jdbc:mysql://localhost:3306/whitelab_db
        db.user=root
        db.password=votre_mot_de_passe
        ```

3.  **Compilation du Projet**
    Utilisez Maven pour installer les dépendances et compiler le projet :
    ```bash
    mvn clean install
    ```

4.  **Lancement de l'Application**
    Vous pouvez lancer l'application directement depuis votre IDE en exécutant la classe principale :
    `ma.WhiteLab.WhiteLabApp`

    Ou via la ligne de commande après le build :
    ```bash
    java -jar target/WhiteLab-1.0-SNAPSHOT.jar
    ```

## 🏗️ Structure du Code

```
WhiteLab/
└── src/main/java/ma/WhiteLab/
    ├── WhiteLabApp.java   # Point d'entrée de l'application
    ├── conf/               # Configuration (contexte applicatif, DB)
    ├── entities/           # Modèle de données (POJOs/Entités)
    ├── mvc/                # Contrôleurs et Vues (Swing)
    ├── repository/         # Accès aux données (DAOs)
    ├── service/            # Logique métier
    ├── common/             # Utilitaires partagés
    └── security/           # Authentification et sécurité
```

## 👥 Auteurs

*   Équipe WhiteLab - EMSI 2026

---
© 2026 WhiteLab. Tous droits réservés.
