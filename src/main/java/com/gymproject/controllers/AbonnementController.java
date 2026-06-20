package com.gymproject.controllers;

import com.gymproject.dao.AbonnementDAO;
import com.gymproject.dao.IAbonnementDAO;
import com.gymproject.dao.IMembreDAO;
import com.gymproject.dao.MembreDAO;
import com.gymproject.models.Abonnement;
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
 * Contrôleur de la vue Gestion des Abonnements.
 * Connecte abonnement.fxml ↔ IAbonnementDAO pour un CRUD complet.
 * Exploite la jointure SQL via nomCompletMembre dans le TableView.
 */
public class AbonnementController implements Initializable {

    // =========================================================
    //  Injections FXML — Formulaire
    // =========================================================
    @FXML private ComboBox<Membre>  membreCombo;
    @FXML private ComboBox<String>  typeAbonnementCombo;
    @FXML private DatePicker        dateDebutPicker;
    @FXML private DatePicker        dateFinPicker;
    @FXML private Spinner<Double>   prixSpinner;
    @FXML private CheckBox          payeCheckBox;
    @FXML private TextArea          remarquesArea;

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
    @FXML private TableView<Abonnement>            abonnementsTable;
    @FXML private TableColumn<Abonnement, Integer> colId;
    @FXML private TableColumn<Abonnement, String>  colMembre;
    @FXML private TableColumn<Abonnement, String>  colType;
    @FXML private TableColumn<Abonnement, String>  colDateDebut;
    @FXML private TableColumn<Abonnement, String>  colDateFin;
    @FXML private TableColumn<Abonnement, String>  colPrix;
    @FXML private TableColumn<Abonnement, Boolean> colPaye;
    @FXML private TableColumn<Abonnement, String>  colStatut;
    @FXML private TableColumn<Abonnement, String>  colRemarques;

    // =========================================================
    //  Injections FXML — Filtres et Labels
    // =========================================================
    @FXML private ComboBox<String> filtreTypeCombo;
    @FXML private ComboBox<String> filtreStatutCombo;
    @FXML private Label            labelIdSelectionne;
    @FXML private Label            labelStatut;
    @FXML private Label            labelCompteur;

    // =========================================================
    //  État interne
    // =========================================================
    private final IAbonnementDAO               abonnementDAO    = new AbonnementDAO();
    private final IMembreDAO                   membreDAO        = new MembreDAO();
    private final ObservableList<Abonnement>   listeAbonnements =
            FXCollections.observableArrayList();
    private final DateTimeFormatter            dateFormatter    =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int idAbonnementSelectionne = -1;

