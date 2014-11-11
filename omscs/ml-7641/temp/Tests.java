package edu.gatech.ml.assignment2;

import java.util.Arrays;
import java.util.Random;

import dist.DiscreteDependencyTree;
import dist.DiscretePermutationDistribution;
import dist.DiscreteUniformDistribution;
import dist.Distribution;
import func.nn.backprop.BackPropagationNetwork;
import func.nn.backprop.BackPropagationNetworkFactory;
import opt.DiscreteChangeOneNeighbor;
import opt.EvaluationFunction;
import opt.GenericHillClimbingProblem;
import opt.HillClimbingProblem;
import opt.NeighborFunction;
import opt.OptimizationAlgorithm;
import opt.RandomizedHillClimbing;
import opt.SimulatedAnnealing;
import opt.SwapNeighbor;
import opt.example.ContinuousPeaksEvaluationFunction;
import opt.example.CountOnesEvaluationFunction;
import opt.example.FourPeaksEvaluationFunction;
import opt.example.KnapsackEvaluationFunction;
import opt.example.NeuralNetworkOptimizationProblem;
import opt.example.TravelingSalesmanCrossOver;
import opt.example.TravelingSalesmanEvaluationFunction;
import opt.example.TravelingSalesmanRouteEvaluationFunction;
import opt.example.TravelingSalesmanSortEvaluationFunction;
import opt.ga.CrossoverFunction;
import opt.ga.DiscreteChangeOneMutation;
import opt.ga.GenericGeneticAlgorithmProblem;
import opt.ga.GeneticAlgorithmProblem;
import opt.ga.MutationFunction;
import opt.ga.SingleCrossOver;
import opt.ga.StandardGeneticAlgorithm;
import opt.ga.SwapMutation;
import opt.ga.UniformCrossOver;
import opt.prob.GenericProbabilisticOptimizationProblem;
import opt.prob.MIMIC;
import opt.prob.ProbabilisticOptimizationProblem;
import opt.test.ContinuousPeaksTest;
import opt.test.CountOnesTest;
import opt.test.FlipFlopTest;
import opt.test.KnapsackTest;
import opt.test.TravelingSalesmanTest;
import opt.test.XORTest;
import shared.DataSet;
import shared.ErrorMeasure;
import shared.FixedIterationTrainer;
import shared.Instance;
import shared.SumOfSquaresError;

public class Tests {

