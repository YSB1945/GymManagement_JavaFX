package com.gymproject.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe utilitaire de connexion à la base de données MySQL.
 *
 * Pattern : Singleton
 * Garantit qu'une seule instance de connexion JDBC est créée
 * et partagée à travers toute l'application.
 *
 * Package : com.gymproject.dao
 */
public class Database {

    // =========================================================
    //  Constantes de configuration JDBC
    // =========================================================

    /** URL de connexion — modifie le port si ton MySQL n'est pas sur 3306 */
    private static final String URL      = "jdbc:mysql://localhost:3306/club_sportif"
                                         + "?useSSL=false"
                                         + "&serverTimezone=Africa/Casablanca"
                                         + "&allowPublicKeyRetrieval=true"
                                         + "&characterEncoding=UTF-8";

    private static final String USER     = "root";

    /** ⚠️ Remplace cette valeur par ton mot de passe MySQL local */
    private static final String PASSWORD = "root";

    // =========================================================
    //  Instance unique — cœur du pattern Singleton
    // =========================================================
    private static Database    instance   = null;
    private        Connection  connection = null;

    // =========================================================
    //  Constructeur privé
    //  Empêche toute instanciation extérieure (new Database())
    // =========================================================
    private Database() {
        try {
            // Chargement explicite du driver — nécessaire dans certains
            // environnements sans ServiceLoader (ex : modules Java 9+)
            Class.forName("com.mysql.cj.jdbc.Driver");

            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[Database] ✅ Connexion MySQL établie avec succès.");

        } catch (ClassNotFoundException e) {
            System.err.println("[Database] ❌ Driver MySQL introuvable : " + e.getMessage());
            System.err.println("           → Vérifie que mysql-connector-j est bien dans ton pom.xml");
            throw new RuntimeException("Driver JDBC non trouvé.", e);

        } catch (SQLException e) {
            System.err.println("[Database] ❌ Échec de la connexion MySQL : " + e.getMessage());
            System.err.println("           → Vérifie l'URL, le nom d'utilisateur et le mot de passe.");
            throw new RuntimeException("Connexion à la base de données impossible.", e);
        }
    }

    // =========================================================
    //  Point d'accès global — getInstance()
    //  Crée l'instance uniquement si elle n'existe pas encore,
    //  ou si la connexion précédente a été fermée/perdue.
    // =========================================================
    public static Database getInstance() {
        try {
            if (instance == null || instance.connection.isClosed()) {
                instance = new Database();
            }
        } catch (SQLException e) {
            System.err.println("[Database] ⚠️  Vérification de la connexion échouée : " + e.getMessage());
            instance = new Database(); // tentative de reconnexion
        }
        return instance;
    }

    // =========================================================
    //  Récupération de la connexion active
    //  Utilisée par tous les DAO : Database.getInstance().getConnection()
    // =========================================================
    public Connection getConnection() {
        return this.connection;
    }

    // =========================================================
    //  Fermeture propre de la connexion
    //  À appeler à la fermeture de l'application (dans MainApp.java)
    // =========================================================
    public void closeConnection() {
        if (this.connection != null) {
            try {
                if (!this.connection.isClosed()) {
                    this.connection.close();
                    instance = null; // réinitialise le Singleton
                    System.out.println("[Database] 🔒 Connexion MySQL fermée proprement.");
                }
            } catch (SQLException e) {
                System.err.println("[Database] ⚠️  Erreur lors de la fermeture : " + e.getMessage());
            }
        }
    }
}