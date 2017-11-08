package tn.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

import tn.Assignment;
import tn.Demand;
import tn.ODPair;
import tn.Route;
import tn.TrafficNetwork;
import tn.algorithm.FrankWolfeRouter;
//import tn.algorithm.IncrementalRouter;
//import tn.algorithm.IterativeRouter;
import tn.algorithm.ShortestPathRouter;
import tn.misc.LinkDoublePropertyMap;
//import tn.test.GameMethod.Game.CostTransformer;

public class Iteration2Run {
	private TrafficNetwork trafficNetwork; //<R>instance of the traffic network class
	private Demand demand; //<R>link demands on each link
	//long runtime;
	private PrintWriter TFile;
	private PrintWriter linkflowFile;
	private PrintWriter sFile; /* S-expect value */
		
	public static void main(String[] args) {
		Iteration2Run iteration2run = new Iteration2Run();
		iteration2run.process();
	}
	
	public void process(){
		
		TestNetwork testNetwork = new TestNetwork(TestNetwork.NetworkId.SiouxFalls16demand);
		trafficNetwork = testNetwork.getNetwork();
		demand = testNetwork.getDemand();
		//LinkDoublePropertyMap S;
		//S = new LinkDoublePropertyMap("S", trafficNetwork);
		new Surcharge().run();
		
	}
	
	class Surcharge {

		long runtime;  /* stores algorithm running time */
		
		LinkDoublePropertyMap                 linkToll;  /* total travel cost ( time cost + surcharge) for every link in network */
		LinkDoublePropertyMap             linkToll_last;  /* total travel cost ( time cost + surcharge) in last iteration for every link */
		LinkDoublePropertyMap 	     travelTime;  /* Travel time cost for every link */
		LinkDoublePropertyMap  linkMarginalCost;  /* marginal cost for every link */
		LinkDoublePropertyMap        linkVolume;  /* traffic along each link (flow) */
		LinkDoublePropertyMap     linkVolumeOrg;  /* Link volume before surcharge */
		LinkDoublePropertyMap       linkFFTTime;  /* free flow travel time along each link */
		LinkDoublePropertyMap    linkCapacities;  /* total capacity on each link */
		
		FrankWolfeRouter          router;  /* the UE traffic assignment */
		ShortestPathRouter shortestroute;  /* shortest path between OD pair */
		
		final double epsilon = 1;  /* TODO figure out what this means -> (((trafficNetwork.getLinkCount())^2)*(552.0/16.0)); */
		private double error = 0;  /* toll difference between current and previous iteration, convergence occurs when error < epsilon */
		
		int n = 1;  /* number of iterations completed so far */
		
		double sumToll;  /* current iteration sum sum of all links toll */
		double sumToll_1;  /* last current iteration sum of all links toll */
		
