/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStructures.DataPoint;
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
    private int k = 0; // Number of neighbors k
    private double r = 0.0; // User-defined distance r

    // Stream density estimator
    private StreamDensity SDEstimator = null;

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
     * @param h the initialization threshold used for the first stage of the
     * algorithm
     * @param k minimum amount of neighbors for a data point to not be an
     * outlier.
     * @param r radius distance r of a data point. If data point has fewer than
     * k neighbors within the distance r, it is potentially an outlier.
     *
     */
    public CBOrion(int S, int h, int k, double r) {

        // Assign all variables
        this.slide = new Slide(S); // Data slide of size S
        this.initializationThreshold = h; // Initialization threshold for first stage of algorithm
        this.k = k; // K-neighbor of a data point
        this.r = r; // Maximum distance r of a data point
    }

    /**
     *
     * @param dt
     */
    private void initialize(DataPoint dt) {

        // Initialize the default mean, covariance matrix, etc when the first
        // data point arrives
        if (this.currentMean == null) {

            // Get the data point dimension
            int dimension = dt.getValues().data.length;

            // Initialize original mean
            this.currentMean = DoubleMatrix.zeros(dimension);

            // Initialize original covariance matrix
            this.currentCovariance = DoubleMatrix.zeros(dimension, dimension);

            // Initialize the stream density estimator
            SDEstimator = new StreamDensity(dimension, this.r);
        }

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

        // Learn the forgetting factor λ once Orion has received enough data points
        if (this.initializationThreshold == 0) {
            SDEstimator.updateForgettingFactor(slide);
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

        // Learn parameters for Data Density Function
        SDEstimator.updateDDFparameters(dt, currentMean, currentCovariance);

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

}
