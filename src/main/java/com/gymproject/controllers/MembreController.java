package com.gymproject.controllers;

import com.gymproject.dao.IMembreDAO;
import com.gymproject.dao.MembreDAO;
import com.gymproject.models.Membre;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur de la vue Gestion des Membres.
 * Connecte membre.fxml ↔ IMembreDAO pour un CRUD complet.
 */
public class MembreController implements Initializable {

    // =========================================================
    //  Injections FXML — Formulaire
    // =========================================================
    @FXML private TextField   nomField;
    @FXML private TextField   prenomField;
    @FXML private TextField   emailField;
    @FXML private TextField   telephoneField;
    @FXML private RadioButton radioMasculin;
    @FXML private RadioButton radioFeminin;
    @FXML private DatePicker  dateNaissancePicker;
    @FXML private TextField   sportPratiqueField;
    @FXML private DatePicker  dateInscriptionPicker;
    @FXML private CheckBox    actifCheckBox;

    // =========================================================
    //  Injections FXML — Boutons
    // =========================================================
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnVider;

    // =========================================================
    //  Injections FXML — TableView et colonnes
    // =========================================================
    @FXML private TableView<Membre>            membresTable;
    @FXML private TableColumn<Membre, Integer> colId;
    @FXML private TableColumn<Membre, String>  colNom;
    @FXML private TableColumn<Membre, String>  colPrenom;
    @FXML private TableColumn<Membre, String>  colEmail;
    @FXML private TableColumn<Membre, String>  colTelephone;
    @FXML private TableColumn<Membre, String>  colSexe;
    @FXML private TableColumn<Membre, String>  colDateNaiss;
    @FXML private TableColumn<Membre, String>  colSport;
    @FXML private TableColumn<Membre, String>  colDateInscr;
    @FXML private TableColumn<Membre, Boolean> colActif;

    // =========================================================
    //  Injections FXML — Labels et recherche
    // =========================================================
    @FXML private TextField searchField;
    @FXML private Label     labelIdSelectionne;
    @FXML private Label     labelCompteur;

    // =========================================================
    //  État interne
    // =========================================================
    private final IMembreDAO            membreDAO  = new MembreDAO();
    private final ObservableList<Membre> listeMembres = FXCollections.observableArrayList();
    private final ToggleGroup            sexeGroup    = new ToggleGroup();
    private final DateTimeFormatter      dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** ID du membre actuellement sélectionné dans le tableau (-1 = aucun) */
    private int idMembreSelectionne = -1;

