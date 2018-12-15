/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStructures.DataPoint;
import dataStructures.Stream;
import fileIO.FileReader;
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

        String filePath = System.getProperty("user.dir") + "\\datasets\\random4.csv";
        char separator = ',';
        boolean hasHeader = false;
        double[][] incomingData = FileReader.readCSV(filePath, separator, hasHeader);

        // Data stream
        Stream stream = new Stream(false); // Create a count-based timestamp stream
        stream.writeToStream(incomingData);
        int count = 0;

        CBOrion instance = new CBOrion(200, 100, 10, 30);
        while (!stream.isEmpty()) {
            DataPoint dt = stream.readFromStream();

            System.out.print("--" + count + "-- ");
            boolean result = instance.detectOutlier(dt);
            ++count;
        }

    }

}
