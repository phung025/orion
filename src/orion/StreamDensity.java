/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStructures.DataPoint;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import math.Statistics;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;

/**
 *
 * @author Nam Phung
 */
public class StreamDensity {

    // Parameters for data density function
    private double forgettingFactor = 0.5; // Forgetting factor, a real number in range (0,1)
    private double binWidth = 1.0; // bin width for computing the scaled neighbor distance
    private final double c = Math.E; // A non-zero constant c for computing vector a
    private DoubleMatrix a = null; // Vector a used for computing the bin width. The vector has same dimension as an incoming data point
    private double r = 0.0; // User-defined distance r for computing the scaled neighbor distance

    private StreamDensity() {

    }

    /**
     * Parameterized constructor for initializing a stream density estimator
     *
     * @param dim
     * @param r
     */
    public StreamDensity(int dim, double r) {
        this.a = DoubleMatrix.zeros(dim); // Vector a used for computing data density
        this.r = r;
    }

    /**
     *
     * @param x
     * @param type
     * @param bandwidth
     * @return
     */
    private double kernelAlongBandwidth(double x, String type, double bandwidth) {

        double k = 0.0; // The result of computing the kernel function on input x

        switch (type) {
            case "sigmoid":
                k = (2.0 / Math.PI) * (1 / (Math.exp(x / bandwidth) + Math.exp(-x / bandwidth)));
                break;
            case "gaussian":
                k = (1 / Math.sqrt(2 * Math.PI)) * Math.exp(-Math.pow(x / bandwidth, 2) / 2);
                break;
            case "uniform":
                k = 1.0 / 2.0;
                break;
            default:
                break;
        }

        return k / bandwidth;
    }

    /**
     * Compute the bandwidth/smoothing parameter used in the kernel function
     * along that bandwidth
     *
     * @param stdeviation
     * @param n
     * @return
     */
    private double computeBandwidth(double stdeviation, int n) {
        return Math.sqrt(5) * stdeviation * Math.pow(n, -1.0 / 5.0);
    }

    /**
     * Estimate the stream density of an incoming data point. This function
     * computes the percentage of neighbors of a data point along a p-dimension
     * within a scaled neighbor distance.
     *
     * @param dt the incoming data point
     * @param allDataPoints every data points that are active so far
     * @param pDimension the desired p-dimension
     * @param kernelType type of the kernel function used for computing the bin
     * width. The options are "sigmoid", "gaussian", and "uniform"
     * @return double value indicating the neighbors density of the incoming
     * data point
     */
    public double estimateStreamDensity(DataPoint dt, List<DataPoint> allDataPoints, DoubleMatrix pDimension, String kernelType) {

        // Compute the standard deviation of the data points projected on the p-dimension.
        // All data points are projected on the p-dimension to compute the standard deviation.
        // THIS MIGHT BE FURTHER OPTIMIZED USING AN ONLINE ALGORITHM
        List<Double> tmp = allDataPoints.parallelStream().map(i -> projectOnDimension(i, pDimension)).collect(Collectors.toList());
        double stdevation = Math.sqrt(Statistics.computeVariance(tmp));

        return estimateStreamDensityHelper(dt, allDataPoints, stdevation, pDimension, kernelType);
    }

    /**
     *
     * @param dt
     * @param allDataPoints
     * @param stdevation
     * @param pDimension
     * @param kernelType
     * @return
     */
    public double estimateStreamDensity(DataPoint dt, List<DataPoint> allDataPoints, double stdevation, DoubleMatrix pDimension, String kernelType) {
        return estimateStreamDensityHelper(dt, allDataPoints, stdevation, pDimension, kernelType);
    }

    private double estimateStreamDensityHelper(DataPoint dt, List<DataPoint> allDataPoints, double stdevation, DoubleMatrix pDimension, String kernelType) {
        double scaledNeighborDist = this.r * this.binWidth; // Compute the scaled neighbor distance
        double projectedDT = projectOnDimension(dt, pDimension); // Project the incoming data point on the p-dimension

        // Within a scaled neighbor distance from the incoming data point dt, approximate the stream density of dt
        double streamDensity = 0.0;
        Iterator<DataPoint> iter = allDataPoints.iterator();
        long T = allDataPoints.get(allDataPoints.size() - 1).getTimestamp(); // Timestamp of the newest data point in the window
        while (iter.hasNext()) {
            DataPoint next = iter.next();
            
            // Project the data point on the p-dimension            
            double projectedNext = projectOnDimension(next, pDimension); 

            // If that projected data point is within a scaled-neighbor distance, proceed to compute the DDF
            if (projectedDT - scaledNeighborDist <= projectedNext && projectedNext <= projectedDT + scaledNeighborDist) {
                streamDensity += DDF(projectedNext, allDataPoints, T, stdevation, kernelType);
            }
        }
        return streamDensity;
    }

