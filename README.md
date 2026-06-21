# 🏋️ Club Sportif Manager

> Application de gestion d'un club sportif développée en **JavaFX + MySQL**  
> Mini-Projet — Module Développement Java IHM  
> ENSAO GI3 — Année 2025/2026

---

## 👥 Réalisé par

| Étudiant | Rôle |
|----------|------|
| **Aymane Amzir** | Frontend (JavaFX / FXML / CSS) & Rapport |
| **Yassine Bouallaq** | Backend (DAO / JDBC / MySQL) & GitHub & Vidéo |

**Encadrante :** Mme Douae EL HILA

---

## 🎬 Vidéo de démonstration

▶️ [Regarder la vidéo sur Google Drive](https://drive.google.com/file/d/13g4Bryl3rrtcUpM1HOJR3l5egM15fgG4/view?usp=drive_link)

---

## 📋 Description du projet

**Club Sportif Manager** est une application de bureau permettant à un gestionnaire de club sportif de :

- Gérer les **membres** du club (ajout, modification, suppression, recherche en temps réel)
- Gérer les **abonnements** (Mensuel / Trimestriel / Annuel) avec calcul automatique de la date de fin et du prix
- Visualiser un **tableau de bord** interactif avec statistiques, chiffre d'affaires, alertes d'expiration et objectif mensuel (Slider + ProgressBar)
- **Exporter** les données en fichier CSV compatible Excel (UTF-8 avec BOM)

### Architecture

```
MVC + DAO + JDBC
├── Modèle   → Membre.java, Abonnement.java (JavaFX Properties)
├── Vue      → Fichiers FXML + CSS
├── Contrôleur → MainController, MembreController, AbonnementController, StatistiquesController
└── DAO      → Database.java (Singleton), IMembreDAO/MembreDAO, IAbonnementDAO/AbonnementDAO
```

---

## ⚙️ Prérequis

Avant de lancer l'application, assurez-vous d'avoir installé :

| Outil | Version minimale | Lien |
|-------|-----------------|------|
| **JDK** | 21 (LTS) | https://adoptium.net |
| **Maven** | 3.9+ | https://maven.apache.org |
| **MySQL Server** | 8.0+ | https://dev.mysql.com/downloads |
| **MySQL Connector/J** | Inclus via Maven | — |

> 💡 **Visual Studio Code** avec l'extension **MySQL** peut être utilisé à la place d'un serveur WAMP/XAMPP pour gérer la base de données directement.

---

## 🗄️ Initialisation de la base de données

### 1. Créer la base de données

Connectez-vous à MySQL et exécutez le script d'initialisation :

```bash
mysql -u root -p < database/init_db.sql
```

Ou ouvrez le fichier `database/init_db.sql` dans votre client MySQL (VS Code, DBeaver, phpMyAdmin…) et exécutez-le.

Ce script crée :
- La base de données `club_sportif`
- Les tables `membre` et `abonnement` (avec contrainte `ON DELETE CASCADE`)
- Les vues SQL `vue_abonnements_complets` et `vue_stats_type_abonnement`
- 20 membres et 20 abonnements de données de test

### 2. Vérifier les paramètres de connexion

Ouvrez le fichier `src/main/java/com/gymproject/dao/Database.java` et vérifiez :

```java
private static final String URL      = "jdbc:mysql://localhost:3306/club_sportif"
                                      + "?useSSL=false&serverTimezone=Africa/Casablanca"
                                      + "&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
private static final String USER     = "root";
private static final String PASSWORD = "root";   // ← Modifier si nécessaire
```

> ⚠️ Modifiez `USER` et `PASSWORD` selon votre configuration MySQL locale.

---

## 🚀 Instructions de lancement

### Option 1 — Via Maven (recommandé)

```bash
# 1. Cloner le dépôt
git clone https://github.com/YSB1945/GymManagement_JavaFX.git
cd GymManagement_JavaFX

# 2. Compiler et lancer
mvn clean javafx:run
```

### Option 2 — Via Visual Studio Code

1. Ouvrir le dossier `GymManagement_JavaFX/` dans VS Code
2. Installer les extensions : **Extension Pack for Java** + **Maven for Java**
3. Clic droit sur `src/main/java/com/gymproject/main/Launcher.java`
4. Sélectionner **Run Java**

### Option 3 — Générer un JAR exécutable

```bash
mvn clean package
java -jar target/GymManagement_JavaFX-1.0.jar
```

---

## 📁 Structure du projet

```
GymManagement_JavaFX/
├── database/
│   └── init_db.sql                    # Script SQL d'initialisation
├── src/main/
│   ├── java/com/gymproject/
│   │   ├── controllers/               # Contrôleurs MVC
│   │   │   ├── MainController.java
│   │   │   ├── MembreController.java
│   │   │   ├── AbonnementController.java
│   │   │   └── StatistiquesController.java
│   │   ├── dao/                       # Couche d'accès aux données
│   │   │   ├── Database.java          # Singleton JDBC
│   │   │   ├── IMembreDAO.java
│   │   │   ├── MembreDAO.java
│   │   │   ├── IAbonnementDAO.java
│   │   │   └── AbonnementDAO.java
│   │   ├── models/                    # Entités métier
│   │   │   ├── Membre.java
│   │   │   └── Abonnement.java
│   │   └── main/
│   │       ├── MainApp.java           # Classe JavaFX principale
│   │       └── Launcher.java          # Point d'entrée
│   └── resources/com/gymproject/
│       ├── fxml/                      # Vues FXML
│       │   ├── main.fxml
│       │   ├── membre.fxml
│       │   ├── abonnement.fxml
│       │   └── statistiques.fxml
│       └── css/
│           └── style.css              # Feuille de style globale
├── pom.xml                            # Configuration Maven
└── README.md
```

---

## 🖥️ Fonctionnalités principales

### 👤 Gestion des Membres
- Ajouter, modifier, supprimer un membre
- Recherche en temps réel (nom, prénom, sport)
- Filtrage par statut actif/inactif

### 📋 Gestion des Abonnements
- Créer un abonnement avec calcul automatique de la date de fin
- Prix par défaut selon le type : Mensuel (150 DH) / Trimestriel (400 DH) / Annuel (1 200 DH)
- Filtrage par type et par statut de paiement
- Coloration dynamique du statut : 🟢 Valide / 🟠 Expire bientôt / 🔴 Expiré

### 📊 Tableau de Bord
- Statistiques en temps réel (CA total, encaissé, en attente)
- Slider objectif mensuel + ProgressBar colorée
- Répartition par type (ProgressBar horizontales)
- Alertes membres à relancer (ListView)

### 📤 Export CSV
- Export complet des abonnements
- Encodage UTF-8 avec BOM (compatible Excel)
- Sélection du fichier via FileChooser natif

---

## 🛠️ Contrôles JavaFX utilisés

`TextField` · `TextArea` · `Button` · `Label` · `RadioButton` · `ToggleGroup` · `CheckBox` · `ComboBox` · `ListView` · `TableView` · `DatePicker` · `Slider` · `Spinner` · `ProgressBar` · `ProgressIndicator` · `Tooltip` · `MenuBar` · `Alert` · `Accordion` · `TitledPane` · `FileChooser`

---

## 📚 Technologies

- **Java 21** — Langage principal
- **JavaFX 21** — Interface graphique
- **Maven 3.9** — Gestion des dépendances
- **MySQL 8.0** — Base de données
- **JDBC** — Connexion Java ↔ MySQL
- **CSS** — Personnalisation de l'interface

---

## 📄 Références

- Documentation JavaFX : https://openjfx.io
- Documentation MySQL : https://dev.mysql.com/doc/connector-j/
- Apache Maven : https://maven.apache.org/guides/

---

*ENSAO — École Nationale des Sciences Appliquées d'Oujda | GI3 | 2025/2026*
