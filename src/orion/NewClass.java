/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import java.util.LinkedList;
import math.Statistics;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

/**
 *
 * @author Nam Phung
 */
public class NewClass {

    public static void main(String[] args) {
        LinkedList<Double> vals = new LinkedList();
        vals.add(5.0);
        vals.add(9.0);
        vals.add(4.0);
        vals.add(6.0);
        vals.add(1.51);
        vals.add(3.14);
        vals.add(6.12);
        vals.add(9.4);

        double mean = Statistics.computeMean(vals);
        double variance = Statistics.computeVariance(vals);
        System.out.println(mean);
        System.out.println(variance);

        System.out.println("----");

        double removing = 6.12;
        double newMean = Statistics.revertMean(removing, mean, vals.size());
        System.out.println(newMean);
        System.out.println(Statistics.revertVariance(removing, variance, mean, vals.size(), newMean));

        vals.remove(removing);
        System.out.println(Statistics.computeVariance(vals));
    }
}