    /**
     *
     * @param dt
     * @param allDataPoints
     * @param T
     * @param stdevation
     * @param kernelType
     * @return
     */
    private double DDF(double dt,
            List<DataPoint> allDataPoints,
            double T,
            double stdevation,
            String kernelType) {

        double numerator = 0.0;
        double denominator = 0.0;

        Iterator<DataPoint> iter = allDataPoints.iterator();
        while (iter.hasNext()) {
            DataPoint next = iter.next();

            // Compute the weight of the data point
            double ffactor = Math.pow(this.forgettingFactor, T - next.getTimestamp());

            // Compute the projected kernel on the p-dimension along the bandwidth
            double kza = kernelAlongBandwidth(
                    this.a.transpose().mmul(next.getValues()).sum() - dt,
                    kernelType,
                    computeBandwidth(stdevation, allDataPoints.size()));

            numerator += ffactor * kza;
            denominator += ffactor;
        }

        return numerator / denominator;
    }

    /**
     * Project a data point on a dimension. The projected point dt on the
     * dimension pDimension is computed as the linear combination of the 2
     * vectors.
     *
     * @param dt a data point
     * @param pDimension vector with same dimension as the data point
     * representing the new p-dimension.
     * @return the projected data point on the p-dimension.
     */
    public double projectOnDimension(DataPoint dt, DoubleMatrix pDimension) {
        return dt.getValues().transpose().mmul(pDimension).sum();
    }

    /**
     * Update the parameters used by the Stream Density function.
     *
     * @param dt incoming data point
     * @param currentMean current mean value of all data points
     * @param currentCovariance current covariance matrix of all data points
     */
    public void updateDDFparameters(DataPoint dt, DoubleMatrix currentMean, DoubleMatrix currentCovariance) {

        // Update the vector parameter a using the formula
        // a = (cov^-1)*(dt.V - μ)*c
        this.a = Solve.pinv(currentCovariance).mmul(dt.getValues().sub(currentMean)).mul(this.c);

        // Update the bin width parameter
        this.binWidth = 6.0 * Math.sqrt(a.transpose().mmul(currentCovariance).mmul(a).sum()) / 400.0;
    }

    /**
     * Update the forgetting factor of the stream density estimator given a list
     * of data points using auto regression model
     *
     * @param allDataPoints list of all data points used for computing the
     * forgetting factor
     */
    public void updateForgettingFactor(List<DataPoint> allDataPoints) {
        List<DataPoint> prediction = allDataPoints.subList(0, allDataPoints.size() / 2);
        List<DataPoint> desired = allDataPoints.subList(allDataPoints.size() / 2, allDataPoints.size());

        double maxFFactor = 0.995;
        this.forgettingFactor = 0.0; // Reset forgetting factor

        double varianceError = 0.0;
        {
            double sum = 0.0;
            double sumSquared = 0.0;
            for (int i = 0; i < Math.min(prediction.size(), desired.size()); ++i) {
                DataPoint x = prediction.get(i);
                DataPoint y = desired.get(i);

                // Compute error between predicted & desired output
                double err = x.getValues().sub(y.getValues()).sum();
                varianceError += Statistics.computeVarianceOnline(i + 1, sum, sumSquared, err);
                sum += err;
                sumSquared += Math.pow(err, 2.0);
            }
        }

        for (int i = 0; i < Math.min(prediction.size(), desired.size()); ++i) {
            DataPoint x = prediction.get(i);
            DataPoint y = desired.get(i);

            // Compute error between predicted & desired output
            DoubleMatrix err = x.getValues().sub(y.getValues());

            double q_t = err.transpose().mmul(err).sum() / err.data.length;
            double L_t = varianceError / (q_t * (1 - maxFFactor));
            double ffactor_t = (1 - (1.0 / L_t)) / (1.0 * Math.min(prediction.size(), desired.size()));

            this.forgettingFactor += ffactor_t;
        }
    }

    /**
     * Get the forgetting factor of this stream density estimator
     *
     * @return forgetting factor of the auto regression model
     */
    public double getForgettingFactor() {
        return this.forgettingFactor;
    }
}
