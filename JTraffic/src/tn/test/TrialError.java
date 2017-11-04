package tn.test;

//copied from GameMethod
import tn.Demand;
import tn.TrafficNetwork;

public class TrialError {
	
	private TrafficNetwork trafficNetwork;//<R>instance of the traffic network class
	private Demand demand;//<R>link demands on each link
	
/*	Public void run(){
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
		
	}*/

/*	
	private void updateEdgeCosts(){
		for(int link : trafficNetwork.getLinks()){
			
			 // C_ is the cost of edge e in a normal state
		 
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

//	Class Trail{

//	} 
	    public static void main(String[] args) {
	    	TrialError networkTest = new TrialError();
	    	networkTest.test();
	    }
*/
}

	
