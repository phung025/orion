/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataStream;

import java.util.Arrays;
import org.jblas.DoubleMatrix;

/**
 * This is a wrapper class for a data point in a data stream. The DataPoint object
 * has 2 variables timestamp which indicates the arrival time of the data point and
 * a vector containing the values of that point.
 * @author Nam Phung
 */
public class DataPoint {
    
    private long timestamp = -1; // Arrival time of the data point
    private DoubleMatrix values = null; // Value vector of the data point
    
    private DataPoint() {
        // Default constructor not allowed
    }
    
    public DataPoint(long time_stamp, DoubleMatrix values) {
        
        // Initialize the data point by setting the observation's timestamp
        // and the value vector
        this.timestamp = time_stamp;
        this.values = values;
    }
    
    /**
     * Return the arrival time of the data point.
     * @return DataPoint's timestamp
     */
    public long getTimestamp() {
        return this.timestamp;
    }
    
    /**
     * Return the value vector of the data point
     * @return DataPoint's values as a vector
     */
    public DoubleMatrix getValues() {
        return this.values;
    }
    
    @Override
    public String toString() {
        return "@" + this.timestamp + "\t" + Arrays.toString(values.data);
    }
}
