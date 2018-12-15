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
     * algorithm. The h coefficient must be smaller than the S coefficient
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
    private void initialize(int dimension) {
        // Initialize the mean, covariance matrix, and the stream density estimator
        List<DoubleMatrix> allPoints = new LinkedList();
        this.slide.forEach((p) -> {
            allPoints.add(p.getValues());
        });
        this.currentMean = Statistics.computeVectorMean(allPoints);
        this.currentCovariance = Statistics.computeCovarianceMatrix(allPoints);
        SDEstimator = new StreamDensity(dimension, this.r);

        // Learn the forgetting factor λ once Orion has received enough data points
        SDEstimator.updateForgettingFactor(slide);
    }

    public boolean detectOutlier(DataPoint dt) {

        // Add incoming data point to the slide
        DataPoint oldestPoint = this.slide.peekFirst(); // The data point that will be removed if the slide is full
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
        if (initializationThreshold > 1) {
            // Decrease the threshold, once it reaches 0, start the initialization stage
            --initializationThreshold;
            return false;
        } else if (initializationThreshold == 1) {
            this.initialize(this.slide.element().getValues().data.length); // Start the initialization stage
            initializationThreshold = 0; // Finish the initialization stage
            return false;
        }

        /**
         * INCREMENTAL STAGE
         */
        // If the slide is not full, update the running mean and covariance matrix
        // normally. However, if the slide is full, before an incoming data point arrives
        // in the slide, the oldest data point will be removed from the slide, the mean and
        // covariance matrix will be revert back to the state where that oldest point has
        // not arrived at the slide
        if (this.slide.isFull()) {

            // New mean after the oldest point removed from the slide
            this.currentMean = this.currentMean.mul((slide.size() * 1.0) / (slide.size() * 1.0 - 1.0)).sub(oldestPoint.getValues().div(this.slide.size() - 1.0));

            // Compute the projected value vector of the data point being removed to compute the running covariance
            // after that point is removed from the slide
            DoubleMatrix lhs = currentCovariance.mul((slide.size() * 1.0) / (slide.size() - 1.0));
            DoubleMatrix rhs = oldestPoint.getValues().sub(this.currentMean).mmul(oldestPoint.getValues().sub(this.currentMean).transpose()).mul((1.0 * slide.size() - 1.0) / (slide.size())).div(slide.size() - 1);
            this.currentCovariance = lhs.sub(rhs);
        }

        // Update covariance matrix when data point arrives
        this.currentCovariance = Statistics.computeCovarianceMatrixOnline(
                slide.size() - 1,
                this.currentCovariance,
                this.currentMean,
                dt.getValues());

        // Update mean value when data point dt arrives at time t
        this.currentMean = Statistics.computeVectorMeanOnline(
                slide.size() - 1,
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
                slide.size() - 1,
                this.meanAbsoluteNormalizedDeviation,
                absoluteNormalizedDeviation);

        return false;
    }

}
