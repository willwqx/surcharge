package tn.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import tn.Assignment;
import tn.Demand;
import tn.TrafficNetwork;
import tn.algorithm.FrankWolfeRouter;
//import tn.algorithm.ShortestPathRouter;
import tn.misc.LinkDoublePropertyMap;
//import tn.Assignment;
//import tn.Network;



public class SOcharge {
	public static void main(String[] args){
	double FFTT = 10;
	double Demand = 293.356;
	double Cap = 200;
	
	System.out.println("BPR travle time: " + BPR(FFTT,Demand,Cap));
	System.out.println("Surcharge: " + surcharge(FFTT,Demand,Cap));

	double NewTravelcost = BPR(FFTT,Demand,Cap) + surcharge(FFTT,Demand,Cap);
	System.out.println("New travle cost: " + NewTravelcost);
	
	double residue = 0;
	if (surcharge(FFTT,Demand,Cap) > BPR(FFTT,Demand,Cap)){
		residue = 0;
	}
	else{
		residue = BPR(FFTT,Demand,Cap) - surcharge(FFTT,Demand,Cap);
	}
	System.out.println("residue: " + residue);
	}
	
//	
	 private void UEAssignment() throws FileNotFoundException
		{	
	    	
		
		//TrafficNetwork trafficNetwork;//<R>instance of the traffic network class
		//Demand demand;//<R>link demands on each link
	
		TestNetwork testNetwork = new TestNetwork(TestNetwork.NetworkId.SiouxFalls);
		TrafficNetwork trafficNetwork = testNetwork.getNetwork();
		Demand demand = testNetwork.getDemand();
		
		FrankWolfeRouter router = null;//FrankWolfeRouter router;
		LinkDoublePropertyMap x , T;
		
		T = new LinkDoublePropertyMap("T", trafficNetwork);

		
		try{	
			double tolerance = 1e-4;
			FileOutputStream out = new FileOutputStream("FrankWolfeOuput.txt");
			PrintStream printStream = new PrintStream(out);
			//FrankWolfeRouter p = new FrankWolfeRouter(tolerance, T, printStream);
			router = new FrankWolfeRouter(tolerance, T, printStream);
		}
		catch(Exception e)
		{
			System.out.println("\nERROR ERROR ERROR\n");
		}
	
		Assignment newA = router.route(demand);
		x = newA.getFlow();
	}
//	


	// BPR:  T*(1+0.15*((V/C)^4))), require 3 parameters T,V,C
	private static double BPR(double T,double V, double C) {	
		return T*(1+Math.pow((V/C),4)*0.15);  //return T*(1+(V/C)*(V/C)*(V/C)*(V/C)*0.15);
	}
	
	// surcharge: X*(dX/dT)
	private static double surcharge(double T,double V, double C){
		return V * T * (4 * Math.pow(V ,3) * 0.15 / Math.pow(C ,4)); 
	}
	
	// newdemand 
	private static double newdemand(double T,double V, double C, double Sur){
		
		return T;
		
	}
}

