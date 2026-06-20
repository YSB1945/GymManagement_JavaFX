package com.gymproject.dao;

import com.gymproject.models.Abonnement;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implémentation concrète de IAbonnementDAO.
 *
 * Points clés :
 *  - listerTous() et obtenirParId() utilisent vue_abonnements_complets
 *    → nomCompletMembre est rempli automatiquement via la jointure SQL
 *  - getStatistiquesParType() utilise vue_stats_type_abonnement
 *  - PreparedStatement systématique pour la sécurité
 *  - Conversion LocalDate ↔ sql.Date centralisée dans des méthodes privées
 */
public class AbonnementDAO implements IAbonnementDAO {

    // =========================================================
    //  Requêtes SQL — centralisées en constantes
    // =========================================================

    // -- CRUD de base (table directe) -------------------------
    private static final String SQL_INSERT =
        "INSERT INTO abonnement " +
        "(id_membre, type_abonnement, date_debut, date_fin, prix, paye, remarques) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE abonnement " +
        "SET id_membre=?, type_abonnement=?, date_debut=?, date_fin=?, " +
        "    prix=?, paye=?, remarques=? " +
        "WHERE id_abonnement=?";

    private static final String SQL_DELETE =
        "DELETE FROM abonnement WHERE id_abonnement=?";

    // -- Lectures via la vue de jointure ----------------------
    private static final String SQL_SELECT_BY_ID =
        "SELECT * FROM vue_abonnements_complets WHERE id_abonnement=?";

    private static final String SQL_SELECT_ALL =
        "SELECT * FROM vue_abonnements_complets ORDER BY date_debut DESC";

    private static final String SQL_SELECT_BY_MEMBRE =
        "SELECT * FROM vue_abonnements_complets " +
        "WHERE id_membre=? ORDER BY date_debut DESC";

    private static final String SQL_SELECT_BY_TYPE =
        "SELECT * FROM vue_abonnements_complets " +
        "WHERE type_abonnement=? ORDER BY date_debut DESC";

    private static final String SQL_SELECT_BY_PAIEMENT =
        "SELECT * FROM vue_abonnements_complets " +
        "WHERE paye=? ORDER BY date_debut DESC";

    // -- Statistiques simples ---------------------------------
    private static final String SQL_COUNT_TOTAL =
        "SELECT COUNT(*) FROM abonnement";

    private static final String SQL_COUNT_PAYES =
        "SELECT COUNT(*) FROM abonnement WHERE paye=1";

    private static final String SQL_CA_TOTAL =
        "SELECT COALESCE(SUM(prix), 0) FROM abonnement";

    private static final String SQL_CA_ENCAISSE =
        "SELECT COALESCE(SUM(prix), 0) FROM abonnement WHERE paye=1";

    private static final String SQL_COUNT_EXPIRANT =
        "SELECT COUNT(*) FROM abonnement " +
        "WHERE date_fin >= CURRENT_DATE " +
        "AND date_fin < DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY)";

    // -- Statistiques par type (via vue dédiée) ---------------
    private static final String SQL_STATS_PAR_TYPE =
        "SELECT type_abonnement, nombre, chiffre_affaires, nombre_payes " +
        "FROM vue_stats_type_abonnement";

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
    public boolean ajouter(Abonnement abonnement) {
        PreparedStatement stmt = null;
        try {
            stmt = getConn().prepareStatement(SQL_INSERT);

            stmt.setInt    (1, abonnement.getIdMembre());
            stmt.setString (2, abonnement.getTypeAbonnement());
            stmt.setDate   (3, toSqlDate(abonnement.getDateDebut()));
            stmt.setDate   (4, toSqlDate(abonnement.getDateFin()));
            stmt.setDouble (5, abonnement.getPrix());
            stmt.setBoolean(6, abonnement.isPaye());

            // remarques est nullable (TextArea peut être vide)
            if (abonnement.getRemarques() == null || abonnement.getRemarques().isBlank()) {
                stmt.setNull(7, Types.VARCHAR);
            } else {
                stmt.setString(7, abonnement.getRemarques());
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[AbonnementDAO] ❌ Erreur ajouter() : " + e.getMessage());
            return false;
        } finally {
            fermerStatement(stmt);
        }
    }

    // =========================================================
    //  MODIFIER
    // =========================================================
    @Override
    public boolean modifier(Abonnement abonnement) {
        PreparedStatement stmt = null;
        try {
            stmt = getConn().prepareStatement(SQL_UPDATE);

            stmt.setInt    (1, abonnement.getIdMembre());
            stmt.setString (2, abonnement.getTypeAbonnement());
            stmt.setDate   (3, toSqlDate(abonnement.getDateDebut()));
            stmt.setDate   (4, toSqlDate(abonnement.getDateFin()));
            stmt.setDouble (5, abonnement.getPrix());
            stmt.setBoolean(6, abonnement.isPaye());

            if (abonnement.getRemarques() == null || abonnement.getRemarques().isBlank()) {
                stmt.setNull(7, Types.VARCHAR);
            } else {
                stmt.setString(7, abonnement.getRemarques());
            }

            stmt.setInt(8, abonnement.getIdAbonnement()); // clause WHERE

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[AbonnementDAO] ❌ Erreur modifier() : " + e.getMessage());
            return false;
        } finally {
            fermerStatement(stmt);
        }
    }

    // =========================================================
    //  SUPPRIMER
    // =========================================================
    @Override
    public boolean supprimer(int idAbonnement) {
        PreparedStatement stmt = null;
        try {
            stmt = getConn().prepareStatement(SQL_DELETE);
            stmt.setInt(1, idAbonnement);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[AbonnementDAO] ❌ Erreur supprimer() : " + e.getMessage());
            return false;
        } finally {
            fermerStatement(stmt);
        }
    }

    // =========================================================
    //  OBTENIR PAR ID  — via vue_abonnements_complets
    // =========================================================
    @Override
    public Abonnement obtenirParId(int idAbonnement) {
        PreparedStatement stmt = null;
        ResultSet         rs   = null;
        try {
            stmt = getConn().prepareStatement(SQL_SELECT_BY_ID);
            stmt.setInt(1, idAbonnement);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return construireAbonnement(rs);
            }
            return null;

        } catch (SQLException e) {
            System.err.println("[AbonnementDAO] ❌ Erreur obtenirParId() : " + e.getMessage());
            return null;
        } finally {
            fermerResultSetEtStatement(rs, stmt);
        }
    }

