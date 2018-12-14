/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStream.DataPoint;
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
    private final double c = 3.14; // A non-zero constant c for computing vector a    
    private DoubleMatrix a = null; // Vector a for computing the bin width. The vector has same dimension as an incoming data point
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
     *
     * @param dt
     * @param allDataPoints
     * @param pDimension
     * @param kernelType
     * @return
     */
    public double estimateStreamDensity(DataPoint dt, List<DataPoint> allDataPoints, DoubleMatrix pDimension, String kernelType) {

        // Compute the standard deviation of the data points projected on the p-dimension
        // THIS CAN BE FURTHER OPTIMIZED USING AN ONLINE ALGORITHM
        List<Double> tmp = allDataPoints.stream().map(i -> projectOnDimension(i, pDimension)).collect(Collectors.toList());
        double stdevation = Math.sqrt(Statistics.computeVariance(tmp));

        // Compute the scaled neighbor distance
        double scaledNeighborDist = this.r * this.binWidth;
        double projectedDT = projectOnDimension(dt, pDimension);

        // Within a scaled neighbor distance from the data point dt, approximate the stream density
        double streamDensity = 0.0;
        Iterator<DataPoint> iter = allDataPoints.iterator();
        while (iter.hasNext()) {
            DataPoint next = iter.next();
            double projectedNext = projectOnDimension(next, pDimension);
            if (projectedDT - scaledNeighborDist <= projectedNext && projectedNext <= projectedDT + scaledNeighborDist) {
                streamDensity += DDF(projectedNext, allDataPoints, kernelType, stdevation);
            }
        }

        return streamDensity;
    }

    /**
     *
     * @param dt
     * @param allDataPoints
     * @param pDimension
     * @return
     */
    private double DDF(double dt, List<DataPoint> allDataPoints, String kernelType, double stdevation) {

        Iterator<DataPoint> iter = allDataPoints.iterator();
        double numerator = 0.0;
        double denominator = 0.0;
        while (iter.hasNext()) {
            DataPoint next = iter.next();

            double ffactor = Math.pow(this.forgettingFactor, allDataPoints.get(allDataPoints.size() - 1).getTimestamp() - next.getTimestamp());
            denominator += ffactor;
            double kza = kernelAlongBandwidth(
                    this.a.transpose().mmul(next.getValues()).data[0] - dt,
                    kernelType,
                    computeBandwidth(stdevation, allDataPoints.size()));
            numerator += ffactor * kza;
        }

        return numerator / denominator;
    }

    /**
     * Project a data point on a dimension
     *
     * @param dt
     * @param pDimension
     * @return
     */
    public double projectOnDimension(DataPoint dt, DoubleMatrix pDimension) {
        return dt.getValues().transpose().mmul(pDimension).data[0];
    }

    /**
     *
     * @param dt
     * @param currentMean
     * @param currentCovariance
     */
    public void updateDDFparameters(DataPoint dt, DoubleMatrix currentMean, DoubleMatrix currentCovariance) {

        // Update the vector parameter a
        this.a = Solve.pinv(currentCovariance).mmul(dt.getValues().sub(currentMean)).mul(this.c);

        // Update the bin width parameter
        this.binWidth = 6.0 * Math.sqrt(a.transpose().mmul(currentCovariance).mmul(a).data[0]) / 400.0;
    }

    public void updateForgettingFactor(List<DataPoint> allDataPoints) {
        this.forgettingFactor = 0.5;
    }
}
