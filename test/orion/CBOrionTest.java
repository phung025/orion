/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orion;

import dataStructures.DataPoint;
import dataStructures.Stream;
import fileIO.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.Statistics;

/**
 *
 * @author Nam Phung
 */
public class CBOrionTest {

    public CBOrionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of detectOutlier method, of class CBOrion.
     *
     * @throws java.lang.InterruptedException
     */
    @Test
    public void testDetectOutlier() throws InterruptedException, Exception {
        int[] w_size = new int[]{500}; // window size
        int[] s_size = new int[]{250, 300, 350, 400}; // slide size
        double[] k_values = new double[]{0.2, 0.25, 0.3}; // different k values
        double[] r_values = new double[]{10, 20, 30, 40, 50}; // different r values

        for (int windowSize : w_size) {
            for (int slideSize : s_size) {
                for (double k : k_values) {
                    for (double r : r_values) {

                        List<Double> precisions = new LinkedList<>();
                        List<Double> recalls = new LinkedList<>();
                        List<Double> jaccardCoefficients = new LinkedList<>();
                        List<Double> f1Scores = new LinkedList<>();

                        for (int iteration = 0; iteration < 3; ++iteration) {

                            String datasetName = "mulcross";
                            System.out.println("iteration " + iteration);
                            System.out.println("detecting outliers in " + datasetName + " dataset");
                            System.out.println("window size: " + windowSize);
                            System.out.println("slide siz: " + slideSize);
                            System.out.println("k: " + k);
                            System.out.println("r " + r);
                            System.out.println("\n\n");

                            String inputFilePath = System.getProperty("user.dir") + "/datasets/" + datasetName + ".csv";
                            char separator = ',';
                            boolean hasHeader = false;
                            double[][] incomingData = FileReader.readCSV(inputFilePath, separator, hasHeader);

                            // Data stream
                            Stream stream = new Stream(false); // Create a count-based timestamp stream
                            stream.writeToStream(incomingData);

                            // Perform outlier detection
                            ArrayList<Boolean> allResult = new ArrayList<>(windowSize);
                            CBOrion instance = new CBOrion(windowSize, slideSize, 50);
                            while (!stream.isEmpty()) {
                                LinkedList<DataPoint> window = stream.readFromStream(windowSize);
                                for (boolean pred : instance.detectOutliers(window)) {
                                    allResult.add(pred);
                                }
                            }

                            // Read the true output class
                            String classFilePath = System.getProperty("user.dir") + "/datasets/mulcross_true.csv";
                            double[][] trueClass = FileReader.readCSV(classFilePath, separator, hasHeader);

                            // Compute the statistics
                            double truePositive = 0;
                            double falsePositive = 0;
                            double trueNegative = 0;
                            double falseNegative = 0;
                            for (int i = 0; i < trueClass.length; ++i) {
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
                        }

                        // Save the statistics to file
                        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("output1.csv", true)))) {
                            out.println("--------------------------Performance Analysis--------------------------");
                            out.println("window size: " + windowSize);
                            out.println("slide size: " + slideSize);
                            out.println("k: " + k);
                            out.println("r: " + r);
                            out.println("precision: " + Statistics.computeMean(precisions));
                            out.println("recall: " + Statistics.computeMean(recalls));
                            out.println("jaccard coefficient: " + Statistics.computeMean(jaccardCoefficients));
                            out.println("f1 score: " + Statistics.computeMean(f1Scores));
                            out.println("--------------------------Performance Analysis--------------------------");
                            out.println("");
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
