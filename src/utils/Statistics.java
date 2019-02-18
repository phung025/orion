/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.List;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;

/**
 *
 * @author Nam Phung
 */
public class Statistics {

    /**
     * Compute the mean value of a list of number
     *
     * @param values
     * @return
     */
    public static double computeMean(List<Double> values) {
        return values.stream().mapToDouble(i -> i).sum() / values.size();
    }

    /**
     * Online algorithm to compute the mean value of a list of number
     *
     * @param previousCount
     * @param previousMean
     * @param nextValue
     * @return
     */
    public static double computeMeanOnline(long previousCount, double previousMean, double nextValue) {
        return ((previousCount / (previousCount + 1.0)) * previousMean) + (nextValue / (previousCount + 1.0));
    }

    /**
     * Compute sample variance of all values in a list
     *
     * @param values
     * @return
     */
    public static double computeVariance(List<Double> values) {
        Double mean = Statistics.computeMean(values);
        Double variance = 0.0;
        variance = values.stream().map((v) -> Math.pow(v - mean, 2.0) / (values.size() - 1)).reduce(variance, (accumulator, _item) -> accumulator + _item);
        return variance;
    }

    /**
     * Compute a running variance using an online algorithm. The documentation
     * for the algorithm can be found at:
     * https://www.johndcook.com/blog/standard_deviation/
     *
     * @param previousCount total number of values excluding the incoming point
     * @param previousSum sum of all values excluding the incoming point
     * @param previousSumSquared sum of all squared values excluding the
     * incoming point
     * @param nextValue value of the incoming point
     * @return running variance when a new data point arrives
     */
    public static double computeVarianceOnline(long previousCount, double previousSum, double previousSumSquared, double nextValue) {
        return (1.0 / ((previousCount + 1.0) * previousCount)) * ((previousCount + 1.0) * (previousSumSquared + Math.pow(nextValue, 2.0)) - Math.pow(previousSum + nextValue, 2.0));
    }

    /**
     * Compute a running sample variance using Welford's Online algorithm.The
     * documentation for the algorithm can be found at:
     * https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
     *
     * @param previousCount total number of values excluding the incoming point
     * @param previousMean mean of all values excluding the next value
     * @param previousVariance variance of all values excluding the next value
     * @param nextValue value of the incoming point
     * @return running variance when a new data point arrives
     */
    public static double computeVarianceOnline2(long previousCount, double previousMean, double previousVariance, double nextValue) {
        return previousVariance + (Math.pow((nextValue - previousMean), 2.0) / (previousCount + 1.0)) - (previousVariance / previousCount);
    }

    /**
     * Compute the mean vector from a list of vectors
     *
     * @param vectors
     * @return
     */
    public static DoubleMatrix computeVectorMean(List<DoubleMatrix> vectors) {

        // Return null value if the list of vectors is empty
        if (vectors.isEmpty()) {
            return null;
        }

        // Compute the sum of all vectors in the list
        DoubleMatrix meanVector = vectors.get(0);
        for (DoubleMatrix vect : vectors.subList(1, vectors.size())) {
            meanVector = meanVector.add(vect);
        }

        // Divide the sum vector by total number of vectors in the list
        meanVector = meanVector.div(vectors.size());

        return meanVector;
    }

    /**
     * Compute the mean vector from a list of vectors online
     *
     * @param previousCount
     * @param previousMean
     * @param nextValue
     * @return
     */
    public static DoubleMatrix computeVectorMeanOnline(long previousCount, DoubleMatrix previousMean, DoubleMatrix nextValue) {

        // Compute the mean using the online algorithm
        // mean_(n+1) = [n/(n+1)]*mean_(n) + [1/(n+1)]*value_(n+1)
        DoubleMatrix previousSum = previousMean.mul((previousCount / (previousCount + 1.0)));
        nextValue = nextValue.mul(1.0 / (previousCount + 1.0));
        DoubleMatrix newMean = previousSum.add(nextValue);

        return newMean;
    }