    // =========================================================
    //  INITIALISATION
    // =========================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerToggleGroup();
        configurerDatePickers();
        configurerColonnes();
        configurerSelectionTableau();
        chargerTousMembres();
        System.out.println("[MembreController] ✅ Vue Membres initialisée.");
    }

    // =========================================================
    //  Configuration du ToggleGroup Sexe
    // =========================================================
    private void configurerToggleGroup() {
        radioMasculin.setToggleGroup(sexeGroup);
        radioFeminin.setToggleGroup(sexeGroup);
        radioMasculin.setSelected(true); // sélection par défaut
    }

    // =========================================================
    //  Configuration des DatePickers (format fr)
    // =========================================================
    private void configurerDatePickers() {
        StringConverter<LocalDate> converter = new StringConverter<>() {
            @Override public String toString(LocalDate date) {
                return (date != null) ? dateFormatter.format(date) : "";
            }
            @Override public LocalDate fromString(String s) {
                return (s != null && !s.isBlank())
                       ? LocalDate.parse(s, dateFormatter) : null;
            }
        };
        dateNaissancePicker.setConverter(converter);
        dateInscriptionPicker.setConverter(converter);
        dateInscriptionPicker.setValue(LocalDate.now()); // date du jour par défaut
    }

    // =========================================================
    //  Configuration des colonnes du TableView
    // =========================================================
    private void configurerColonnes() {

        colId.setCellValueFactory(new PropertyValueFactory<>("idMembre"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colSexe.setCellValueFactory(new PropertyValueFactory<>("sexe"));
        colSport.setCellValueFactory(new PropertyValueFactory<>("sportPratique"));

        // Dates formatées en dd/MM/yyyy
        colDateNaiss.setCellValueFactory(cellData ->
            new SimpleStringProperty(
                cellData.getValue().getDateNaissance() != null
                ? cellData.getValue().getDateNaissance().format(dateFormatter) : ""
            )
        );
        colDateInscr.setCellValueFactory(cellData ->
            new SimpleStringProperty(
                cellData.getValue().getDateInscription() != null
                ? cellData.getValue().getDateInscription().format(dateFormatter) : ""
            )
        );

        // Colonne Actif avec CheckBox visuel (lecture seule dans le tableau)
        colActif.setCellValueFactory(cellData ->
            new SimpleBooleanProperty(cellData.getValue().isActif())
        );
        colActif.setCellFactory(CheckBoxTableCell.forTableColumn(colActif));

        membresTable.setItems(listeMembres);
    }

    // =========================================================
    //  Listener de sélection dans le tableau
    //  Remplit le formulaire quand on clique sur une ligne
    // =========================================================
    private void configurerSelectionTableau() {
        membresTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, ancien, selectionne) -> {
                if (selectionne != null) {
                    remplirFormulaire(selectionne);
                    idMembreSelectionne = selectionne.getIdMembre();
                    labelIdSelectionne.setText(String.valueOf(idMembreSelectionne));
                    btnModifier.setDisable(false);
                    btnSupprimer.setDisable(false);
                } else {
                    viderFormulaire();
                }
            });
    }

    // =========================================================
    //  CHARGEMENT DES DONNÉES
    // =========================================================
    private void chargerTousMembres() {
        listeMembres.clear();
        List<Membre> membres = membreDAO.listerTous();
        listeMembres.addAll(membres);
        mettreAJourCompteur();
    }

    private void mettreAJourCompteur() {
        int total = listeMembres.size();
        labelCompteur.setText("Total : " + total + " membre(s)");
    }

    // =========================================================
    //  HANDLER — Ajouter
    // =========================================================
    @FXML
    private void handleAjouter() {
        if (!validerFormulaire()) return;

        Membre nouveau = new Membre(
            nomField.getText().trim(),
            prenomField.getText().trim(),
            emailField.getText().trim(),
            telephoneField.getText().trim(),
            getSexeSelectionne(),
            dateNaissancePicker.getValue(),
            sportPratiqueField.getText().trim(),
            dateInscriptionPicker.getValue(),
            actifCheckBox.isSelected()
        );

        boolean succes = membreDAO.ajouter(nouveau);

        if (succes) {
            afficherInfo("✅ Succès", "Membre ajouté avec succès !");
            chargerTousMembres();
            viderFormulaire();
        } else {
            afficherErreur("❌ Erreur", "Impossible d'ajouter le membre.\n" +
                           "Vérifiez que l'email n'est pas déjà utilisé.");
        }
    }

    // =========================================================
    //  HANDLER — Modifier
    // =========================================================
    @FXML
    private void handleModifier() {
        if (idMembreSelectionne == -1) {
            afficherErreur("Aucune sélection", "Sélectionnez un membre dans le tableau.");
            return;
        }
        if (!validerFormulaire()) return;

        Membre modifie = new Membre(
            idMembreSelectionne,
            nomField.getText().trim(),
            prenomField.getText().trim(),
            emailField.getText().trim(),
            telephoneField.getText().trim(),
            getSexeSelectionne(),
            dateNaissancePicker.getValue(),
            sportPratiqueField.getText().trim(),
            dateInscriptionPicker.getValue(),
            actifCheckBox.isSelected()
        );

        boolean succes = membreDAO.modifier(modifie);

        if (succes) {
            afficherInfo("✅ Succès", "Membre modifié avec succès !");
            chargerTousMembres();
            viderFormulaire();
        } else {
            afficherErreur("❌ Erreur", "Impossible de modifier le membre.");
        }
    }

    // =========================================================
    //  HANDLER — Supprimer
    // =========================================================
    @FXML
    private void handleSupprimer() {
        if (idMembreSelectionne == -1) {
            afficherErreur("Aucune sélection", "Sélectionnez un membre à supprimer.");
            return;
        }

        Membre selectionne = membresTable.getSelectionModel().getSelectedItem();
        String nomComplet  = selectionne != null ? selectionne.getNomComplet() : "";

        // Confirmation avant suppression
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer " + nomComplet + " ?");
        confirmation.setContentText(
            "⚠️  Cette action supprimera également tous les abonnements liés.\n" +
            "Cette action est irréversible."
        );

        Optional<ButtonType> reponse = confirmation.showAndWait();
        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            boolean succes = membreDAO.supprimer(idMembreSelectionne);
            if (succes) {
                afficherInfo("✅ Supprimé", nomComplet + " a été supprimé avec succès.");
                chargerTousMembres();
                viderFormulaire();
            } else {
                afficherErreur("❌ Erreur", "Impossible de supprimer ce membre.");
            }
        }
    }

    // =========================================================
    //  HANDLER — Vider le formulaire
    // =========================================================
    @FXML
    private void handleVider() {
        viderFormulaire();
        membresTable.getSelectionModel().clearSelection();
    }

    // =========================================================
    //  HANDLER — Recherche en temps réel
    // =========================================================
    @FXML
    private void handleRecherche() {
        String terme = searchField.getText().trim();
        if (terme.isEmpty()) {
            chargerTousMembres();
        } else {
            listeMembres.clear();
            listeMembres.addAll(membreDAO.rechercher(terme));
            mettreAJourCompteur();
        }
    }

    // =========================================================
    //  HANDLER — Vider la recherche
    // =========================================================
    @FXML
    private void handleViderRecherche() {
        searchField.clear();
        chargerTousMembres();
    }

    // =========================================================
    //  HANDLER — Actualiser
    // =========================================================
    @FXML
    private void handleActualiser() {
        chargerTousMembres();
        afficherInfo("🔃 Actualisé", "La liste des membres a été rechargée.");
    }

    // =========================================================
    //  MÉTHODES UTILITAIRES PRIVÉES
    // =========================================================

    /** Remplit le formulaire avec les données d'un membre sélectionné. */
    private void remplirFormulaire(Membre m) {
        nomField.setText(m.getNom());
        prenomField.setText(m.getPrenom());
        emailField.setText(m.getEmail());
        telephoneField.setText(m.getTelephone() != null ? m.getTelephone() : "");
        sportPratiqueField.setText(m.getSportPratique());
        dateNaissancePicker.setValue(m.getDateNaissance());
        dateInscriptionPicker.setValue(m.getDateInscription());
        actifCheckBox.setSelected(m.isActif());

        if ("F".equals(m.getSexe())) {
            radioFeminin.setSelected(true);
        } else {
            radioMasculin.setSelected(true);
        }
    }

    /** Réinitialise tous les champs du formulaire. */
    private void viderFormulaire() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        telephoneField.clear();
        sportPratiqueField.clear();
        dateNaissancePicker.setValue(null);
        dateInscriptionPicker.setValue(LocalDate.now());
        actifCheckBox.setSelected(true);
        radioMasculin.setSelected(true);
        idMembreSelectionne = -1;
        labelIdSelectionne.setText("—");
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }

    /** Retourne "M" ou "F" selon le RadioButton sélectionné. */
    private String getSexeSelectionne() {
        return radioFeminin.isSelected() ? "F" : "M";
    }

    /**
     * Valide les champs obligatoires du formulaire.
     * @return true si tous les champs requis sont remplis.
     */
    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (nomField.getText().trim().isEmpty())
            erreurs.append("• Le nom est obligatoire.\n");
        if (prenomField.getText().trim().isEmpty())
            erreurs.append("• Le prénom est obligatoire.\n");
        if (emailField.getText().trim().isEmpty())
            erreurs.append("• L'email est obligatoire.\n");
        if (dateNaissancePicker.getValue() == null)
            erreurs.append("• La date de naissance est obligatoire.\n");
        if (sportPratiqueField.getText().trim().isEmpty())
            erreurs.append("• Le sport pratiqué est obligatoire.\n");
        if (dateInscriptionPicker.getValue() == null)
            erreurs.append("• La date d'inscription est obligatoire.\n");

        if (erreurs.length() > 0) {
            afficherErreur("Champs incomplets", erreurs.toString());
            return false;
        }
        return true;
    }

    /** Affiche une Alert de type INFORMATION. */
    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Affiche une Alert de type ERROR. */
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}