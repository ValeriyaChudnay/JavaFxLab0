package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;


public class Controller {

    private final int[] mainArray = new int[100];
    private boolean isrunCount;
    private boolean firstTime = true;
    private final Random random = new Random();
    private Thread thread;
    private int times = 0;
    private volatile int c = 0;
    @FXML
    private Button but1;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Button but2;

    @FXML
    private TextField input;

    @FXML
    private BarChart<String, Number> chart;

    @FXML
    private ProgressBar progresBar;
    @FXML
    private Text errorText;
    @FXML
    private Button restart;
    @FXML
    private CheckBox slov;

    @FXML
    void restartClick(MouseEvent event) throws InterruptedException {
        Arrays.fill(mainArray, 0);
        but1.setDisable(false);
        but2.setDisable(false);
        but1.setText("Start");
        progresBar.setProgress(0);
        chart.getData().clear();
        yAxis.setUpperBound(0);
        firstTime = true;
        if (Objects.nonNull(thread)) {
            thread.interrupt();
        }
        times = 0;
        c=0;
    }

    @FXML
    void but1Cliked(MouseEvent event) {
        times = getInputNumber();
        if (times > 0) {
            if (but1.getText().equals("Start")) {
                but1.setText("Pause");
                isrunCount = true;
                if (firstTime) {
                    thread = new Thread(countNumber());
                    thread.start();
                    firstTime = false;
                }
            } else {
                buildChart();
                isrunCount = false;
                but1.setText("Start");
            }
        }
    }

    private void makeSlovly(int c) {
        try {
            if (c % 500.0 == 0) {
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Runnable countNumber() {
        return () -> {
            while (c < times) {
                synchronized (mainArray) {
                    if (isrunCount) {
                        int r = random.nextInt(100);
                        mainArray[r] = mainArray[r] + 1;
                        if (slov.isSelected()) {
                            makeSlovly(c);
                        }
                        progresBar.setProgress((float) c / times);
                        c++;
                    } else {
                        try {
                            mainArray.wait(1);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                }
            }
            Platform.runLater(() -> {
                buildChart();
                stop();
            });
        };
    }

    @FXML
    void onBut2Cliced(MouseEvent event) {
        Platform.runLater(() -> {
            buildChart();
            stop();
        });
    }

    private void buildChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < mainArray.length; i++) {
            series.getData().add(new XYChart.Data<>(String.valueOf(i + 1), (float) mainArray[i] / c));
        }
        chart.getData().clear();
        chart.getData().add(series);
    }


    private void stop() {
        progresBar.setProgress(1);
        but1.setText("Start");
        but1.setDisable(true);
        but2.setDisable(true);
        times = 0;
    }

    private int getInputNumber() {
        errorText.setText("");
        int t = 0;
        try {
            t = Integer.parseInt(input.getText());
        } catch (RuntimeException e) {
            errorText.setText("Only int values");
        }
        return t;
    }
}
