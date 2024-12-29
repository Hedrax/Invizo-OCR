package com.example.ocrdesktop.utils;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.List;

public class TextFieldBoundingBox {
    public StringProperty label = new SimpleStringProperty();

    //Points [X_min,Y_min][X_max,Y_max]
    public List<Double> points;
    public ENTRY_TYPE type;
    public ObservableList<String> possibilities;
    public RectangleBox drawnRectangle;
    private void initializeDrawnRectangle(){
        drawnRectangle = new RectangleBox(points.get(0), points.get(1));
    }
    private void setPointsListener(){
        drawnRectangle.x_start.addListener((it, old, newVal)->{points.set(0, (Double) newVal);});
        drawnRectangle.y_start.addListener((it, old, newVal)->{points.set(1, (Double) newVal);});
        drawnRectangle.x_end.addListener((it, old, newVal)->{points.set(2, (Double) newVal);});
        drawnRectangle.y_end.addListener((it, old, newVal)->{points.set(3, (Double) newVal);});
    }
    public TextFieldBoundingBox(String label,List<Double> points,ENTRY_TYPE type, ObservableList<String>possibilities){
        this.label.set(label);
        this.points = points;
        this.type = type;
        this.possibilities = possibilities;
        initializeDrawnRectangle();
        setPointsListener();
    }
    public static class RectangleBox extends Pane {
        Circle topLeft;
        Circle bottomRight;
        StringProperty label = new SimpleStringProperty();
        DoubleProperty x_start = new SimpleDoubleProperty();
        DoubleProperty y_start = new SimpleDoubleProperty();
        DoubleProperty x_end = new SimpleDoubleProperty();
        DoubleProperty y_end = new SimpleDoubleProperty();
        public String touchedHandle;
        double initDeltaX, initDeltaY = -1;
        public void bindLabel(StringProperty label){
            this.label.bind(label);}
        RectangleBox(double x, double y) {
            this.getStyleClass().add("annotation-rectangle");
            this.setLayoutX(x);
            this.setLayoutY(y);
            x_start.set(x);
            y_start.set(y);
            addResizeHandles();
            //drawing label
            // Create a Text
            Text text = new Text();

            this.getChildren().add(text);
            text.textProperty().bind(label);
            text.setStyle("-fx-font-size: 15px; -fx-fill: red;");
            text.setLayoutX(2); // Slight adjustment to position above and to the left
            text.setLayoutY(15); // Slight adjustment to position above the pane



            this.setOnMouseReleased(it->{initDeltaY = -1;initDeltaX = -1;set_coordinates();touchedHandle= null;});
            //updating the handles once anything changes
            this.widthProperty().addListener((it)->{updateHandles();});
            this.layoutBoundsProperty().addListener((it)->{updateHandles();});
        }

        // Optional: Add resize handles for rectangles
        private void addResizeHandles() {
            topLeft = createHandle(0, 0);
            bottomRight = createHandle(this.getWidth(), this.getHeight());

            this.getChildren().addAll(topLeft, bottomRight);
        }

        public void adjust_size(double x, double y){
            switch (touchedHandle) {
                case ("topLeft"):
                    this.setLayoutX(x);
                    this.setLayoutY(y);
                    this.setPrefWidth(x_end.get() - x);
                    this.setPrefHeight(y_end.get() - y);
                    break;
                case ("bottomRight"):
                    this.setPrefWidth(x - x_start.get());
                    this.setPrefHeight(y - y_start.get());
                    break;

                default:
                    throw new IllegalStateException("Invalid handle: " + touchedHandle);
            }
            double thresholdY = y_end.get() -10;
            double thresholdX = x_end.get() -10;
            if (getLayoutY() > thresholdY){ setLayoutY(thresholdY);setPrefHeight(10);}
            if (getLayoutX() > thresholdX) {setLayoutX(thresholdX);setPrefWidth(10);}
        }

        private void set_coordinates() {
            this.x_start.set(this.getLayoutX());
            this.y_start.set(this.getLayoutY());
            this.x_end.set(x_start.get() + getWidth());
            this.y_end.set(y_start.get() + getHeight());
        }

        private Circle createHandle(double x, double y) {
            Circle handle = new Circle(x, y, 3, Color.RED);
            handle.setStroke(Color.WHITE);
            handle.setStrokeWidth(1);
            return handle;
        }

        private void updateHandles() {
            bottomRight.setCenterX(getPrefWidth());
            bottomRight.setCenterY(getPrefHeight());
        }
        public boolean isTouchingPositioningHandles(double Xin, double Yin){
            double x = Xin - getLayoutX();
            double y = Yin - getLayoutY();
            if (topLeft.contains(x, y) ){
                touchedHandle = "topLeft";
                return true;
            }
            else if (bottomRight.contains(x, y) ){
                touchedHandle = "bottomRight";
                return true;
            }
            return false;
        }

        public boolean contains(double x, double y) {
            if (x > this.x_start.get() && x < this.x_end.get())
                return y > this.y_start.get() && y < this.y_end.get();
            return false;
        }
        public void adjustPosition(double x, double y) {
            if (initDeltaY == -1){
                initDeltaX = x - x_start.get();
                initDeltaY = y - y_start.get();
            }
            else {
                this.setLayoutX(x - initDeltaX);
                this.setLayoutY(y - initDeltaY);
            }
        }

        public void adjustRectangle(double x1, double y1, double x2, double y2) {
            // Calculate the top-left corner and width/height
            double x = Math.min(x1, x2);
            double y = Math.min(y1, y2);
            double width = Math.abs(x2 - x1);
            double height = Math.abs(y2 - y1);

            this.setLayoutX(x);
            this.setLayoutY(y);
            this.setPrefWidth(width);
            this.setPrefHeight(height);

            set_coordinates();
        }
    }
    @Override
    public String toString() {
        return label.get();
    }

    public enum ENTRY_TYPE{
        NUMBER,
        DATE,
        DEFINED_LABEL,
        MULTIPLE_LINE,
        SINGLE_LINE
    }
}
