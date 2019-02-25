/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evolutionaryEngine;

import dataStructures.DataPoint;
import dataStructures.Dimension;
import dataStructures.Slide;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import utils.Statistics;
import org.jblas.DoubleMatrix;
import org.uncommons.maths.random.MersenneTwisterRNG;
import outlierMetrics.StreamDensity;
import utils.Projector;

/**
 *
 * @author Nam Phung
 */
public class EvolutionaryComputation {

    // Stream density estimator used by the fitness function to rank the dimension
    private StreamDensity sdEstimator = null;

    // The slide containing current active data points being processed
    private Slide slide = null;

    private EvolutionaryComputation() {

    }

    /**
     * Parameterized constructor for the evolutionary computation object.The
     * evolutionary computation requires a stream density estimator to acts as a
     * fitness function. A dimension that produces the lowest stream density for
     * a given data point is considered to have a high fitness ranking in a set
     * of population
     *
     * @param sd
     * @param allDataPoints
     */
    public EvolutionaryComputation(StreamDensity sd, Slide allDataPoints) {
        this.sdEstimator = sd;
        this.slide = allDataPoints;
    }

    /**
     *
     * @param population
     * @param dt
     * @param epochs
     * @return
     */
    public Dimension evolve(Dimension[] population, DataPoint dt, int epochs) {

        Comparator<Dimension> cmp = (Dimension o1, Dimension o2) -> {
            double o1Density = sdEstimator.estimateStreamDensity(dt, Math.sqrt(o1.getVariance()), o1);
            double o2Density = sdEstimator.estimateStreamDensity(dt, Math.sqrt(o2.getVariance()), o2);

            // Compare the stream density of data point when projected on 2 different dimensions
            int res = 0;
            if (o1Density - o2Density < 0.0) {
                res = -1;
            } else if (o1Density - o2Density > 0.0) {
                res = 1;
            }
            return res;
        };

        // Perform evolve for some iteration
        for (int i = 0; i < epochs; ++i) {

            // Pick 2 candidate dimensions
            Dimension candidateX = population[new MersenneTwisterRNG().nextInt(population.length)];
            Dimension candidateY = population[new MersenneTwisterRNG().nextInt(population.length)];

            // Create the offspring dimension using crossover and compute the projected mean and variance
            // of the values on that projected dimension
            DoubleMatrix dimension = this.crossover(candidateX.getValues(), candidateY.getValues());
            List<Double> projected = new LinkedList<>();
            for (int j = 0; j < this.slide.size(); ++j) {
                projected.add(Projector.projectOnDimension(this.slide.points()[j], dimension));
            }
            Dimension offspring = new Dimension(dimension, Statistics.computeMean(projected), Statistics.computeVariance(projected));

            // Add the new off-spring to the population and then remove the
            // offspring with highest SD from the population
            Dimension[] newPopulation = new Dimension[population.length + 1];
            newPopulation[newPopulation.length - 1] = offspring;
            for (int j = 0; j < population.length; ++j) {
                newPopulation[j] = population[j];
            }
            Arrays.parallelSort(newPopulation, cmp);
            population = Arrays.copyOfRange(newPopulation, 0, newPopulation.length - 1);
        }

        // Return the best-fit p-dimension
        Arrays.parallelSort(population, cmp);
        return population[0];
    }

    /**
     *
     * @param dimensionX
     * @param dimensionY
     * @return
     */
    private DoubleMatrix crossover(DoubleMatrix dimensionX, DoubleMatrix dimensionY) {

        Random random = new MersenneTwisterRNG();
        int cutoff = random.nextInt(dimensionX.data.length);
        double[] dimensionZ = new double[dimensionX.data.length];

        // Perform crossover
        for (int i = 0; i < dimensionX.length; ++i) {
            if (i != cutoff) {
                double gamma = random.nextDouble();
                dimensionZ[i] = (gamma * dimensionX.data[i]) + ((1 - gamma) * dimensionY.data[i]);
            } else {
                dimensionZ[i] = (random.nextDouble() - 1.0) + (random.nextDouble());
            }
        }
        return new DoubleMatrix(dimensionZ);
    }
}