    // =========================================================
    //  LISTER TOUS  — via vue_abonnements_complets
    // =========================================================
    @Override
    public List<Abonnement> listerTous() {
        List<Abonnement>  liste = new ArrayList<>();
        PreparedStatement stmt  = null;
        ResultSet         rs    = null;
        try {
            stmt = getConn().prepareStatement(SQL_SELECT_ALL);
            rs   = stmt.executeQuery();

            while (rs.next()) {
                liste.add(construireAbonnement(rs));
            }

        } catch (SQLException e) {
            System.err.println("[AbonnementDAO] ❌ Erreur listerTous() : " + e.getMessage());
        } finally {
            fermerResultSetEtStatement(rs, stmt);
        }
        return liste;
    }

    // =========================================================
    //  LISTER PAR MEMBRE
    // =========================================================
    @Override
    public List<Abonnement> listerParMembre(int idMembre) {
        List<Abonnement>  liste = new ArrayList<>();
        PreparedStatement stmt  = null;
        ResultSet         rs    = null;
        try {
            stmt = getConn().prepareStatement(SQL_SELECT_BY_MEMBRE);
            stmt.setInt(1, idMembre);
            rs = stmt.executeQuery();

            while (rs.next()) {
                liste.add(construireAbonnement(rs));
            }

        } catch (SQLException e) {
            System.err.println("[AbonnementDAO] ❌ Erreur listerParMembre() : " + e.getMessage());
        } finally {
            fermerResultSetEtStatement(rs, stmt);
        }
        return liste;
    }

    // =========================================================
    //  LISTER PAR TYPE
    // =========================================================
    @Override
    public List<Abonnement> listerParType(String typeAbonnement) {
        List<Abonnement>  liste = new ArrayList<>();
        PreparedStatement stmt  = null;
        ResultSet         rs    = null;
        try {
            stmt = getConn().prepareStatement(SQL_SELECT_BY_TYPE);
            stmt.setString(1, typeAbonnement);
            rs = stmt.executeQuery();

            while (rs.next()) {
                liste.add(construireAbonnement(rs));
            }

        } catch (SQLException e) {
            System.err.println("[AbonnementDAO] ❌ Erreur listerParType() : " + e.getMessage());
        } finally {
            fermerResultSetEtStatement(rs, stmt);
        }
        return liste;
    }

    // =========================================================
    //  LISTER PAR STATUT DE PAIEMENT
    // =========================================================
    @Override
    public List<Abonnement> listerParStatutPaiement(boolean paye) {
        List<Abonnement>  liste = new ArrayList<>();
        PreparedStatement stmt  = null;
        ResultSet         rs    = null;
        try {
            stmt = getConn().prepareStatement(SQL_SELECT_BY_PAIEMENT);
            stmt.setBoolean(1, paye);
            rs = stmt.executeQuery();

            while (rs.next()) {
                liste.add(construireAbonnement(rs));
            }

        } catch (SQLException e) {
            System.err.println("[AbonnementDAO] ❌ Erreur listerParStatutPaiement() : " + e.getMessage());
        } finally {
            fermerResultSetEtStatement(rs, stmt);
        }
        return liste;
    }

    // =========================================================
    //  STATISTIQUES
    // =========================================================

