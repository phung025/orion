# Orion

Orion is a cluster-based outlier detection algorithm that was introduced by Le Gruenwald and Eleazar Leal from the University of Oklahoma and Shiblee Sadik from Amazon Web Services. Orion uses an evolutionary algorithm and a clustering algorithm to detect anomalies within a dataset. Using an initial set of dimensions of a dataset, Orion uses an evolutionary algorithm to evolve the population set to find a dimension that minimizes the stream density of a data point. Using the stream density and another metric called k-integral, Orion performs co-clustering to assign the data point into a cluster. Based on the cluster, Orion decides whether the data point is an outlier or not.

This repository contains the implmentation of the algorithm in Java. This is part of the research project to re-implement the algorithm to analyze the performance of Orion against several different outlier detection algorithms that have been introduced in the past as well as come up with different approaches to improve the performance of Orion. 

# References:

[In Pursuit of Outliers in Multi-dimensional Data Streams](https://ieeexplore.ieee.org/document/7840642)
