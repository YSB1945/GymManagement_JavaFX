package com.gymproject.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Membre {

    private final IntegerProperty  idMembre        = new SimpleIntegerProperty();
    private final StringProperty   nom             = new SimpleStringProperty();
    private final StringProperty   prenom          = new SimpleStringProperty();
    private final StringProperty   email           = new SimpleStringProperty();
    private final StringProperty   telephone       = new SimpleStringProperty();
    private final StringProperty   sexe            = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateNaissance   = new SimpleObjectProperty<>();
    private final StringProperty   sportPratique   = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateInscription = new SimpleObjectProperty<>();
    private final BooleanProperty  actif           = new SimpleBooleanProperty();

    // Constructeur vide
    public Membre() {}

    // Constructeur sans ID (INSERT)
    public Membre(String nom, String prenom, String email, String telephone,
                  String sexe, LocalDate dateNaissance, String sportPratique,
                  LocalDate dateInscription, boolean actif) {
        setNom(nom);
        setPrenom(prenom);
        setEmail(email);
        setTelephone(telephone);
        setSexe(sexe);
        setDateNaissance(dateNaissance);
        setSportPratique(sportPratique);
        setDateInscription(dateInscription);
        setActif(actif);
    }

    // Constructeur complet avec ID (SELECT)
    public Membre(int idMembre, String nom, String prenom, String email,
                  String telephone, String sexe, LocalDate dateNaissance,
                  String sportPratique, LocalDate dateInscription, boolean actif) {
        setIdMembre(idMembre);
        setNom(nom);
        setPrenom(prenom);
        setEmail(email);
        setTelephone(telephone);
        setSexe(sexe);
        setDateNaissance(dateNaissance);
        setSportPratique(sportPratique);
        setDateInscription(dateInscription);
        setActif(actif);
    }

    // --- Properties (JavaFX binding) ---
    public IntegerProperty  idMembreProperty()        { return idMembre; }
    public StringProperty   nomProperty()             { return nom; }
    public StringProperty   prenomProperty()          { return prenom; }
    public StringProperty   emailProperty()           { return email; }
    public StringProperty   telephoneProperty()       { return telephone; }
    public StringProperty   sexeProperty()            { return sexe; }
    public ObjectProperty<LocalDate> dateNaissanceProperty()   { return dateNaissance; }
    public StringProperty   sportPratiqueProperty()   { return sportPratique; }
    public ObjectProperty<LocalDate> dateInscriptionProperty() { return dateInscription; }
    public BooleanProperty  actifProperty()           { return actif; }

    // --- Getters ---
    public int       getIdMembre()        { return idMembre.get(); }
    public String    getNom()             { return nom.get(); }
    public String    getPrenom()          { return prenom.get(); }
    public String    getEmail()           { return email.get(); }
    public String    getTelephone()       { return telephone.get(); }
    public String    getSexe()            { return sexe.get(); }
    public LocalDate getDateNaissance()   { return dateNaissance.get(); }
    public String    getSportPratique()   { return sportPratique.get(); }
    public LocalDate getDateInscription() { return dateInscription.get(); }
    public boolean   isActif()            { return actif.get(); }

    // --- Setters ---
    public void setIdMembre(int v)              { idMembre.set(v); }
    public void setNom(String v)                { nom.set(v); }
    public void setPrenom(String v)             { prenom.set(v); }
    public void setEmail(String v)              { email.set(v); }
    public void setTelephone(String v)          { telephone.set(v); }
    public void setSexe(String v)               { sexe.set(v); }
    public void setDateNaissance(LocalDate v)   { dateNaissance.set(v); }
    public void setSportPratique(String v)      { sportPratique.set(v); }
    public void setDateInscription(LocalDate v) { dateInscription.set(v); }
    public void setActif(boolean v)             { actif.set(v); }

    // --- Utilitaires ---
    public String getNomComplet() { return prenom.get() + " " + nom.get(); }

    @Override
    public String toString() { return getNomComplet(); }
}