    @Override
    public int compterTotal() {
        return executerCompte(SQL_COUNT_TOTAL);
    }

    @Override
    public int compterPayes() {
        return executerCompte(SQL_COUNT_PAYES);
    }

    @Override
    public int compterExpirantBientot() {
        return executerCompte(SQL_COUNT_EXPIRANT);
    }

    @Override
    public double getChiffreAffairesTotal() {
        return executerSomme(SQL_CA_TOTAL);
    }

    @Override
    public double getChiffreAffairesEncaisse() {
        return executerSomme(SQL_CA_ENCAISSE);
    }

    /**
     * Utilise vue_stats_type_abonnement pour retourner la répartition complète.
     *
     * Structure de la Map retournée :
     * {
     *   "Mensuel"      → [3.0,  450.0, 2.0],
     *   "Trimestriel"  → [2.0,  800.0, 1.0],
     *   "Annuel"       → [1.0, 1200.0, 1.0]
     * }
     * Index du tableau : [0] = nombre, [1] = chiffre_affaires, [2] = nombre_payes
     */
    @Override
    public Map<String, double[]> getStatistiquesParType() {
        Map<String, double[]> stats = new HashMap<>();
        PreparedStatement     stmt  = null;
        ResultSet             rs    = null;
        try {
            stmt = getConn().prepareStatement(SQL_STATS_PAR_TYPE);
            rs   = stmt.executeQuery();

            while (rs.next()) {
                String   type        = rs.getString("type_abonnement");
                double   nombre      = rs.getDouble("nombre");
                double   ca          = rs.getDouble("chiffre_affaires");
                double   nombrePayes = rs.getDouble("nombre_payes");

                stats.put(type, new double[]{ nombre, ca, nombrePayes });
            }

        } catch (SQLException e) {
            System.err.println("[AbonnementDAO] ❌ Erreur getStatistiquesParType() : " + e.getMessage());
        } finally {
            fermerResultSetEtStatement(rs, stmt);
        }
        return stats;
    }

    // =========================================================
    //  MÉTHODES UTILITAIRES PRIVÉES
    // =========================================================

    /**
     * Construit un objet Abonnement complet depuis une ligne de
     * vue_abonnements_complets. Le champ nomCompletMembre est rempli
     * directement depuis la colonne "membre_nom_complet" de la vue.
     */
    private Abonnement construireAbonnement(ResultSet rs) throws SQLException {
        Abonnement a = new Abonnement(
            rs.getInt    ("id_abonnement"),
            rs.getInt    ("id_membre"),
            rs.getString ("type_abonnement"),
            toLocalDate  (rs.getDate("date_debut")),
            toLocalDate  (rs.getDate("date_fin")),
            rs.getDouble ("prix"),
            rs.getBoolean("paye"),
            rs.getString ("remarques")
        );
        // Champ dénormalisé — rempli grâce à la jointure de la vue SQL
        a.setNomCompletMembre(rs.getString("membre_nom_complet"));
        return a;
    }

    /**
     * Exécute une requête COUNT et retourne le résultat entier.
     * Factorise la logique répétée de compterTotal(), compterPayes(), etc.
     */
    private int executerCompte(String sql) {
        PreparedStatement stmt = null;
        ResultSet         rs   = null;
        try {
            stmt = getConn().prepareStatement(sql);
            rs   = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[AbonnementDAO] ❌ Erreur compte : " + e.getMessage());
        } finally {
            fermerResultSetEtStatement(rs, stmt);
        }
        return 0;
    }

    /**
     * Exécute une requête SUM et retourne le résultat décimal.
     * Factorise la logique de getChiffreAffairesTotal() et getChiffreAffairesEncaisse().
     */
    private double executerSomme(String sql) {
        PreparedStatement stmt = null;
        ResultSet         rs   = null;
        try {
            stmt = getConn().prepareStatement(sql);
            rs   = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("[AbonnementDAO] ❌ Erreur somme : " + e.getMessage());
        } finally {
            fermerResultSetEtStatement(rs, stmt);
        }
        return 0.0;
    }

    private java.sql.Date toSqlDate(LocalDate localDate) {
        return (localDate != null) ? java.sql.Date.valueOf(localDate) : null;
    }

    private LocalDate toLocalDate(java.sql.Date sqlDate) {
        return (sqlDate != null) ? sqlDate.toLocalDate() : null;
    }

    private void fermerStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try { stmt.close(); }
            catch (SQLException e) {
                System.err.println("[AbonnementDAO] ⚠️ Fermeture statement : " + e.getMessage());
            }
        }
    }

    private void fermerResultSetEtStatement(ResultSet rs, PreparedStatement stmt) {
        if (rs != null) {
            try { rs.close(); }
            catch (SQLException e) {
                System.err.println("[AbonnementDAO] ⚠️ Fermeture ResultSet : " + e.getMessage());
            }
        }
        fermerStatement(stmt);
    }
}