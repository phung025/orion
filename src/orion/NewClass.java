/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

/**
 *
 * @author Nam Phung
 */
public class NewClass {
    public static void main(String[] args) {
        DoubleMatrix A = new DoubleMatrix(new double[][] {
            {0, 1},
            {-2, -3}
        });
        
        ComplexDoubleMatrix[] t = Eigen.eigenvectors(A);
        
        System.out.println(t[0].getReal().getRow(0));
        
        
    }
}
