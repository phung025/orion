/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStructures.DataPoint;
import dataStructures.Slide;
import fileIO.FileReader;
import dataStructures.Stream;
import java.util.List;
import math.Statistics;
import org.jblas.DoubleMatrix;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Nam Phung
 */
public class StreamDensityTest {

    public StreamDensityTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of estimateStreamDensity method, of class StreamDensity.
     */
    @Test
    public void testEstimateStreamDensity() {
        System.out.println("estimateStreamDensity");

        String filePath = System.getProperty("user.dir") + "/datasets/random2.csv";
        char separator = ',';
        boolean hasHeader = false;
        double[][] incomingData = FileReader.readCSV(filePath, separator, hasHeader);

        // Data stream
        Stream stream = new Stream(false); // Create a count-based timestamp stream
        stream.writeToStream(incomingData);
        Slide<DataPoint> slide = new Slide(10);
        int count = 0;
        int dimension = incomingData[0].length;

        StreamDensity instance = new StreamDensity(dimension, 20); // Stream density estimator
        boolean updated = false;
        DoubleMatrix pDimension = new DoubleMatrix(new double[]{1, 0}); // Projected dimension

        // Initialize original mean, variance, and covariance matrix
        DoubleMatrix currentMean = DoubleMatrix.zeros(dimension);
        DoubleMatrix currentCovariance = DoubleMatrix.zeros(dimension, dimension);

        while (!stream.isEmpty()) {

            DataPoint incomingPoint = stream.readFromStream();
            slide.add(incomingPoint);

            // Update the mean
            DoubleMatrix previousMean = currentMean;
            currentMean = Statistics.computeVectorMeanOnline(incomingPoint.getTimestamp() - 1, currentMean, incomingPoint.getValues());

            // Update the covariance matrix
            currentCovariance = Statistics.computeCovarianceMatrixOnline(
                    incomingPoint.getTimestamp() - 1,
                    currentCovariance,
                    previousMean,
                    incomingPoint.getValues());

            // Update parameters of the data density function and the forgetting factor lambda
            instance.updateDDFparameters(incomingPoint, currentMean, currentCovariance);

            if (slide.isFull() && !updated) { // Start update forgetting factor when the slide is full
                instance.updateForgettingFactor(slide);
                updated = true; // Indicate that forgetting factor has been updated
            } else if (updated) {
                
                // Estimate density by computing the standard deviation of all data points after they're
                // projected on the p-dimension
                double streamDensity = instance.estimateStreamDensity(incomingPoint, slide, pDimension, "uniform");
                
                // Estimate density by computing the standard deviation of all data points without projecting
                // them on the p-dimension
                //double streamDensity = instance.estimateStreamDensity(incomingPoint, slide, Math.sqrt(currentCovariance.sum()), pDimension, "uniform");
                System.out.println(count + " --- " + streamDensity);
            }
            count++;
        }
    }

    /**
     * Test of updateForgettingFactor method, of class StreamDensity.
     */
    @Test
    public void testUpdateForgettingFactor() {
        System.out.println("updateForgettingFactor");

        String filePath = System.getProperty("user.dir") + "\\datasets\\random2.csv";
        char separator = ',';
        boolean hasHeader = false;
        double[][] incomingData = FileReader.readCSV(filePath, separator, hasHeader);

        // Data stream
        Stream stream = new Stream(false); // Create a count-based timestamp stream
        stream.writeToStream(incomingData);

        List<DataPoint> allDataPoints = stream.readFromStream(200);
        StreamDensity instance = new StreamDensity(incomingData[0].length, 5.4);
        instance.updateForgettingFactor(allDataPoints);
    }
}
