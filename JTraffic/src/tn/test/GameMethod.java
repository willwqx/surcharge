package tn.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.collections15.Transformer;

import tn.Assignment;
import tn.Demand;
import tn.Network;
import tn.TrafficNetwork;
import tn.algorithm.FrankWolfeRouter;
import tn.algorithm.ShortestPathRouter;
import tn.misc.LinkDoublePropertyMap;
import tn.misc.LinkReadOnlyProperty;
import tn.test.GameMethod.Game;

public class GameMethod {
	private TrafficNetwork trafficNetwork;//<R>instance of the traffic network class
	private Demand demand;//<R>link demands on each link
	private PrintWriter vulnerability;
	private PrintWriter failureProbFile;
	private PrintWriter routerProbFile;
	private PrintWriter TFile;
	private PrintWriter linkflowFile;
	private PrintWriter sFile;/*S-expect value*/
	/**
	 * Initializes the network.<br />
	 * Displays the {@link TrafficNetwork}.<br />
	 * Invokes the {@link run} method.
	 */
	public void test(){ 
		init();
		showNetwork();
		new Game().run();
		
	}
	
	/**
	 * Declares and instantiates {@link TestNetwork}.<br />
	 * Populates {@link TrafficNetwork} by invoking {@link TestNetwork.getNetwork} method.<br />
	 * Assigns demand to the edges in the {@link TrafficNetwork} by {@link TestNetwork.getDemand}.
	 */
	private void init(){
    	
		TestNetwork testNetwork = new TestNetwork(TestNetwork.NetworkId.SiouxFalls);
		trafficNetwork = testNetwork.getNetwork();
		demand = testNetwork.getDemand();
		
	}
	/**
	 * Prints out the TrafficNetwork nodes and edges with demand and free-flow time.
	 */
	private void showNetwork(){
		System.out.print(trafficNetwork.toString(new String[]{"FFTTime"}));
		System.out.print(demand);
		System.out.print("\n\n");
		
	}
	
	/**
	 * Inner class Game that runs the Game Theory algorithm to minimize vulnerability.
	 */
	class Game{
		public class CostProperty extends LinkReadOnlyProperty<Double> {

			public CostProperty() {
				super("S-Cost", GameMethod.this.trafficNetwork, 0.0);
			}

			@Override
			public Double get(int linkId) {				
				return Game.this.T.get(linkId);
			}

			@Override
			public LinkReadOnlyProperty<Double> clone(Network network) {
				return this;
			} 
		}
		
		public class CostTransformer implements Transformer<Integer, Number> {

			@Override
			public Double transform(Integer linkId) {				
				return Game.this.T.get(linkId);
			} 
		}
		
		
		LinkReadOnlyProperty<Double> costProperty;
		CostTransformer costTransformer;
		//DijkstraShortestPath<Integer, Integer> algorithm;
		FrankWolfeRouter router;
		ShortestPathRouter shortestroute;
		List<Integer> shortestPath;
		
		/**
		 * rho = probability the tester disables edge e at iteration n.<br />
		 * gamma = probability the router chooses edge e at iteration n.<br />
		 * S = s-expected cost of edge e at iteration n.<br />
		 * CF = cost of edge e in failure scenario F.<br />
		 * x = edge use probability differential of edge e at iteration n.<br />
		 * rho_n_1 = 
		 */
		LinkDoublePropertyMap rho, gamma, T,T_1, S, CF, x, rho_n_1, omega, difficulty, FFTTime, FFTTimeBackup, capacity,linkIterationCost;
		//double doubleT_1;
		
		//Assignment h;
		
		/**
		 * failed edge penalty weight
		 */
		final double beta=10;
		/**
		 * Aggressiveness of the tester
		 */
		final double theta=.5;
		/**
		 * sufficiently small convergence criterion
		 */
		final double epsilon = 0.0001;  //(((trafficNetwork.getLinkCount())^2)*(552.0/16.0));
		/**
		 * vulnerability of the present iteration
		 */
		double V;
		/**
		 * vulnerability of the previous iteration
		 */
		double Vn_1;
		/**
		 * iteration counter
		 */
		int n;
		/**
		 * sum of all demand
		 */
		private double totalDemandVolume;
		
		/**Initialize Game object to process the network and demand in the containing class.
		 * 
		 */
		public Game(){
			totalDemandVolume = demand.getTotal();			
		}
		
