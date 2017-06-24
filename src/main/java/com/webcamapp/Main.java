package com.webcamapp;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamEventType;
import com.github.sarxos.webcam.WebcamPanel;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.awt.*;
import java.util.Arrays;

public class Main extends Application {

    StackPane stackPane;
    Scene scene;
    SwingNode swingPanel;
    BorderPane borderPane;
    HBox hBox;
    VBox vBox;
    WebcamHandler wh;
    WebcamPanel panel;
    Label timerLabel;
    Button recordButton;
    Button resumeButton;
    Button pauseButton;
    Button saveButton;
    Button cancelButton;
    ComboBox<Webcam> webcamsBox;
    ComboBox<Dimension> resolutonBox;
    Thread camThread;
    Slider slider;
    Image recImg = new Image(getClass().getResourceAsStream("/icons/005-video-camera.png"));



    @Override
    public void start(Stage primaryStage) throws Exception{
        initialize();
        primaryStage.getIcons().add(recImg);
        primaryStage.setTitle("Webcam Application");
        primaryStage.setScene(scene);
        primaryStage.show();


        scene.setOnMouseClicked(e->{
            if(e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount()==2){
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
            }
        });

        vBox.setOnMouseEntered(e->{
         /*   hBox.getChildren().stream().forEach(child->{
                child.setVisible(true);
            });*/
            hBox.setVisible(true);
        });

        vBox.setOnMouseExited(e->{
           if(!resolutonBox.isShowing() && !webcamsBox.isShowing())
               hBox.setVisible(false);
          /*  hBox.getChildren().stream().forEach(child->{
                child.setVisible(false);
            });*/
        });


        webcamsBox.valueProperty().addListener((obs, old, n)-> {
            scene.setCursor(Cursor.WAIT);
            panel.stop();
            wh.setWebcam(n);
            panel.start();
            resolutonBox.getItems().clear();
            if(n.getDevice().getName().toUpperCase().contains("MICROSOFT")) {
                Dimension[] dims = new Dimension[1];
                dims[0] = new Dimension(1280, 720);
                if(!Arrays.asList(n.getViewSizes()).contains(dims[0])) {
                n.setCustomViewSizes(dims);
            }
            }
            resolutonBox.getItems().addAll(Arrays.asList(wh.getWebcam().getViewSizes()));
            resolutonBox.getItems().addAll(Arrays.asList(wh.getWebcam().getCustomViewSizes()));
            resolutonBox.getSelectionModel().select((wh.getWebcam().getViewSize()));
        /*    Dimension customDim = new Dimension(1280,720);
            if(!Arrays.asList(wh.getWebcam().getViewSizes()).contains(customDim)){
                resolutonBox.getItems().add(customDim);
            }*/
            scene.setCursor(Cursor.DEFAULT);
        });

        webcamsBox.getSelectionModel().select(Webcam.getDefault());


        resolutonBox.valueProperty().addListener((obs, old, ne)->{
            scene.setCursor(Cursor.WAIT);
            panel.stop();
            wh.setResoultion(ne);
            panel.start();
            scene.setCursor(Cursor.DEFAULT);
        });


        recordButton.setOnMouseClicked(e->{
            wh.startRecording();
//            timer.startCounting();
          //  hBox.getChildren().add(resumeButton);
            //     hBox.getChildren().add(pauseButton);

            hBox.getChildren().remove(0);
            hBox.getChildren().add(0,pauseButton);
            saveButton.setVisible(true);
            cancelButton.setVisible(true);
            resolutonBox.setDisable(true);
            webcamsBox.setDisable(true);
        });

        resumeButton.setOnMouseClicked(e->{
            wh.resumeRecording();
       //     timer.resumeCounting();
            hBox.getChildren().remove(0);
            hBox.getChildren().add(0,pauseButton);
        });

        pauseButton.setOnMouseClicked(e->{
         //   timer.pauseCounting();
            wh.pauseRecording();
            hBox.getChildren().remove(0);
            hBox.getChildren().add(0,resumeButton);
        });

        saveButton.setOnMouseClicked(e->{
         //   timer.pauseCounting();
            wh.saveRecording();
            hBox.getChildren().remove(0);
            hBox.getChildren().add(0,recordButton);
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
            resolutonBox.setDisable(false);
            webcamsBox.setDisable(false);
        });

        cancelButton.setOnMouseClicked(e->{
        //    timer.cancelCounting();
            wh.cancelRecording();
            hBox.getChildren().remove(0);
            hBox.getChildren().add(0,recordButton);
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
            resolutonBox.setDisable(false);
            webcamsBox.setDisable(false);
        });

        slider.valueProperty().addListener((ov, old_v, new_v)->{
          //  wh.setBrightness(new_v.intValue());
            wh.controlBrightness(new_v.floatValue());
        });


        primaryStage.setOnCloseRequest(e->{
            try {
                wh.getWebcam().close();
                wh.getWebcam().getDevice().close();
           //     timer.endThread();
                wh.endThread();
         //       timerThread.join();
                camThread.join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });

    }

    private void initialize(){
        stackPane = new StackPane();
        scene=new Scene(stackPane, 800, 600);
        if(Webcam.getWebcams().size()==0){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No camera found");
            alert.setHeaderText("No camera were found in the system");
            alert.setContentText("No camera were found in the system. The application will be shut down.");
            alert.showAndWait().ifPresent(rs -> {
                System.exit(0);
            });
        }
        wh = new WebcamHandler(Webcam.getDefault());
      //  panel = new WebcamPanel(wh.getWebcam());
        panel = new WebcamPanel(wh.getWebcam());
     //   panel.setFPSLimited(false);
        swingPanel = new SwingNode();
        swingPanel.setContent(panel);
        stackPane.getChildren().add(swingPanel);
        borderPane = new BorderPane();
        stackPane.getChildren().add(borderPane);
        hBox = new HBox();
        vBox = new VBox();
        timerLabel = new Label("00:00:00:00");
        vBox.getChildren().add(timerLabel);
        vBox.getChildren().add(hBox);

        String buttonStyle="-fx-background-radius: 50em; " +
                "-fx-min-width: 50px; " +
                "-fx-min-height: 50px; " +
                "-fx-max-width: 50px; " +
                "-fx-max-height: 50px;";
        Image saveImg = new Image(getClass().getResourceAsStream("/icons/002-save.png"));
        Image pauseImg = new Image(getClass().getResourceAsStream("/icons/003-pause.png"));
        Image resumeImg = new Image(getClass().getResourceAsStream("/icons/004-play-button.png"));
        Image brightImg = new Image(getClass().getResourceAsStream("/icons/001-brightness.png"));
        Image cancelImg = new Image(getClass().getResourceAsStream("/icons/006-square.png"));
        recordButton = new Button();
        recordButton.setGraphic(new ImageView(recImg));
        recordButton.setStyle(buttonStyle);

        resumeButton = new Button();
        resumeButton.setStyle(buttonStyle);
        resumeButton.setGraphic(new ImageView(resumeImg));

        pauseButton = new Button();
        pauseButton.setStyle(buttonStyle);
        pauseButton.setGraphic(new ImageView(pauseImg));


        saveButton = new Button();
        saveButton.setStyle(buttonStyle);
        saveButton.setVisible(false);
        saveButton.setGraphic(new ImageView(saveImg));

        cancelButton = new Button();
        cancelButton.setStyle(buttonStyle);
        cancelButton.setVisible(false);
        cancelButton.setGraphic(new ImageView(cancelImg));


        hBox.getChildren().add(recordButton);
        hBox.getChildren().add(saveButton);
        hBox.getChildren().add(cancelButton);
        hBox.setVisible(false);
        borderPane.setBottom(vBox);
        hBox.setSpacing(20);
        vBox.setAlignment(Pos.BOTTOM_CENTER);
        vBox.setSpacing(10);
        slider = new Slider();
        slider.setMin(-1.0f);
        slider.setMax(1.0f);
        slider.setValue(0.0f);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(50);
        slider.setMinorTickCount(5);
        slider.setShowTickLabels(false);
        slider.setShowTickMarks(false);
        webcamsBox= new ComboBox<Webcam>();
        webcamsBox.getItems().addAll(Webcam.getWebcams());
        hBox.getChildren().add(webcamsBox);
        resolutonBox= new ComboBox<Dimension>();
      //  resolutonBox.setVisibleRowCount(2);
      //  hBox.getChildren().add()
        hBox.getChildren().add(resolutonBox);
        VBox sliderBox = new VBox();
        sliderBox.setAlignment(Pos.CENTER);
        sliderBox.getChildren().add(new ImageView(brightImg));
        sliderBox.getChildren().add(slider);
        hBox.getChildren().add(sliderBox);
        hBox.setAlignment(Pos.CENTER);
        panel.setFPSDisplayed(false);

        camThread = new Thread(wh);
      //  camThread.setPriority(Thread.MAX_PRIORITY);
        camThread.start();
        camThread.setPriority(Thread.MAX_PRIORITY);
       // timerThread.start();
        String labelStyle="-fx-text-fill: white;"+
                "-fx-text-stroke: black;"+
                "-fx-text-stroke-width: 2px ;"+
                "-fx-font-size: 3em";
        timerLabel.setStyle(labelStyle);
        timerLabel.textProperty().bind(wh.messageProperty());

        class ResolutionConverter extends StringConverter<Dimension> {

            @Override
            public String toString(Dimension object) {
                Double d=object.getWidth();
                Double h = object.getHeight();

                return d.intValue()+ "x"+h.intValue();
            }

            @Override
            public Dimension fromString(String string) {
                int xPos = string.indexOf('x');
                int width=Integer.getInteger(string.substring(0,xPos));
                int height=Integer.getInteger(string.substring(xPos+1));
                return new Dimension(width,height);
            }
        }
        resolutonBox.setConverter(new ResolutionConverter());


    }


    public static void main(String[] args) {
        launch(args);
    }
}

