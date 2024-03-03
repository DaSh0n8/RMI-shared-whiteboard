package com.example.whiteboard;

import com.example.whiteboard.client.Client;
import com.example.whiteboard.client.ClientImpl;
import com.example.whiteboard.server.WhiteboardInterface;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.input.MouseEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.BlendMode;

import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class WhiteboardController implements Initializable {
    @FXML
    private Label clientUsername;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Label enterUsernameLabel;
    @FXML
    private Button lineButton;
    @FXML
    private Button circleButton;
    @FXML
    private Button ovalButton;
    @FXML
    private Button rectangleButton;
    @FXML
    private Button textButton;
    @FXML
    private Button eraserButton;
    @FXML
    private ListView<String> userList;
    @FXML
    private Button usernameButton;
    @FXML
    private TextField usernameField;
    @FXML
    private Label errorLabel;
    @FXML
    private ListView<String> chatWindow;
    @FXML
    private AnchorPane realWhiteBoard;
    @FXML
    private AnchorPane whiteboard;
    @FXML
    private AnchorPane waitingScreen;
    @FXML
    private Tab waitingTab;
    @FXML
    private TabPane tabPane;
    @FXML
    private Canvas canvas;
    @FXML
    private Canvas eraserCanvas;
    @FXML
    private ListView<String> waitingList;
    @FXML
    private TextField chatTextField;
    @FXML
    private DialogPane usernamePopup;
    @FXML
    private Label waitingLabel;
    @FXML
    private MenuItem newButton;
    @FXML
    private MenuItem openButton;
    @FXML
    private Button kickButton;
    private Registry registry;
    private boolean eraserMode = false;

    private boolean isDrawing = false;
    private boolean isLineDrawing = false;
    private GraphicsContext graphicsContext;

    {
        try {
            registry = LocateRegistry.getRegistry("localhost", 1233);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private WhiteboardInterface whiteboardServer;
    private ClientImpl clientImpl = new ClientImpl();

    {
        try {
            whiteboardServer = (WhiteboardInterface) registry.lookup("Whiteboard");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    public WhiteboardController() throws RemoteException {

    }

    public void clearUserList(){
        Platform.runLater(() -> {
            userList.getItems().clear();
        });
    }
    public void addName(String username){
        Platform.runLater(() -> {
            userList.getItems().add(username);
        });
    }

    public void clearWaitingList(){
        Platform.runLater(() -> {
            waitingList.getItems().clear();
        });
    }
    public void addWaitingName(String username){
        Platform.runLater(() -> {
            waitingList.getItems().add(username);
        });
    }

    @FXML
    void createUser(MouseEvent event) throws RemoteException {
        String username = usernameField.getText();
        ArrayList<Client> checkList = whiteboardServer.getClientList();

        boolean usernameExists = false;

        for (Client aClient : checkList) {
            if (aClient.getUsername().equals(username)) {
                usernameExists = true;
                break;
            }
        }
        if (usernameExists) {
            errorLabel.setText("Pick another username");
        } else {
                clientImpl.setController(this);

                try {
                    whiteboardServer.registerClient(username, false, clientImpl);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                ArrayList<Client> clientList = whiteboardServer.getClientList();
                Client client = clientList.get(clientList.size() - 1);

                if (client.getClientId() == 1){
                    client.setManager(true);
                    kickButton.setVisible(true);
                    tabPane.getTabs().add(waitingTab);
                    newButton.setDisable(false);
                    openButton.setDisable(false);
                    whiteboardServer.addManagerToClientList();
                } else {
                    waitingScreen.setVisible(true);
                    kickButton.setVisible(false);
                }

                clientUsername.setText(client.getUsername());

                if (client.getClientId() != 1){
                    try {
                        whiteboardServer.distributeImageData(clientImpl);
                        System.out.println("Whiteboard history reflected");
                    } catch (RemoteException e) {
                        System.out.println("Whiteboard has just started");
                    }
                }  else{
                    try {
                        whiteboardServer.receiveImageData(captureCanvas(), clientImpl);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }


                usernamePopup.setVisible(false);
                enterUsernameLabel.setVisible(false);
                usernameField.setVisible(false);
                usernameField.setManaged(false);
                usernamePopup.setManaged(false);
                usernameButton.setVisible(false);
                usernameButton.setManaged(false);
            }
    }

    @FXML
    void createNew(ActionEvent event) throws RemoteException {
        String imagePath = "../Whiteboard/src/main/java/new/file.png";

        try {
            Image image = new Image(new FileInputStream(imagePath));
            ImageView imageView = new ImageView(image);
            realWhiteBoard.getChildren().add(imageView);
            imageView.toFront();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        whiteboardServer.setCurrentCanvas(captureCanvas());
        try {
            whiteboardServer.distributeImageData(clientImpl);
            System.out.println("Whiteboard history reflected");
        } catch (RemoteException e) {
            System.out.println("Whiteboard has just started");
        }
    }

    public void newPanes () {
        Platform.runLater(() -> {
            realWhiteBoard.getChildren().clear();
        });
    }

    @FXML
    void openNew(ActionEvent event) throws RemoteException {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                ImageView imageView = new ImageView(image);
                realWhiteBoard.getChildren().add(imageView);
                imageView.toFront();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        whiteboardServer.setCurrentCanvas(captureCanvas());
        try {
            whiteboardServer.distributeImageData(clientImpl);
            System.out.println("Whiteboard history reflected");
        } catch (RemoteException e) {
            System.out.println("Whiteboard has just started");
        }
    }

    @FXML
    void saveAsFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                WritableImage writableImage = realWhiteBoard.snapshot(null, null);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void saveFile(ActionEvent event) {
        File file = new File("../Whiteboard/src/main/java/saves/file.png");

        try {
            WritableImage writableImage = realWhiteBoard.snapshot(null, null);
            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
            ImageIO.write(renderedImage, "png", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void userApproved(MouseEvent event) {
        String selectedUser = waitingList.getSelectionModel().getSelectedItem();
        try {
            whiteboardServer.removeWaitingScreen(selectedUser);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        try {
            whiteboardServer.distributeImageData(clientImpl);
            System.out.println("Whiteboard history reflected");
        } catch (RemoteException e) {
            System.out.println("Whiteboard has just started");
        }
    }

    public void setWaitingScreen(boolean display){
        Platform.runLater(() -> {
            waitingScreen.setVisible(display);
            waitingList.setVisible(false);
        });
    }

    @FXML
    void userRejected(MouseEvent event) {
        String selectedUser = waitingList.getSelectionModel().getSelectedItem();
        try {
            whiteboardServer.rejectUser(selectedUser);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    void createRectangleShape(MouseEvent event) {
        rectangleButton.setDisable(true);
        circleButton.setDisable(false);
        ovalButton.setDisable(false);
        lineButton.setDisable(false);
        textButton.setDisable(false);
    }

    @FXML
    void createCircleShape(MouseEvent event) {
        lineButton.setDisable(false);
        rectangleButton.setDisable(false);
        circleButton.setDisable(true);
        ovalButton.setDisable(false);
        textButton.setDisable(false);
    }

    @FXML
    void createText(MouseEvent event) {
        lineButton.setDisable(false);
        rectangleButton.setDisable(false);
        circleButton.setDisable(false);
        ovalButton.setDisable(false);
        textButton.setDisable(true);
    }

    @FXML
    void createOvalShape(MouseEvent event) {
        rectangleButton.setDisable(false);
        circleButton.setDisable(false);
        ovalButton.setDisable(true);
        lineButton.setDisable(false);
        textButton.setDisable(false);
    }



    @FXML
    void eraserClicked(MouseEvent event) {
        eraserMode = true;
        isDrawing = false;
        eraserCanvas.getGraphicsContext2D().setGlobalBlendMode(BlendMode.SRC_ATOP);
        if (eraserMode) {
            canvas.toFront();
            graphicsContext.setStroke(Color.web("#F0F0F0"));
            graphicsContext.beginPath();
            graphicsContext.setLineWidth(20);

            canvas.setOnMousePressed(mouseEvent -> {
                if (eraserMode) {
                    graphicsContext.moveTo(mouseEvent.getX(), mouseEvent.getY());
                }
            });

            canvas.setOnMouseDragged(mouseEvent -> {
                if (eraserMode) {
                    graphicsContext.lineTo(mouseEvent.getX(), mouseEvent.getY());
                    graphicsContext.stroke();
                }
            });

            canvas.setOnMouseReleased(mouseEvent -> {
                if (eraserMode) {
                    graphicsContext.lineTo(mouseEvent.getX(), mouseEvent.getY());
                    graphicsContext.stroke();

                    try {
                        whiteboardServer.receiveEraserData(captureCanvas(), clientImpl);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else {
            canvas.setOnMousePressed(null);
            canvas.setOnMouseDragged(null);
            canvas.setOnMouseReleased(null);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        graphicsContext = canvas.getGraphicsContext2D();
        canvas.toFront();
        tabPane.getTabs().remove(waitingTab);
        newButton.setDisable(true);
        openButton.setDisable(true);
        waitingScreen.setVisible(false);
        rectangleButton.setOnDragDetected(dragEvent -> {
            Dragboard dragboard = rectangleButton.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("rectangleButton");
            dragboard.setContent(content);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage dragImage = rectangleButton.snapshot(parameters, null);
            dragboard.setDragView(dragImage);

            dragEvent.consume();
        });

        circleButton.setOnDragDetected(dragEvent -> {
            Dragboard dragboard = circleButton.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("circleButton");
            dragboard.setContent(content);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage dragImage = circleButton.snapshot(parameters, null);
            dragboard.setDragView(dragImage);

            dragEvent.consume();
        });

        ovalButton.setOnDragDetected(dragEvent -> {
            Dragboard dragboard = ovalButton.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("ovalButton");
            dragboard.setContent(content);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage dragImage = ovalButton.snapshot(parameters, null);
            dragboard.setDragView(dragImage);

            dragEvent.consume();
        });

        textButton.setOnDragDetected(dragEvent -> {
            Dragboard dragboard = textButton.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("textButton");
            dragboard.setContent(content);

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            WritableImage dragImage = textButton.snapshot(parameters, null);
            dragboard.setDragView(dragImage);

            dragEvent.consume();
        });

        realWhiteBoard.setOnDragOver(dragEvent -> {
            if (dragEvent.getGestureSource() != realWhiteBoard && dragEvent.getDragboard().hasString()) {
                dragEvent.acceptTransferModes(TransferMode.MOVE);
            }
            dragEvent.consume();
        });

        realWhiteBoard.setOnDragDropped(dragEvent -> {
            Dragboard dragboard = dragEvent.getDragboard();
            boolean success = false;

            if (dragboard.hasString()) {
                if (dragboard.getString().equals("rectangleButton")) {
                    Rectangle newRectangle = new Rectangle(100, 50);
                    newRectangle.setFill(colorPicker.getValue());
                    newRectangle.setX(dragEvent.getX() - newRectangle.getWidth() / 2);
                    newRectangle.setY(dragEvent.getY() - newRectangle.getHeight() / 2);
                    realWhiteBoard.getChildren().add(newRectangle);
                    newRectangle.toFront();

                    try {
                        whiteboardServer.receiveImageData(captureCanvas(), clientImpl);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }

                    success = true;
                } else if (dragboard.getString().equals("circleButton")) {
                    Circle newCircle = new Circle(50);
                    newCircle.setFill(colorPicker.getValue());
                    newCircle.setCenterX(dragEvent.getX());
                    newCircle.setCenterY(dragEvent.getY());
                    realWhiteBoard.getChildren().add(newCircle);
                    newCircle.toFront();

                    try {
                        whiteboardServer.receiveImageData(captureCanvas(), clientImpl);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }

                    success = true;
                }  else if (dragboard.getString().equals("ovalButton")) {
                    Ellipse newEllipse = new Ellipse();
                    newEllipse.setRadiusX(50);
                    newEllipse.setRadiusY(30);
                    newEllipse.setFill(colorPicker.getValue());
                    newEllipse.setCenterX(dragEvent.getX());
                    newEllipse.setCenterY(dragEvent.getY());
                    realWhiteBoard.getChildren().add(newEllipse);
                    newEllipse.toFront();

                    try {
                        whiteboardServer.receiveImageData(captureCanvas(), clientImpl);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    success = true;
                } else if (dragboard.getString().equals("textButton")) {
                    TextField textField = new TextField();
                    textField.setLayoutX(dragEvent.getX());
                    textField.setLayoutY(dragEvent.getY());
                    realWhiteBoard.getChildren().add(textField);
                    textField.toFront();

                    try {
                        whiteboardServer.receiveImageData(captureCanvas(), clientImpl);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    success = true;
                }
            }

            dragEvent.setDropCompleted(success);
            dragEvent.consume();
        });
    }

    @FXML
    void enableDrawing(ActionEvent event) {
        isDrawing = true;
        eraserMode = false;
        canvas.toFront();
        if (isDrawing) {
            graphicsContext.setStroke(colorPicker.getValue());
            graphicsContext.beginPath();
            graphicsContext.setLineWidth(2);

            canvas.setOnMousePressed(mouseEvent -> {
                if (isDrawing) {
                    graphicsContext.moveTo(mouseEvent.getX(), mouseEvent.getY());
                }
            });

            canvas.setOnMouseDragged(mouseEvent -> {
                if (isDrawing) {
                    graphicsContext.lineTo(mouseEvent.getX(), mouseEvent.getY());
                    graphicsContext.stroke();
                }
            });

            canvas.setOnMouseReleased(mouseEvent -> {
                if (isDrawing) {
                    graphicsContext.lineTo(mouseEvent.getX(), mouseEvent.getY());
                    graphicsContext.stroke();

                    try {
                        whiteboardServer.receiveEraserData(captureCanvas(), clientImpl);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else {
            canvas.setOnMousePressed(null);
            canvas.setOnMouseDragged(null);
            canvas.setOnMouseReleased(null);
        }
    }

    public byte[] captureCanvas() {
        Bounds bounds = realWhiteBoard.getBoundsInLocal();
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);

        WritableImage snapshot = new WritableImage((int) bounds.getWidth(), (int) bounds.getHeight());
        realWhiteBoard.snapshot(parameters, snapshot);

        return convertImageToBytes(snapshot, "png");
    }

    private byte[] convertImageToBytes(Image image, String format) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ImageIO.write(bufferedImage, format, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert image to bytes", e);
        }
    }

    public void updateWhiteboard(byte[] imageData) {
        Platform.runLater(() -> {
            Image receivedImage = deserializeImageData(imageData);

            replaceWhiteboardContent(receivedImage);
        });
    }

    public void updateErasedWhiteboard(byte[] imageData) {
        Platform.runLater(() -> {
            Image receivedImage = deserializeImageData(imageData);

            replaceErasedWhiteboardContent(receivedImage);
        });
    }

    private void replaceErasedWhiteboardContent(Image image) {
        Platform.runLater(() -> {

            ImageView imageView = new ImageView(image);

            imageView.setFitWidth(realWhiteBoard.getWidth());
            imageView.setFitHeight(realWhiteBoard.getHeight());

            realWhiteBoard.getChildren().add(imageView);
            canvas.toFront();
            imageView.toFront();

        });
    }

    private Image deserializeImageData(byte[] imageData) {

        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
            BufferedImage bufferedImage = ImageIO.read(bais);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private void replaceWhiteboardContent(Image image) {
        Platform.runLater(() -> {

            ImageView imageView = new ImageView(image);

            imageView.setFitWidth(realWhiteBoard.getWidth());
            imageView.setFitHeight(realWhiteBoard.getHeight());

            realWhiteBoard.getChildren().add(imageView);

            imageView.toFront();
        });
    }

    public void addMessage(String message, String username){
        Platform.runLater(() -> {
            chatWindow.getItems().add(username + " : " + message);
        });
    }

    @FXML
    void kickUser(MouseEvent event) {
        String selectedUser = userList.getSelectionModel().getSelectedItem();
        try {
            whiteboardServer.kickUser(selectedUser);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void sendChat(MouseEvent event) throws RemoteException {
        String message = chatTextField.getText();
        String username = clientUsername.getText();
        whiteboardServer.receiveMessage(message, username);
        chatTextField.setText("");
    }
}