    private static int TESTING_TIME = 300;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//ContinuousPeaksTest.main(null);
		int NUM_ITERATIONS = 2;
		int[] TESTING_TIMES = {5, 10, 30, 60, 120};//, 300};
		for(int time: TESTING_TIMES) {
			TESTING_TIME = time;
			System.out.println("Testing for " + TESTING_TIME + " seconds.");
			int[] NS = {40, 80, 160, 320};
			for(int N: NS) {
				for(int i=0; i<NUM_ITERATIONS; i++) {
					//System.out.println("Continuous Peaks");
					//contPeaks(240);
					//CountOnesTest.main(null);
					//System.out.println("Count 1s");
					//count1s(320);
					//FlipFlopTest.main(null);
					//System.out.println("Flip Flop");
					//flipflop(N);
					//KnapsackTest.main(null);
					//System.out.println("Knapsack");
					//knapsack(N);
					//TravelingSalesmanTest.main(null);
					System.out.println("TSP");
					tsp(N);
					//XORTest.main(null);
				}
			}
		}
	}
	
	public static void contPeaks(int N) {
		System.out.println("N = " + N);
		int T=N/10;
        int[] ranges = new int[N];
        Arrays.fill(ranges, 2);
        EvaluationFunction ef = new ContinuousPeaksEvaluationFunction(T);
        Distribution odd = new DiscreteUniformDistribution(ranges);
        NeighborFunction nf = new DiscreteChangeOneNeighbor(ranges);
        MutationFunction mf = new DiscreteChangeOneMutation(ranges);
        CrossoverFunction cf = new SingleCrossOver();
        Distribution df = new DiscreteDependencyTree(.1, ranges); 
        HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);
        GeneticAlgorithmProblem gap = new GenericGeneticAlgorithmProblem(ef, odd, mf, cf);
        ProbabilisticOptimizationProblem pop = new GenericProbabilisticOptimizationProblem(ef, odd, df);
        FixedTimeTrainer fit;
        /*
        RandomizedHillClimbing rhc = new RandomizedHillClimbing(hcp);      
        FixedIterationTrainer fit = new FixedIterationTrainer(rhc, 200000);
        fit.train();
        System.out.println(ef.value(rhc.getOptimal()));
        */
        double val = -1;
        String winner = "";
        
        SimulatedAnnealing sa = new SimulatedAnnealing(1E11, .95, hcp);
        //fit = new FixedIterationTrainer(sa, 200000);
        fit = new FixedTimeTrainer(sa, TESTING_TIME);
        fit.train();
        val = ef.value(sa.getOptimal());
        winner = "SA";
        System.out.println(ef.value(sa.getOptimal()));
        
        StandardGeneticAlgorithm ga = new StandardGeneticAlgorithm(200, 100, 10, gap);
        //fit = new FixedIterationTrainer(ga, 1000);
        fit = new FixedTimeTrainer(ga, TESTING_TIME);
        fit.train();
        if(ef.value(ga.getOptimal()) > val) {
        	winner = "GA";
        	val = ef.value(ga.getOptimal());
        } else if (ef.value(ga.getOptimal()) == val) {
        	winner = winner + "/GA";
        }
        System.out.println(ef.value(ga.getOptimal()));
        
        MIMIC mimic = new MIMIC(200, 20, pop);
        //fit = new FixedIterationTrainer(mimic, 1000);
        fit = new FixedTimeTrainer(mimic, TESTING_TIME);
        fit.train();
        if(ef.value(mimic.getOptimal()) > val) {
        	winner = "MIMIC";
        	val = ef.value(mimic.getOptimal());
        } else if (ef.value(mimic.getOptimal()) == val) {
        	winner = winner + "/MIMIC";
        }
        System.out.println(ef.value(mimic.getOptimal()));
        System.out.println("Winner is " + winner);
	}
	
	public static void count1s(int N) {
		System.out.println("N = " + N);
		int[] ranges = new int[N];
        Arrays.fill(ranges, 2);
        EvaluationFunction ef = new CountOnesEvaluationFunction();
        Distribution odd = new DiscreteUniformDistribution(ranges);
        NeighborFunction nf = new DiscreteChangeOneNeighbor(ranges);
        MutationFunction mf = new DiscreteChangeOneMutation(ranges);
        CrossoverFunction cf = new UniformCrossOver();
        Distribution df = new DiscreteDependencyTree(.1, ranges); 
        HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);
        GeneticAlgorithmProblem gap = new GenericGeneticAlgorithmProblem(ef, odd, mf, cf);
        ProbabilisticOptimizationProblem pop = new GenericProbabilisticOptimizationProblem(ef, odd, df);
        FixedTimeTrainer fit;
        /*
        RandomizedHillClimbing rhc = new RandomizedHillClimbing(hcp);      
        FixedIterationTrainer fit = new FixedIterationTrainer(rhc, 200);
        fit.train();
        System.out.println(ef.value(rhc.getOptimal()));
        */
        
        double val = -1;
        String winner = "";
        
        SimulatedAnnealing sa = new SimulatedAnnealing(100, .95, hcp);
        //fit = new FixedIterationTrainer(sa, 200);
        fit = new FixedTimeTrainer(sa, TESTING_TIME);
        fit.train();
        val = ef.value(sa.getOptimal());
        winner = "SA";
        System.out.println(ef.value(sa.getOptimal()));
        
        StandardGeneticAlgorithm ga = new StandardGeneticAlgorithm(20, 20, 0, gap);
        //fit = new FixedIterationTrainer(ga, 300);
        fit = new FixedTimeTrainer(ga, TESTING_TIME);
        fit.train();
        if(ef.value(ga.getOptimal()) > val) {
        	winner = "GA";
        	val = ef.value(ga.getOptimal());
        } else if (ef.value(ga.getOptimal()) == val) {
        	winner = winner + "/GA";
        }
        System.out.println(ef.value(ga.getOptimal()));
        
        MIMIC mimic = new MIMIC(50, 10, pop);
        //fit = new FixedIterationTrainer(mimic, 100);
        fit = new FixedTimeTrainer(mimic, TESTING_TIME);
        fit.train();
        if(ef.value(mimic.getOptimal()) > val) {
        	winner = "MIMIC";
        	val = ef.value(mimic.getOptimal());
        } else if (ef.value(mimic.getOptimal()) == val) {
        	winner = winner + "/MIMIC";
        }
        System.out.println(ef.value(mimic.getOptimal()));
        System.out.println("Winner is " + winner);
	}

	public static void flipflop(int N) {
		System.out.println("N = " + N);
		int T=N/10;
        int[] ranges = new int[N];
        Arrays.fill(ranges, 2);
        EvaluationFunction ef = new FourPeaksEvaluationFunction(T);
        Distribution odd = new DiscreteUniformDistribution(ranges);
        NeighborFunction nf = new DiscreteChangeOneNeighbor(ranges);
        MutationFunction mf = new DiscreteChangeOneMutation(ranges);
        CrossoverFunction cf = new SingleCrossOver();
        Distribution df = new DiscreteDependencyTree(.1, ranges); 
        HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);
        GeneticAlgorithmProblem gap = new GenericGeneticAlgorithmProblem(ef, odd, mf, cf);
        ProbabilisticOptimizationProblem pop = new GenericProbabilisticOptimizationProblem(ef, odd, df);
        FixedTimeTrainer fit;
        /*
        RandomizedHillClimbing rhc = new RandomizedHillClimbing(hcp);      
        FixedIterationTrainer fit = new FixedIterationTrainer(rhc, 200000);
        fit.train();
        System.out.println(ef.value(rhc.getOptimal()));
        */
        double val = -1;
        String winner = "";
        
        SimulatedAnnealing sa = new SimulatedAnnealing(100, .95, hcp);
        //fit = new FixedIterationTrainer(sa, 200000);
        fit = new FixedTimeTrainer(sa, TESTING_TIME);
        fit.train();
        val = ef.value(sa.getOptimal());
        winner = "SA";
        System.out.println(ef.value(sa.getOptimal()));
        
        StandardGeneticAlgorithm ga = new StandardGeneticAlgorithm(200, 100, 20, gap);
        //fit = new FixedIterationTrainer(ga, 1000);
        fit = new FixedTimeTrainer(ga, TESTING_TIME);
        fit.train();
        if(ef.value(ga.getOptimal()) > val) {
        	winner = "GA";
        	val = ef.value(ga.getOptimal());
        } else if (ef.value(ga.getOptimal()) == val) {
        	winner = winner + "/GA";
        }
        System.out.println(ef.value(ga.getOptimal()));
        
        MIMIC mimic = new MIMIC(200, 5, pop);
        //fit = new FixedIterationTrainer(mimic, 1000);
        fit = new FixedTimeTrainer(mimic, TESTING_TIME);
        fit.train();
        if(ef.value(mimic.getOptimal()) > val) {
        	winner = "MIMIC";
        	val = ef.value(mimic.getOptimal());
        } else if (ef.value(mimic.getOptimal()) == val) {
        	winner = winner + "/MIMIC";
        }
        System.out.println(ef.value(mimic.getOptimal()));
        System.out.println("Winner is " + winner);
	}
	
	public static void knapsack(int NUM_ITEMS) {
	    System.out.println("N = " + NUM_ITEMS);
		/** Random number generator */
	     Random random = new Random();
	    /** The number of items */
	    /** The number of copies each */
	    int COPIES_EACH = 4;
	    /** The maximum weight for a single element */
	    double MAX_WEIGHT = 50;
	    /** The maximum volume for a single element */
	    double MAX_VOLUME = 50;
	    /** The volume of the knapsack */
	    double KNAPSACK_VOLUME = MAX_VOLUME * NUM_ITEMS * COPIES_EACH * .4;
	        int[] copies = new int[NUM_ITEMS];
	        Arrays.fill(copies, COPIES_EACH);
	        double[] weights = new double[NUM_ITEMS];
	        double[] volumes = new double[NUM_ITEMS];
	        for (int i = 0; i < NUM_ITEMS; i++) {
	            weights[i] = random.nextDouble() * MAX_WEIGHT;
	            volumes[i] = random.nextDouble() * MAX_VOLUME;
	        }
	         int[] ranges = new int[NUM_ITEMS];
	        Arrays.fill(ranges, COPIES_EACH + 1);
	        EvaluationFunction ef = new KnapsackEvaluationFunction(weights, volumes, KNAPSACK_VOLUME, copies);
	        Distribution odd = new DiscreteUniformDistribution(ranges);
	        NeighborFunction nf = new DiscreteChangeOneNeighbor(ranges);
	        MutationFunction mf = new DiscreteChangeOneMutation(ranges);
	        CrossoverFunction cf = new UniformCrossOver();
	        Distribution df = new DiscreteDependencyTree(.1, ranges); 
	        HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);
	        GeneticAlgorithmProblem gap = new GenericGeneticAlgorithmProblem(ef, odd, mf, cf);
	        ProbabilisticOptimizationProblem pop = new GenericProbabilisticOptimizationProblem(ef, odd, df);
	        FixedTimeTrainer fit;
	        /*
	        RandomizedHillClimbing rhc = new RandomizedHillClimbing(hcp);      
	        FixedIterationTrainer fit = new FixedIterationTrainer(rhc, 200000);
	        fit.train();
	        System.out.println(ef.value(rhc.getOptimal()));
	        */
	        double val = -1;
	        String winner = "";
	        
	        SimulatedAnnealing sa = new SimulatedAnnealing(100, .95, hcp);
	        //fit = new FixedIterationTrainer(sa, 200000);
	        fit = new FixedTimeTrainer(sa, TESTING_TIME);
	        fit.train();
	        val = ef.value(sa.getOptimal());
	        winner = "SA";
	        System.out.println(ef.value(sa.getOptimal()));
	        
	        StandardGeneticAlgorithm ga = new StandardGeneticAlgorithm(200, 150, 25, gap);
	        //fit = new FixedIterationTrainer(ga, 1000);
	        fit = new FixedTimeTrainer(ga, TESTING_TIME);
	        fit.train();
	        if(ef.value(ga.getOptimal()) > val) {
	        	winner = "GA";
	        	val = ef.value(ga.getOptimal());
	        } else if (ef.value(ga.getOptimal()) == val) {
	        	winner = winner + "/GA";
	        }
	        System.out.println(ef.value(ga.getOptimal()));
	        
	        MIMIC mimic = new MIMIC(200, 100, pop);
	        //fit = new FixedIterationTrainer(mimic, 1000);
	        fit = new FixedTimeTrainer(mimic, TESTING_TIME);
	        fit.train();
	        if(ef.value(mimic.getOptimal()) > val) {
	        	winner = "MIMIC";
	        	val = ef.value(mimic.getOptimal());
	        } else if (ef.value(mimic.getOptimal()) == val) {
	        	winner = winner + "/MIMIC";
	        }
	        System.out.println(ef.value(mimic.getOptimal()));
	        System.out.println("Winner is " + winner);
	}

	public static void tsp(int N) {
		System.out.println("N = " + N);
        Random random = new Random();
        // create the random points
        double[][] points = new double[N][2];
        for (int i = 0; i < points.length; i++) {
            points[i][0] = random.nextDouble();
            points[i][1] = random.nextDouble();   
        }
        // for rhc, sa, and ga we use a permutation based encoding
        TravelingSalesmanEvaluationFunction ef = new TravelingSalesmanRouteEvaluationFunction(points);
        Distribution odd = new DiscretePermutationDistribution(N);
        NeighborFunction nf = new SwapNeighbor();
        MutationFunction mf = new SwapMutation();
        CrossoverFunction cf = new TravelingSalesmanCrossOver(ef);
        HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);
        GeneticAlgorithmProblem gap = new GenericGeneticAlgorithmProblem(ef, odd, mf, cf);
        FixedTimeTrainer fit;
        /*
        RandomizedHillClimbing rhc = new RandomizedHillClimbing(hcp);      
        FixedIterationTrainer fit = new FixedIterationTrainer(rhc, 200000);
        fit.train();
        System.out.println(ef.value(rhc.getOptimal()));
        */
        double val = -1;
        String winner = "";
        
        SimulatedAnnealing sa = new SimulatedAnnealing(1E12, .95, hcp);
        //fit = new FixedIterationTrainer(sa, 200000);
        fit = new FixedTimeTrainer(sa, TESTING_TIME);
        fit.train();
        val = ef.value(sa.getOptimal());
        winner = "SA";
        System.out.println(ef.value(sa.getOptimal()));
        
        StandardGeneticAlgorithm ga = new StandardGeneticAlgorithm(200, 150, 20, gap);
        //fit = new FixedIterationTrainer(ga, 1000);
        fit = new FixedTimeTrainer(ga, TESTING_TIME);
        fit.train();
        if(ef.value(ga.getOptimal()) > val) {
        	winner = "GA";
        	val = ef.value(ga.getOptimal());
        } else if (ef.value(ga.getOptimal()) == val) {
        	winner = winner + "/GA";
        }
        System.out.println(ef.value(ga.getOptimal()));
        
        // for mimic we use a sort encoding
        ef = new TravelingSalesmanSortEvaluationFunction(points);
        int[] ranges = new int[N];
        Arrays.fill(ranges, N);
        odd = new  DiscreteUniformDistribution(ranges);
        Distribution df = new DiscreteDependencyTree(.1, ranges); 
        ProbabilisticOptimizationProblem pop = new GenericProbabilisticOptimizationProblem(ef, odd, df);
        
        MIMIC mimic = new MIMIC(200, 100, pop);
        //fit = new FixedIterationTrainer(mimic, 1000);
        fit = new FixedTimeTrainer(mimic, TESTING_TIME);
        fit.train();
        if(ef.value(mimic.getOptimal()) > val) {
        	winner = "MIMIC";
        	val = ef.value(mimic.getOptimal());
        } else if (ef.value(mimic.getOptimal()) == val) {
        	winner = winner + "/MIMIC";
        }
        System.out.println(ef.value(mimic.getOptimal()));
        System.out.println("Winner is " + winner);
	}
	
	public static void xor() {
        BackPropagationNetworkFactory factory = 
                new BackPropagationNetworkFactory();
            double[][][] data = {
                   { { 1, 1, 1, 1 }, { 0 } },
                   { { 1, 0, 1, 0 }, { 1 } },
                   { { 0, 1, 0, 1 }, { 1 } },
                   { { 0, 0, 0, 0 }, { 0 } }
            };
            Instance[] patterns = new Instance[data.length];
            for (int i = 0; i < patterns.length; i++) {
                patterns[i] = new Instance(data[i][0]);
                patterns[i].setLabel(new Instance(data[i][1]));
            }
            BackPropagationNetwork network = factory.createClassificationNetwork(
               new int[] { 4, 3, 1 });
            ErrorMeasure measure = new SumOfSquaresError();
            DataSet set = new DataSet(patterns);
            NeuralNetworkOptimizationProblem nno = new NeuralNetworkOptimizationProblem(
                set, network, measure);
            OptimizationAlgorithm o = new RandomizedHillClimbing(nno);
            FixedIterationTrainer fit = new FixedIterationTrainer(o, 5000);
            fit.train();
            Instance opt = o.getOptimal();
            network.setWeights(opt.getData());
            for (int i = 0; i < patterns.length; i++) {
                network.setInputValues(patterns[i].getData());
                network.run();
                System.out.println("~~");
                System.out.println(patterns[i].getLabel());
                System.out.println(network.getOutputValues());
            }
	}
}
