/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import data_stream.DataPoint;
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
    private int initializationThreshold = 500;
    private int k = 0; // Number of neighbors k
    private double r = 0.0; // User-defined distance r
    
    // Statistical data for helping choosing set of p-dimensions
    private DoubleMatrix currentMean = null;
    private DoubleMatrix currentCovariance = null;
    private double meanAbsoluteNormalizedDeviation = 0.0;

    // Partitions of p-dimension used for genetic algorithm and outlier detection
    private final List<DoubleMatrix> a_out = new LinkedList();
    private final List<DoubleMatrix> a_in = new LinkedList();

    private CBOrion() {

    }

    /**
     *
     * @param k
     * @param r
     * @param initThreshold
     */
    public CBOrion(int k, int r, int initThreshold) {
        this.k = k;
        this.r = r;
        this.initializationThreshold = initThreshold;
    }

    private void initialize(DataPoint dt) {
        List<DoubleMatrix> vectors = new LinkedList();
        vectors.add(dt.getValues());

        // Initialize the mean
        currentMean = Statistics.computeVectorMean(vectors);

        // Initialize the covariance matrix
        currentCovariance = Statistics.computeCovarianceMatrix(vectors);

        // Initialize the mean absolute deviation
        meanAbsoluteNormalizedDeviation = Statistics.computeMeanOnline(
                dt.getTimestamp() - 1,
                meanAbsoluteNormalizedDeviation,
                Statistics.computeAbsoluteNormalizedDevitation(dt.getValues(), currentMean, currentCovariance));

        // Learn parameters for Data Density Function
        // Learn the forgetting factor λ
        // Finish first stage: initialization
        --initializationThreshold;
    }

    public boolean detectOutlier(DataPoint dt) {

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
        if (absoluteNormalizedDeviation > this.meanAbsoluteNormalizedDeviation) {

        } else {

        }

        // Update the mean absolute normalized deviation
        this.meanAbsoluteNormalizedDeviation = Statistics.computeMeanOnline(
                dt.getTimestamp() - 1,
                this.meanAbsoluteNormalizedDeviation,
                absoluteNormalizedDeviation);

        return false;
    }
}