		/**
		 * Implementation of the Surcharge algorithm.
		 */
		public void run() {
			runtime = System.currentTimeMillis();
			try {
				String dir = "../JTraffic";
				TFile = new PrintWriter(new FileWriter(dir + "/T.csv"));
				linkflowFile = new PrintWriter(new FileWriter(dir + "/lf.csv"));
				sFile = new PrintWriter(new FileWriter(dir + "/s.csv"));
				
				int numLinks = 76;
	

				TFile.print(" ");
				linkflowFile.printf(" ");
				sFile.printf(" ");
				
				for (int i=1; i<=numLinks; i++) {
					TFile.printf(",link%d", i);
					sFile.printf(",link%d", i);
				}
				TFile.println();
				linkflowFile.println();
				sFile.println();
			}catch (Exception ex) {
				throw new RuntimeException("Failed to open files", ex);
			}

			init();
			
			do {
				showIteration();
		
				//updateDemandProb();
				try{
					UEAssignment();
				}catch(Exception e) {
					e.printStackTrace();
				}
				computeTravelTimeCost();
		    	computeMarginalCost();
		    	calLinkToll();
		    	//updateRouteProb();
		    	updateDemand();
				//newDemandProb();
		    	computeTollDiff();
				n++;
				computeTotalTime();
			}while(error > epsilon);
			
			runtime = System.currentTimeMillis() - runtime;
			
			showIteration();
			System.out.printf("Runtime =%d ms\n", runtime); 
			try {
				linkflowFile.close();
				TFile.close();
				sFile.close();
			}catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		
		/**
		 * This function will update the demand for each pair
		 * given the current demand and the network structure. 
		 */
		private void updateDemand() {
			// demand - set of all pairs
			// trafficNetwork - road network graph
			// S - link marginal costs
			// x - traffic along each link -> need to compute travel cost from this
			
			/* Calculate each origin destination (OD) total cost for each pair */
			Assignment assgn = calculateShortestPath();
			for(int node1 : trafficNetwork.getNodes()) {
				for(int node2 : trafficNetwork.getNodes()) {
					if(node1 != node2) {
						ODPair p = new ODPair(node1, node2);
						
						/* Get route between this pair of nodes */
						Route proute = assgn.getRoute(p);
						if(proute == null) {
							continue;
						}
						
						/* Compute route total cost and route marginal cost for this pair */
						double routeTotalCost = 0;
						double routeMarginalCost = 0;
						for(int link : proute.getLinks()) {
							//routeTotalCost += getTravelTimeCost(link);  		//old version
							routeTotalCost += getOrigTravelCost(link);
							//routeMarginalCost += linkMarginalCost.get(link);  //old version
							routeMarginalCost += linkToll.get(link);
						}
						
						/* Calculate residual cost (residualCost) for each pair */
						double routeResidualCost = routeTotalCost - routeMarginalCost;
						
						/* Estimate new demand needed to attain given residual cost */
						int volume = 0;
						double estResidualCost = Double.NEGATIVE_INFINITY;
						do {
							estResidualCost = 0;
							for(int link : proute.getLinks())
								estResidualCost += getTravelTimeCost(link, volume);
							volume += 1;
						}while(estResidualCost < routeResidualCost);
						volume -= 1;
						
						/* Update demand with new demand */
						// BUGS? Route residual cost is negative??
						System.out.println(p + " : " + "route total cost=" + routeTotalCost + ", route marginal cost=" + routeMarginalCost + " : " + demand.get(p) + " -> " + volume);
						demand.set(p, volume);
					}
				}
			}
		}
		
		/**
		 * Initializes all local variables to default values. 
		 */
		private void init() {
			linkToll = new LinkDoublePropertyMap("T", trafficNetwork);
			linkToll_last = new LinkDoublePropertyMap("T_1", trafficNetwork);
			linkMarginalCost = new LinkDoublePropertyMap("S", trafficNetwork);
			linkFFTTime = new LinkDoublePropertyMap("FFTTime", trafficNetwork);
			linkCapacities = new LinkDoublePropertyMap("capacity", trafficNetwork);
			//h = new Assignment(network);
			linkVolume = new LinkDoublePropertyMap("linkFlows", trafficNetwork);
			
			for(int link: trafficNetwork.getLinks()) {
				linkToll.set(link,trafficNetwork.getTravelTime(link));
				linkFFTTime.set(link, trafficNetwork.getFreeFlowTravelTime(link));
				linkCapacities.set(link, trafficNetwork.getCapacity(link));
			}
		}
		
		private void showIteration() {
			//System.out.printf("*** %s \n", T.get(1));
			TFile.printf("%d%s\n", n, linkToll.toRowString());
			linkflowFile.printf("%d%s\n", n, linkVolume.toRowString());
			sFile.printf("%d%s\n", n, linkMarginalCost.toRowString());
		}
		
		/**
		 * Compute the measure of successful average (MSA) toll for the current iteration.
		 */
		private void calLinkToll() {
			/*if(n == 1){
				Tlast = linkFFTTime.clone();
				T = Tlast.clone();
			}else{ 
				for(int link: trafficNetwork.getLinks()) {
					//T.set(link, S.get(link));
					T.set(link, (1/n)*linkMarginalCost.get(link)+(1-1/n)*Tlast.get(link));
				}
				Tlast = T.clone();
			}*/
			if(n == 1){
				linkToll = linkFFTTime.clone();
				linkToll_last = linkToll.clone();
			}
			else{ 
				for(int link: trafficNetwork.getLinks()) {
					linkToll.set(link, (1/n)*(linkMarginalCost.get(link) )+(1-1/n)*linkToll_last.get(link)); // GeneralCost = Marginal cost + travel time cost 
					}
				linkToll_last = linkToll.clone();
			}
		}
		
		/**
		 * Compute the Link travel time cost
		 */
		private void computeTravelTimeCost() {
			if(n == 1){
				travelTime = linkFFTTime.clone();
			}else{
				for(int link: trafficNetwork.getLinks()) {
					travelTime.set(link, getTravelTimeCost(link));
				}
			}
		}
		
		/*private void originalTravelCost() {
			if(n == 1){
				travelTime = linkFFTTime.clone();
			}else{
				for(int link: trafficNetwork.getLinks()) {
					travelTime.set(link, getOrigTravelCost(link));
				}
			}
		}*/
		
		private double getOrigTravelCost(int linkid) {
			double volume = linkVolumeOrg.get(linkid);
			return getTravelTimeCost(linkid, volume);
		}
		
		/**
		 * Compute the travel time cost for a given link.
		 * @param linkid the given link
		 * @return travel time cost for given link
		 */
		private double getTravelTimeCost(int linkid) {
			double volume = linkVolume.get(linkid);
			return getTravelTimeCost(linkid, volume);
		}
		
		private double getTravelTimeCost(int linkid, double volume) {
			double cap = trafficNetwork.getCapacity(linkid);
			double fftTime = linkFFTTime.get(linkid);
			return fftTime * (1 + 0.15 * Math.pow((volume / cap), 4));
		}
		
		/**
		 * Computes the user equilibrium traffic assignment.
		 * @throws FileNotFoundException
		 */
		private void UEAssignment() throws FileNotFoundException {	
			FrankWolfeRouter router = null;
			//IncrementalRouter router = null;
	    	try {
				double tolerance = 1e-4;
				FileOutputStream out = new FileOutputStream("FrankWolfeOuput.txt");
				PrintStream printStream = new PrintStream(out);
				router = new FrankWolfeRouter(tolerance, linkToll, printStream);
				//router = new FrankWolfeRouterMSA(tolerance, printStream,T);//this just the part of try..  catch...
				//router = new IncrementalRouter(trafficNetwork, tolerance, printStream);
			}catch(Exception e) {
				System.out.println("\nERROR ERROR ERROR\n");
			}
			//Assignment newA = router.newroute(demand);
	    	
	    	/*int a = 1, b = 7, c = 0;
	    	demand.add(new ODPair(a,b), c);*/
	    	
			Assignment newA = router.route(demand);
			linkVolume = newA.getFlow();
			if (n == 1){
			  linkVolumeOrg = newA.getFlow();}
			 
			//linkTimeCost = BPR;
		}
		
		/**
		 * Compute marginal cost for each link in the network.
		 * This is stored in the variable 'S'.
		 */
		private void computeMarginalCost() {
			for(int link: trafficNetwork.getLinks()){
				double FFTT = trafficNetwork.getFreeFlowTravelTime(link);
				double Vol = linkVolume.get(link);
				double Cap = trafficNetwork.getCapacity(link); 
				linkMarginalCost.set(link, Vol * FFTT * (4 * Math.pow(Vol,3) * 0.15 / Math.pow(Cap,4))); //S.set(link, Vol * FFTT * (4 * Math.pow(Vol,3) * 0.15 / Math.pow(Cap,4)));
			}
		}
		
		/**
		 * Computes the toll difference between each iteration.
		 * This is used to determine when the algorithm should terminate.
		 */
		private void computeTollDiff() {
			sumToll_1 = 0;
			sumToll = 0;
			for(int link: trafficNetwork.getLinks()) {
				sumToll += linkMarginalCost.get(link);
			}
			error = Math.abs(sumToll - sumToll_1);
			sumToll_1 = sumToll;
		}
		
		/**
		 * Computes sum of travel times over all links in the traffic network.
		 * This is used to evaluate transportation network performance.
		 */
		private void computeTotalTime() {
			double totaltime = 0;
			for(int link: trafficNetwork.getLinks()) {
				double FFTT = trafficNetwork.getFreeFlowTravelTime(link);
				double Vol = linkVolume.get(link);
				double Cap = trafficNetwork.getCapacity(link); 
				totaltime = totaltime + Vol * FFTT * (1 + Math.pow(Vol/Cap, 4) * 0.15); 
			}
			System.out.printf("System total travel time = %f \n", totaltime);
		}
		
		/**
		 * Compute the shortest paths between all OD pairs.
		 */
		private Assignment calculateShortestPath() {			
			shortestroute = new ShortestPathRouter(trafficNetwork, linkToll, null);
			Assignment newA = shortestroute.route(demand);
			return newA;
		}
	}
}

	 



