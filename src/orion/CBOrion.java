/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStream.DataPoint;
import dataStructures.Slide;
import java.util.LinkedList;
import java.util.List;
import math.Statistics;
import org.jblas.DoubleMatrix;

/**
 * Class definition and implementation of the Orion algorithm using a
 * count-based method
 *
 * @author Nam Phung
 */
public class CBOrion {

    // Class attributes used for setting up the Orion algorithm
    private int initializationThreshold = 0;
    private int d = 0;
    private int k = 0; // Number of neighbors k
    private double r = 0.0; // User-defined distance r
    
    // Parameters for data density function
    private double forgettingFactor = 0.0;
    private double binWidth = 0.0;
    private double c = 0.0; // A non-zero constant c for computing vector a
    private DoubleMatrix a = null; // Vector a for computing the bin width. The vector has same dimension as an incoming data point

    // Statistical variables for helping choosing set of p-dimensions
    private DoubleMatrix currentMean = null;
    private DoubleMatrix currentCovariance = null;
    private double meanAbsoluteNormalizedDeviation = 0.0;

    // Partitions of p-dimension used by genetic algorithm and outlier detection
    private final List<DoubleMatrix> A_out = new LinkedList();
    private final List<DoubleMatrix> A_in = new LinkedList();

    // The slide containing all active data points, every data points not
    // in this slide are considered expired & shall not be used to determine
    // outlierness of an incoming data point
    private Slide<DataPoint> slide = null;

    private CBOrion() {

    }

    /**
     * Parameterized constructor for constructing the count-based orion
     * algorithm. The constructor takes in a slide size, the number of neighbors
     * k, user-defined distance r to detect outlier, and initialization
     * threshold h for the initialization stage. The threshold indicates the
     * first h data points that will be used for the initialization stage. These
     * first h points will not be detected as outliers. Any incoming data points
     * (h+1), (h+2), ... will be computed to reveal if they are outliers or not.
     *
     * @param S slide size.
     * @param d dimension of the data (i.e. The dimension of an incoming data
     * point)
     * @param k minimum amount of neighbors for a data point to not be an
     * outlier.
     * @param r radius distance r of a data point. If data point has fewer than
     * k neighbors within the distance r, it is potentially an outlier.
     * @param h the initialization threshold used for the first stage of the
     * algorithm
     */
    public CBOrion(int S, int d, int k, int r, int h) {

        // Assign all variables
        this.slide = new Slide(S);
        this.d = d;
        this.k = k;
        this.r = r;
        this.initializationThreshold = h;

        // Initialize the default mean, covariance matrix, etc
        this.currentMean = DoubleMatrix.zeros(d);
        this.currentCovariance = DoubleMatrix.zeros(d, d);
        this.a = DoubleMatrix.zeros(d); // Vector a used for computing data density
    }

    /**
     *
     * @param dt
     */
    private void initialize(DataPoint dt) {

        // Decrease the threshold, once it reaches 0, start the auto regression
        // analysis to learn the forgetting factor
        --initializationThreshold;

        // Initialize the mean
        DoubleMatrix previousMean = currentMean;
        currentMean = Statistics.computeVectorMeanOnline(dt.getTimestamp() - 1, this.currentMean, dt.getValues());

        // Initialize the covariance matrix
        currentCovariance = Statistics.computeCovarianceMatrixOnline(
                dt.getTimestamp() - 1,
                currentCovariance,
                previousMean,
                currentMean,
                dt.getValues());

        // Initialize the mean absolute deviation
        meanAbsoluteNormalizedDeviation = Statistics.computeMeanOnline(
                dt.getTimestamp() - 1,
                meanAbsoluteNormalizedDeviation,
                Statistics.computeAbsoluteNormalizedDevitation(dt.getValues(), currentMean, currentCovariance));

        // Learn parameters for Data Density Function
        updateDDFparameters();
        
        // Learn the forgetting factor λ
        if (this.initializationThreshold == 0) {
            
        }
    }

    public boolean detectOutlier(DataPoint dt) {

        // Add incoming data point to the slide
        this.slide.add(dt);

        /**
         * INITIALIZATION STAGE
         */
        // For the first few rounds unti the threshold reaches 0, all the incoming
        // data points will be detected as non-outlier. While the threshold hasn't
        // reached 0, these incoming data points will be used for the initialization
        // stage. Once the threshold has reached 0, the second stage of the algorithm
        // will be executed, all data points come in after that will be checked to
        // reveal their outlierness.
        if (initializationThreshold > 0) {
            this.initialize(dt);
            return false;
        }

        /**
         * INCREMENTAL STAGE
         */
        // Update mean when data point dt arrives at time t
        DoubleMatrix previousMean = this.currentMean;
        this.currentMean = Statistics.computeVectorMeanOnline(
                dt.getTimestamp() - 1,
                this.currentMean,
                dt.getValues());

        // Update covariance matrix when data point arrives
        this.currentCovariance = Statistics.computeCovarianceMatrixOnline(
                dt.getTimestamp() - 1,
                this.currentCovariance,
                previousMean,
                this.currentMean,
                dt.getValues());

        // Compute the absolute normalized deviation to determine which partition
        // the algorithm will use to find the p-dimension
        double absoluteNormalizedDeviation = Statistics.computeAbsoluteNormalizedDevitation(
                dt.getValues(),
                this.currentMean,
                this.currentCovariance);

        // Select the best partition that can reveal the p-dimension for data point dt
        List<DoubleMatrix> A_t = null; // Candidate p-dimension list
        if (absoluteNormalizedDeviation > this.meanAbsoluteNormalizedDeviation) {
            A_t = A_out;
        } else {
            A_t = A_in;
        }

        // Update the mean absolute normalized deviation
        this.meanAbsoluteNormalizedDeviation = Statistics.computeMeanOnline(
                dt.getTimestamp() - 1,
                this.meanAbsoluteNormalizedDeviation,
                absoluteNormalizedDeviation);

        return false;
    }

    private void updateDDFparameters() {
        
    }
}
