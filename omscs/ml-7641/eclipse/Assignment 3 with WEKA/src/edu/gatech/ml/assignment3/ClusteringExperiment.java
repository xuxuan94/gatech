package edu.gatech.ml.assignment3;

import util.linalg.DenseVector;
import util.linalg.Vector;
import weka.clusterers.AbstractClusterer;
import weka.core.Instances;


public class ClusteringExperiment {

	private AbstractClusterer func;
	private boolean timing;
	private int labels;
	private Instances labeledSet;

	public ClusteringExperiment(AbstractClusterer func, int labels) {
		this.func = func;
		this.labels = labels;
	}
	
	public void setTiming(boolean timing) {
		this.timing = timing;
	}
	
	public void setSet(Instances labeledSet) {
		this.labeledSet = labeledSet;
	}
	
	public void runExperiment(Instances set) {
		long startTime = System.nanoTime();
		try {
			func.buildClusterer(set);
			long estimateTime = System.nanoTime();
			if(timing) {
				System.out.println("Estimating took " + (estimateTime - startTime) / ((double)1000000 * 1000) + " seconds");
			}
			printResults(set);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void printResults(Instances set) throws Exception {
		int total = 0;
        int matching = 0;
        
		int[] cluster_sizes = new int[this.labels];
        int[] original_cluster_sizes = new int[this.labels];

        Vector[] pred_to_orig = new Vector[this.labels];
        Vector[] orig_to_pred = new Vector[this.labels];
        
        for(int i=0; i<this.labels; i++) {
        	pred_to_orig[i] = new DenseVector(this.labels);
        	orig_to_pred[i] = new DenseVector(this.labels);
        }
        
        for(int i=0; i<set.size(); i++) {
        	int correct_label = (int)labeledSet.get(i).classValue();
        	int predicted_label = func.clusterInstance(set.get(i));
        	
        	original_cluster_sizes[correct_label]++;
        	cluster_sizes[predicted_label]++;
        	orig_to_pred[correct_label].set(predicted_label, orig_to_pred[correct_label].get(predicted_label) + 1);
        	pred_to_orig[predicted_label].set(correct_label, pred_to_orig[predicted_label].get(correct_label) + 1);

        	for(int j=0; j<i; j++) {
        		int correct_label_j =  (int)labeledSet.get(j).classValue();
        		if (correct_label == correct_label_j) {
        			total++;
        			int predicted_label_j = func.clusterInstance(set.get(j));
        			if(predicted_label == predicted_label_j) {
        				matching++;
        			}
        		}
        	}        	
        }
        System.out.println("P = " + ((double)matching)/total);

        double c1 = 0;
        int c1_count = 0;
        double c2 = 0;
        int c2_count = 0;
        
        for(int i=0; i<this.labels;i++) {
        	c1 = (c1*c1_count +(((double)pred_to_orig[i].get(pred_to_orig[i].argMax()))/cluster_sizes[i]))/(c1_count + 1); 
        	c2 = (c2*c2_count + (((double)orig_to_pred[i].get(orig_to_pred[i].argMax()))/original_cluster_sizes[i]))/(c2_count + 1);
        	
        	c1_count++;
        	c2_count++;
        }
        System.out.println("C1 = " + c1);
        System.out.println("C2 = " + c2);

		
		//p(set);
		//C(set);
	}
	
}
