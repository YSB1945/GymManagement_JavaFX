package com.gymproject.models;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Modèle représentant un abonnement au club sportif.
 * Lié à un Membre via idMembre (clé étrangère).
 * Contient également nomCompletMembre pour les affichages TableView (jointure).
 */
public class Abonnement {

    // =========================================================
    //  Champs — JavaFX Properties
    // =========================================================
    private final IntegerProperty  idAbonnement    = new SimpleIntegerProperty();
    private final IntegerProperty  idMembre        = new SimpleIntegerProperty();   // FK
    private final StringProperty   typeAbonnement  = new SimpleStringProperty();    // ComboBox
    private final ObjectProperty<LocalDate> dateDebut = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateFin   = new SimpleObjectProperty<>();
    private final DoubleProperty   prix            = new SimpleDoubleProperty();    // Spinner
    private final BooleanProperty  paye            = new SimpleBooleanProperty();   // CheckBox
    private final StringProperty   remarques       = new SimpleStringProperty();    // TextArea

    /**
     * Champ dénormalisé — NON mappé à une colonne directe.
     * Rempli par le DAO lors d'un SELECT avec jointure (vue_abonnements_complets).
     * sans que le contrôleur ait besoin de faire une deuxième requête.
     */
    private final StringProperty nomCompletMembre = new SimpleStringProperty();

    // =========================================================
    //  Constructeur vide
    // =========================================================
    public Abonnement() {}

    // =========================================================
    //  Constructeur sans ID  (INSERT — nouvel enregistrement)
    // =========================================================
    public Abonnement(int idMembre, String typeAbonnement, LocalDate dateDebut,
                      LocalDate dateFin, double prix, boolean paye, String remarques) {
        setIdMembre(idMembre);
        setTypeAbonnement(typeAbonnement);
        setDateDebut(dateDebut);
        setDateFin(dateFin);
        setPrix(prix);
        setPaye(paye);
        setRemarques(remarques);
    }

    // =========================================================
    //  Constructeur complet avec ID  (SELECT — lecture BDD)
    // =========================================================
    public Abonnement(int idAbonnement, int idMembre, String typeAbonnement,
                      LocalDate dateDebut, LocalDate dateFin, double prix,
                      boolean paye, String remarques) {
        setIdAbonnement(idAbonnement);
        setIdMembre(idMembre);
        setTypeAbonnement(typeAbonnement);
        setDateDebut(dateDebut);
        setDateFin(dateFin);
        setPrix(prix);
        setPaye(paye);
        setRemarques(remarques);
    }

    // =========================================================
    //  Accesseurs Property  (pour le binding JavaFX / TableView)
    // =========================================================
    public IntegerProperty  idAbonnementProperty()   { return idAbonnement; }
    public IntegerProperty  idMembreProperty()        { return idMembre; }
    public StringProperty   typeAbonnementProperty()  { return typeAbonnement; }
    public ObjectProperty<LocalDate> dateDebutProperty() { return dateDebut; }
    public ObjectProperty<LocalDate> dateFinProperty()   { return dateFin; }
    public DoubleProperty   prixProperty()            { return prix; }
    public BooleanProperty  payeProperty()            { return paye; }
    public StringProperty   remarquesProperty()       { return remarques; }
    public StringProperty   nomCompletMembreProperty(){ return nomCompletMembre; }

    // =========================================================
    //  Getters standard  (pour JDBC et la logique métier)
    // =========================================================
    public int       getIdAbonnement()    { return idAbonnement.get(); }
    public int       getIdMembre()        { return idMembre.get(); }
    public String    getTypeAbonnement()  { return typeAbonnement.get(); }
    public LocalDate getDateDebut()       { return dateDebut.get(); }
    public LocalDate getDateFin()         { return dateFin.get(); }
    public double    getPrix()            { return prix.get(); }
    public boolean   isPaye()             { return paye.get(); }
    public String    getRemarques()       { return remarques.get(); }
    public String    getNomCompletMembre(){ return nomCompletMembre.get(); }

    // =========================================================
    //  Setters standard
    // =========================================================
    public void setIdAbonnement(int v)          { idAbonnement.set(v); }
    public void setIdMembre(int v)              { idMembre.set(v); }
    public void setTypeAbonnement(String v)     { typeAbonnement.set(v); }
    public void setDateDebut(LocalDate v)       { dateDebut.set(v); }
    public void setDateFin(LocalDate v)         { dateFin.set(v); }
    public void setPrix(double v)               { prix.set(v); }
    public void setPaye(boolean v)              { paye.set(v); }
    public void setRemarques(String v)          { remarques.set(v); }
    public void setNomCompletMembre(String v)   { nomCompletMembre.set(v); }

    // =========================================================
    //  Méthode utilitaire
    // =========================================================

    /**
     * Calcule le statut de l'abonnement côté Java.
     * Cohérent avec la logique CASE de la vue SQL vue_abonnements_complets.
     */
    public String getStatut() {
        if (dateFin.get() == null) return "Inconnu";
        LocalDate today = LocalDate.now();
        if (dateFin.get().isBefore(today))
            return "Expiré";
        if (dateFin.get().isBefore(today.plusDays(30)))
            return "Expire bientôt";
        return "Valide";
    }

    @Override
    public String toString() {
        return typeAbonnement.get() + " — " + nomCompletMembre.get();
    }
}