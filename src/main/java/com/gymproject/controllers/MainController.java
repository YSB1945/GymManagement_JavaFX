package com.gymproject.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur du layout principal — Navigation entre les vues.
 * Version finale : charge dynamiquement membre.fxml,
 * abonnement.fxml et statistiques.fxml dans le StackPane central.
 */
public class MainController implements Initializable {

    // =========================================================
    //  Injections FXML
    // =========================================================
    @FXML private StackPane contentArea;
    @FXML private VBox      welcomePane;
    @FXML private Button    btnMembres;
    @FXML private Button    btnAbonnements;
    @FXML private Button    btnStatistiques;
    @FXML private Label     labelStatutAction;
    @FXML private Label     labelStatutDB;
    @FXML private Label     labelVersion;

    // =========================================================
    //  Chemins FXML — centralisés en constantes
    // =========================================================
    private static final String FXML_MEMBRES      =
            "/com/gymproject/fxml/membre.fxml";
    private static final String FXML_ABONNEMENTS  =
            "/com/gymproject/fxml/abonnement.fxml";
    private static final String FXML_STATISTIQUES =
            "/com/gymproject/fxml/statistiques.fxml";

    // Bouton actuellement actif dans le menu latéral
    private Button boutonActif;

    // =========================================================
    //  INITIALISATION
    // =========================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        boutonActif = btnMembres;
        appliquerStyleActif(btnMembres);
        mettreAJourStatut("Application prête — Bienvenue !");
        System.out.println("[MainController] ✅ Layout principal initialisé.");
    }

    // =========================================================
    //  HANDLERS — Menu latéral
    // =========================================================

    @FXML
    private void handleNavMembres() {
        chargerVue(FXML_MEMBRES, btnMembres, "Gestion des Membres");
    }

    @FXML
    private void handleNavAbonnements() {
        chargerVue(FXML_ABONNEMENTS, btnAbonnements, "Gestion des Abonnements");
    }

    @FXML
    private void handleNavStatistiques() {
        chargerVue(FXML_STATISTIQUES, btnStatistiques, "Tableau de Bord — Statistiques");
    }

    // =========================================================
    //  HANDLERS — MenuBar
    // =========================================================

    @FXML
    private void handleNavMembres_menu() {
        chargerVue(FXML_MEMBRES, btnMembres, "Gestion des Membres");
    }

    @FXML
    private void handleNavAbonnements_menu() {
        chargerVue(FXML_ABONNEMENTS, btnAbonnements, "Gestion des Abonnements");
    }

    @FXML
    private void handleNavStatistiques_menu() {
        chargerVue(FXML_STATISTIQUES, btnStatistiques, "Tableau de Bord — Statistiques");
    }

    @FXML
    private void handleQuitter() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Quitter l'application");
        confirmation.setHeaderText("Confirmer la fermeture");
        confirmation.setContentText(
            "Voulez-vous vraiment quitter Club Sportif Manager ?"
        );
        confirmation.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.OK) {
                Platform.exit();
            }
        });
    }

    @FXML
    private void handleAPropos() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("À propos");
        info.setHeaderText("Club Sportif Manager — v1.0");
        info.setContentText(
            "Application de gestion d'un club sportif.\n\n" +
            "Développé dans le cadre du Mini-projet JavaFX\n" +
            "ENSAO GI3 — 2025/2026\n\n" +
            "Architecture : MVC + DAO + JDBC (MySQL)"
        );
        info.showAndWait();
    }

    // =========================================================
    //  MÉTHODE CENTRALE DE NAVIGATION
    // =========================================================

    /**
     * Charge dynamiquement un fichier FXML dans le contentArea central.
     *
     * Fonctionnement :
     *  1. Masque le panneau de bienvenue à la première navigation.
     *  2. Charge le FXML via FXMLLoader — chaque vue a son propre
     *     contrôleur instancié automatiquement par JavaFX.
     *  3. Remplace le contenu précédent du StackPane.
     *  4. Met à jour le style du bouton actif et la barre de statut.
     *  5. En cas d'IOException, affiche une Alert sans faire crasher l'app.
     *
     * @param cheminFxml  Chemin classpath absolu vers le fichier FXML.
     * @param bouton      Bouton du menu latéral qui a déclenché la navigation.
     * @param titreAction Texte affiché dans la barre de statut en bas.
     */
   private void chargerVue(String cheminFxml, Button bouton, String titreAction) {
        try {
            URL ressource = getClass().getResource(cheminFxml);
            if (ressource == null) {
                throw new IOException("Fichier FXML introuvable : " + cheminFxml);
            }

            FXMLLoader loader = new FXMLLoader(ressource);
            Node nouvelleVue = loader.load();

            // ... (reste du code de masquage et ajout)
            if (welcomePane != null && welcomePane.isVisible()) {
                welcomePane.setVisible(false);
                welcomePane.setManaged(false);
            }
            contentArea.getChildren().removeIf(node -> node != welcomePane);
            contentArea.getChildren().add(nouvelleVue);

            appliquerStyleActif(bouton);
            mettreAJourStatut(titreAction);

        } catch (Exception e) { // CHANGÉ en Exception pour attraper les RuntimeException aussi
            e.printStackTrace(); // AJOUTÉ : cela affichera l'erreur complète dans la console

            Alert erreur = new Alert(Alert.AlertType.ERROR);
            erreur.setTitle("Erreur de navigation");
            erreur.setHeaderText("Impossible de charger la vue");
            // On affiche le message de l'exception pour voir si c'est une NullPointerException par exemple
            erreur.setContentText("Détail : " + e.toString()); 
            erreur.showAndWait();
        }
    }

    // =========================================================
    //  MÉTHODES UTILITAIRES
    // =========================================================

    /**
     * Active visuellement le bouton sélectionné dans le menu latéral
     * en ajoutant la classe CSS "nav-button-active" et en la retirant
     * du bouton précédemment actif.
     */
    private void appliquerStyleActif(Button nouveauBouton) {
        if (boutonActif != null) {
            boutonActif.getStyleClass().remove("nav-button-active");
        }
        if (!nouveauBouton.getStyleClass().contains("nav-button-active")) {
            nouveauBouton.getStyleClass().add("nav-button-active");
        }
        boutonActif = nouveauBouton;
    }

    /** Met à jour le label de statut dans la barre du bas. */
    private void mettreAJourStatut(String message) {
        if (labelStatutAction != null) {
            labelStatutAction.setText(message);
        }
    }
}