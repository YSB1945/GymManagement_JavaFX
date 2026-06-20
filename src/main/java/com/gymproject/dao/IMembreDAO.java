package com.gymproject.dao;

import com.gymproject.models.Membre;
import java.util.List;

/**
 * Contrat DAO pour l'entité Membre.
 * Les contrôleurs de Kawtar dépendent uniquement de cette interface,
 * jamais de l'implémentation concrète — principe d'inversion de dépendance.
 */
public interface IMembreDAO {

    /**
     * Insère un nouveau membre en base de données.
     * @param membre Objet Membre sans ID (l'ID est généré par AUTO_INCREMENT).
     * @return true si l'insertion a réussi, false sinon.
     */
    boolean ajouter(Membre membre);

    /**
     * Met à jour un membre existant.
     * @param membre Objet Membre avec ID existant et nouvelles valeurs.
     * @return true si la mise à jour a affecté au moins une ligne.
     */
    boolean modifier(Membre membre);

    /**
     * Supprime un membre par son identifiant.
     * La suppression en cascade (définie en SQL) supprimera
     * également tous ses abonnements liés.
     * @param idMembre Identifiant du membre à supprimer.
     * @return true si la suppression a réussi.
     */
    boolean supprimer(int idMembre);

    /**
     * Récupère un membre unique par son ID.
     * @param idMembre Identifiant à rechercher.
     * @return L'objet Membre correspondant, ou null si introuvable.
     */
    Membre obtenirParId(int idMembre);

    /**
     * Récupère tous les membres de la base de données.
     * @return Liste complète des membres (vide si aucun résultat).
     */
    List<Membre> listerTous();

    /**
     * Recherche des membres dont le nom, prénom ou sport contient le terme.
     * Utilisée par la barre de recherche du TableView de Kawtar.
     * @param terme Chaîne de recherche (insensible à la casse).
     * @return Liste filtrée des membres correspondants.
     */
    List<Membre> rechercher(String terme);

    /**
     * Récupère uniquement les membres actifs.
     * Utilisée pour peupler le ComboBox de sélection dans le formulaire Abonnement.
     * @return Liste des membres dont le champ actif = true.
     */
    List<Membre> listerMembresActifs();

    /**
     * Compte le nombre total de membres inscrits.
     * Utilisée pour les statistiques du tableau de bord.
     * @return Nombre total de membres.
     */
    int compterTotal();
    
}