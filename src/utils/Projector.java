/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import dataStructures.DataPoint;
import org.jblas.DoubleMatrix;

/**
 *
 * @author phung
 */
public class Projector {

    /**
     * Project a data point on a dimension. The projected point dt on the
     * dimension pDimension is computed as the linear combination of the 2
     * vectors.
     *
     * @param dt a data point
     * @param pDimension vector with same dimension as the data point
     * representing the new p-dimension.
     * @return the projected data point on the p-dimension.
     */
    public static double projectOnDimension(DataPoint dt, DoubleMatrix pDimension) {
        return dt.getValues().transpose().mmul(pDimension).sum();
    }
}
