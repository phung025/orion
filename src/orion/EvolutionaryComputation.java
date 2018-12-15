/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStructures.DataPoint;
import java.util.List;
import java.util.Random;
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
     *
     * @param sd
     */
    public EvolutionaryComputation(StreamDensity sd) {
        this.sdEstimator = sd;
    }

    public void evolve(List<DoubleMatrix> candidateDimensions, DataPoint dt, List<DataPoint> allDataPoints) {
        
        // Return the best-fit p-dimension together with the stream density of the data point in that dimension
    }

    /**
     *
     * @param dimensionX
     * @param dimensionY
     * @return
     */
    public DoubleMatrix crossover(DoubleMatrix dimensionX, DoubleMatrix dimensionY) {

        int cutoff = new Random().nextInt(dimensionX.data.length);
        double[] dimensionZ = new double[dimensionX.data.length];

        // Perform crossover
        for (int i = 0; i < dimensionX.length; ++i) {
            if (i != cutoff) {
                double gamma = new Random().nextDouble();
                dimensionZ[i] = (gamma * dimensionX.data[i]) + ((1 - gamma) * dimensionY.data[i]);
            } else {
                dimensionZ[i] = (new Random().nextDouble() - 1.0) + (new Random().nextDouble());;
            }
        }
        return new DoubleMatrix(dimensionZ);
    }

    public DoubleMatrix mutate(DoubleMatrix dimension) {
        return null;
    }
}
