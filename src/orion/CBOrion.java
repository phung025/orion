/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import outlierMetrics.StreamDensity;
import clusteringEngine.CoClusterer;
import dataStructures.DataPoint;
import dataStructures.Dimension;
import dataStructures.Slide;
import evolutionaryEngine.EvolutionaryEngine;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import utils.Statistics;
import org.jblas.DoubleMatrix;
import org.jblas.Eigen;
import outlierMetrics.KIntegral;
import utils.Projector;

/**
 * Class definition and implementation of the Orion algorithm using a
 * count-based method
 *
 * @author Nam Phung
 */
public class CBOrion {

    // Class attributes used for setting up the Orion algorithm
    private double k = 0; // Percentage of neighbor k
    private double r = 0.0; // User-defined distance r
    private int W = 0; // Capacity of the window
    private int S = 0; // Capacity of the slide

    private int initializationThreshold = 0;
    private boolean isInitialized = false;

    // Stream density estimator and k-integral estimator computing
    // outlier metrics
    private StreamDensity sdEstimator = null;
    private KIntegral kIntegeral = null;

    // Statistical variables for helping choosing set of p-dimensions
    private DoubleMatrix currentMean = null;
    private DoubleMatrix currentCovariance = null;
    private double meanAbsoluteNormalizedDeviation = 0.0;

    // Partitions of p-dimension used by genetic algorithm and outlier detection
    private Dimension[] A_out = null;
    private Dimension[] A_in = null;

    private EvolutionaryEngine evolutionEngine = null;

    // The slide containing all active data points, every data points not
    // in this slide are considered expired & shall not be used to determine
    // outlierness of an incoming data point
    private Slide slide = null;
    private LinkedList<DataPoint> window = null;

    private CBOrion() {

    }

    /**
     * Parameterized constructor for constructing the count-based orion
     * algorithm.The constructor takes in a slide size, the number of neighbors
     * k, user-defined distance r to detect outlier, and initialization
     * threshold h for the initialization stage. The threshold indicates the
     * first h data points that will be used for the initialization stage. These
     * first h points will not be detected as outliers. Any incoming data points
     * (h+1), (h+2), ... will be computed to reveal if they are outliers or not.
     *
     * @param W window size.
     * @param S slide size.
     * @param k minimum amount of neighbors for a data point to not be an
     * outlier.
     * @param r radius distance r of a data point. If data point has fewer than
     * k neighbors within the distance r, it is potentially an outlier.
     *
     */
    public CBOrion(int W, int S, double k, double r) {

        // Slide size cannot be greater than the window size
        if (S > W) {
            throw new IllegalArgumentException("Slide size must not be greater than the window size.");
        }

        // K value must be in range (0,1)
        if ((k <= 0) || (k > 1)) {
            throw new IllegalArgumentException("k value must be in range (0, 1]");
        }

        // Assign all variables
        this.S = S; // Capacity of the slide
        this.W = W; // Capacity of the window

        this.slide = new Slide(this.S); // Data slide of size S
        this.window = new LinkedList<>(); // Window containing the incoming data points
        this.initializationThreshold = S; // Initialization threshold for first stage of algorithm

        this.k = k; // K-neighbor of a data point
        this.r = r; // Maximum distance r of a data point
    }

