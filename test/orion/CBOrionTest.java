/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStructures.DataPoint;
import dataStructures.Stream;
import fileIO.FileReader;
import java.util.Collections;
import math.Statistics;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Nam Phung
 */
public class CBOrionTest {

    public CBOrionTest() {
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
     * Test of detectOutlier method, of class CBOrion.
     */
    @Test
    public void testDetectOutlier() {
        System.out.println("detectOutlier");
        int[] w_size = new int[]{20, 50, 100, 150, 200, 250, 300};

        for (int size : w_size) {
            String filePath = System.getProperty("user.dir") + "/datasets/random.csv";
            char separator = ',';
            boolean hasHeader = false;
            double[][] incomingData = FileReader.readCSV(filePath, separator, hasHeader);

            // Data stream
            Stream stream = new Stream(false); // Create a count-based timestamp stream
            stream.writeToStream(incomingData);
            int count = 0;
            double meanExecutionTime = 0;

            CBOrion instance = new CBOrion(size, 100, 20, 50);
            while (!stream.isEmpty()) {
                long startTime = System.nanoTime();
                DataPoint dt = stream.readFromStream();
                boolean result = instance.detectOutlier(++count, dt);
                long endTime = System.nanoTime();

                // Update the mean execution time
                double executionTime = (endTime - startTime) / 1000000;
                meanExecutionTime = Statistics.computeMeanOnline(count - 1, meanExecutionTime, executionTime);

                // Output mean execution time
//            System.out.println("Mean execution time: " + meanExecutionTime + " ms");
            }
            System.out.println("Avg time " + Stats.avgEfficientESD);
        }
    }
}
