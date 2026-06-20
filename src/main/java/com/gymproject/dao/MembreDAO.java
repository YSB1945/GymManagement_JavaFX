package com.gymproject.dao;

import com.gymproject.models.Membre;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation concrète de IMembreDAO.
 * Toutes les requêtes SQL concernant l'entité Membre sont centralisées ici.
 *
 * Conventions appliquées :
 *  - PreparedStatement systématique  → protection contre les injections SQL
 *  - Conversion LocalDate ↔ sql.Date → cohérence avec le modèle JavaFX
 *  - Fermeture des ressources en bloc finally → pas de fuite mémoire
 */
public class MembreDAO implements IMembreDAO {

    // =========================================================
    //  Requêtes SQL — centralisées en constantes
    //  Avantage : une seule ligne à modifier si le schéma change
    // =========================================================
    private static final String SQL_INSERT =
        "INSERT INTO membre (nom, prenom, email, telephone, sexe, " +
        "date_naissance, sport_pratique, date_inscription, actif) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE membre SET nom=?, prenom=?, email=?, telephone=?, sexe=?, " +
        "date_naissance=?, sport_pratique=?, date_inscription=?, actif=? " +
        "WHERE id_membre=?";

    private static final String SQL_DELETE =
        "DELETE FROM membre WHERE id_membre=?";

    private static final String SQL_SELECT_BY_ID =
        "SELECT * FROM membre WHERE id_membre=?";

    private static final String SQL_SELECT_ALL =
        "SELECT * FROM membre ORDER BY nom, prenom";

    private static final String SQL_SEARCH =
        "SELECT * FROM membre " +
        "WHERE LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(sport_pratique) LIKE ? " +
        "ORDER BY nom, prenom";

    private static final String SQL_ACTIFS =
        "SELECT * FROM membre WHERE actif=1 ORDER BY nom, prenom";

    private static final String SQL_COUNT =
        "SELECT COUNT(*) FROM membre";

    // =========================================================
    //  Raccourci vers la connexion active
    // =========================================================
    private Connection getConn() {
        return Database.getInstance().getConnection();
    }

    // =========================================================
    //  AJOUTER
    // =========================================================
    @Override
    public boolean ajouter(Membre membre) {
        PreparedStatement stmt = null;
        try {
            stmt = getConn().prepareStatement(SQL_INSERT);

            stmt.setString(1, membre.getNom());
            stmt.setString(2, membre.getPrenom());
            stmt.setString(3, membre.getEmail());
            stmt.setString(4, membre.getTelephone());
            stmt.setString(5, membre.getSexe());
            stmt.setDate  (6, toSqlDate(membre.getDateNaissance()));
            stmt.setString(7, membre.getSportPratique());
            stmt.setDate  (8, toSqlDate(membre.getDateInscription()));
            stmt.setBoolean(9, membre.isActif());

            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;

        } catch (SQLException e) {
            System.err.println("[MembreDAO] ❌ Erreur ajouter() : " + e.getMessage());
            return false;
        } finally {
            fermerStatement(stmt);
        }
    }

    // =========================================================
    //  MODIFIER
    // =========================================================
    @Override
    public boolean modifier(Membre membre) {
        PreparedStatement stmt = null;
        try {
            stmt = getConn().prepareStatement(SQL_UPDATE);

            stmt.setString (1,  membre.getNom());
            stmt.setString (2,  membre.getPrenom());
            stmt.setString (3,  membre.getEmail());
            stmt.setString (4,  membre.getTelephone());
            stmt.setString (5,  membre.getSexe());
            stmt.setDate   (6,  toSqlDate(membre.getDateNaissance()));
            stmt.setString (7,  membre.getSportPratique());
            stmt.setDate   (8,  toSqlDate(membre.getDateInscription()));
            stmt.setBoolean(9,  membre.isActif());
            stmt.setInt    (10, membre.getIdMembre());   // clause WHERE

            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;

        } catch (SQLException e) {
            System.err.println("[MembreDAO] ❌ Erreur modifier() : " + e.getMessage());
            return false;
        } finally {
            fermerStatement(stmt);
        }
    }

    // =========================================================
    //  SUPPRIMER
    // =========================================================
    @Override
    public boolean supprimer(int idMembre) {
        PreparedStatement stmt = null;
        try {
            stmt = getConn().prepareStatement(SQL_DELETE);
            stmt.setInt(1, idMembre);

            int lignesAffectees = stmt.executeUpdate();
            return lignesAffectees > 0;

        } catch (SQLException e) {
            System.err.println("[MembreDAO] ❌ Erreur supprimer() : " + e.getMessage());
            return false;
        } finally {
            fermerStatement(stmt);
        }
    }