    /**
     *
     * @param dt
     */
    private void initialize(List<DataPoint> samples) {

        // Dimension of the data point
        int dimension = samples.get(0).getValues().data.length;

        // Initialize the mean, covariance matrix
        List<DoubleMatrix> allPoints = new LinkedList();
        samples.forEach((p) -> {
            allPoints.add(p.getValues());
        });
        this.currentMean = Statistics.computeVectorMean(allPoints);
        this.currentCovariance = Statistics.computeCovarianceMatrix(allPoints);

        // Initialize the stream density estimator
        this.sdEstimator = new StreamDensity("uniform", dimension, this.r, this.slide);

        // Initialize the k-integral estimator
        this.kIntegeral = new KIntegral(this.slide);

        // Learn the forgetting factor λ once Orion has received enough data points
        // THIS NEEDS TO BE CHECKED
        // this.sdEstimator.updateForgettingFactor(samples);
        // Compute the initial set of p-dimensions (population set)
        // The set of candidate p-dimenion has size of at least 10
        List<Dimension> dimensions = new LinkedList();
        DoubleMatrix eigen = Eigen.eigenvectors(this.currentCovariance)[0].getReal();
        for (int i = 0; i < eigen.getRows(); ++i) {
            DoubleMatrix p = eigen.getRow(i).transpose();
            List<Double> projected = samples.parallelStream().map(k -> Projector.projectOnDimension(k, p)).collect(Collectors.toList());
            Dimension candidate = new Dimension(p, Statistics.computeMean(projected), Statistics.computeVariance(projected));
            dimensions.add(candidate);
        }

        // Randomly partition the set of dimensions into 2 subset A_in and A_out
        // Create 2 linked list that holds each half of the dimensions lists
        LinkedList<Dimension> A_inList = new LinkedList<>();
        LinkedList<Dimension> A_outList = new LinkedList<>();
        Collections.shuffle(dimensions);
        int cutoff = (dimensions.size() / 2);
        for (Iterator<Dimension> iter = dimensions.iterator(); iter.hasNext();) {
            if (cutoff > 0) {
                A_inList.add(iter.next());
            } else {
                A_outList.add(iter.next());
            }
            --cutoff;
        }

        // Convert the linked list of dimensions into an array of dimensions
        A_in = new Dimension[A_inList.size()];
        A_in = A_inList.toArray(A_in);
        A_out = new Dimension[A_outList.size()];
        A_out = A_outList.toArray(A_out);

        // Initialize the evolutionary computation module
        this.evolutionEngine = new EvolutionaryEngine(sdEstimator, this.slide);
    }

    /**
     * Detect outliers within a window. Return ordered list of array containing
     * boolean variable that indicate if a data point is outlier or not and the
     * probability that the point is an outlier
     *
     * @param batch
     * @return
     * @throws Exception
     */
    public List<Object[]> detectOutliers(LinkedList<DataPoint> batch) throws Exception {

        // Reference to the current window
        this.window = batch;

        // INITIALIZATION STAGE
        // For the first few rounds unti the threshold reaches 0, all the incoming
        // data points will be detected as non-outlier. While the threshold hasn't
        // reached 0, these incoming data points will be used for the initialization
        // stage. Once the threshold has reached 0, the second stage of the algorithm
        // will be executed, all data points come in after that will be checked to
        // reveal their outlierness.
        if (!isInitialized) {
            this.initialize(this.window.subList(0, Math.min(this.initializationThreshold, this.window.size())));
            isInitialized = true; // Finish the initialization stage
        }

        // Compute stream density of all data points in the incoming window
        double[] allDensities = new double[this.window.size()];
        double[] allKIntegrals = new double[this.window.size()];
        {
            int i = 0;
            for (Iterator<DataPoint> iter = this.window.iterator(); iter.hasNext(); ++i) {
                double[] metrics = computeOutlierMetrics(iter.next());
                allDensities[i] = metrics[0];
                allKIntegrals[i] = metrics[1];
                //System.out.println("Density: " + allDensities[i] + "    k-integral: " + allKIntegrals[i]);
            }
        }

        // Perform clustering the data points based on stream density
        CoClusterer clusterer = new CoClusterer();
        Object[] sdCluster = clusterer.clusterDensity(allDensities);
        double[] sdClusterMean = (double[]) sdCluster[0];
        double[] sdClusterAssignments = (double[]) sdCluster[1];
        double[][] sdClusterDistribution = (double[][]) sdCluster[2];

        // Perform clustering the data points based on k-integral
        Object[] kIntegralCluster = clusterer.clusterKIntegral(allKIntegrals);
        double[] kIntegralClusterMean = (double[]) kIntegralCluster[0]; // Array contain the mean of the clusters
        double[] kIntegralClusterAssignments = (double[]) kIntegralCluster[1];
        double[][] kIntegralClusterDistribution = (double[][]) kIntegralCluster[2];

        // Find the cluster contains data points with low density
        int sdIdx = 0;
        double smallestMean = sdClusterMean[0];
        for (int i = 0; i < sdClusterMean.length; ++i) {
            smallestMean = Math.min(smallestMean, sdClusterMean[i]);
            if (sdClusterMean[i] == smallestMean) {
                sdIdx = i;
            }
        }

        // Find the cluster contains data points with high k-integral
        int kIdx = 0;
        double largestMean = kIntegralClusterMean[0];
        for (int i = 0; i < kIntegralClusterMean.length; ++i) {
            largestMean = Math.max(largestMean, kIntegralClusterMean[i]);
            if (kIntegralClusterMean[i] == largestMean) {
                kIdx = i;
            }
        }

        // Assign outliers based on cluster result
        List<Object[]> result = new LinkedList<>();
        for (int i = 0; i < sdClusterAssignments.length; ++i) {
            // Probability that the data point falls into the outlier cluster
            double probability = sdClusterDistribution[i][sdIdx] * kIntegralClusterDistribution[i][kIdx];
            if (((int) kIntegralClusterAssignments[i] == kIdx) && ((int) sdClusterAssignments[i] == sdIdx)) {
                result.add(new Object[]{true, probability});
            } else {
                result.add(new Object[]{false, probability});
            }
        }

        // Read the data points in the window sequentially
        return result;
    }

