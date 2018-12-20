/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStructures.DataPoint;
import dataStructures.Dimension;
import java.util.Collections;
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

        Comparator<Dimension> cmp = new Comparator<Dimension>() {
            @Override
            public int compare(Dimension o1, Dimension o2) {

                double o1Density = sdEstimator.estimateStreamDensity(dt, allDataPoints, Math.sqrt(o1.getVariance()), o1.getValues(), "uniform");
                double o2Density = sdEstimator.estimateStreamDensity(dt, allDataPoints, Math.sqrt(o2.getVariance()), o2.getValues(), "uniform");

                // Compare the stream density of data point when projected on 2 different dimensions
                int res = 0;
                if (o1Density - o2Density < 0.0) {
                    res = -1;
                } else if (o1Density - o2Density > 0.0) {
                    res = 1;
                }
                return res;
            }
        };
        Collections.sort(population, cmp);

        // Perform evolve for some iteration
        for (int i = 0; i < epochs; ++i) {

            // Pick 2 candidate dimensions
            Dimension candidateX = population.get(new Random().nextInt(population.size()));
            Dimension candidateY = population.get(new Random().nextInt(population.size()));

            // Create the offspring dimension using crossover and compute the projected mean and variance
            // of the values on that projected dimension
            DoubleMatrix dimension = this.crossover(candidateX.getValues(), candidateY.getValues());
            List<Double> projected = allDataPoints.parallelStream().map(k -> sdEstimator.projectOnDimension(k, dimension)).collect(Collectors.toList());
            Dimension offspring = new Dimension(dimension, Statistics.computeMean(projected), Statistics.computeVariance(projected));

            // Add the new off-spring to the population and at then remove the
            // offspring with highest SD from the population
            population.add(offspring);
        }

        // Remove the worst p-dimensions from the population
        Collections.sort(population, cmp);
        for (int i = 0; i < epochs; ++i) {
            population.remove(population.size() - 1);
        }

        // Return the best-fit p-dimension together with the stream density of 
        // the data point in that dimension, and the set of new p-dimension population
        Dimension candidate = population.get(0);
        double candidateSD = sdEstimator.estimateStreamDensity(dt, allDataPoints, Math.sqrt(candidate.getVariance()), candidate.getValues(), "uniform");

        return new Object[]{candidate, candidateSD};
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
}
