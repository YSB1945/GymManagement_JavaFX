package com.gymproject.controllers;

import com.gymproject.dao.AbonnementDAO;
import com.gymproject.dao.IAbonnementDAO;
import com.gymproject.dao.IMembreDAO;
import com.gymproject.dao.MembreDAO;
import com.gymproject.models.Abonnement;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Contrôleur du Tableau de Bord — Statistiques & Export CSV.
 * Exploite les méthodes statistiques de AbonnementDAO
 * et la vue SQL vue_stats_type_abonnement.
 */
public class StatistiquesController implements Initializable {

    // =========================================================
    //  Injections FXML — Cartes rapides
    // =========================================================
    @FXML private Label labelTotalMembres;
    @FXML private Label labelTotalAbonnements;
    @FXML private Label labelAbonnementsPayes;
    @FXML private Label labelExpirantBientot;

    // =========================================================
    //  Injections FXML — Chiffre d'affaires
    // =========================================================
    @FXML private Label       labelCATotal;
    @FXML private Label       labelCAEncaisse;
    @FXML private Label       labelCAAttente;
    @FXML private Label       labelObjectif;
    @FXML private Label       labelProgression;
    @FXML private Slider      objectifSlider;
    @FXML private ProgressBar progressBar;

    // =========================================================
    //  Injections FXML — Répartition par type
    // =========================================================
    @FXML private Label       labelNbMensuel;
    @FXML private Label       labelNbTrimestriel;
    @FXML private Label       labelNbAnnuel;
    @FXML private Label       labelCaMensuel;
    @FXML private Label       labelCaTrimestriel;
    @FXML private Label       labelCaAnnuel;
    @FXML private ProgressBar barMensuel;
    @FXML private ProgressBar barTrimestriel;
    @FXML private ProgressBar barAnnuel;

    // =========================================================
    //  Injections FXML — Membres à relancer
    // =========================================================
    @FXML private ListView<String>    listeMembresARelancer;
    @FXML private Label               labelNbARelancer;
    @FXML private ProgressIndicator   loadingIndicator;

    // =========================================================
    //  Injections FXML — Export
    // =========================================================
    @FXML private Label labelExportStatut;
    @FXML private Accordion accordion;

    // =========================================================
    //  État interne
    // =========================================================
    private final IAbonnementDAO abonnementDAO = new AbonnementDAO();
    private final IMembreDAO     membreDAO     = new MembreDAO();
    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private double caEncaisse = 0.0;

