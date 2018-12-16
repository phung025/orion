/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStructures;

import org.jblas.DoubleMatrix;

/**
 *
 * @author Nam Phung
 */
public class Dimension {
    public DoubleMatrix values; // The value vector to project original data point to this dimension
    public double mean; // Mean of all values along this dimension
    public double variance; // Variance of all values along this dimension
    
    private Dimension() {
        
    }
    
    public Dimension(DoubleMatrix values, double mean, double variance) {
        this.values = values;
        this.mean = mean;
        this.variance = variance;
    }
}
