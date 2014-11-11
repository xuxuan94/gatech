package edu.gatech.ml.assignment3;

import shared.DataSet;
import shared.filt.LinearDiscriminantAnalysis;
import func.EMClusterer;
import func.KMeansClusterer;

public class LDAClusterExperiments {


	public static void main(String[] args) {
		        
		String filename = "/Users/swapnilr/gatech/omscs/ml-7641/modifiedLetter.data";
		ReadDataset reader = new ReadDataset(filename);
		DataSet set = reader.read();
		LinearDiscriminantAnalysis pca = new LinearDiscriminantAnalysis(set) ;
        pca.filter(set);
        
		
		System.out.println("Doing K Means Clustering for " + filename);
		KMeansClusterer k_26 = new KMeansClusterer(26);
		ClusteringExperiment exp = new ClusteringExperiment(k_26, 26);
		exp.setTiming(true);
		exp.runExperiment(set);
		
		System.out.println("Doing EM Clustering for " + filename);
		EMClusterer em = new EMClusterer(26, 1E-6, 1000);
		exp = new ClusteringExperiment(em, 26);
		exp.setTiming(true);
		exp.runExperiment(set);
		
		filename = "/Users/swapnilr/gatech/omscs/ml-7641/waveform.data";
		ReadEMDataset emreader = new ReadEMDataset(filename);
		set = emreader.read();
		pca = new LinearDiscriminantAnalysis(set) ;
	    pca.filter(set);
	    
		System.out.println("Doing K Means Clustering for " + filename);
		KMeansClusterer k_3 = new KMeansClusterer(3);
		exp = new ClusteringExperiment(k_3, 3);
		exp.setTiming(true);
		exp.runExperiment(set);
		
		System.out.println("Doing EM Clustering for " + filename);
		em = new EMClusterer(3, 1E-6, 1000);
		exp = new ClusteringExperiment(em, 3);
		exp.setTiming(true);
		exp.runExperiment(set);
		
	

	}

	
	
}
