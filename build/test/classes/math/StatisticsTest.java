/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

import java.util.Arrays;
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
public class StatisticsTest {

    public StatisticsTest() {
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
     * Test of computeMean method, of class Mean.
     */
    @Test
    public void testComputeMean() {
        System.out.println("computeMean");
        List<Double> values = new LinkedList();
        values.add(1.0);
        values.add(2.0);
        values.add(3.0);

        Double expResult = 2.0;
        Double result = Statistics.computeMean(values);
        assertTrue(expResult.equals(result));
    }

    /**
     * Test of computeMeanOnline method, of class Mean.
     */
    @Test
    public void testComputeMeanOnline() {
        System.out.println("computeMeanOnline");
        Integer previousCount = 2;
        Double previousMean = 1.5;
        Double nextValue = 3.0;
        Double expResult = 2.0;
        Double result = Statistics.computeMeanOnline(previousCount, previousMean, nextValue);
        assertEquals(expResult, result);
    }

    /**
     * Test of computeVectorMean method, of class Mean.
     */
    @Test
    public void testComputeVectorMean() {
        System.out.println("computeVectorMean");
        List<DoubleMatrix> vectors = new LinkedList();
        vectors.add(new DoubleMatrix(new double[]{0, 1, 1}));
        vectors.add(new DoubleMatrix(new double[]{2, 3, 2}));
        vectors.add(new DoubleMatrix(new double[]{1, 3, 2}));
        vectors.add(new DoubleMatrix(new double[]{4, 2, 2}));

        DoubleMatrix expResult = new DoubleMatrix(new double[]{7.0 / 4.0, 9.0 / 4.0, 7.0 / 4.0});
        DoubleMatrix result = Statistics.computeVectorMean(vectors);

        assertEquals(expResult, result);
    }

    /**
     * Test of computeVectorMeanOnline method, of class Mean.
     */
    @Test
    public void testComputeVectorMeanOnline() {
        System.out.println("computeVectorMeanOnline");
        Integer previousCount = 3;
        DoubleMatrix previousMean = new DoubleMatrix(new double[]{1.0, 7.0 / 3.0, 5.0 / 3.0});
        DoubleMatrix nextValue = new DoubleMatrix(new double[]{4, 2, 2});
        DoubleMatrix expResult = new DoubleMatrix(new double[]{7.0 / 4.0, 9.0 / 4.0, 7.0 / 4.0});;
        DoubleMatrix result = Statistics.computeVectorMeanOnline(previousCount, previousMean, nextValue);

        assertEquals(expResult, result);
    }

    /**
     * Test of computeCovarianceMatrix method, of class Statistics.
     */
    @Test
    public void testComputeCovarianceMatrix() {
        System.out.println("computeCovarianceMatrix");

        List<DoubleMatrix> vectors = new LinkedList();
        vectors.add(new DoubleMatrix(new double[]{90, 60, 90}));
        vectors.add(new DoubleMatrix(new double[]{90, 90, 30}));
        vectors.add(new DoubleMatrix(new double[]{60, 60, 60}));
        vectors.add(new DoubleMatrix(new double[]{60, 60, 90}));
        vectors.add(new DoubleMatrix(new double[]{30, 30, 30}));

        DoubleMatrix expResult = new DoubleMatrix(new double[][]{
            {504, 360, 180},
            {360, 360, 0},
            {180, 0, 720}
        });

        DoubleMatrix result = Statistics.computeCovarianceMatrix(vectors);
        assertEquals(expResult, result);
    }

    /**
     * Test of computeCovarianceMatrixOnline method, of class Statistics.
     */
    @Test
    public void testComputeCovarianceMatrixOnline() {
        System.out.println("computeCovarianceMatrixOnline");

        // Statistical computation before a data point arrives
        List<DoubleMatrix> vectors = new LinkedList();
        vectors.add(new DoubleMatrix(new double[]{90, 60, 90}));
        vectors.add(new DoubleMatrix(new double[]{90, 90, 30}));
        vectors.add(new DoubleMatrix(new double[]{60, 60, 60}));
        vectors.add(new DoubleMatrix(new double[]{60, 60, 90}));
        int prevCount = vectors.size();
        DoubleMatrix prevCov = Statistics.computeCovarianceMatrix(vectors);
        DoubleMatrix prevMean = Statistics.computeVectorMean(vectors);

        // A new point came in        
        DoubleMatrix nextPoint = new DoubleMatrix(new double[]{30, 30, 30});
        vectors.add(nextPoint);
        DoubleMatrix nextMean = Statistics.computeVectorMean(vectors);

        DoubleMatrix expResult = new DoubleMatrix(new double[][]{
            {504, 360, 180},
            {360, 360, 0},
            {180, 0, 720}
        });
        DoubleMatrix result = Statistics.computeCovarianceMatrixOnline(prevCount, prevCov, prevMean, nextMean, nextPoint);
        assertEquals(expResult, result);
    }

    /**
     * Test of computeAbsoluteNormalizedDevitation method, of class Statistics.
     */
    @Test
    public void testComputeAbsoluteNormalizedDevitation() {
        System.out.println("computeAbsoluteNormalizedDevitation");

        List<DoubleMatrix> vectors = new LinkedList();
        vectors.add(new DoubleMatrix(new double[]{90, 60, 90}));
        vectors.add(new DoubleMatrix(new double[]{90, 90, 30}));
        vectors.add(new DoubleMatrix(new double[]{60, 60, 60}));
        vectors.add(new DoubleMatrix(new double[]{60, 60, 90}));

        DoubleMatrix nextPoint = new DoubleMatrix(new double[]{30, 30, 30});
        vectors.add(nextPoint);

        DoubleMatrix nextMean = Statistics.computeVectorMean(vectors);
        DoubleMatrix nextCovariance = Statistics.computeCovarianceMatrix(vectors);
        double result = Statistics.computeAbsoluteNormalizedDevitation(nextPoint, nextMean, nextCovariance);
    }

    /**
     * Test of computeVariance method, of class Statistics.
     */
    @Test
    public void testComputeVariance() {
        System.out.println("computeVariance");
        List<Double> values = new LinkedList();
        values.add(17.0);
        values.add(15.0);
        values.add(23.0);
        values.add(7.0);
        values.add(9.0);
        values.add(13.0);
        
        double expResult = 33.2;
        double result = Statistics.computeVariance(values);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of computeVarianceOnline method, of class Statistics.
     */
    @Test
    public void testComputeVarianceOnline() {
        System.out.println("computeVarianceOnline");
        long previousCount = 5;
        double previousSum = 71;
        double previousSumSquared = 1173;
        double nextValue = 13;
        double expResult = 33.2;
        double result = Statistics.computeVarianceOnline(previousCount, previousSum, previousSumSquared, nextValue);
        assertEquals(expResult, result, 0.0);
    }
}
