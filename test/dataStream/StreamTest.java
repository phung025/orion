/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStream;

import java.util.LinkedList;
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
public class StreamTest {

    public StreamTest() {
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
     * Test of writeToStream method, of class Stream.
     */
    @Test
    public void testWriteToStream() {
        System.out.println("writeToStream");
        String filePath = System.getProperty("user.dir") + "\\datasets\\tao.txt";
        char separator = ',';
        boolean hasHeader = false;
        double[][] incomingData = FileReader.readCSV(filePath, separator, hasHeader);
        Stream instance = new Stream(true);
        instance.writeToStream(incomingData);
    }

    /**
     * Test of readFromStream method, of class Stream.
     */
    @Test
    public void testReadFromStream() {
        System.out.println("readFromStream");

        String filePath = System.getProperty("user.dir") + "\\datasets\\tao.txt";
        char separator = ',';
        boolean hasHeader = false;
        double[][] incomingData = FileReader.readCSV(filePath, separator, hasHeader);

        Stream instance = new Stream(false); // Create a count-based timestamp stream
        instance.writeToStream(incomingData);
        int windowSize = 50000;
        LinkedList<DataPoint> result = instance.readFromStream(windowSize);
        assertTrue(result.size() == 50000);

        instance = new Stream(true); // Create a time-based timestamp stream
        instance.writeToStream(incomingData);
        windowSize = 2;
        // Read from the streams the data points that were created in the 2 milliseconds interval
        result = instance.readFromStream(windowSize);
    }
}
