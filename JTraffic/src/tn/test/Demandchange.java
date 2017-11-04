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
import tn.test.GameMethod.Game.CostProperty;
import tn.test.GameMethod.Game.CostTransformer;


public class Demandchange {
	private TrafficNetwork trafficNetwork;//<R>instance of the traffic network class
	private Demand demand;//<R>link demands on each link
	//long runtime;
	private PrintWriter TFile;
	private PrintWriter linkflowFile;
	private PrintWriter sFile;/*S-expect value*/
		
	public static void main(String[] args) {
		Demandchange Demandchange = new Demandchange ();
		Demandchange.process();
	}
	
	public void process(){
		
		TestNetwork testNetwork = new TestNetwork(TestNetwork.NetworkId.SiouxFalls16demand);
		trafficNetwork = testNetwork.getNetwork();
		demand = testNetwork.getDemand();
		new Demand1change().run();	
		
	}
	
	class Demand1change{
		
		    long runtime;
			//runtime = System.currentTimeMillis();
		    LinkDoublePropertyMap rho, gamma, T,T_1, S, CF, x, rho_n_1, omega, difficulty, FFTTime, FFTTimeBackup, capacity,linkIterationCost;
			LinkReadOnlyProperty<Double> costProperty;
			CostTransformer costTransformer;
			//DijkstraShortestPath<Integer, Integer> algorithm;
			FrankWolfeRouter router;
			ShortestPathRouter shortestroute;
			List<Integer> shortestPath;

			double toll;
			double toll_1;
			double totaltime;
			
			final double beta=10;

			final double theta=.5;

			final double epsilon = 1;  //(((trafficNetwork.getLinkCount())^2)*(552.0/16.0));
			
			private double error = 0;

			int n = 1;
			
		public void run(){
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
			}
			catch (Exception ex) {
				throw new RuntimeException("Failed to open files", ex);
			}

			Init();
			
			do{ 
				showIteration();
				computeMeanEdgeCosts();
				//updateDemandProb();
				try{
					//UEAssignment();
					calculateShortestPath();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		    	computeEdgetoll();
		    	//updateRouteProb();
				//newDemandProb();
		    	computetolldiff();
				n++;
				computetotaltime();
			}while (error>epsilon);
			
			runtime = System.currentTimeMillis()-runtime;
			
			showIteration();
			System.out.printf("Runtime =%d ms\n", runtime); 
			try {
				linkflowFile.close();
				TFile.close();
				sFile.close();
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
				
				
		private void Init(){

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
			
			double initRho = 1.0/trafficNetwork.getLinkCount(); //double initRho = 1.0/76; 
			for(int link: trafficNetwork.getLinks()){
				rho.set(link, initRho);
				gamma.set(link, 0.0);
				T.set(link,trafficNetwork.getTravelTime(link));
				omega.set(link, trafficNetwork.getOmega(link));
				difficulty.set(link, trafficNetwork.getDifficulty(link));
				FFTTime.set(link, trafficNetwork.getTravelTime(link));
				//System.out.print(FFTTime+"\n");
				FFTTimeBackup.set(link, trafficNetwork.getTravelTime(link));
				capacity.set(link, trafficNetwork.getCapacity(link));
				
			}
			
		}
		
		private void showIteration(){
			//System.out.printf("*** %s \n", T.get(1));
			TFile.printf("%d%s\n", n, T.toRowString());
			linkflowFile.printf("%d%s\n", n, x.toRowString());
			sFile.printf("%d%s\n", n, S.toRowString());
		}
		
		private void computeMeanEdgeCosts(){
			
			if(n == 1){
				T_1 = FFTTime.clone();
				T = T_1.clone();
			}
			
			else { 
				for(int link: trafficNetwork.getLinks()){
					//T.set(link, S.get(link));
					T.set(link, (1/n)*S.get(link)+(1-1/n)*T_1.get(link));
					//System.out.println(1);
				}
				T_1 = T.clone();
			}
			
		}
		
		 private void UEAssignment() throws FileNotFoundException
			{	
			 FrankWolfeRouter router = null;
			 //IncrementalRouter router = null;
			 
		    	try{
					double tolerance = 1e-4;
					FileOutputStream out = new FileOutputStream("FrankWolfeOuput.txt");
					PrintStream printStream = new PrintStream(out);
					router = new FrankWolfeRouter(tolerance, T, printStream);
					//router = new FrankWolfeRouterMSA(tolerance, printStream,T);//this just the part of try..  catch...
					//router = new IncrementalRouter(trafficNetwork, tolerance, printStream);
					
				}
				catch(Exception e)
				{
					System.out.println("\nERROR ERROR ERROR\n");
				}
				//Assignment newA = router.newroute(demand); 	    	
				Assignment newA = router.route(demand);
				x = newA.getFlow();
			}
		 
		private void computeEdgetoll(){
				
			for(int link: trafficNetwork.getLinks()){
					
				double FFTT = trafficNetwork.getFreeFlowTravelTime(link);
				double Vol = x.get(link);
				double Cap = trafficNetwork.getCapacity(link); 
				S.set(link, FFTT + Vol * FFTT * (4 * Math.pow(Vol,3) * 0.15 / Math.pow(Cap,4))); //S.set(link, Vol * FFTT * (4 * Math.pow(Vol,3) * 0.15 / Math.pow(Cap,4)));
			}
	
		}
		
		private void computetolldiff(){
			
			toll_1 = toll;
			toll = 0;
			for(int link: trafficNetwork.getLinks()){
				toll = toll + S.get(link);
			}

			error = Math.abs(toll - toll_1);
		}
		
		private void computetotaltime(){
			
			totaltime = 0;
			for(int link: trafficNetwork.getLinks()){
				double FFTT = trafficNetwork.getFreeFlowTravelTime(link);
				double Vol = x.get(link);
				double Cap = trafficNetwork.getCapacity(link); 
				totaltime = totaltime + Vol * FFTT * (1 + Math.pow(Vol/Cap, 4) * 0.15); 
			}

			System.out.printf("System total travel time = %f \n", totaltime);
		}
		
		private void calculateShortestPath(){			
		//router = new ShortestPathRouter(trafficNetwork, new CostProperty(), null);
		//shortestroute = new ShortestPathRouter(trafficNetwork, trafficNetwork.getTotalTravelCost(), null);
		shortestroute = new ShortestPathRouter(trafficNetwork, T, null);
		Assignment newA = router.route(demand);
		x = newA.getFlow();
		//h.combine(newA, (1-1.0/n)); //combine using MSA
	}
	
	}
	
}

