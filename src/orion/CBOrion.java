/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStructures.DataPoint;
import dataStructures.Dimension;
import dataStructures.Slide;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import math.Statistics;
import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

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
    private List<Dimension> A_out = new LinkedList();
    private List<Dimension> A_in = new LinkedList();

    private EvolutionaryComputation ea = null;

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

        // Compute the initial set of p-dimensions (population set)
        // The set of candidate p-dimenion has size of at least 10
        List<Dimension> dimensions = new LinkedList();
        DoubleMatrix eigen = Eigen.eigenvectors(this.currentCovariance)[0].getReal();
        for (int i = 0; i < eigen.getRows(); ++i) {
            DoubleMatrix p = eigen.getRow(i).transpose();
            List<Double> projected = slide.parallelStream().map(k -> SDEstimator.projectOnDimension(k, p)).collect(Collectors.toList());
            Dimension candidate = new Dimension(p, Statistics.computeMean(projected), Statistics.computeVariance(projected));
            dimensions.add(candidate);
        }
        for (int i = 0; i < 10 - eigen.getRows(); ++i) {
            DoubleMatrix p = DoubleMatrix.rand(dimension);
            List<Double> projected = slide.parallelStream().map(k -> SDEstimator.projectOnDimension(k, p)).collect(Collectors.toList());
            Dimension candidate = new Dimension(p, Statistics.computeMean(projected), Statistics.computeVariance(projected));
            dimensions.add(candidate);
        }

        // Randomly partition the set of dimensions into 2 subset A_in and A_out
        Collections.shuffle(dimensions);
        int cutoff = (dimensions.size() / 2);
        Iterator<Dimension> iter = dimensions.iterator();
        while (iter.hasNext()) {
            if (cutoff > 0) {
                A_in.add(iter.next());
            } else {
                A_out.add(iter.next());
            }
            --cutoff;
        }

        // Initialize the evolutionary computation object
        ea = new EvolutionaryComputation(SDEstimator);
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

            // Revert the mean absolute normalized deviation (meanAD) to when the oldest point is removed from
            // First, get the absolute normalized deviation (AD) of the oldest point, then exclude
            // the contribution of the AD of the oldest point from the meanAD
            double oldestAD = Statistics.computeAbsoluteNormalizedDevitation(
                    oldestPoint.getValues(),
                    currentMean,
                    currentCovariance);
            double meanAD = this.meanAbsoluteNormalizedDeviation;
            this.meanAbsoluteNormalizedDeviation = Statistics.revertMean(oldestAD, meanAD, slide.size());

            // New mean after the oldest point removed from the slide
            this.currentMean = Statistics.revertVectorMean(oldestPoint.getValues(), currentMean, slide.size());

            // Revert the covariance matrix to not include the oldest point
            this.currentCovariance = Statistics.revertCovarianceMatrix(oldestPoint.getValues(), currentCovariance, currentMean, slide.size());

            // Revert the mean and variance of the projected dimensions and then update the variance and mean
            // when new data point comes in
            for (int i = 0; i < A_in.size(); ++i) {
                Dimension p = A_in.get(i);
                double oldMean = p.mean;
                p.mean = Statistics.revertMean(SDEstimator.projectOnDimension(oldestPoint, p.values), p.mean, slide.size());
                p.variance = Statistics.revertVariance(
                        SDEstimator.projectOnDimension(oldestPoint, p.values),
                        p.variance,
                        oldMean,
                        slide.size(),
                        p.mean);

                oldMean = p.mean;
                p.mean = Statistics.computeMeanOnline(slide.size() - 1, p.mean, SDEstimator.projectOnDimension(oldestPoint, p.values));
                p.variance = Statistics.computeVarianeOnline2(slide.size() - 1, oldMean, p.variance, p.mean, SDEstimator.projectOnDimension(oldestPoint, p.values));
            }
            for (int i = 0; i < A_out.size(); ++i) {
                Dimension p = A_out.get(i);
                double oldMean = p.mean;
                p.mean = Statistics.revertMean(SDEstimator.projectOnDimension(oldestPoint, p.values), p.mean, slide.size());
                p.variance = Statistics.revertVariance(
                        SDEstimator.projectOnDimension(oldestPoint, p.values),
                        p.variance,
                        oldMean,
                        slide.size(),
                        p.mean);

                oldMean = p.mean;
                p.mean = Statistics.computeMeanOnline(slide.size() - 1, p.mean, SDEstimator.projectOnDimension(oldestPoint, p.values));
                p.variance = Statistics.computeVarianeOnline2(slide.size() - 1, oldMean, p.variance, p.mean, SDEstimator.projectOnDimension(oldestPoint, p.values));
            }
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
        // Perform evolutionary computation to find the p-dimension for the given data point dt
        Dimension pDimension = null;
        double pDimensionDensity = 0.0;
        if (absoluteNormalizedDeviation > this.meanAbsoluteNormalizedDeviation) {
            Object[] evolved = ea.evolve(A_out, dt, slide, 5);
            pDimension = (Dimension) evolved[0];
            pDimensionDensity = (double) evolved[1];
            A_out = (List<Dimension>) evolved[2];
        } else {
            Object[] evolved = ea.evolve(A_in, dt, slide, 5);
            pDimension = (Dimension) evolved[0];
            pDimensionDensity = (double) evolved[1];
            A_in = (List<Dimension>) evolved[2];
        }
        
        // Update the mean absolute normalized deviation
        this.meanAbsoluteNormalizedDeviation = Statistics.computeMeanOnline(
                slide.size() - 1,
                this.meanAbsoluteNormalizedDeviation,
                absoluteNormalizedDeviation);

        return false;
    }

}
