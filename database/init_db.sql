-- =============================================================
--  init_db.sql
--  Gestion d'un Club Sportif — Membres & Abonnements
--  ENSAO GI3 | Mini-projet JavaFX
-- =============================================================

-- 1. Création et sélection de la base de données
-- -------------------------------------------------------------
DROP DATABASE IF EXISTS club_sportif;
CREATE DATABASE club_sportif
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE club_sportif;

-- =============================================================
--  TABLE : membre
--  Première entité principale
-- =============================================================
CREATE TABLE membre (
    id_membre       INT             NOT NULL AUTO_INCREMENT,
    nom             VARCHAR(50)     NOT NULL,
    prenom          VARCHAR(50)     NOT NULL,
    email           VARCHAR(100)    NOT NULL UNIQUE,
    telephone       VARCHAR(20),
    sexe            ENUM('M', 'F')  NOT NULL,
    date_naissance  DATE            NOT NULL,
    sport_pratique  VARCHAR(50)     NOT NULL,
    date_inscription DATE           NOT NULL DEFAULT (CURRENT_DATE),
    actif           TINYINT(1)      NOT NULL DEFAULT 1,

    CONSTRAINT pk_membre PRIMARY KEY (id_membre)
);

-- =============================================================
--  TABLE : abonnement
--  Deuxième entité — liée à membre via clé étrangère
-- =============================================================
CREATE TABLE abonnement (
    id_abonnement   INT             NOT NULL AUTO_INCREMENT,
    id_membre       INT             NOT NULL,                   -- FK → membre
    type_abonnement ENUM('Mensuel', 'Trimestriel', 'Annuel')
                                    NOT NULL DEFAULT 'Mensuel',
    date_debut      DATE            NOT NULL,
    date_fin        DATE            NOT NULL,
    prix            DECIMAL(8, 2)   NOT NULL,
    paye            TINYINT(1)      NOT NULL DEFAULT 0,         -- CheckBox "Payé"
    remarques       TEXT,                                       -- TextArea

    CONSTRAINT pk_abonnement    PRIMARY KEY (id_abonnement),
    CONSTRAINT fk_abonnement_membre
        FOREIGN KEY (id_membre)
        REFERENCES membre(id_membre)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- =============================================================
--  DONNÉES DE TEST (20 Membres au total)
-- =============================================================
INSERT INTO membre (nom, prenom, email, telephone, sexe, date_naissance, sport_pratique, date_inscription, actif)
VALUES
    ('Alami',    'Youssef',  'youssef.alami@gmail.com',   '0661234567', 'M', '1998-04-15', 'Football',    '2026-01-15', 1),
    ('Benali',   'Kawtar',   'kawtar.benali@gmail.com',   '0662345678', 'F', '2000-11-22', 'Natation',    '2026-02-10', 1),
    ('Chraibi',  'Imane',    'imane.chraibi@gmail.com',   '0663456789', 'F', '1995-07-08', 'Yoga',        '2026-03-01', 1),
    ('Daoudi',   'Mehdi',    'mehdi.daoudi@gmail.com',    '0664567890', 'M', '2001-02-28', 'Basketball',  '2026-03-12', 0),
    ('El Fassi', 'Sara',     'sara.elfassi@gmail.com',    '0665678901', 'F', '1999-09-03', 'Tennis',      '2026-04-05', 1),
    ('Amrani',   'Amine',    'amine.amrani@gmail.com',    '0671987654', 'M', '1997-12-05', 'Football',    '2026-04-20', 1),
    ('Tazi',     'Sami',     'sami.tazi@gmail.com',       '0672876543', 'M', '1996-08-20', 'Natation',    '2026-05-02', 1),
    ('Bennani',  'Reda',     'reda.bennani@gmail.com',    '0673765432', 'M', '2003-01-25', 'Basketball',  '2026-05-15', 1),
    ('Mezouar',  'Sofia',    'sofia.mezouar@gmail.com',   '0674654321', 'F', '1999-10-14', 'Yoga',        '2026-05-28', 1),
    ('Jouahri',  'Hamza',    'hamza.jouahri@gmail.com',   '0675543210', 'M', '1994-03-18', 'Tennis',      '2026-06-01', 0),
    ('El Idrissi','Saad',    'saad.idrissi@gmail.com',    '0651122334', 'M', '2000-06-30', 'Football',    '2026-06-05', 1),
    ('Naciri',   'Ghita',    'ghita.naciri@gmail.com',    '0652233445', 'F', '2001-11-02', 'Natation',    '2026-06-10', 1),
    ('Filali',   'Walid',    'walid.filali@gmail.com',    '0653344556', 'M', '1998-02-14', 'Basketball',  '2026-06-12', 1),
    ('Kadiri',   'Zainab',   'zainab.kadiri@gmail.com',   '0654455667', 'F', '1995-09-22', 'Yoga',        '2026-06-15', 1),
    ('Sadiki',   'Mourad',   'mourad.sadiki@gmail.com',   '0655566778', 'M', '1997-07-07', 'Tennis',      '2026-06-16', 1),
    ('Berrada',  'Lina',     'lina.berrada@gmail.com',    '0611239847', 'F', '2002-04-04', 'Football',    '2026-06-17', 0),
    ('Mansouri', 'Othmane',  'othmane.mansouri@gmail.com','0612847392', 'M', '1999-12-12', 'Natation',    '2026-06-18', 1),
    ('Cherkaoui','Hajar',    'hajar.cherkaoui@gmail.com', '0613958473', 'F', '2000-01-01', 'Basketball',  '2026-06-19', 1),
    ('Belkhayat','Omar',     'omar.belkhayat@gmail.com',  '0614029384', 'M', '1996-06-18', 'Yoga',        '2026-06-19', 1),
    ('Bouras',   'Myriam',   'myriam.bouras@gmail.com',   '0615284019', 'F', '2001-08-11', 'Tennis',      '2026-06-19', 1);

INSERT INTO abonnement (id_membre, type_abonnement, date_debut, date_fin, prix, paye, remarques)
VALUES
    (1,  'Annuel',      '2026-01-15', '2027-01-14', 1200.00, 1, 'Tarif réduit employé'),
    (2,  'Mensuel',     '2026-06-01', '2026-12-31',  150.00, 1, NULL),
    (3,  'Trimestriel', '2026-06-01', '2027-03-31',  400.00, 0, 'Paiement en attente'),
    (4,  'Mensuel',     '2026-03-12', '2026-11-30',  150.00, 1, NULL),
    (5,  'Trimestriel', '2026-04-05', '2026-12-31',  400.00, 1, NULL),
    (6,  'Mensuel',     '2026-04-20', '2026-12-20',  150.00, 1, 'Renouvellement automatique'),
    (7,  'Annuel',      '2026-05-02', '2027-05-01', 1200.00, 1, 'Paiement complet cash'),
    (8,  'Trimestriel', '2026-05-15', '2027-02-14',  400.00, 1, NULL),
    (9,  'Mensuel',     '2026-05-28', '2026-12-28',  150.00, 0, 'Rappel envoyé'),
    (10, 'Annuel',      '2026-06-01', '2027-05-31', 1200.00, 1, NULL),
    (11, 'Mensuel',     '2026-06-05', '2026-12-05',  150.00, 1, NULL),
    (12, 'Trimestriel', '2026-06-10', '2027-03-10',  400.00, 1, NULL),
    (13, 'Mensuel',     '2026-06-12', '2026-12-12',  150.00, 1, NULL),
    (14, 'Trimestriel', '2026-06-15', '2027-03-15',  400.00, 0, 'Chèque en attente'),
    (15, 'Annuel',      '2026-06-16', '2027-06-15', 1200.00, 1, 'Tarif étudiant'),
    (16, 'Mensuel',     '2026-06-17', '2026-12-17',  150.00, 1, NULL),
    (17, 'Trimestriel', '2026-06-18', '2027-03-18',  400.00, 1, NULL),
    (18, 'Mensuel',     '2026-06-19', '2026-12-19',  150.00, 1, NULL),
    (19, 'Annuel',      '2026-06-19', '2027-06-18', 1200.00, 1, NULL),
    (20, 'Mensuel',     '2026-06-19', '2026-12-19',  150.00, 0, 'Nouveau membre');

-- =============================================================
--  VUES UTILITAIRES (pour les requêtes statistiques du DAO)
-- =============================================================

-- Vue principale : tableau récapitulatif avec jointure
CREATE VIEW vue_abonnements_complets AS
SELECT
    a.id_abonnement,
    m.id_membre,
    CONCAT(m.prenom, ' ', m.nom)    AS membre_nom_complet,
    m.email,
    m.sport_pratique,
    a.type_abonnement,
    a.date_debut,
    a.date_fin,
    a.prix,
    a.paye,
    a.remarques,
    m.actif                         AS membre_actif,
    CASE
        WHEN a.date_fin < CURRENT_DATE THEN 'Expiré'
        WHEN a.date_fin < DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY) THEN 'Expire bientôt'
        ELSE 'Valide'
    END                             AS statut_abonnement
FROM abonnement a
INNER JOIN membre m ON a.id_membre = m.id_membre;

-- Vue statistiques : répartition par type
CREATE VIEW vue_stats_type_abonnement AS
SELECT
    type_abonnement,
    COUNT(*)                        AS nombre,
    SUM(prix)                       AS chiffre_affaires,
    SUM(CASE WHEN paye = 1 THEN 1 ELSE 0 END) AS nombre_payes
FROM abonnement
GROUP BY type_abonnement;