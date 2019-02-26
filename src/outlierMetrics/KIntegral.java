/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package outlierMetrics;

import dataStructures.DataPoint;
import dataStructures.Dimension;
import dataStructures.Slide;
import java.util.Arrays;
import utils.Projector;

/**
 * This class contains the definition of the K-Integral function to compute the
 * k-integral metric for detecting the "outlier-ness" of a data point.
 * K-Integral is defined as the integral that include k percent of the data
 * points along a p-dimension Z_a
 *
 * @author phung
 */
public class KIntegral {

    private final Slide slide; // The slide containing all currently active data points    

    /**
     * Default constructor of KIntegral class. The constructor takes in
     * reference to the current slide containing all active data points to help
     * compute the K-Integral metric
     *
     * @param slide the slide containing all active data points
     */
    public KIntegral(Slide slide) {
        this.slide = slide;
    }

    /**
     *
     * @param dt
     * @param p
     * @param k
     * @return
     */
    public double computeKIntegral(DataPoint dt, Dimension p, double k, double mean, double stddeviation) {

        // Project the data points onto dimension p
        double[] allProjectedPoints = new double[this.slide.size()];
        for (int i = 0; i < this.slide.size(); ++i) {
            allProjectedPoints[i] = (Projector.projectOnDimension(slide.points()[i], p.getValues()) - mean) / stddeviation;
        }

        // Project dt onto dimension p
        double projectedDT = (Projector.projectOnDimension(dt, p.getValues()) - mean) / stddeviation;

        // Compute the left and right boundary of data point dt
        Arrays.parallelSort(allProjectedPoints); // sort the projected points
        int indexDT = Arrays.binarySearch(allProjectedPoints, projectedDT); // index of dt in the the projected points
        int d = (int) ((allProjectedPoints.length * k) / 2.0); // The (ideal) distance of the 2 boundary encapsulating the data point dt on dimension p
        double leftBound = (indexDT - d < 0) ? allProjectedPoints[0] : allProjectedPoints[indexDT - d];
        double rightBound = (indexDT + d >= allProjectedPoints.length) ? allProjectedPoints[allProjectedPoints.length - 1] : allProjectedPoints[indexDT + d];

        // Scale the k-integral value with respect to the maximum dispersion along the p-dimension
        double kintegral = projectedDT - leftBound;
        double maximumDispersion = rightBound - leftBound;

        // Return the scaled k-integral
        return (maximumDispersion == 0) ? 0 : kintegral / maximumDispersion;
    }
}
