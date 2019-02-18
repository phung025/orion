/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evolutionaryEngine;

import dataStructures.Dimension;
import java.util.Random;
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

/**
 *
 * @author phung
 */
public class DimensionFactory extends AbstractCandidateFactory<Dimension> {

    private Dimension[] population = null;
    
    public DimensionFactory(Dimension[] dimensions) {
        this.population = dimensions;
    }
    
    @Override
    public Dimension generateRandomCandidate(Random rng) {
        return population[rng.nextInt(population.length)];
    }
    
}
