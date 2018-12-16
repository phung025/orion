/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStructures.DataPoint;
import dataStructures.Stream;
import fileIO.FileReader;

/**
 *
 * @author Nam Phung
 */
public class NewClass {

    public static void main(String[] args) {
        System.out.println("detectOutlier");

        String filePath = System.getProperty("user.dir") + "/datasets/tao.txt";
        char separator = ',';
        boolean hasHeader = false;
        double[][] incomingData = FileReader.readCSV(filePath, separator, hasHeader);

        // Data stream
        Stream stream = new Stream(false); // Create a count-based timestamp stream
        stream.writeToStream(incomingData);
        int count = 0;

        CBOrion instance = new CBOrion(100, 100, 20, 50);
        while (!stream.isEmpty()) {
            long startTime = System.nanoTime();
            DataPoint dt = stream.readFromStream();
            boolean result = instance.detectOutlier(dt);
            ++count;
            long endTime = System.nanoTime();
            System.out.println("Execution time: " + (endTime - startTime) / 1000000 + " ms");
        }
    }
}
