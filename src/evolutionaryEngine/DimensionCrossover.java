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
import java.util.stream.Collectors;
import utils.Statistics;
import org.jblas.DoubleMatrix;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.operators.AbstractCrossover;
import utils.Projector;

/**
 *
 * @author phung
 */
public class DimensionCrossover extends AbstractCrossover<Dimension> {
    
    private Slide slide = null; // The slide containing all current active data points

    public DimensionCrossover(Slide slide, int crossoverPoints, Probability probability) {
        super(crossoverPoints, probability);
        this.slide = slide;
    }

    @Override
    protected List<Dimension> mate(Dimension t1, Dimension t2, int k, Random random) {

        double[] d1 = new double[t1.getValues().data.length];
        double[] d2 = new double[t1.getValues().data.length];

        // Perform crossover according to the paper
        int cutoff = random.nextInt(t1.getValues().data.length);
        for (int i = 0; i < t1.getValues().data.length; ++i) {
            if (i != cutoff) {
                double gamma = random.nextDouble();
                d1[i] = (gamma * t1.getValues().data[i]) + ((1 - gamma) * t2.getValues().data[i]);
                d2[i] = (gamma * t2.getValues().data[i]) + ((1 - gamma) * t1.getValues().data[i]);
            } else {
                d1[i] = (random.nextDouble() - 1.0) + (random.nextDouble());
                d2[i] = (random.nextDouble() - 1.0) + (random.nextDouble());
            }
        }
        DoubleMatrix dimension1 = new DoubleMatrix(d1);
        DoubleMatrix dimension2 = new DoubleMatrix(d2);

        List<Double> projected1 =  ((List<DataPoint>) slide).parallelStream().map(x -> Projector.projectOnDimension(x, dimension1)).collect(Collectors.toList());
        Dimension offspring1 = new Dimension(dimension1, Statistics.computeMean(projected1), Statistics.computeVariance(projected1));
        
        List<Double> projected2 = ((List<DataPoint>) slide).parallelStream().map(x -> Projector.projectOnDimension(x, dimension2)).collect(Collectors.toList());
        Dimension offspring2 = new Dimension(dimension2, Statistics.computeMean(projected2), Statistics.computeVariance(projected2));

        List<Dimension> offsprings = new LinkedList();
        offsprings.add(offspring1);
        offsprings.add(offspring2);
        return offsprings;
    }

}