    // =========================================================
    //  OBTENIR PAR ID
    // =========================================================
    @Override
    public Membre obtenirParId(int idMembre) {
        PreparedStatement stmt = null;
        ResultSet rs   = null;
        try {
            stmt = getConn().prepareStatement(SQL_SELECT_BY_ID);
            stmt.setInt(1, idMembre);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return construireMembre(rs);
            }
            return null; // aucun résultat

        } catch (SQLException e) {
            System.err.println("[MembreDAO] ❌ Erreur obtenirParId() : " + e.getMessage());
            return null;
        } finally {
            fermerResultSetEtStatement(rs, stmt);
        }
    }

    // =========================================================
    //  LISTER TOUS
    // =========================================================
    @Override
    public List<Membre> listerTous() {
        List<Membre>      membres = new ArrayList<>();
        PreparedStatement stmt    = null;
        ResultSet         rs      = null;
        try {
            stmt = getConn().prepareStatement(SQL_SELECT_ALL);
            rs   = stmt.executeQuery();

            while (rs.next()) {
                membres.add(construireMembre(rs));
            }

        } catch (SQLException e) {
            System.err.println("[MembreDAO] ❌ Erreur listerTous() : " + e.getMessage());
        } finally {
            fermerResultSetEtStatement(rs, stmt);
        }
        return membres;
    }

    // =========================================================
    //  RECHERCHER
    // =========================================================
    @Override
    public List<Membre> rechercher(String terme) {
        List<Membre>      resultats = new ArrayList<>();
        PreparedStatement stmt      = null;
        ResultSet         rs        = null;
        try {
            String motif = "%" + terme.toLowerCase() + "%";

            stmt = getConn().prepareStatement(SQL_SEARCH);
            stmt.setString(1, motif); // nom
            stmt.setString(2, motif); // prenom
            stmt.setString(3, motif); // sport_pratique

            rs = stmt.executeQuery();
            while (rs.next()) {
                resultats.add(construireMembre(rs));
            }

        } catch (SQLException e) {
            System.err.println("[MembreDAO] ❌ Erreur rechercher() : " + e.getMessage());
        } finally {
            fermerResultSetEtStatement(rs, stmt);
        }
        return resultats;
    }

    // =========================================================
    //  LISTER MEMBRES ACTIFS
    // =========================================================
    @Override
    public List<Membre> listerMembresActifs() {
        List<Membre>      membres = new ArrayList<>();
        PreparedStatement stmt    = null;
        ResultSet         rs      = null;
        try {
            stmt = getConn().prepareStatement(SQL_ACTIFS);
            rs   = stmt.executeQuery();

            while (rs.next()) {
                membres.add(construireMembre(rs));
            }

        } catch (SQLException e) {
            System.err.println("[MembreDAO] ❌ Erreur listerMembresActifs() : " + e.getMessage());
        } finally {
            fermerResultSetEtStatement(rs, stmt);
        }
        return membres;
    }

    // =========================================================
    //  COMPTER TOTAL
    // =========================================================
    @Override
    public int compterTotal() {
        PreparedStatement stmt = null;
        ResultSet         rs   = null;
        try {
            stmt = getConn().prepareStatement(SQL_COUNT);
            rs   = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("[MembreDAO] ❌ Erreur compterTotal() : " + e.getMessage());
        } finally {
            fermerResultSetEtStatement(rs, stmt);
        }
        return 0;
    }

    // =========================================================
    //  MÉTHODES UTILITAIRES PRIVÉES
    // =========================================================

    /**
     * Construit un objet Membre complet à partir d'une ligne ResultSet.
     * Centralisé ici pour éviter la duplication dans chaque méthode SELECT.
     * C'est le seul endroit où les noms de colonnes SQL sont référencés.
     */
    private Membre construireMembre(ResultSet rs) throws SQLException {
        return new Membre(
            rs.getInt    ("id_membre"),
            rs.getString ("nom"),
            rs.getString ("prenom"),
            rs.getString ("email"),
            rs.getString ("telephone"),
            rs.getString ("sexe"),
            toLocalDate  (rs.getDate("date_naissance")),
            rs.getString ("sport_pratique"),
            toLocalDate  (rs.getDate("date_inscription")),
            rs.getBoolean("actif")
        );
    }

    /**
     * Convertit un java.time.LocalDate en java.sql.Date pour PreparedStatement.
     * Retourne null si la date source est null (champ optionnel).
     */
    private java.sql.Date toSqlDate(LocalDate localDate) {
        return (localDate != null) ? java.sql.Date.valueOf(localDate) : null;
    }

    /**
     * Convertit un java.sql.Date issu du ResultSet en java.time.LocalDate.
     * Retourne null si la colonne SQL était NULL.
     */
    private LocalDate toLocalDate(java.sql.Date sqlDate) {
        return (sqlDate != null) ? sqlDate.toLocalDate() : null;
    }

    /**
     * Ferme un PreparedStatement sans propager d'exception.
     */
    private void fermerStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try { stmt.close(); }
            catch (SQLException e) {
                System.err.println("[MembreDAO] ⚠️ Fermeture statement échouée : " + e.getMessage());
            }
        }
    }

    /**
     * Ferme un ResultSet puis son PreparedStatement dans le bon ordre.
     */
    private void fermerResultSetEtStatement(ResultSet rs, PreparedStatement stmt) {
        if (rs != null) {
            try { rs.close(); }
            catch (SQLException e) {
                System.err.println("[MembreDAO] ⚠️ Fermeture ResultSet échouée : " + e.getMessage());
            }
        }
        fermerStatement(stmt);
    }
}