/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data_stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * FileReader class containing a static function that helps read a CSV file into
 * a 2D double array.
 * @author Nam Phung
 */
public class FileReader {

    /**
     * Read dataset from a CSV file into a 2d double arrays. If the first line in CSV
     * file contains the header, ignore it. The function will separate each line in
     * the file using a separator character.
     * 
     * @param filePath file path of the CSV file
     * @param separator character that separates each attribute in each line of the file
     * @param hasHeader boolean variable indicating if the CSV file contains a header or not
     * @return 2d double array containing all values from the CSV file
     */
    public static double[][] readCSV(String filePath, char separator, boolean hasHeader) {

        // The master array containing all processed data points in a file
        double[][] data = null;

        try {
            BufferedReader bfr = new BufferedReader(new java.io.FileReader(new File(filePath)));

            String line = "";
            try {

                // Count number of observations in the dataset
                int lines = (hasHeader) ? -1 : 0;
                while (bfr.readLine() != null) {
                    lines++;
                }
                data = new double[lines][];

                // Read the file in line-by-line
                bfr = new BufferedReader(new java.io.FileReader(new File(filePath)));
                if (hasHeader) {
                    bfr.readLine(); // Ignore the first line if it is a header
                }
                int observationIndex = 0;
                while ((line = bfr.readLine()) != null) {

                    // Split the line by the separator character
                    String[] atts = line.split(String.valueOf(separator));

                    // Create an array containing all attributes of an observation
                    double[] observation = new double[atts.length];
                    for (int i = 0; i < atts.length; ++i) {
                        observation[i] = Double.valueOf(atts[i]);
                    }

                    // Add observation into the master array containing all observations
                    data[observationIndex] = observation;
                    ++observationIndex;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return data;
    }
}
