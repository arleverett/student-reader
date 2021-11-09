import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.*;
import java.net.URISyntaxException;

public class Window extends Application {
    @Override
    public void start(Stage primaryStage) throws URISyntaxException {
        String artifactName = "studentReader-1.0.jar";
        String localPath = new File(Window.class.getProtectionDomain().getCodeSource().getLocation()
                                                .toURI()).getPath().replace(artifactName,"");

        VBox dragTarget = createStudentTransformDrag(localPath);
        VBox dragTarget2 = createTesDrag(localPath);
       // VBox dragTarget3 = Findhistoricmarks(localPath);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().add(new Tab("Sort Students", dragTarget));
        tabPane.getTabs().add(new Tab("Calculate TES"  , dragTarget2));
        //tabPane.getTabs().add(new Tab("Find historic marks"  , dragTarget3));
        VBox tabContents = new VBox(tabPane);
        tabPane.setPrefHeight(250);

        VBox.setVgrow(tabContents, Priority.ALWAYS);

        Scene scene = new Scene(tabContents, 500, 250);
        scene.getStylesheets().add("style.css");

        primaryStage.setTitle("Student Reader");
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.heightProperty().addListener((observable, oldValue, newValue) -> tabPane.setPrefHeight(newValue.doubleValue()));
    }

    private VBox createStudentTransformDrag(String localPath) {
        Label label = new Label("Transform: Drag xls here.");
        Label result = new Label("");
        VBox dragTarget = new VBox();
        dragTarget.setId("drag");
        dragTarget.alignmentProperty().setValue(Pos.CENTER);
        dragTarget.getChildren().addAll(label,result);
        dragTarget.setOnDragOver(event -> {
            if (event.getGestureSource() != dragTarget
                    && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        dragTarget.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles() && db.getFiles().size()==1) {
                String name = db.getFiles().get(0).getName();
                String path = db.getFiles().get(0).toString();
                System.out.println(name);
                System.out.println(path.replace(name,""));
                System.out.println(localPath);

                try {
                    new StudentReader(name, path, localPath).executeStudentTransform();
                    result.setText(path + " success" + " - Result saved to: " + localPath +name);
                } catch (IOException e) {
                    result.setText(path + " failed");
                    e.printStackTrace();
                }
                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        });
        VBox.setVgrow(dragTarget, Priority.ALWAYS);
        return dragTarget;
    }
    /*
    private VBox Findhistoricmarks(String localPath) {
        Label label = new Label("Historic marks: Drag xls here.");
        Label result = new Label("");
        VBox dragTarget = new VBox();
        dragTarget.setId("drag");
        dragTarget.alignmentProperty().setValue(Pos.CENTER);
        dragTarget.getChildren().addAll(label,result);
        dragTarget.setOnDragOver(event -> {
            if (event.getGestureSource() != dragTarget
                    && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        dragTarget.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles() && db.getFiles().size()==1) {
                String name = db.getFiles().get(0).getName();
                String path = db.getFiles().get(0).toString();
                System.out.println(name);
                System.out.println(path.replace(name,""));
                System.out.println(localPath);

                try {
                    new StudentReader(name, path, localPath).executehistoricmarks();
                    result.setText(path + " success" + " - Result saved to: " + localPath +name);
                } catch (IOException e) {
                    result.setText(path + " failed");
                    e.printStackTrace();
                }
                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        });
        VBox.setVgrow(dragTarget, Priority.ALWAYS);
        return dragTarget;

    }
    */
    private VBox createTesDrag(String localPath) {
        Label label = new Label("TES: Drag xls here.");
        Label result = new Label("");
        VBox dragTarget = new VBox();
        dragTarget.setId("drag");
        dragTarget.alignmentProperty().setValue(Pos.CENTER);
        dragTarget.getChildren().addAll(label,result);
        dragTarget.setOnDragOver(event -> {
            if (event.getGestureSource() != dragTarget
                    && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        dragTarget.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles() && db.getFiles().size()==1) {
                String name = db.getFiles().get(0).getName();
                String path = db.getFiles().get(0).toString();
                System.out.println(name);
                System.out.println(path.replace(name,""));
                System.out.println(localPath);

                try {
                    new StudentReader(name, path, localPath).executeStudentTes();
                    result.setText(path + " success" + " - Result saved to: " + localPath +name);
                } catch (IOException e) {
                    result.setText(path + " failed");
                    e.printStackTrace();
                }
                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        });
        VBox.setVgrow(dragTarget, Priority.ALWAYS);
        return dragTarget;
    }
}
