/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStructures.DataPoint;
import dataStructures.Dimension;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.Collectors;
import math.Statistics;
import org.jblas.DoubleMatrix;

/**
 *
 * @author Nam Phung
 */
public class EvolutionaryComputation {

    // Stream density estimator used by the fitness function to rank the dimension
    private StreamDensity sdEstimator = null;

    private EvolutionaryComputation() {

    }

    /**
     * Parameterized constructor for the evolutionary computation object. The
     * evolutionary computation requires a stream density estimator to acts as a
     * fitness function. A dimension that produces the lowest stream density for
     * a given data point is considered to have a high fitness ranking in a set
     * of population
     *
     * @param sd
     */
    public EvolutionaryComputation(StreamDensity sd) {
        this.sdEstimator = sd;
    }

    /**
     *
     * @param population
     * @param dt
     * @param allDataPoints
     * @param epochs
     * @return
     */
    public Object[] evolve(List<Dimension> population, DataPoint dt, List<DataPoint> allDataPoints, int epochs) {

        // Because the ranking of the candidate dimensions is different for each incoming data point. For
        // example, given a p-dimension X, then stream density (SD) of a data point Y on X can be small, but
        // the SD for another data point Z on X can be very large, because of this, we maintain a sorted
        // data structure that hold the candidate dimensions, during the evolution process, the cost of adding
        // the offspring and removing the worst offspring will be low for each iteration due to the insert
        // sorted process of the data structure, which allow us to ignore trying to re-sort the list of
        // candidate dimensions after each evolution iteration
        TreeSet<Dimension> candidateDimensions = new TreeSet(new Comparator<Dimension>() {
            @Override
            public int compare(Dimension o1, Dimension o2) {

                double o1Density = sdEstimator.estimateStreamDensity(dt, allDataPoints, Math.sqrt(o1.variance), o1.values, "uniform");
                double o2Density = sdEstimator.estimateStreamDensity(dt, allDataPoints, Math.sqrt(o2.variance), o2.values, "uniform");

                // Compare the stream density of data point when projected on 2 different dimensions
                int res = 0;
                if (o1Density - o2Density < 0.0) {
                    res = -1;
                } else if (o1Density - o2Density > 0.0) {
                    res = 1;
                }
                return res;
            }
        });
        Iterator<Dimension> iter = population.iterator();
        while (iter.hasNext()) {
            candidateDimensions.add(iter.next());
        }
        
        // Perform evolve for some iteration
        while (epochs > 0) {
            // Pick 2 candidate dimensions
            Dimension candidateX = null;
            int xIndex = 0;
            Dimension candidateY = null;
            int yIndex = 0;

            // Pick 2 non-equal random index in range(0, population.size())
            do {
                xIndex = new Random().nextInt(candidateDimensions.size());
                yIndex = new Random().nextInt(candidateDimensions.size());
            } while (xIndex == yIndex);

            iter = candidateDimensions.iterator();
            while (iter.hasNext()) {
                if (xIndex == 0) {
                    candidateX = iter.next();
                } else if (yIndex == 0) {
                    candidateY = iter.next();
                } else {
                    iter.next();
                }
                --xIndex;
                --yIndex;
            }

            // Create the offspring dimension using crossover and compute the projected mean and variance
            // of the values on that projected dimension
            DoubleMatrix dimension = this.crossover(candidateX.values, candidateY.values);
            List<Double> projected = allDataPoints.parallelStream().map(k -> sdEstimator.projectOnDimension(k, dimension)).collect(Collectors.toList());
            Dimension offspring = new Dimension(dimension, Statistics.computeMean(projected), Statistics.computeVariance(projected));

            // Add the new off-spring to the population
            boolean wasAdded = candidateDimensions.add(offspring);

            // Remove the offspring with highest SD from the population
            if (wasAdded) {
                candidateDimensions.pollLast();
            }

            // Decrement the number of iterations left for evolution process
            --epochs;
        }

        // Return the best-fit p-dimension together with the stream density of 
        // the data point in that dimension, and the set of new p-dimension population
        Dimension candidate = candidateDimensions.first();
        double candidateSD = sdEstimator.estimateStreamDensity(dt, allDataPoints, Math.sqrt(candidate.variance), candidate.values, "uniform");
        List<DoubleMatrix> mutatedPopulation = new LinkedList(candidateDimensions);
        return new Object[]{candidate, candidateSD, mutatedPopulation};
    }

    /**
     *
     * @param dimensionX
     * @param dimensionY
     * @return
     */
    private DoubleMatrix crossover(DoubleMatrix dimensionX, DoubleMatrix dimensionY) {

        int cutoff = new Random().nextInt(dimensionX.data.length);
        double[] dimensionZ = new double[dimensionX.data.length];

        // Perform crossover
        for (int i = 0; i < dimensionX.length; ++i) {
            if (i != cutoff) {
                double gamma = new Random().nextDouble();
                dimensionZ[i] = (gamma * dimensionX.data[i]) + ((1 - gamma) * dimensionY.data[i]);
            } else {
                dimensionZ[i] = (new Random().nextDouble() - 1.0) + (new Random().nextDouble());
            }
        }
        return new DoubleMatrix(dimensionZ);
    }

    /**
     *
     * @param dimension
     * @return
     */
    private DoubleMatrix mutate(DoubleMatrix dimension) {

        int index = new Random().nextInt(dimension.data.length);
        double[] mutatedDimension = dimension.data;
        mutatedDimension[index] *= (new Random().nextDouble() - 1.0) + (new Random().nextDouble());

        return new DoubleMatrix(mutatedDimension);
    }
}
