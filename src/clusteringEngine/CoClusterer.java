/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clusteringEngine;

import java.util.ArrayList;
import java.util.Random;
import org.uncommons.maths.random.MersenneTwisterRNG;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author phung
 */
public class CoClusterer {

    private EM densityClusterer = null;
    private EM kIntegralClusterer = null;

    public CoClusterer() throws Exception {

        // Random number generator
        Random random = new MersenneTwisterRNG();

        // Initialize the density clusterer
        densityClusterer = new EM();
        densityClusterer.setMaxIterations(100);
        densityClusterer.setMaximumNumberOfClusters(3);
        densityClusterer.setMinLogLikelihoodImprovementCV(1.0E-6);
        densityClusterer.setMinLogLikelihoodImprovementIterating(1.0E-6);
        densityClusterer.setMinStdDev(1.0E-6);
        densityClusterer.setNumClusters(3);
        densityClusterer.setNumExecutionSlots(1);
        densityClusterer.setNumFolds(10);
        densityClusterer.setNumKMeansRuns(10);
        densityClusterer.setSeed(random.nextInt());

        // Initialize the k-integral clusterer
        kIntegralClusterer = new EM();
        kIntegralClusterer.setMaxIterations(100);
        kIntegralClusterer.setMaximumNumberOfClusters(3);
        kIntegralClusterer.setMinLogLikelihoodImprovementCV(1.0E-6);
        kIntegralClusterer.setMinLogLikelihoodImprovementIterating(1.0E-6);
        kIntegralClusterer.setMinStdDev(1.0E-6);
        kIntegralClusterer.setNumClusters(3);
        kIntegralClusterer.setNumExecutionSlots(1);
        kIntegralClusterer.setNumFolds(10);
        kIntegralClusterer.setNumKMeansRuns(10);
        kIntegralClusterer.setSeed(random.nextInt());

    }

    public double[][] clusterDensity(double[] densities) throws Exception {

        // List of all attributes in the clustering process
        ArrayList<Attribute> attributes = new ArrayList<>();
        Attribute densityAttr = new Attribute("density");
        attributes.add(densityAttr);

        // Create the instance for clustering algorithm
        Instances dataset = new Instances("clusteredByDensity", attributes, 0);
        for (int i = 0; i < densities.length; ++i) {
            Instance inst = new DenseInstance(1);
            inst.setValue(densityAttr, densities[i]);
            dataset.add(inst);
        }

        // Cluster all the instances in the density list
        densityClusterer.buildClusterer(dataset);
        ClusterEvaluation evaluator = new ClusterEvaluation();
        evaluator.setClusterer(densityClusterer);
        evaluator.evaluateClusterer(dataset);

        // Mean density of the clusters
        int numberOfClusters = densityClusterer.getClusterModelsNumericAtts().length;
        double[] clusterDensities = new double[numberOfClusters];
        for (int i = 0; i < numberOfClusters; ++i) {
            clusterDensities[i] = densityClusterer.getClusterModelsNumericAtts()[i][0][0];
        }

        return new double[][]{clusterDensities, evaluator.getClusterAssignments()};
    }

    public double[][] clusterKIntegral(double[] kIntegrals) throws Exception {

        // List of all attributes in the clustering process
        ArrayList<Attribute> attributes = new ArrayList<>();
        Attribute kIntegralAttr = new Attribute("k-integral");
        attributes.add(kIntegralAttr);

        // Create the instance for clustering algorithm
        Instances dataset = new Instances("clusteredByKIntegral", attributes, 0);
        for (int i = 0; i < kIntegrals.length; ++i) {
            Instance inst = new DenseInstance(1);
            inst.setValue(kIntegralAttr, kIntegrals[i]);
            dataset.add(inst);
        }

        // Cluster all the instances in the density list
        kIntegralClusterer.buildClusterer(dataset);
        ClusterEvaluation evaluator = new ClusterEvaluation();
        evaluator.setClusterer(kIntegralClusterer);
        evaluator.evaluateClusterer(dataset);

        // Mean density of the clusters
        int numberOfClusters = kIntegralClusterer.getClusterModelsNumericAtts().length;
        double[] clusterKIntegrals = new double[numberOfClusters];
        for (int i = 0; i < numberOfClusters; ++i) {
            clusterKIntegrals[i] = kIntegralClusterer.getClusterModelsNumericAtts()[i][0][0];
        }

        return new double[][]{clusterKIntegrals, evaluator.getClusterAssignments()};
    }

}