		/**
		 * Initializes variables for the Game Theory algorithm.
		 */
		private void GameInit(){
			costTransformer = new CostTransformer();
			costProperty = new CostProperty();
			rho = new LinkDoublePropertyMap("rho", trafficNetwork);
			gamma = new LinkDoublePropertyMap("gamma", trafficNetwork);
			
			T = new LinkDoublePropertyMap("T", trafficNetwork);
			T_1 = new LinkDoublePropertyMap("T_1", trafficNetwork);
			S = new LinkDoublePropertyMap("S", trafficNetwork);
			linkIterationCost = new LinkDoublePropertyMap("linkIterationCost", trafficNetwork);
			CF = new LinkDoublePropertyMap("CF", trafficNetwork);
			omega = new LinkDoublePropertyMap("omega", trafficNetwork);
			difficulty = new LinkDoublePropertyMap("difficulty", trafficNetwork);
			FFTTime = new LinkDoublePropertyMap("FFTTime", trafficNetwork);
			FFTTimeBackup = new LinkDoublePropertyMap("FFTTimeBackup", trafficNetwork);
			capacity = new LinkDoublePropertyMap("capacity", trafficNetwork);
			//h = new Assignment(network);
			x = new LinkDoublePropertyMap("linkFlows", trafficNetwork);
			
			double initRho = 1.0/trafficNetwork.getLinkCount();
			for(int link: trafficNetwork.getLinks()){
				rho.set(link, initRho);
				gamma.set(link, 0.0);
				T.set(link,initRho);
				omega.set(link, trafficNetwork.getOmega(link));
				difficulty.set(link, trafficNetwork.getDifficulty(link));
				FFTTime.set(link, trafficNetwork.getTravelTime(link));
				//System.out.print(FFTTime+"\n");
				FFTTimeBackup.set(link, trafficNetwork.getTravelTime(link));
				capacity.set(link, trafficNetwork.getCapacity(link));
				
			}
			
			V = 0;
			Vn_1 = 1e5;
			n = 1;
		}
		
		/**
		 * Update edge costs.
		 */
		private void updateEdgeCosts(){
			for(int link : trafficNetwork.getLinks()){
				/**
				 * C_ is the cost of edge e in a normal state
				 */
				double C_ = trafficNetwork.getFreeFlowTravelTime(link);
				if(rho.get(link)>0){
					CF.set(link, beta*C_);
					//CF.set(link, (1.1*76)*C_);//added alpha*|E|*FFTT
				}
				else{
					CF.set(link, C_);
				}
			}
		}
		/**
		 * Calculate s-expected edge costs.
		 */
		private void computeMeanEdgeCosts(){
			
			//T = new LinkDoublePropertyMap("T", trafficNetwork);
			//update for the FFTT
			if(n==1){
				T_1 = FFTTime.clone();
				
				//Syste.out.print(T_1+"\n");
			}
			for(int link: trafficNetwork.getLinks()){
				
				double C = trafficNetwork.getFreeFlowTravelTime(link);
				//double doubleT_1;
				//System.out.print(C+"\n");
				//double C = FFTTimeBackup.get(iteration);
				//double T_1;
				
				//doubleT_1 = updateLinkIterationCost(link);				
				//System.out.print(doubleT_1+"\n");				
				//S.set(link, C * (1-rho.get(link)) + beta * C * rho.get(link));
				
				S.set(link, C * (1-rho.get(link)) + CF.get(link) * rho.get(link));
				
				//System.out.print(S.get(link)+"\n");
				//T.set(link, (1/n)*S.get(link)+(1-1/n)*doubleT_1);
				
				double doubleN = Double.parseDouble(Integer.toString(n)); 
				T.set(link, (1/doubleN)*S.get(link)+(1-1/doubleN)*T_1.get(link));
				
				
				//System.out.print(T+"\n");
				//T_1.set(link, T.get(link));
				//System.out.print(T_1+"\n");
				//S.set(iteration, C * (1-rho.get(iteration)) + beta * C * rho.get(iteration));
				//T.set(iteration, (1/iteration)*S.get(iteration)+(1-1/iteration)*S.get(iteration-1));
				
				
				///System.out.printf("iteration: %s\n, %s -> %s\n", iteration, 
				//		S.get(iteration),
				//		C * (1-rho.get(iteration)) + beta * C * rho.get(iteration));
				//System.out.printf(  "%s -> %s\n", T.get(iteration),
				//		(1/iteration)*S.get(iteration)+(1-1/iteration)*S.get(iteration));
				//System.out.printf(" after set: %s\n", T.get(iteration));
			}
			T_1 = T.clone();
			trafficNetwork.setTravelTime(T);
			//System.out.printf("T.get(1) = %s\n", T.get(1));
		}
		
