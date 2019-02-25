/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evolutionaryEngine;

import dataStructures.DataPoint;
import dataStructures.Dimension;
import java.util.List;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import outlierMetrics.StreamDensity;

/**
 *
 * @author phung
 */
public class DimensionEvaluator implements FitnessEvaluator<Dimension> {

    private final StreamDensity densityEstimator;
    private DataPoint dt;

    public DimensionEvaluator(DataPoint datapoint, StreamDensity densityEstimator) {
        this.densityEstimator = densityEstimator; // Density estimator engine for computing the stream density of the datapoint dt in a dimension
        this.dt = datapoint; // The incoming data point that will be used to calculate the fitness of a dimension
    }

    /**
     * Get fitness of a dimension based on the stream density of the data point dt in that
     * dimension
     *
     * @param candidate
     * @param population
     * @return
     */
    @Override
    public double getFitness(Dimension candidate, List<? extends Dimension> population) {
        return densityEstimator.estimateStreamDensity(dt, Math.sqrt(candidate.getVariance()), candidate);
    }

    @Override
    public boolean isNatural() {

        // Natural fitness scores are those in which the fittest individual in a population has the highest fitness value. 
        // In this case the algorithm is attempting to maximise fitness scores. There need not be a specified maximum possible value.
        
        // In contrast, non-natural fitness evaluation results in fitter individuals being assigned lower scores than weaker individuals. 
        // In the case of non-natural fitness, the algorithm is attempting to minimise fitness scores.
        return false;
    }

    /**
     * Change the data point used for evaluating the fitness of the dimension.
     * The data point is used to compute its stream density, which is also the
     * fitness of the dimension
     *
     * @param dt
     */
    public void setDataPoint(DataPoint dt) {
        this.dt = dt;
    }

}