    /**
     *
     * @param dt
     * @return
     */
    public double[] computeOutlierMetrics(DataPoint dt) {

        // Add incoming data point to the slide
        DataPoint oldestPoint = this.slide.oldest(); // The data point that will be removed if the slide is full
        this.slide.add(dt);

        /**
         * INCREMENTAL STAGE
         */
        // If the slide is not full, update the running mean and covariance matrix
        // normally. However, if the slide is full, before an incoming data point arrives
        // in the slide, the oldest data point will be removed from the slide, the mean and
        // covariance matrix will be revert back to the state where that oldest point has
        // not arrived at the slide
//        if (this.slide.isFull()) {
//
//            // Revert the mean absolute normalized deviation (meanAD) to when the oldest point is removed from
//            // First, get the absolute normalized deviation (AD) of the oldest point, then exclude
//            // the contribution of the AD of the oldest point from the meanAD
//            double oldestAD = Statistics.computeAbsoluteNormalizedDevitation(
//                    oldestPoint.getValues(),
//                    currentMean,
//                    currentCovariance);
//            this.meanAbsoluteNormalizedDeviation = Statistics.revertMean(oldestAD, this.meanAbsoluteNormalizedDeviation, slide.size());
//
//            // New mean after the oldest point removed from the slide
//            this.currentMean = Statistics.revertVectorMean(oldestPoint.getValues(), currentMean, slide.size());
//
//            // Revert the covariance matrix to exclude the oldest point
//            this.currentCovariance = Statistics.revertCovarianceMatrix(oldestPoint.getValues(), currentCovariance, currentMean, slide.size());
//
//            // Revert the mean and variance of the projected dimensions and then update the variance and mean
//            // when new data point comes in
//            for (int k = 0; k < 2; ++k) {
//                Dimension[] partition = (k == 0) ? A_in : A_out;
//                for (int i = 0; i < partition.length; ++i) {
//                    Dimension p = partition[i];
//
//                    // Revert
//                    p.setMean(Statistics.revertMean(Projector.projectOnDimension(oldestPoint, p.getValues()), p.getMean(), slide.size()));
//                    p.setVariance(Statistics.revertVariance(Projector.projectOnDimension(oldestPoint, p.getValues()), p.getVariance(), slide.size(), p.getMean()));
//
//                    // Update
//                    p.setVariance(Statistics.computeVarianceOnline2(slide.size() - 1, p.getMean(), p.getVariance(), Projector.projectOnDimension(dt, p.getValues())));
//                    p.setMean(Statistics.computeMeanOnline(slide.size() - 1, p.getMean(), Projector.projectOnDimension(dt, p.getValues())));
//                }
//            }
//        }

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

        // Update parameters for Data Density Function
        // sdEstimator.updateDDFparameters(dt, currentMean, currentCovariance);
        // Select the best partition that can reveal the p-dimension for data point dt
        // Perform evolutionary computation to find the p-dimension for the given data point dt
        Dimension pDimension = null;
        if (absoluteNormalizedDeviation > this.meanAbsoluteNormalizedDeviation) {
            pDimension = evolutionEngine.evolve(A_out, dt, 1);
//            pDimension = A_out[new Random().nextInt(A_out.length)];
        } else {
            pDimension = evolutionEngine.evolve(A_in, dt, 1);
//            pDimension = A_in[new Random().nextInt(A_in.length)];
        }

        // Update the mean absolute normalized deviation after evolutionary step 
        // to find a candidate p-dimension for the incoming data point and the stream 
        // density around that data point on the selected p-dimension
        this.meanAbsoluteNormalizedDeviation = Statistics.computeMeanOnline(
                slide.size() - 1,
                this.meanAbsoluteNormalizedDeviation,
                absoluteNormalizedDeviation);

        // Compute the stream density and k-integral of data point dt
        double streamDensity = sdEstimator.estimateStreamDensity(dt, Math.sqrt(pDimension.getVariance()), pDimension);
        double kIntegral = kIntegeral.computeKIntegral(dt, pDimension, this.k);

        return new double[]{streamDensity, kIntegral};
    }

}
