/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Nam Phung
 */
public class FileReaderTest {

    public FileReaderTest() {
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
     * Test of readCSV method, of class FileReader.
     */
    @Test
    public void testReadCSV() {
        System.out.println("readCSV");

        String filePath = System.getProperty("user.dir") + "\\datasets\\tao.txt";
        char separator = ',';
        boolean hasHeader = false;
        double[][] result = FileReader.readCSV(filePath, separator, hasHeader);

        Assert.assertTrue(result.length == 575468);
        Assert.assertTrue(result[0].length == 3);
    }
}
