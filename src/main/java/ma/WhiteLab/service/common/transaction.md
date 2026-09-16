# Transaction – JDBC Transaction Utility

## 📦 Package
`ma.emsi.service.common`

---

## 📘 Présentation générale

La classe **`Transaction`** est une classe utilitaire destinée à la **gestion centralisée des transactions JDBC**.

Elle permet d’exécuter un bloc de code métier **dans une transaction SQL complète**, tout en assurant automatiquement :

- l’ouverture de la connexion JDBC
- la désactivation de `l’auto-commit
- le commit si tout se passe bien
- le rollback en cas d’erreur
- la fermeture automatique de la connexion

Cette classe est conçue pour **éviter la duplication du code transactionnel**
dans les services et repositories et pour garantir la **cohérence des données**.

---

## 🎯 Raison d’être de la classe

Dans une application Java SE utilisant JDBC (sans Spring / JPA),  
la gestion des transactions est **manuelle**, répétitive et source d’erreurs.

La classe `Transaction` a été créée pour :

- centraliser la gestion transactionnelle
- simplifier le code métier
- réduire les erreurs liées aux commits / rollbacks
- améliorer la lisibilité et la maintenabilité du code

---

## ❌ Approche JDBC classique (sans `Transaction`)

Dans l’approche classique, chaque service doit gérer lui-même la transaction :

```java
Connection c = dataSource.getConnection();
c.setAutoCommit(false);
try {
    // requêtes SQL
    c.commit();
} catch (Exception e) {
    c.rollback();
} finally {
    c.close();
}
``` 

## ❌ Inconvénients de cette approche

- duplication massive du code transactionnel
- risque d’oublier commit() ou rollback()
- code métier pollué par du code technique 
- maintenance difficile 
- forte probabilité d’erreurs transactionnelles 
- code peu lisible pour les étudiants et juniors

## ✅ Approche avec la classe Transaction

- Avec la classe Transaction, toute la logique transactionnelle est centralisée. 
- Le développeur écrit uniquement le code métier.

```java
UtilisateurDto medecin = Transaction.initTransaction(connection -> {

    UtilisateurRepository userRepo = utilisateurRepoFactory.create(connection);
    RoleRepository roleRepo        = roleRepoFactory.create(connection);
    StaffRepository staffRepo      = staffRepoFactory.create(connection);
    MedecinRepository medRepo      = medecinRepoFactory.create(connection);

    ensureUnique(userRepo, medecinDto.login(), medecinDto.email());

    String encoded = passwordEncoder.encode(medecinDto.motDePasse());

    Medecin newMedecin = Medecin.buildFromDto(medecinDto);

    userRepo.create(newMedecin);
    staffRepo.insertStaffFields(newMedecin);
    medRepo.insertMedecinFields(newMedecin);

    Role role = RoleUtils.getRequiredRole(roleRepo, RoleType.MEDECIN);
    roleRepo.assignRoleToUser(newMedecin.getId(), role.getId());

    return buildUserDto(userRepo, roleRepo, newMedecin.getId());
});
```
## ✅ Avantages de cette approche

- aucune duplication du code transactionnel 
- code métier clair, lisible et concis 
- rollback automatique en cas d’erreur 
- gestion centralisée des transactions 
- forte cohérence des données 
- approche proche de `@Transactional (Spring)
- idéale pour projets Java SE / JDBC

## 🧠 Principe de fonctionnement

La classe Transaction repose sur :
- une classe utilitaire (final, constructeur privé)
- une interface fonctionnelle 
- l’utilisation des expressions lambda 
- le pattern Template Method

## 🔁 Interface fonctionnelle TransactionBlocExecuter<T>
```java
@FunctionalInterface
    public interface TransactionBlocExecuter<T> {

        T run(Connection c) throws Exception;
    }
```
## Rôle

- encapsule le code métier transactionnel 
- reçoit une connexion JDBC déjà ouverte 
- retourne un résultat métier 
- toute exception déclenche automatiquement un rollback

## 🚀 Méthode principale : initTransaction

```java
public static <T> T initTransaction(TransactionBlocExecuter<T> blocTransactionnelAExecuter) 
```

## Étapes exécutées

1. ouverture de la connexion JDBC 
2. sauvegarde de l’état `autoCommit`
3. désactivation de `l’auto-commit`
4. exécution du code métier fourni 
5. commit si aucune exception n’est levée 
6. `rollback` en cas d’erreur 
7. restauration de l’état initial 
8. `fermeture automatique` de la connexion

## 🧱 Responsabilités de la classe

La classe Transaction garantit :

- l’atomicité des opérations 
- la cohérence des données 
- la sécurité transactionnelle 
- la gestion correcte des exceptions SQL 
- la fermeture propre des ressources JDBC

## 🧩 Quand utiliser cette classe ?

Utiliser Transaction lorsque :

- plusieurs repositories sont appelés dans un même service 
- plusieurs requêtes JDBC doivent être atomiques 
- une cohérence forte des données est requise

## Exemples concrets

- création utilisateur + rôles 
- création staff + spécialisation 
- suppression avec dépendances 
- opérations financières critiques
