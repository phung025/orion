/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStructures.DataPoint;
import dataStructures.Stream;
import fileIO.FileReader;
import java.util.LinkedList;
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
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testDetectOutlier() throws InterruptedException, Exception {
        System.out.println("detectOutlier");
        int[] w_size = new int[]{500}; // window size
        int[] s_size = new int[]{250}; // slide size

        for (int windowSize : w_size) {
            for (int slideSize : s_size) {
                String filePath = System.getProperty("user.dir") + "/datasets/tao.csv";
                char separator = ',';
                boolean hasHeader = false;
                double[][] incomingData = FileReader.readCSV(filePath, separator, hasHeader);

                // Data stream
                Stream stream = new Stream(false); // Create a count-based timestamp stream
                stream.writeToStream(incomingData);
                int count = 0;
                double meanExecutionTime = 0;

                CBOrion instance = new CBOrion(windowSize, slideSize, 0.2, 50);
                while (!stream.isEmpty()) {
                    LinkedList<DataPoint> window = stream.readFromStream(windowSize);
                    LinkedList<Boolean> result = instance.detectOutliers(window);
                }
            }
        }
    }
}
