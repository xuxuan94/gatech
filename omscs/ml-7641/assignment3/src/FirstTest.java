package edu.gatech.ml.assignment3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import func.EMClusterer;
import func.KMeansClusterer;
import shared.DataSet;
import shared.DataSetDescription;
import shared.Instance;
import shared.filt.IndependentComponentAnalysis;
import shared.filt.PrincipalComponentAnalysis;
import util.linalg.DenseVector;
import util.linalg.Vector;

public class FirstTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("/Users/swapnilr/gatech/omscs/ml-7641/modifiedLetter.data"));
			
	        String line;
	        List<Instance> data = new ArrayList<Instance>();
	        Pattern pattern = Pattern.compile("[ ,]+");
	        while ((line = br.readLine()) != null) {
	            String[] split = pattern.split(line.trim());
	            double[] input = new double[split.length - 1];
	            for (int i = 0; i < input.length; i++) {
	                input[i] = Double.parseDouble(split[i]);
	            }
	            int label = ((int)split[input.length].charAt(0) - (int)'A');
	            double[] labelValues = new double[26];
	            for(int i=0; i<26; i++) {
	            	if(i==label) labelValues[i]=1;
	            	else labelValues[i] = 0;
	            }
	            Instance instance = new Instance((Vector) new DenseVector(input), new Instance(labelValues));
	            data.add(instance);
	        }
	        br.close();
	        Instance[] instances = (Instance[]) data.toArray(new Instance[0]);
	        DataSet set = new DataSet(instances);
	        set.setDescription(new DataSetDescription(set));
	        //System.out.println(set.size());
	        KMeansClusterer k_26 = new KMeansClusterer(26);
	        //EMClusterer k_26 = new EMClusterer(26, 1E-6, 1000);
	        /*System.out.println(set.get(0).getData());
	        
	        long startTime = System.nanoTime();
	        PrincipalComponentAnalysis pca = new PrincipalComponentAnalysis(set) ;
	        long pcaTime = System.nanoTime();
	        pca.filter(set);
	        pca.getEigenValues();
	        System.out.println("PCA Time = " + (pcaTime - startTime) / ((double)1000000 * 1000));
	        IndependentComponentAnalysis ica = new IndependentComponentAnalysis(set);
	        long icaTime = System.nanoTime();
	        System.out.println(set.get(0).getData().size());
	        System.out.println(ica.getProjection().times(set.get(0).getData()).size());
	        System.out.println(set.get(0).getData());
	        System.out.println(ica.getProjection().times(set.get(0).getData()));
	        //ica.filter(set);
	        System.out.println("ICA Time = " + (icaTime - pcaTime) / ((double)1000000 * 1000));*/
	        k_26.estimate(set);
	        //long estimateTime = System.nanoTime();
	        //System.out.println("Estimate Time = " + (estimateTime - icaTime) / ((double)1000000 * 1000));
	        int[] cluster_sizes = new int[26];
	        int[] original_cluster_sizes = new int[26];
	        //int correct = 0;
	        
	        Vector[] pred_to_orig = new Vector[26];
	        //Vector[] orig_to_pred = new Vector[26];
	        
	        for(int i=0; i<26; i++) {
	        	pred_to_orig[i] = new DenseVector(26);
	        	//orig_to_pred[i] = new DenseVector(26);
	        }
	        int total = 0;
	        int matching = 0;
	        for(int i=0; i<set.size(); i++) {
	        	for(int j=0; j<i; j++) {
	        		if (set.get(i).getLabel().getData().argMax() == set.get(j).getLabel().getData().argMax()) {
	        			total++;
	        			if(k_26.distributionFor(set.get(i)).mode().getDiscrete() == k_26.distributionFor(set.get(j)).mode().getDiscrete()) {
	        				matching++;
	        			}
	        		}
	        	}
	        	
	        	/*original_cluster_sizes[set.get(i).getLabel().getData().argMax()]++;
	        	cluster_sizes[k_26.distributionFor(set.get(i)).mode().getDiscrete()]++;
	        	int correct_label = set.get(i).getLabel().getData().argMax();
	        	int predicted_label = k_26.distributionFor(set.get(i)).mode().getDiscrete();
	        	pred_to_orig[correct_label].set(predicted_label, pred_to_orig[correct_label].get(predicted_label) + 1);*/
	        	//orig_to_pred[correct_label].set(predicted_label, pred_to_orig[correct_label].get(predicted_label) + 1);
	        	
	        }
	        System.out.println(((double)matching)/total);
	        //System.out.println(correct);
	        //System.out.println(((double)correct)/set.size());
	        
	        //System.out.println();
	        long testTime = System.nanoTime();
	        //System.out.println("Test Time = " + (testTime - estimateTime) / ((double)1000000 * 1000));
	        for(int i=0; i<26; i++) {
	        	int max = pred_to_orig[i].argMax();
	        	//System.out.println("Matching label for " + i + " is " + max + " with " + pred_to_orig[i].get(max) + " of " + original_cluster_sizes[i]);	
	        }
	        
	        //System.out.println();
	        //System.out.println(cluster_sizes.toString());
	        
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}