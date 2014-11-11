package edu.gatech.ml.assignment2;

import opt.OptimizationAlgorithm;
import opt.RandomizedHillClimbing;
import opt.SimulatedAnnealing;
import opt.example.NeuralNetworkOptimizationProblem;
import opt.ga.StandardGeneticAlgorithm;
//import func.nn.FeedForwardLayer;
//import func.nn.FeedForwardNetwork;
//import func.nn.FeedForwardNode;
import func.nn.NeuralNetwork;
//import func.nn.activation.HyperbolicTangentSigmoid;
import func.nn.backprop.BackPropagationNetwork;
import func.nn.backprop.BackPropagationNetworkFactory;
import shared.DataSet;
//import shared.DataSet;
import shared.DataSetDescription;
//import shared.Instance;
import shared.ErrorMeasure;
//import shared.FixedIterationTrainer;
import shared.Instance;
import shared.SumOfSquaresError;

//import shared.DataSetReader;

public class DataSetReadingTest {
	
	private static void test(FixedTimeTrainer fit, OptimizationAlgorithm o,
			DataSet trainingSet, DataSet testSet, NeuralNetwork network){
		long startTime = System.nanoTime();
		fit.train();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.print("Time Taken = " + duration/1000000 + " milliseconds; "); 
  	   
		   Instance opt = o.getOptimal();
		   network.setWeights(opt.getData());
		   
		   int total = 0;
		   int correct = 0;
		   Instance[] patterns = trainingSet.getInstances();
		   for (int i = 0; i < patterns.length; i++) {
		     network.setInputValues(patterns[i].getData());
		     network.run();
		     //System.out.println(network.getOutputValues());
		     if(patterns[i].getLabel().getData().argMax() == network.getOutputValues().argMax()) {
		    	 correct++;
		     }
		     total++;
		   }
		   System.out.print(((double)correct/(double)total * 100) + "% correct; ");
		   
		   total = 0;
		   correct = 0;
		   patterns = testSet.getInstances();
		   for (int i = 0; i < patterns.length; i++) {
		     network.setInputValues(patterns[i].getData());
		     network.run();
		     //System.out.println(network.getOutputValues());
		     if(patterns[i].getLabel().getData().argMax() == network.getOutputValues().argMax()) {
		    	 correct++;
		     }
		     total++;
		   }
		   System.out.println(((double)correct/(double)total * 100) + "% correct");
	}