    // =========================================================
    //  INITIALISATION
    // =========================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerSpinner();
        configurerDatePickers();
        configurerComboTypes();
        configurerComboFiltres();
        chargerMembresCombo();
        configurerColonnes();
        configurerSelectionTableau();
        chargerTousAbonnements();
        System.out.println("[AbonnementController] ✅ Vue Abonnements initialisée.");
    }

    // =========================================================
    //  Configuration du Spinner Prix
    // =========================================================
    private void configurerSpinner() {
        SpinnerValueFactory<Double> factory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 99999.0, 150.0, 50.0);

        // Converter pour afficher/saisir les doubles proprement
        factory.setConverter(new StringConverter<Double>() {
            @Override public String toString(Double v) {
                return v == null ? "0.00" : String.format("%.2f", v);
            }
            @Override public Double fromString(String s) {
                try { return Double.parseDouble(s.replace(",", ".")); }
                catch (NumberFormatException e) { return 0.0; }
            }
        });

        prixSpinner.setValueFactory(factory);

        // Recalcul automatique du prix quand le type change
        typeAbonnementCombo.setOnAction(e -> {
            appliquerPrixParDefaut();
            calculerDateFin();
        });
    }

    // =========================================================
    //  Configuration DatePickers (format fr)
    // =========================================================
    private void configurerDatePickers() {
        StringConverter<LocalDate> converter = new StringConverter<>() {
            @Override public String toString(LocalDate d) {
                return (d != null) ? dateFormatter.format(d) : "";
            }
            @Override public LocalDate fromString(String s) {
                return (s != null && !s.isBlank())
                       ? LocalDate.parse(s, dateFormatter) : null;
            }
        };
        dateDebutPicker.setConverter(converter);
        dateFinPicker.setConverter(converter);
        dateDebutPicker.setValue(LocalDate.now());
    }

    // =========================================================
    //  ComboBox Types d'abonnement
    // =========================================================
    private void configurerComboTypes() {
        typeAbonnementCombo.setItems(FXCollections.observableArrayList(
            "Mensuel", "Trimestriel", "Annuel"
        ));
        typeAbonnementCombo.setValue("Mensuel");
        appliquerPrixParDefaut();
    }

    // =========================================================
    //  ComboBox Filtres (en-tête du tableau)
    // =========================================================
    private void configurerComboFiltres() {
        filtreTypeCombo.setItems(FXCollections.observableArrayList(
            "Mensuel", "Trimestriel", "Annuel"
        ));
        filtreStatutCombo.setItems(FXCollections.observableArrayList(
            "Payés", "Non payés"
        ));
    }

    // =========================================================
    //  Chargement du ComboBox Membres (membres actifs seulement)
    // =========================================================
    private void chargerMembresCombo() {
        List<Membre> membres = membreDAO.listerMembresActifs();
        membreCombo.setItems(FXCollections.observableArrayList(membres));

        // Affiche "Prénom Nom" dans le ComboBox
        membreCombo.setConverter(new StringConverter<Membre>() {
            @Override public String toString(Membre m) {
                return (m != null) ? m.getNomComplet() : "";
            }
            @Override public Membre fromString(String s) { return null; }
        });
    }

    // =========================================================
    //  Configuration des colonnes du TableView
    // =========================================================
    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idAbonnement"));

        // Nom du membre — rempli par la jointure SQL via nomCompletMembre
        colMembre.setCellValueFactory(new PropertyValueFactory<>("nomCompletMembre"));

        colType.setCellValueFactory(new PropertyValueFactory<>("typeAbonnement"));

        // Dates formatées
        colDateDebut.setCellValueFactory(cellData ->
            new SimpleStringProperty(
                cellData.getValue().getDateDebut() != null
                ? cellData.getValue().getDateDebut().format(dateFormatter) : ""
            )
        );
        colDateFin.setCellValueFactory(cellData ->
            new SimpleStringProperty(
                cellData.getValue().getDateFin() != null
                ? cellData.getValue().getDateFin().format(dateFormatter) : ""
            )
        );

        // Prix formaté avec 2 décimales
        colPrix.setCellValueFactory(cellData ->
            new SimpleStringProperty(
                String.format("%.2f", cellData.getValue().getPrix())
            )
        );

        // Colonne Payé avec CheckBox visuel
        colPaye.setCellValueFactory(cellData ->
            new SimpleBooleanProperty(cellData.getValue().isPaye())
        );
        colPaye.setCellFactory(CheckBoxTableCell.forTableColumn(colPaye));

        // Statut calculé (Valide / Expire bientôt / Expiré)
        colStatut.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStatut())
        );

        // Couleur du statut
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    switch (statut) {
                        case "Expiré"         -> setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold;");
                        case "Expire bientôt" -> setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                        default               -> setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colRemarques.setCellValueFactory(new PropertyValueFactory<>("remarques"));

        abonnementsTable.setItems(listeAbonnements);
    }

    // =========================================================
    //  Listener de sélection dans le tableau
    // =========================================================
    private void configurerSelectionTableau() {
        abonnementsTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, ancien, selectionne) -> {
                if (selectionne != null) {
                    remplirFormulaire(selectionne);
                    idAbonnementSelectionne = selectionne.getIdAbonnement();
                    labelIdSelectionne.setText(String.valueOf(idAbonnementSelectionne));
                    labelStatut.setText(selectionne.getStatut());
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
    private void chargerTousAbonnements() {
        listeAbonnements.clear();
        listeAbonnements.addAll(abonnementDAO.listerTous());
        mettreAJourCompteur();
    }

    private void mettreAJourCompteur() {
        labelCompteur.setText("Total : " + listeAbonnements.size() + " abonnement(s)");
    }

    // =========================================================
    //  HANDLER — Calcul automatique de la date de fin
    // =========================================================
    @FXML
    private void handleDateDebutChange() {
        calculerDateFin();
    }

    private void calculerDateFin() {
        LocalDate debut = dateDebutPicker.getValue();
        String type     = typeAbonnementCombo.getValue();
        if (debut == null || type == null) return;

        LocalDate fin = switch (type) {
            case "Mensuel"      -> debut.plusMonths(1).minusDays(1);
            case "Trimestriel"  -> debut.plusMonths(3).minusDays(1);
            case "Annuel"       -> debut.plusYears(1).minusDays(1);
            default             -> debut.plusMonths(1).minusDays(1);
        };
        dateFinPicker.setValue(fin);
    }

    private void appliquerPrixParDefaut() {
        String type = typeAbonnementCombo.getValue();
        if (type == null) return;
        double prix = switch (type) {
            case "Mensuel"      -> 150.0;
            case "Trimestriel"  -> 400.0;
            case "Annuel"       -> 1200.0;
            default             -> 150.0;
        };
        prixSpinner.getValueFactory().setValue(prix);
    }

    // =========================================================
    //  HANDLER — Ajouter
    // =========================================================
    @FXML
    private void handleAjouter() {
        if (!validerFormulaire()) return;

        Abonnement nouveau = new Abonnement(
            membreCombo.getValue().getIdMembre(),
            typeAbonnementCombo.getValue(),
            dateDebutPicker.getValue(),
            dateFinPicker.getValue(),
            prixSpinner.getValue(),
            payeCheckBox.isSelected(),
            remarquesArea.getText().trim()
        );

        boolean succes = abonnementDAO.ajouter(nouveau);
        if (succes) {
            afficherInfo("✅ Succès", "Abonnement ajouté avec succès !");
            chargerTousAbonnements();
            viderFormulaire();
        } else {
            afficherErreur("❌ Erreur", "Impossible d'ajouter l'abonnement.");
        }
    }

    // =========================================================
    //  HANDLER — Modifier
    // =========================================================
    @FXML
    private void handleModifier() {
        if (idAbonnementSelectionne == -1) {
            afficherErreur("Aucune sélection",
                           "Sélectionnez un abonnement dans le tableau.");
            return;
        }
        if (!validerFormulaire()) return;

        Abonnement modifie = new Abonnement(
            idAbonnementSelectionne,
            membreCombo.getValue().getIdMembre(),
            typeAbonnementCombo.getValue(),
            dateDebutPicker.getValue(),
            dateFinPicker.getValue(),
            prixSpinner.getValue(),
            payeCheckBox.isSelected(),
            remarquesArea.getText().trim()
        );

        boolean succes = abonnementDAO.modifier(modifie);
        if (succes) {
            afficherInfo("✅ Succès", "Abonnement modifié avec succès !");
            chargerTousAbonnements();
            viderFormulaire();
        } else {
            afficherErreur("❌ Erreur", "Impossible de modifier l'abonnement.");
        }
    }

    // =========================================================
    //  HANDLER — Supprimer
    // =========================================================
    @FXML
    private void handleSupprimer() {
        if (idAbonnementSelectionne == -1) {
            afficherErreur("Aucune sélection",
                           "Sélectionnez un abonnement à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer cet abonnement ?");
        confirmation.setContentText("Cette action est irréversible.");

        Optional<ButtonType> reponse = confirmation.showAndWait();
        if (reponse.isPresent() && reponse.get() == ButtonType.OK) {
            boolean succes = abonnementDAO.supprimer(idAbonnementSelectionne);
            if (succes) {
                afficherInfo("✅ Supprimé", "Abonnement supprimé avec succès.");
                chargerTousAbonnements();
                viderFormulaire();
            } else {
                afficherErreur("❌ Erreur", "Impossible de supprimer cet abonnement.");
            }
        }
    }

    // =========================================================
    //  HANDLER — Vider
    // =========================================================
    @FXML
    private void handleVider() {
        viderFormulaire();
        abonnementsTable.getSelectionModel().clearSelection();
    }

    // =========================================================
    //  HANDLER — Filtres
    // =========================================================
    @FXML
    private void handleFiltreType() {
        String type = filtreTypeCombo.getValue();
        if (type == null) return;
        filtreStatutCombo.setValue(null);
        listeAbonnements.clear();
        listeAbonnements.addAll(abonnementDAO.listerParType(type));
        mettreAJourCompteur();
    }

    @FXML
    private void handleFiltreStatut() {
        String statut = filtreStatutCombo.getValue();
        if (statut == null) return;
        filtreTypeCombo.setValue(null);
        listeAbonnements.clear();
        listeAbonnements.addAll(
            abonnementDAO.listerParStatutPaiement("Payés".equals(statut))
        );
        mettreAJourCompteur();
    }

    @FXML
    private void handleReinitialiserFiltres() {
        filtreTypeCombo.setValue(null);
        filtreStatutCombo.setValue(null);
        chargerTousAbonnements();
    }

    // =========================================================
    //  HANDLER — Actualiser
    // =========================================================
    @FXML
    private void handleActualiser() {
        chargerMembresCombo();
        chargerTousAbonnements();
        afficherInfo("🔃 Actualisé", "La liste des abonnements a été rechargée.");
    }

    // =========================================================
    //  MÉTHODES UTILITAIRES PRIVÉES
    // =========================================================

    private void remplirFormulaire(Abonnement a) {
        // Retrouve le Membre correspondant dans la ComboBox
        membreCombo.getItems().stream()
            .filter(m -> m.getIdMembre() == a.getIdMembre())
            .findFirst()
            .ifPresent(membreCombo::setValue);

        typeAbonnementCombo.setValue(a.getTypeAbonnement());
        dateDebutPicker.setValue(a.getDateDebut());
        dateFinPicker.setValue(a.getDateFin());
        prixSpinner.getValueFactory().setValue(a.getPrix());
        payeCheckBox.setSelected(a.isPaye());
        remarquesArea.setText(a.getRemarques() != null ? a.getRemarques() : "");
    }

    private void viderFormulaire() {
        membreCombo.setValue(null);
        typeAbonnementCombo.setValue("Mensuel");
        dateDebutPicker.setValue(LocalDate.now());
        calculerDateFin();
        appliquerPrixParDefaut();
        payeCheckBox.setSelected(false);
        remarquesArea.clear();
        idAbonnementSelectionne = -1;
        labelIdSelectionne.setText("—");
        labelStatut.setText("—");
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (membreCombo.getValue() == null)
            erreurs.append("• Sélectionnez un membre.\n");
        if (typeAbonnementCombo.getValue() == null)
            erreurs.append("• Choisissez un type d'abonnement.\n");
        if (dateDebutPicker.getValue() == null)
            erreurs.append("• La date de début est obligatoire.\n");
        if (dateFinPicker.getValue() == null)
            erreurs.append("• La date de fin est obligatoire.\n");
        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null
            && dateFinPicker.getValue().isBefore(dateDebutPicker.getValue()))
            erreurs.append("• La date de fin doit être après la date de début.\n");

        if (erreurs.length() > 0) {
            afficherErreur("Champs incomplets", erreurs.toString());
            return false;
        }
        return true;
    }

    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}