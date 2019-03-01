/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import dataStructures.DataPoint;
import dataStructures.Stream;
import fileIO.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import orion.CBOrion;

/**
 *
 * @author phung
 */
public class IncrementingR_PerformanceAnalysis {

    public static void main(String[] args) throws Exception {

        /**
         * window size: 2000 slide size: 50 75 100 125 150 175 200 k: 0.25 r: 2
         * 16 27 39 56 70
         */
        String datasetName = null;
        Integer windowSize = null;
        Integer slideSize = null;
        Double k = null;
        Double r = 0.0;
        
        Double bestR = 0.0;
        Double bestF1 = 0.0;

        if (args.length == 1 && "-help".equals(args[0])) {
            System.out.println("to execute the orion outlier detection algorithm, specify all of these parameters:");
            System.out.println("-dataset        the file name of the dataset. The input dataset must be put in a folder named datasets");
            System.out.println("-window         size of the window");
            System.out.println("-slide          size of the slide. Must be smaller than or equal to the size of the window");
            System.out.println("-k              k metric for the k-integral");
            return;
        }

        for (int i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "-dataset":
                    datasetName = args[i + 1];
                    break;
                case "-window":
                    windowSize = Integer.parseInt(args[i + 1]);
                    break;
                case "-slide":
                    slideSize = Integer.parseInt(args[i + 1]);
                    break;
                case "-k":
                    k = Double.parseDouble(args[i + 1]);
                    break;
                default:
                    break;
            }
        }

        while (true) {
            
            // Increment r
            r += new Random().nextDouble();

            String inputFilePath = System.getProperty("user.dir") + "/datasets/" + datasetName + ".csv";
            char separator = ',';
            boolean hasHeader = false;
            double[][] incomingData = FileReader.readCSV(inputFilePath, separator, hasHeader);

            // Data stream
            Stream stream = new Stream(false); // Create a count-based timestamp stream
            stream.writeToStream(incomingData);

            // Perform outlier detection
            ArrayList<Boolean> allResult = new ArrayList<>(windowSize);
            ArrayList<Double> allProbability = new ArrayList<>(windowSize);
            CBOrion instance = new CBOrion(windowSize, slideSize, k, r);
            while (!stream.isEmpty()) {
                LinkedList<DataPoint> window = stream.readFromStream(windowSize);
                for (Object[] pred : instance.detectOutliers(window)) {
                    allResult.add((boolean) pred[0]);
                    allProbability.add((double) pred[1]);
                }
            }

            // Read the true output class
            String classFilePath = System.getProperty("user.dir") + "/datasets/" + datasetName + "_true.csv";
            double[][] trueClass = FileReader.readCSV(classFilePath, separator, hasHeader);

            // Compute the statistics
            double truePositive = 0;
            double falsePositive = 0;
            double trueNegative = 0;
            double falseNegative = 0;
            for (int i = 0; i < allResult.size(); ++i) {
                if (((int) trueClass[i][0]) == ((allResult.get(i) == true) ? 1 : 0)) {
                    if (allResult.get(i) == true) {
                        ++truePositive;
                    } else {
                        ++trueNegative;
                    }
                } else {
                    if ((((int) trueClass[i][0]) == 1) && (allResult.get(i) == false)) {
                        ++falseNegative;
                    } else {
                        ++falsePositive;
                    }
                }
            }

            double precision = truePositive / (truePositive + falsePositive);
            double recall = truePositive / (truePositive + falseNegative);
            double jaccardCoefficient = (truePositive + trueNegative) / (truePositive + trueNegative + falsePositive + falseNegative);
            double f1Score = (2 * precision * recall) / (precision + recall);

            bestF1 = Math.max(f1Score, bestF1);
            if (f1Score == bestF1) {
                bestR = r;
            }
            
            System.out.println("window: " + windowSize + "\tslide: " + slideSize + "\tk: " + k + "\tr: " + r);
            System.out.println("precision: " + precision + "\trecall: " + recall + "\tjc: " + jaccardCoefficient + "\tf1 score: " + f1Score);
            System.out.println("best f1 score so far: " + bestF1 + "\tbest r: " + bestR);
            System.out.println("\n\n");
        }
    }
}