	public static void main(String[] args) {
		MyDataSetReader data = new MyDataSetReader("/Users/swapnilr/gatech/omscs/ml-7641/modifiedLetter.data");
		try {
			SubsettableDataSet set = data.read();
			//Vector v = i.getData();
			/*for(int k=0; k<set.size();k++) {
				Instance i = set.get(k);
				for(int d=0; d<i.size(); d++) {
					System.out.print(i.getDiscrete(d) + ", ");
				}
				System.out.println((char)('A' + i.getLabel().getDiscrete()));
			}*/
			System.out.println("Size = " + set.size());
			DataSetDescription desc = set.getDescription();
			System.out.println("AttributeCount = " + desc.getAttributeCount());
			//System.out.println(set.getInstances()[0]);
			//System.out.println(set.getInstances()[0].getLabel().getDiscrete());
			
			SubsettableDataSet trainingSet = set.getSubset(0, 16000);
			System.out.println("Size = " + trainingSet.size());
			DataSetDescription trainingDesc = trainingSet.getDescription();
			System.out.println("AttributeCount = " + trainingDesc.getAttributeCount());
			//System.out.println(trainingSet.getInstances()[0]);
			//System.out.println(trainingSet.getInstances()[0].getLabel());
			
			SubsettableDataSet testSet = set.getSubset(16000, set.size());
			System.out.println("Size = " + testSet.size());
			DataSetDescription testDesc = testSet.getDescription();
			System.out.println("AttributeCount = " + testDesc.getAttributeCount());
			
			
			//BackPropagationNetworkFactory factory = 
		    //        new BackPropagationNetworkFactory();
		    //    double[][][] data1 = {
		    //           { { 1, 1, 1, 1 }, { 0 } },
		    //           { { 1, 0, 1, 0 }, { 1 } },
		    //           { { 0, 1, 0, 1 }, { 1 } },
		    //           { { 0, 0, 0, 0 }, { 0 } }
		    //    };
		    //    Instance[] patterns = new Instance[data1.length];
		    //    for (int i = 0; i < patterns.length; i++) {
		    //       patterns[i] = new Instance(data1[i][0]);
		    //        patterns[i].setLabel(new Instance(data1[i][1]));
		    //    }
		    //    BackPropagationNetwork network = factory.createClassificationNetwork(
		    //       new int[] { 4, 3, 1 });
			
			// Input Layer
			/*FeedForwardLayer input = new FeedForwardLayer();
			for(int i=0;i<desc.getAttributeCount();i++) {
				input.addNode(new FeedForwardNode(new HyperbolicTangentSigmoid()));
			}
			// Hidden Layer 1
			FeedForwardLayer hidden1 = new FeedForwardLayer();
			for(int i=0;i<31;i++) {
				hidden1.addNode(new FeedForwardNode(new HyperbolicTangentSigmoid()));
			}
			// Hidden Layer 2
			FeedForwardLayer hidden2 = new FeedForwardLayer();
			for(int i=0;i<26;i++) {
				hidden2.addNode(new FeedForwardNode(new HyperbolicTangentSigmoid()));
			}
			// Output Layer
			FeedForwardLayer output = new FeedForwardLayer();
			for(int i=0;i<26;i++) {
				output.addNode(new FeedForwardNode(new HyperbolicTangentSigmoid()));
			}
			FeedForwardNetwork network = new FeedForwardNetwork();
			network.setInputLayer(input);
			network.addHiddenLayer(hidden1);
			network.addHiddenLayer(hidden2);
		    network.setOutputLayer(output);
			input.connect(hidden1);
			hidden1.connect(hidden2);
			for(int i=0; i<hidden2.getNodeCount();i++) {
				hidden2.getNode(i).connect(output.getNode(i));
			}
			System.out.println(network.getLinks().size());*/
			
			//network.connect();
		    //ErrorMeasure measure = new SumOfSquaresError();
		    //    DataSet set1 = new DataSet(patterns);
		   //NeuralNetworkOptimizationProblem nno = new NeuralNetworkOptimizationProblem(
		   //         trainingSet, network, measure);
		   //System.out.println(network.getLinks().size());
		   //int NUMBER_OF_ITERATIONS = 2000;
		   //OptimizationAlgorithm o = new RandomizedHillClimbing(nno);
		   //FixedIterationTrainer fit = new FixedIterationTrainer(o, NUMBER_OF_ITERATIONS);
		   //test(fit,o, trainingSet, testSet, network);
		   
		   //SimulatedAnnealing sa = new SimulatedAnnealing(100, .95, nno);
		   //fit = new FixedIterationTrainer(sa, NUMBER_OF_ITERATIONS);
		   //test(fit,sa, trainingSet, testSet, network);
		   //StandardGeneticAlgorithm ga = new StandardGeneticAlgorithm(200, 100, 20, nno);
	       //fit = new FixedIterationTrainer(ga, NUMBER_OF_ITERATIONS);
		   //test(fit,ga, trainingSet, testSet,network);
			int NUMBER_OF_SECONDS = 1800;
			System.out.println("Testing for " + NUMBER_OF_SECONDS + " seconds.");
		   BackPropagationNetworkFactory factory = 
		            new BackPropagationNetworkFactory();
		   BackPropagationNetwork network1 = factory.createClassificationNetwork(
		           new int[] { 16, 31, 26, 26 });
		        ErrorMeasure measure1 = new SumOfSquaresError();
		        //DataSet set = new DataSet(patterns);
		        NeuralNetworkOptimizationProblem nno1 = new NeuralNetworkOptimizationProblem(
		            trainingSet, network1, measure1);
		        OptimizationAlgorithm o1 = new RandomizedHillClimbing(nno1);
		        System.out.println("Randomized Hill Climbing");
		        //o1 = new StandardGeneticAlgorithm(2000, 1000, 200, nno1);
		        FixedTimeTrainer fit1 = new FixedTimeTrainer(o1, NUMBER_OF_SECONDS);
		        fit1.train();
		        test(fit1,o1, trainingSet, testSet,network1);
		        o1 = new StandardGeneticAlgorithm(2000, 1000, 200, nno1);
		        System.out.println("Genetic Algorithm");
		        fit1 = new FixedTimeTrainer(o1, NUMBER_OF_SECONDS);
		        fit1.train();
		        test(fit1,o1, trainingSet, testSet,network1);
		        o1 = new SimulatedAnnealing(100, .95, nno1);
		        System.out.println("Simulated Annealing");
		        //o1 = new StandardGeneticAlgorithm(2000, 1000, 20, nno1);
		        fit1 = new FixedTimeTrainer(o1, NUMBER_OF_SECONDS);
		        fit1.train();
		        test(fit1,o1, trainingSet, testSet,network1);
		        
		   
		   
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
