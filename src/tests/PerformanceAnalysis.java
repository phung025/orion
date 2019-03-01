/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import dataStructures.DataPoint;
import dataStructures.Stream;
import fileIO.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import orion.CBOrion;
import utils.Statistics;

/**
 *
 * @author phung
 */
public class PerformanceAnalysis {

    public static void main(String[] args) throws Exception {

        /**
         * window size: 2000 slide size: 50 75 100 125 150 175 200 k: 0.25 r: 2
         * 16 27 39 56 70
         */
        String datasetName = null;
        Integer windowSize = null;
        Integer slideSize = null;
        Double k = null;
        Double r = null;
        char hasOutput = ' ';

        if (args.length == 1 && "-help".equals(args[0])) {
            System.out.println("to execute the orion outlier detection algorithm, specify all of these parameters:");
            System.out.println("-dataset        the file name of the dataset. The input dataset must be put in a folder named datasets");
            System.out.println("-window         size of the window");
            System.out.println("-slide          size of the slide. Must be smaller than or equal to the size of the window");
            System.out.println("-r              distance r");
            System.out.println("-k              k metric for the k-integral");
            System.out.println("-output         determines if an output file will be created (t/f)");
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
                case "-r":
                    r = Double.parseDouble(args[i + 1]);
                    break;
                case "-output":
                    hasOutput = args[i + 1].charAt(0);
                    break;
                default:
                    break;
            }
        }

        List<Double> precisions = new LinkedList<>();
        List<Double> recalls = new LinkedList<>();
        List<Double> jaccardCoefficients = new LinkedList<>();
        List<Double> f1Scores = new LinkedList<>();

        System.out.println("detecting outliers in " + datasetName + " dataset");
        System.out.println("window size: " + windowSize);
        System.out.println("slide size: " + slideSize);
        System.out.println("k: " + k);
        System.out.println("r " + r);

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
        int window_count = 0;
        while (!stream.isEmpty()) {
            System.out.println("Detecting outliers in window " + ++window_count + "...");
            LinkedList<DataPoint> window = stream.readFromStream(windowSize);
            for (Iterator<Object[]> iter = instance.detectOutliers(window).iterator(); iter.hasNext();) {
                Object[] pred = iter.next();
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

        precisions.add(truePositive / (truePositive + falsePositive));
        recalls.add(truePositive / (truePositive + falseNegative));
        jaccardCoefficients.add((truePositive + trueNegative) / (truePositive + trueNegative + falsePositive + falseNegative));
        f1Scores.add((2 * precisions.get(precisions.size() - 1) * recalls.get(recalls.size() - 1)) / (precisions.get(precisions.size() - 1) + recalls.get(recalls.size() - 1)));

        // Save the statistics to file
        if (hasOutput == 't') {
            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Arrays.toString(args) + ".txt", true)))) {
                out.println("--------------------------Performance Analysis--------------------------");
                out.println("dataset: " + datasetName);
                out.println("window size: " + windowSize);
                out.println("slide size: " + slideSize);
                out.println("k: " + k);
                out.println("r: " + r);
                out.println("");
                out.println("precision: " + Statistics.computeMean(precisions));
                out.println("recall: " + Statistics.computeMean(recalls));
                out.println("jaccard coefficient: " + Statistics.computeMean(jaccardCoefficients));
                out.println("f1 score: " + Statistics.computeMean(f1Scores));
                out.println("--------------------------Performance Analysis--------------------------");
                out.println("\n\n\n\n");
                allProbability.stream().forEach(x -> {
                    out.println(x.toString());
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
