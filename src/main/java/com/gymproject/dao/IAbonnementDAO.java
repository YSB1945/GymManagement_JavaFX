package com.gymproject.dao;

import com.gymproject.models.Abonnement;
import java.util.List;
import java.util.Map;

/**
 * Contrat DAO pour l'entité Abonnement.
 * Inclut les méthodes CRUD standard + les requêtes statistiques
 * exigées par le tableau de bord du mini-projet.
 */
public interface IAbonnementDAO {

    // =========================================================
    //  CRUD de base
    // =========================================================

    /**
     * Insère un nouvel abonnement lié à un membre existant.
     * @param abonnement Objet sans ID (généré par AUTO_INCREMENT).
     * @return true si l'insertion a réussi.
     */
    boolean ajouter(Abonnement abonnement);

    /**
     * Met à jour un abonnement existant.
     * @param abonnement Objet avec ID existant et nouvelles valeurs.
     * @return true si la mise à jour a affecté au moins une ligne.
     */
    boolean modifier(Abonnement abonnement);

    /**
     * Supprime un abonnement par son identifiant.
     * @param idAbonnement Identifiant de l'abonnement à supprimer.
     * @return true si la suppression a réussi.
     */
    boolean supprimer(int idAbonnement);

    /**
     * Récupère un abonnement par son ID via la vue de jointure.
     * Le champ nomCompletMembre est automatiquement rempli.
     * @param idAbonnement Identifiant à rechercher.
     * @return L'objet Abonnement complet, ou null si introuvable.
     */
    Abonnement obtenirParId(int idAbonnement);

    /**
     * Récupère tous les abonnements via vue_abonnements_complets.
     * Chaque objet retourné a son nomCompletMembre rempli — prêt pour le TableView.
     * @return Liste complète triée par date de début décroissante.
     */
    List<Abonnement> listerTous();

    /**
     * Récupère tous les abonnements d'un membre spécifique.
     * @param idMembre Identifiant du membre.
     * @return Liste des abonnements du membre.
     */
    List<Abonnement> listerParMembre(int idMembre);

    /**
     * Filtre les abonnements par type.
     * Utilisée par le ComboBox de filtrage du TableView.
     * @param typeAbonnement "Mensuel", "Trimestriel" ou "Annuel".
     * @return Liste filtrée avec nomCompletMembre rempli.
     */
    List<Abonnement> listerParType(String typeAbonnement);

    /**
     * Filtre les abonnements par statut de paiement.
     * @param paye true = payés, false = impayés.
     * @return Liste filtrée.
     */
    List<Abonnement> listerParStatutPaiement(boolean paye);

    // =========================================================
    //  Méthodes statistiques — Tableau de bord
    // =========================================================

    /**
     * Compte le nombre total d'abonnements.
     * @return Nombre total d'abonnements enregistrés.
     */
    int compterTotal();

    /**
     * Compte le nombre d'abonnements payés.
     * @return Nombre d'abonnements dont paye = true.
     */
    int compterPayes();

    /**
     * Calcule le chiffre d'affaires total (somme de tous les prix).
     * @return Montant total en dirhams.
     */
    double getChiffreAffairesTotal();

    /**
     * Calcule le chiffre d'affaires des abonnements payés uniquement.
     * @return Montant encaissé réel.
     */
    double getChiffreAffairesEncaisse();

    /**
     * Retourne la répartition des abonnements par type via vue_stats_type_abonnement.
     * Structure retournée : clé = type ("Mensuel", etc.),
     * valeur = tableau [nombre, chiffre_affaires, nombre_payes].
     * Utilisée pour alimenter les graphiques et indicateurs du tableau de bord.
     * @return Map<typeAbonnement, double[]{ nombre, ca, nombrePayes }>.
     */
    Map<String, double[]> getStatistiquesParType();

    /**
     * Compte les abonnements dont la date de fin est dans moins de 30 jours.
     * Utilisée pour l'indicateur d'alerte "Expirent bientôt".
     * @return Nombre d'abonnements expirant prochainement.
     */
    int compterExpirantBientot();
}