    /**
     * An offline algorithm that computes the covariance matrix
     *
     * @param vectorList a list containing all the value vector of different
     * data points
     * @return the computed covariance matrix from matrix A.
     */
    public static DoubleMatrix computeCovarianceMatrix(List<DoubleMatrix> vectorList) {

        // Convert the vector list into a 2d double array. Each row in the array
        // is an observation and each column is a different attribute of the data point
        double[][] tmp = new double[vectorList.size()][vectorList.get(0).data.length];
        for (int i = 0; i < tmp.length; ++i) {
            tmp[i] = vectorList.get(i).data;
        }

        // Computation of covariance matrix can be found at
        // https://stattrek.com/matrix-algebra/covariance-matrix.aspx
        DoubleMatrix A = new DoubleMatrix(tmp);
        int n = A.getRows();
        DoubleMatrix ones = DoubleMatrix.ones(n);
        DoubleMatrix a = A.sub(ones.mmul(ones.transpose()).mmul(A).mul(1.0 / n));

        return a.transpose().mmul(a).div(n * 1.0);
    }

    /**
     *
     * @param prevCount
     * @param prevCov
     * @param prevMean
     * @param nextPoint
     * @return
     */
    public static DoubleMatrix computeCovarianceMatrixOnline(long prevCount, DoubleMatrix prevCov, DoubleMatrix prevMean, DoubleMatrix nextPoint) {

        // Online algorithm for computing covariance matrix
        // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
        DoubleMatrix lhs = prevCov.mul((1.0 * prevCount) / (prevCount + 1.0));
        DoubleMatrix rhs = nextPoint.sub(prevMean).mmul(nextPoint.sub(prevMean).transpose()).mul((1.0 * prevCount) / (prevCount + 1.0)).div(prevCount + 1.0);

        return lhs.add(rhs);
    }

    /**
     * Online algorithm for computing the absolute normalized deviation when a
     * new data point arrives
     *
     * @param nextPoint vector contains all values of the new data point
     * @param nextMean mean value vector when a new data point arrives
     * @param nextCovariance covariance matrix when a new data point arrives
     * @return absolute normalized deviation
     */
    public static double computeAbsoluteNormalizedDevitation(DoubleMatrix nextPoint, DoubleMatrix nextMean, DoubleMatrix nextCovariance) {
        return Math.sqrt(nextPoint.sub(nextMean).transpose().mmul(Solve.pinv(nextCovariance)).mmul(nextPoint.sub(nextMean)).data[0]);
    }

    public static DoubleMatrix revertVectorMean(DoubleMatrix removedPoint, DoubleMatrix currentMean, int currentCount) {
        return currentMean.mul((currentCount * 1.0) / ((currentCount - 1) * 1.0)).sub(removedPoint.div((currentCount - 1) * 1.0));
    }

    /**
     * Revert the mean of Nth numbers to mean of (N-1)th numbers
     * @param removedValue the value being removed from the list
     * @param currentMean current mean of all values
     * @param currentCount the number of values contributing to the mean
     * @return the new mean of numbers excluding removedValue
     */
    public static double revertMean(double removedValue, double currentMean, int currentCount) {
        return (currentMean * ((currentCount * 1.0) / ((currentCount - 1) * 1.0))) - (removedValue / ((currentCount - 1) * 1.0));
    }

    public static DoubleMatrix revertCovarianceMatrix(DoubleMatrix removedPoint, DoubleMatrix currentCovariance, DoubleMatrix currentMean, int currentCount) {
        DoubleMatrix lhs = currentCovariance.mul((currentCount * 1.0) / (currentCount - 1.0));
        DoubleMatrix rhs = removedPoint.sub(currentMean).mmul(removedPoint.sub(currentMean).transpose()).mul((1.0 * currentCount - 1.0) / (currentCount * 1.0)).div(currentCount - 1);
        return lhs.sub(rhs);
    }

    /**
     * Revert the running variance at time N back to the variance at time (N-1)
     * using Welford's online algorithm
     *
     * @param removedValue the value being removed from the data
     * @param currentVariance current variance that the removedValue
     * contributing to
     * @param currentCount current number of data points including the value
     * being removed
     * @param newMean the new mean of all data points after the value removed
     * from the data
     * @return
     */
    public static double revertVariance(double removedValue, double currentVariance, int currentCount, double newMean) {
        double numerator = currentVariance - (Math.pow(removedValue - newMean, 2.0) / (currentCount * 1.0));
        double denominator = 1.0 - (1.0 / ((currentCount - 1) * 1.0));
        return numerator / denominator;
    }
}