    // =========================================================
    //  INITIALISATION
    // =========================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // CORRECTION ICI : On utilise Platform.runLater pour éviter le crash d'injection FXML (NullPointerException)
        Platform.runLater(() -> {
            chargerTouttesStatistiques();
            System.out.println("[StatistiquesController] ✅ Tableau de bord initialisé.");
        });
    }

    // =========================================================
    //  CHARGEMENT GLOBAL
    // =========================================================
    private void chargerTouttesStatistiques() {
        chargerCartesRapides();
        chargerChiffreAffaires();
        chargerRepartitionParType();
        chargerMembresARelancer();
    }

    // =========================================================
    //  CARTES RAPIDES
    // =========================================================
    private void chargerCartesRapides() {
        int totalMembres      = membreDAO.compterTotal();
        int totalAbonnements  = abonnementDAO.compterTotal();
        int totalPayes        = abonnementDAO.compterPayes();
        int expirantBientot   = abonnementDAO.compterExpirantBientot();

        labelTotalMembres.setText(String.valueOf(totalMembres));
        labelTotalAbonnements.setText(String.valueOf(totalAbonnements));
        labelAbonnementsPayes.setText(totalPayes + " / " + totalAbonnements);
        labelExpirantBientot.setText(String.valueOf(expirantBientot));
    }

    // =========================================================
    //  CHIFFRE D'AFFAIRES + PROGRESSBAR + SLIDER
    // =========================================================
    private void chargerChiffreAffaires() {
        double caTotal   = abonnementDAO.getChiffreAffairesTotal();
        caEncaisse       = abonnementDAO.getChiffreAffairesEncaisse();
        double caAttente = caTotal - caEncaisse;

        labelCATotal.setText(String.format("%.2f DH", caTotal));
        labelCAEncaisse.setText(String.format("%.2f DH", caEncaisse));
        labelCAAttente.setText(String.format("%.2f DH", caAttente));

        mettreAJourProgressBar();
    }

    private void mettreAJourProgressBar() {
        if (objectifSlider == null || progressBar == null) return;
        
        double objectif    = objectifSlider.getValue();
        double progression = (objectif > 0) ? (caEncaisse / objectif) : 0.0;
        progression        = Math.min(progression, 1.0); // cap à 100%

        progressBar.setProgress(progression);
        labelObjectif.setText(String.format("%.0f DH", objectif));
        labelProgression.setText(String.format("%.1f %%", progression * 100));

        // Couleur dynamique selon avancement
        if (progression >= 1.0) {
            progressBar.setStyle("-fx-accent: #4CAF50;");
        } else if (progression >= 0.5) {
            progressBar.setStyle("-fx-accent: #FF9800;");
        } else {
            progressBar.setStyle("-fx-accent: #e94560;");
        }
    }

    // =========================================================
    //  HANDLER — Slider objectif mensuel
    // =========================================================
    @FXML
    private void handleSliderChange() {
        mettreAJourProgressBar();
    }

    // =========================================================
    //  RÉPARTITION PAR TYPE — via vue_stats_type_abonnement
    // =========================================================
    private void chargerRepartitionParType() {
        Map<String, double[]> stats = abonnementDAO.getStatistiquesParType();
        int total = abonnementDAO.compterTotal();
        if (total == 0) total = 1; // évite division par zéro

        // Mensuel
        double[] mensuel = stats.getOrDefault("Mensuel", new double[]{0, 0, 0});
        labelNbMensuel.setText((int) mensuel[0] + " abonnement(s)");
        labelCaMensuel.setText(String.format("%.2f DH", mensuel[1]));
        barMensuel.setProgress(mensuel[0] / total);

        // Trimestriel
        double[] trimestriel = stats.getOrDefault("Trimestriel", new double[]{0, 0, 0});
        labelNbTrimestriel.setText((int) trimestriel[0] + " abonnement(s)");
        labelCaTrimestriel.setText(String.format("%.2f DH", trimestriel[1]));
        barTrimestriel.setProgress(trimestriel[0] / total);

        // Annuel
        double[] annuel = stats.getOrDefault("Annuel", new double[]{0, 0, 0});
        labelNbAnnuel.setText((int) annuel[0] + " abonnement(s)");
        labelCaAnnuel.setText(String.format("%.2f DH", annuel[1]));
        barAnnuel.setProgress(annuel[0] / total);
    }

    // =========================================================
    //  MEMBRES À RELANCER — ListView
    // =========================================================
    private void chargerMembresARelancer() {
        loadingIndicator.setVisible(true);

        List<Abonnement> tous = abonnementDAO.listerTous();
        ObservableList<String> aRelancer = FXCollections.observableArrayList();

        for (Abonnement a : tous) {
            String statut = a.getStatut();
            if ("Expiré".equals(statut) || "Expire bientôt".equals(statut)) {
                String ligne = String.format(
                    "%s  ·  %s  ·  Fin : %s  ·  %s",
                    a.getNomCompletMembre(),
                    a.getTypeAbonnement(),
                    a.getDateFin() != null
                        ? a.getDateFin().format(dateFormatter) : "—",
                    statut
                );
                aRelancer.add(ligne);
            }
        }

        listeMembresARelancer.setItems(aRelancer);
        labelNbARelancer.setText(String.valueOf(aRelancer.size()));
        loadingIndicator.setVisible(false);

        // Coloration des cellules selon statut
        listeMembresARelancer.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Expiré")) {
                        setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    // =========================================================
    //  HANDLER — Actualiser
    // =========================================================
    @FXML
    private void handleActualiser() {
        chargerTouttesStatistiques();
        labelExportStatut.setText("✅ Données actualisées.");
    }

    // =========================================================
    //  HANDLER — Export CSV avec FileChooser
    // =========================================================
    @FXML
    private void handleExporterCSV() {
        // 1. Ouvre le FileChooser pour choisir l'emplacement
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les abonnements en CSV");
        fileChooser.setInitialFileName("abonnements_export.csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv")
        );

        File fichier = fileChooser.showSaveDialog(
            labelExportStatut.getScene().getWindow()
        );

        if (fichier == null) {
            // L'utilisateur a annulé
            return;
        }

        // 2. Récupère tous les abonnements via le DAO
        List<Abonnement> abonnements = abonnementDAO.listerTous();

        // 3. Écriture du fichier CSV
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(fichier, java.nio.charset.StandardCharsets.UTF_8))) {

            // En-tête BOM UTF-8 pour compatibilité Excel
            writer.write('\uFEFF');

            // Ligne d'en-tête
            writer.write("ID;Membre;Type;Date Début;Date Fin;Prix (DH);Payé;Statut;Remarques");
            writer.newLine();

            // Lignes de données
            for (Abonnement a : abonnements) {
                String ligne = String.join(";",
                    String.valueOf(a.getIdAbonnement()),
                    escaperCSV(a.getNomCompletMembre()),
                    escaperCSV(a.getTypeAbonnement()),
                    a.getDateDebut() != null
                        ? a.getDateDebut().format(dateFormatter) : "",
                    a.getDateFin() != null
                        ? a.getDateFin().format(dateFormatter) : "",
                    String.format("%.2f", a.getPrix()),
                    a.isPaye() ? "Oui" : "Non",
                    escaperCSV(a.getStatut()),
                    escaperCSV(a.getRemarques() != null ? a.getRemarques() : "")
                );
                writer.write(ligne);
                writer.newLine();
            }

            // Message de succès
            String msg = String.format(
                "✅ Export réussi ! %d abonnement(s) exporté(s) vers :\n%s",
                abonnements.size(), fichier.getAbsolutePath()
            );
            labelExportStatut.setText(msg);
            labelExportStatut.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");

            afficherInfo("✅ Export CSV réussi",
                abonnements.size() + " abonnements exportés avec succès !\n\n" +
                "Fichier : " + fichier.getName()
            );

        } catch (IOException e) {
            String errMsg = "❌ Erreur lors de l'export : " + e.getMessage();
            labelExportStatut.setText(errMsg);
            labelExportStatut.setStyle("-fx-text-fill: #e94560;");
            afficherErreur("❌ Erreur Export", errMsg);
            System.err.println("[StatistiquesController] " + errMsg);
        }
    }

    // =========================================================
    //  MÉTHODES UTILITAIRES PRIVÉES
    // =========================================================

    /**
     * Échappe les champs CSV contenant des points-virgules ou des guillemets.
     * Entoure le champ de guillemets si nécessaire.
     */
    private String escaperCSV(String valeur) {
        if (valeur == null) return "";
        if (valeur.contains(";") || valeur.contains("\"") || valeur.contains("\n")) {
            return "\"" + valeur.replace("\"", "\"\"") + "\"";
        }
        return valeur;
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