		/**
		 * Identify shortest path and travel demand between O-D pair.
		 */
		private void calculateShortestPath(){			
			shortestroute = new ShortestPathRouter(trafficNetwork, new CostProperty(), null);
			//router = new ShortestPathRouter(trafficNetwork, new CostProperty(), null);
			Assignment newA = router.route(demand);
			x = newA.getFlow();
			//h.combine(newA, (1-1.0/n)); //combine using MSA
		}
		
		/** Use Frankwolfe to assign the traffic based on current traffic condition*/
	    private void UEAssignment() throws FileNotFoundException
		{	
	    	
	    	try{
				double tolerance = 1e-4;
				FileOutputStream out = new FileOutputStream("FrankWolfeOuput.txt");
				PrintStream printStream = new PrintStream(out);
				router = new FrankWolfeRouter(tolerance, T, printStream);
				
			}
			catch(Exception e)
			{
				System.out.println("\nERROR ERROR ERROR\n");
			}
			Assignment newA = router.route(demand);
			x = newA.getFlow();
		}
		
		/** Update edge use probability.*/
		private void updateRouteProbability(){
			LinkDoublePropertyMap qlink = 
				new LinkDoublePropertyMap("qlink", trafficNetwork);
			
			double Q=0;
			for(int link: trafficNetwork.getLinks()){
			/*MSA	//gamma.set(link, (1.0/(1*n)) * (x.get(link)/totalDemandVolume) + (1-1.0/(1*n))*gamma.get(link));					
				//for(int F:trafficNetwork.getLinks()){
					//old entropy function approach
					//double q = Math.exp(theta* gamma.get(F)*CF.get(F)-1);
					//double q = gamma.get(F)*(CF.get(F)+omega.get(F))/difficulty.get(F);*/
				
				//Compute BPR Function with x as volume
				//PolynomialFlowModel flowModel = new PolynomialFlowModel(0.15, 4.0);
				//double q= flowModel.getTravelTime(FFTTime.get(link), capacity.get(link),x.get(link));
				double q=x.get(link);
				qlink.set(link, q); 
				Q+=q;
			}
			for(int edge: trafficNetwork.getLinks()){				
				gamma.set(edge, qlink.get(edge)/Q);
			}
		}
		
		/**
		 * Update edge costs.
		 */
		/*private void updateEdgeCosts(){
			for(int link : trafficNetwork.getLinks()){
				/**
				 * C_ is the cost of edge e in a normal state
				 */
				/*double C_ = trafficNetwork.getFreeFlowTravelTime(link);
				if(rho.get(link)>0){
					CF.set(link, beta*C_);
				}
				else{
					CF.set(link, C_);
				}
			}
	}*/
		
		/**
		 * Update the probability of which nodes the tester will fail.
		 */
		private void updateFailureProbability(){
			LinkDoublePropertyMap qF = 
					new LinkDoublePropertyMap("gF", trafficNetwork);
				
			double Q=0;
	
			for(int F:trafficNetwork.getLinks()){
				//old entropy function approach
				//double q = Math.exp(theta* gamma.get(F)*CF.get(F)-1);
				double q = gamma.get(F)*(T.get(F)+omega.get(F))/difficulty.get(F);
				qF.set(F, q); 
				Q+=q;
			}

			//rho_n_1 = rho.clone();

			for(int link: trafficNetwork.getLinks()){				
				rho.set(link, qF.get(link)/Q);
			}
		}
		/**
		 * The difference in vulnerability in two successive iterations.
		 */
		private double error;
		
		/*private Double updateLinkIterationCost(int linkId){
			//for(int link: trafficNetwork.getLinks()){
				linkIterationCost.set(linkId, T_1.get(linkId));
				return Game.this.T_1.get(linkId);
				//trafficNetwork.setFreeFlowTravelTime(T);
			//}
		}*/
		
