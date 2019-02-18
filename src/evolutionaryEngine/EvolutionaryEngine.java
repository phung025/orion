/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evolutionaryEngine;

import dataStructures.DataPoint;
import dataStructures.Dimension;
import dataStructures.Slide;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.selection.TruncationSelection;
import org.uncommons.watchmaker.framework.termination.GenerationCount;
import outlierMetrics.StreamDensity;

/**
 *
 * @author phung
 */
public class EvolutionaryEngine {

    private StreamDensity sdEstimator = null; // Stream density estimator used by the fitness function to rank the dimension
    private Slide slide = null; // The slide containing current active data points being processed

    EvolutionaryOperator<Dimension> pipeline = null; // Compound evolutionary operator that chains together multiple operators of the same type
    DimensionEvaluator fitnessEvaluator = null; // Fitness evaluator of a candidate dimension
    SelectionStrategy<Object> selection = null; // Strategy for selecting a candidade
    Random rng = null; // Random number generator

    public EvolutionaryEngine(StreamDensity estimator, Slide slide) {
        this.sdEstimator = estimator;
        this.slide = slide;

        // Add operators for evolutionary computation
        List<EvolutionaryOperator<Dimension>> operators = new LinkedList<>();
        operators.add(new DimensionCrossover(this.slide, 1, new Probability(0.05)));
        this.pipeline = new EvolutionPipeline<>(operators);

        // Setup the fitness evaluator
        this.fitnessEvaluator = new DimensionEvaluator(null, this.sdEstimator);

        // Setup the candidate selection strategy
        this.selection = new TruncationSelection(0.5);

        // Setup the random number generator
        this.rng = new MersenneTwisterRNG();
    }

    /**
     *
     * @param population
     * @param dt
     * @param epochs
     * @return
     */
    public Dimension evolve(Dimension[] population, DataPoint dt, int epochs) {

        // Factory for candidate dimensions to choose from for performing mutation, crossover, etc
        CandidateFactory<Dimension> factory = new DimensionFactory(population);

        // Setup the fitness evaluator
        fitnessEvaluator.setDataPoint(dt);

        // Evolutionary engine
        EvolutionEngine<Dimension> engine = new GenerationalEvolutionEngine<>(factory, this.pipeline, this.fitnessEvaluator, this.selection, this.rng);

        return engine.evolve(population.length, 0, new GenerationCount(epochs));
    }
}
