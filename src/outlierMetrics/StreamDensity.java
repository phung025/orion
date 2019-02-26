/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package outlierMetrics;

import dataStructures.DataPoint;
import dataStructures.Dimension;
import dataStructures.Slide;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import utils.Statistics;
import org.jblas.DoubleMatrix;
import utils.Projector;

/**
 *
 * @author Nam Phung
 */
public class StreamDensity {

    // Parameters for data density function
    private double forgettingFactor = 0.5; // Forgetting factor, a real number in range (0,1)
    private double r = 0.0; // User-defined distance r for computing the scaled neighbor distance
    private Slide slide;
    private String kernelType = null;

    private StreamDensity() {

    }

    /**
     * Parameterized constructor for initializing a stream density estimator
     *
     * @param kernelType
     * @param dim
     * @param r
     * @param slide
     */
    public StreamDensity(String kernelType, int dim, double r, Slide slide) {
        this.kernelType = kernelType;
        this.r = r;
        this.slide = slide;
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
        return Math.sqrt(5.0) * stdeviation * Math.pow(n, -1.0 / 5.0);
    }

    /**
     * Estimate the stream density of an incoming data point. This function
     * computes the percentage of neighbors of a data point along a p-dimension
     * within a scaled neighbor distance.
     *
     * @param dt the incoming data point
     * @param pDimension the desired p-dimension
     * @param kernelType type of the kernel function used for computing the bin
     * width. The options are "sigmoid", "gaussian", and "uniform"
     * @return double value indicating the neighbors density of the incoming
     * data point
     */
    public double estimateStreamDensity(DataPoint dt, Dimension pDimension, String kernelType) {
        // Compute the standard deviation of the data points projected on the p-dimension.
        // All data points are projected on the p-dimension to compute the standard deviation.
        // THIS MIGHT BE FURTHER OPTIMIZED USING AN ONLINE ALGORITHM
        List<Double> tmp = new LinkedList<>();
        for (int i = 0; i < slide.size(); ++i) {
            tmp.add(Projector.projectOnDimension(slide.points()[i], pDimension.getValues()));
        }

        double mean = Statistics.computeMean(tmp);
        double stdevation = Math.sqrt(Statistics.computeVariance(tmp));

        return estimateStreamDensityHelper(dt, mean, stdevation, pDimension);
    }

    /**
     *
     * @param dt
     * @param stdevation
     * @param pDimension
     * @return
     */
    public double estimateStreamDensity(DataPoint dt, double mean, double stdevation, Dimension pDimension) {
        return estimateStreamDensityHelper(dt, mean, stdevation, pDimension);
    }

    private double estimateStreamDensityHelper(DataPoint dt, double mean, double stdevation, Dimension pDimension) {
        double scaledNeighborDist = this.r * (6.0 * stdevation / 400.0); // Compute the scaled neighbor distance
        double projectedDT = Projector.projectOnDimension(dt, pDimension.getValues()); // Project the incoming data point on the p-dimension

        // Within a scaled neighbor distance from the incoming data point dt, approximate the stream density of dt
        List<Double> neighbors = new LinkedList(); // List of all neighbors within the scaled distance        
        for (int i = 0; i < this.slide.size(); ++i) {

            // Project the data point on the p-dimension            
            DataPoint next = this.slide.points()[i];
            double projectedNext = Projector.projectOnDimension(next, pDimension.getValues());

            // If that projected data point is within a scaled-neighbor distance, proceed to compute the DDF
            if (projectedDT - scaledNeighborDist <= projectedNext && projectedNext <= projectedDT + scaledNeighborDist) {
                neighbors.add((projectedNext - mean) / stdevation);
            }
        }

        double streamDensity = 0.0;
        if (!neighbors.isEmpty()) {
            long T = this.slide.size(); // Timestamp of the newest data point in the window        
            for (Iterator<Double> iter = neighbors.iterator(); iter.hasNext();) {
                streamDensity += DDF(iter.next(), pDimension, T, mean, stdevation);
            }
        }

        return streamDensity;
    }

    /**
     *
     * @param z
     * @param allDataPoints
     * @param T
     * @param stdevation
     * @return
     */
    private double DDF(double z,
            Dimension pDimension,
            double T,
            double mean,
            double stdevation) {
        double numerator = 0.0;
        double denominator = 0.0;

        for (int i = 0; i < this.slide.size(); ++i) {
            DataPoint next = this.slide.points()[i];

            // Compute the weight of the data point
            double ffactor = Math.pow(this.forgettingFactor, T - i);

            // Compute the projected kernel on the p-dimension along the bandwidth
            double kza = kernelAlongBandwidth(
                    ((Projector.projectOnDimension(next, pDimension.getValues()) - mean) / stdevation) - z,
                    this.kernelType,
                    computeBandwidth(stdevation, slide.size()));

            numerator += ffactor * kza;
            denominator += ffactor;
        }
        return numerator / denominator;
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