		/**
		 * The objective function.
		 */
		private void updateVulnerability(){
			Vn_1 = V;
			V = 0;
			for(int link: trafficNetwork.getLinks()){
				//V = V + gamma.get(link) * rho.get(link) * CF.get(link);
				V = V + gamma.get(link) * rho.get(link) * T_1.get(link);
			}
			
			//error = 0.0;
			//for(int link:trafficNetwork.getLinks()){
				//error+=Math.abs(rho.get(link)-rho_n_1.get(link));
			//}	
			error = Math.abs(V - Vn_1);
		}
		
		
		/**
		 * Displays the vulnerability, failure probability, Router Probability, and S-Expected Link Cost for the iteration.
		 */
		private void showIteration(){
			//System.out.printf("*** %s \n", T.get(1));
			System.out.printf("Iteration #%d Vulnerability = %f\n",n,V);
			/*System.out.printf("Iteration #%d\nVulnerability = %f\nFailure Probability = \n%s\nRouter Probability = \n%s\n S-Expected Link Cost= \n%s\n Link Flows= \n%s\n",
					n, V, rho.toString(), gamma.toString(), T.toString(), x.toString());*/
			vulnerability.printf("%s\n",Double.toString(V));
			failureProbFile.printf("%d%s\n", n, rho.toRowString());
			routerProbFile.printf("%d%s\n", n, gamma.toRowString());
			TFile.printf("%d%s\n", n, T.toRowString());
			linkflowFile.printf("%d%s\n", n, x.toRowString());
			sFile.printf("%d%s\n", n, S.toRowString());
		}
		
		/**
		 * Displays the last iteration and the runtime in milliseconds.
		 */
		private void showResult(){
			showIteration();
			System.out.printf("Runtime =%d ms\n", runtime);
			
		}
		
		/**
		 * The runtime of the entire algorithm in milliseconds.
		 */
		long runtime;
		
		/**
		 * Runs the initialization method {@link Game.GameInit}.<br />
		 * Executes the main Game Theory algorithm loop.<br />
		 * Displays the result including the runtime using the {@link Game.showResult} method.
		 */
		public void run(){
			runtime = System.currentTimeMillis();
			
			try {
				String dir = "../JTraffic";
				vulnerability = new PrintWriter(new FileWriter(dir + "/v.csv"));
				failureProbFile = new PrintWriter(new FileWriter(dir + "/fp.csv"));
				routerProbFile = new PrintWriter(new FileWriter(dir + "/rp.csv"));
				TFile = new PrintWriter(new FileWriter(dir + "/T.csv"));
				linkflowFile = new PrintWriter(new FileWriter(dir + "/lf.csv"));
				sFile = new PrintWriter(new FileWriter(dir + "/s.csv"));
				
				int numLinks = 76;
				vulnerability.print(" ");
				failureProbFile.print(" ");
				routerProbFile.print(" ");
				TFile.print(" ");
				linkflowFile.printf(" ");
				sFile.printf(" ");
				
				for (int i=1; i<=numLinks; i++) {
					failureProbFile.printf(",link%d", i);
					routerProbFile.printf(",link%d", i);
					TFile.printf(",link%d", i);
					sFile.printf(",link%d", i);
				}
				vulnerability.println();
				failureProbFile.println();
				routerProbFile.println();
				TFile.println();
				linkflowFile.println();
				sFile.println();
			}
			catch (Exception ex) {
				throw new RuntimeException("Failed to open files", ex);
			}
			/*PrintStream printStreamGraphs;
			try{
				FileOutputStream outGraphs = new FileOutputStream("GraphsOuput.txt");
				printStreamGraphs = new PrintStream(outGraphs);
			}
			catch(Exception e)
			{
				System.out.println("\nERROR ERROR ERROR\n");
			}
			*/
			
			GameInit();
			//System.out.print(FFTTime+"\n");
			//updateEdgeCosts();//Moved outside of loop
			updateEdgeCosts();//Eq 5
			do{
				//updateEdgeCosts();//Eq 5
				computeMeanEdgeCosts();//Eq 6
				showIteration();
				try{
					UEAssignment();//Eq 7 (part A)
					//calculateShortestPath();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		    	updateRouteProbability();//Eq 8
				updateFailureProbability();//Eq 10
				updateVulnerability();//Eq 9 (part A)
				n = n + 1; 
			}while (error>epsilon);
			
			runtime = System.currentTimeMillis()-runtime;
			showResult();
		
			/*try{
				printStreamGraphs.close();
			}
			catch(Exception e)
			{}
			*/
			try {
				vulnerability.close();
				linkflowFile.close();
				failureProbFile.close();
				routerProbFile.close();
				TFile.close();
				sFile.close();
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}		
	}
	
	/**
	 * Main method; call this method to run the Game Theory algorithm
	 * @param args
	 */
    public static void main(String[] args) {
    	GameMethod networkTest = new GameMethod();
    	networkTest.test();
    }	
}
