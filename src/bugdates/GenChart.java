/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * This source code is provided to illustrate the usage of a given feature
 * or technique and has been deliberately simplified. Additional steps
 * required for a production-quality application, such as security checks,
 * input validation, and proper error handling, might not be present in
 * this sample code.
 */

package bugdates;

import java.io.IOException;
import java.time.LocalDate;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Main program to generate a JavaFX chart from the bug data.
 * 
 * Note: the horizontal axis of the chart is the date, and the
 * vertical axis is the bug count. The horizontal axis is not
 * labeled properly. More effort could be put in here to label
 * the horizontal axis properly.
 */
public class GenChart extends Application {
    @Override public void start(Stage stage) {
        stage.setTitle("JIRA Bug History");
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Number of Month");
        //creating the chart
        @SuppressWarnings({"rawtypes", "unchecked"})
        final LineChart<Long,Long> lineChart =
        	(LineChart<Long,Long>) new LineChart(xAxis,yAxis);
                
        lineChart.setTitle("JIRA Bug History");
        lineChart.setCreateSymbols(false);
        
        //defining a series
        XYChart.Series<Long,Long> created = new XYChart.Series<>();
        created.setName("Created");
        
        XYChart.Series<Long,Long> resolved = new XYChart.Series<>();
        resolved.setName("Resolved");
        
        XYChart.Series<Long,Long> net = new XYChart.Series<>();
        net.setName("Net");
        
        LocalDate firstDate = LocalDate.parse("2005-08-10");
        
        try {
            BugDates.generate((curdate, ncre, nres, nnet) -> {
                long ddays = curdate.toEpochDay() - firstDate.toEpochDay();
                created.getData().add(new XYChart.Data<>(ddays, ncre));
                resolved.getData().add(new XYChart.Data<>(ddays, nres));
                net.getData().add(new XYChart.Data<>(ddays, nnet));
            });
        } catch (IOException ioe) { }
        
        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(created);
        lineChart.getData().add(resolved);
        lineChart.getData().add(net);
       
        stage.setScene(scene);
        stage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}
