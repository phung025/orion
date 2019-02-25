/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructures;

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
public class SlideTest {

    public SlideTest() {
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
     * Test of add method, of class Slide.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        DataPoint p = new DataPoint(0, null);
        Slide instance = new Slide(2);

        instance.add(p);
        assertTrue(instance.size() == 1);

        instance.add(p);
        assertTrue(instance.size() == 2);

        instance.add(p);
        assertTrue(instance.size() == 2);
    }

    /**
     * Test of peek method, of class Slide.
     */
    @Test
    public void testPeek() {
        System.out.println("peek");

        DataPoint expResult = new DataPoint(1, null);

        Slide instance = new Slide(2);
        instance.add(expResult);

        DataPoint result = instance.newest();
        assertEquals(expResult, result);
    }

    /**
     * Test of isFull method, of class Slide.
     */
    @Test
    public void testIsFull() {
        System.out.println("isFull");
        DataPoint p = new DataPoint(0, null);
        Slide instance = new Slide(2);

        instance.add(p);
        assertTrue(instance.size() == 1);

        instance.add(p);
        assertTrue(instance.size() == 2);

        instance.add(p);
        assertTrue(instance.size() == 2);

        assertTrue(instance.isFull());
        assertTrue(instance.size() == 2);
    }

}
