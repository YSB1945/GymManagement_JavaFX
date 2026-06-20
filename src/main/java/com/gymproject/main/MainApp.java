package com.gymproject.main;

import com.gymproject.dao.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Point d'entrée de l'application JavaFX — Club Sportif Manager.
 * Charge la vue principale (main.fxml) et gère le cycle de vie de l'app.
 */
public class MainApp extends Application {

    // Dimensions initiales de la fenêtre
    private static final int LARGEUR  = 1100;
    private static final int HAUTEUR  =  700;
    private static final String TITRE = "🏋️ Club Sportif Manager";

    @Override
    public void start(Stage primaryStage) {
        try {
            // Chargement du layout principal
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/gymproject/fxml/main.fxml")
            );
            Parent root = loader.load();

            // Configuration de la scène
            Scene scene = new Scene(root, LARGEUR, HAUTEUR);

            // Liaison de la feuille de style CSS
            scene.getStylesheets().add(
                Objects.requireNonNull(
                    getClass().getResource("/com/gymproject/css/style.css")
                ).toExternalForm()
            );

            // Configuration de la fenêtre principale
            primaryStage.setTitle(TITRE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.show();

            System.out.println("[MainApp] ✅ Application démarrée avec succès.");

        } catch (IOException e) {
            System.err.println("[MainApp] ❌ Erreur chargement main.fxml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Appelée automatiquement par JavaFX à la fermeture de la fenêtre.
     * Ferme proprement la connexion JDBC avant de quitter.
     */
    @Override
    public void stop() {
        Database.getInstance().closeConnection();
        System.out.println("[MainApp] 🔒 Application fermée proprement.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}