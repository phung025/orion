/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package math;

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
    public static Double computeMean(List<Double> values) {
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
    public static Double computeMeanOnline(Integer previousCount, Double previousMean, Double nextValue) {
        return ((previousCount / (previousCount + 1.0)) * previousMean) + (nextValue / (previousCount + 1.0));
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
    public static DoubleMatrix computeVectorMeanOnline(Integer previousCount, DoubleMatrix previousMean, DoubleMatrix nextValue) {

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
     * @param nextMean
     * @param nextPoint
     * @return
     */
    public static DoubleMatrix computeCovarianceMatrixOnline(int prevCount, DoubleMatrix prevCov, DoubleMatrix prevMean, DoubleMatrix nextMean, DoubleMatrix nextPoint) {

        // Online algorithm for computing covariance matrix
        // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
        DoubleMatrix lhs = prevCov.mul(prevCount / (prevCount + 1.0));
        DoubleMatrix rhs = nextPoint.sub(nextMean).mmul(nextPoint.sub(prevMean).transpose()).div(prevCount + 1.0);

        return lhs.add(rhs);
    }
    
    /**
     * Online algorithm for computing the absolute normalized deviation when a new data point arrives
     * @param nextPoint vector contains all values of the new data point
     * @param nextMean mean value vector when a new data point arrives
     * @param nextCovariance covariance matrix when a new data point arrives
     * @return absolute normalized deviation
     */
    public static double computeAbsoluteNormalizedDevitation(DoubleMatrix nextPoint, DoubleMatrix nextMean, DoubleMatrix nextCovariance) {
        return nextPoint.sub(nextMean).transpose().mmul(Solve.pinv(nextCovariance)).mmul(nextPoint.sub(nextMean)).data[0];
    }
}
