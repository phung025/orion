/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructures;

import java.util.Objects;
import org.jblas.DoubleMatrix;

/**
 * Wrapper class for a projected dimension. The class contains 3 attributes:
 * DoubleMatrix values, double mean, and double variance. The DoubleMatrix
 * values contain the value vector this is used to perform matrix multiplication
 * with a data point to project that point on to the new dimension. The mean and
 * variance variable contains the statistical attributes of all projected data
 * points on that dimension.
 *
 * @author Nam Phung
 */
public class Dimension {

    private DoubleMatrix values; // The value vector to project original data point to this dimension
    private double mean = 0; // Mean of all values along this dimension
    private double variance = 0; // Variance of all values along this dimension

    private Dimension() {

    }

    public Dimension(DoubleMatrix values, double mean, double variance) {
        this.values = values;
        this.mean = mean;
        this.variance = variance;
    }

    /**
     * Set the new mean of all projected data points on this dimension
     *
     * @param newMean new mean of all projected data points
     */
    public void setMean(double newMean) {
        this.mean = newMean;
    }

    /**
     * Set the new variance of all projected data points on this dimension
     *
     * @param newVariance new variance of all projected data points
     */
    public void setVariance(double newVariance) {
        this.variance = newVariance;
    }

    /**
     * Get the value vector of this dimension. This value vector is used to
     * multiply with the data point to project that data point to the new
     * dimension
     *
     * @return DoubleMatrix value vector with same dimension of a data point
     */
    public DoubleMatrix getValues() {
        return this.values;
    }

    /**
     * Return the mean of all projected data points on this dimension
     *
     * @return mean value of all projected data points
     */
    public double getMean() {
        return this.mean;
    }

    /**
     * Return the variance of all projected data points on this dimension
     *
     * @return variance of all projected data points
     */
    public double getVariance() {
        return this.variance;
    }

    @Override
    public boolean equals(Object rhs) {
        return (values.equals(((Dimension) rhs).values)) && (mean == ((Dimension) rhs).mean) && (variance == ((Dimension) rhs).variance);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.values);
        hash = 31 * hash + (int) (Double.doubleToLongBits(this.mean) ^ (Double.doubleToLongBits(this.mean) >>> 32));
        hash = 31 * hash + (int) (Double.doubleToLongBits(this.variance) ^ (Double.doubleToLongBits(this.variance) >>> 32));
        return hash;
    }
}
