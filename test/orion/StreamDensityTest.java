/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStream.DataPoint;
import dataStream.FileReader;
import dataStructures.Stream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jblas.DoubleMatrix;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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

        String filePath = System.getProperty("user.dir") + "\\datasets\\random.csv";
        char separator = ',';
        boolean hasHeader = false;
        double[][] incomingData = FileReader.readCSV(filePath, separator, hasHeader);

        // Data stream
        Stream stream = new Stream(false); // Create a count-based timestamp stream
        stream.writeToStream(incomingData);
        LinkedList<DataPoint> window = new LinkedList();

        StreamDensity instance = new StreamDensity(2, 5.4); // Stream density estimator
        DoubleMatrix pDimension = new DoubleMatrix(new double[]{0, 1}); // Projected dimension

        while (!stream.isEmpty()) {

            DataPoint incomingPoint = stream.readFromStream();
            window.add(incomingPoint);

            double streamDensity = instance.estimateStreamDensity(incomingPoint, window, pDimension, "uniform");

            System.out.println(window.size() - 1 + " --- " + streamDensity);
        }
    }

    /**
     * Test of updateDDFparameters method, of class StreamDensity.
     */
    @Test
    public void testUpdateDDFparameters() {
        System.out.println("updateDDFparameters");
//        DataPoint dt = null;
//        DoubleMatrix currentMean = null;
//        DoubleMatrix currentCovariance = null;
//        StreamDensity instance = null;
//        instance.updateDDFparameters(dt, currentMean, currentCovariance);
    }

    /**
     * Test of updateForgettingFactor method, of class StreamDensity.
     */
    @Test
    public void testUpdateForgettingFactor() {
//        System.out.println("updateForgettingFactor");
//        List<DataPoint> allDataPoints = null;
//        StreamDensity instance = null;
//        instance.updateForgettingFactor(allDataPoints);
    }

}
