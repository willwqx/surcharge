package tn.test;

import tn.Demand;
import tn.FlowModel;
import tn.ODPair;
import tn.PolynomialFlowModel;
import tn.TrafficNetwork;
import tn.io.NetworkReader;

/**
 * To add a new sample.:
 * 1. add the sample name under the enum NetworkId
 * 2. add the network topology and demand definition to the constructor Sample(NetworkId networkId) as follows:
 * 	2.a add a case under switch(networkId)
 *  2.b copy the definition code from another case and modify it
// *  Note: don't forget the "break" line at the end of the case you will add
 * 
 *
 */
public class TestNetwork {
	
	public enum NetworkId{
		Sample4nodes6links,
		//Sample4nodes6links1ODDemand,
		Sample6nodes9links,
		Sample4nodes4links,
		SiouxFalls,
		SiouxFalls1ODPair,
		//SiouxFalls16ODPair,
		SiouxFalls16demand,
		SiouxFalls16lowdemand,
		SiouxFalls16highdemand,
		Anaheim
	}
	
	TrafficNetwork network;
	Demand demand;
	
	public TrafficNetwork getNetwork() {
		TrafficNetwork trafficNetwork = network;
		return trafficNetwork;
	}

	public Demand getDemand() {
		return demand;
	}
	
	TestNetwork(NetworkId networkId){
		
    	FlowModel flowModel = new PolynomialFlowModel(0.15,4);

    	try{
	    	switch(networkId){
	    		
			case Sample4nodes4links:
				//construct network 
				network = new TrafficNetwork(4, 4, 4);
		    	
		    	//add links  from, to, capacity, distance, free flow travel time, speed,
				//  toll, link type, flow model, extra cost(omega), difficulty
				
		    	network.addLink(1, 2, 9.0, 1, 5.0, 0, 0, 1, flowModel,0,1);	//1
		    	network.addLink(1, 3, 7.0, 1, 3.0, 0, 0, 1, flowModel,0,1);	//2
		    	network.addLink(2, 4, 8.0, 1, 4.0, 0, 0, 1, flowModel,0,1);	//3
		    	network.addLink(3, 4, 6.0, 1, 3.0, 0, 0, 1, flowModel,0,1);	//4
		    	
		    	demand  = new Demand(network);
		    	demand.add(new ODPair(1,4), 10);
		    	break;

			case Sample4nodes6links:
				network = new TrafficNetwork(4, 4, 6);
		    	
				/* from, to, capacity, distance, free flow travel time, speed,
				*  toll, link type, flow model, extra cost(omega), difficulty
				*/
		    																		//FFTT	 BPR		CCF
/*
				network.addLink(1, 2, 9.0, 1, 5.7437, 0, 0, 1, flowModel,0.1,1); //a     //5      5.66462    9.75164
				network.addLink(1, 3, 7.0, 1, 6.38477, 0, 0, 1, flowModel,2,2); //b     //3      3.56709    6.31790
				network.addLink(2, 4, 8.0, 1, 13.3739, 0, 0, 1, flowModel,3,3); //c     //4      6.48572    20.7919
				network.addLink(2, 3, 4.0, 1, 4.98185, 0, 0, 1, flowModel,4,4); //d     //1      1.20951    1.85582
				network.addLink(3, 2, 3.0, 1, 1.14064, 0, 0, 1, flowModel,5,5); //e     //1      1.00000    1.00000
				network.addLink(3, 4, 6.0, 1, 4.12674, 0, 0, 1, flowModel,6,6); //f     //3      3.77792    5.84522	
*/				

/*				network.addLink(1, 2, 2.0, 1, 5.316, 0, 0, 1, flowModel,1,1); //a     //5      5.66462    9.75164
				network.addLink(1, 3, 1.0, 1, 4.335, 0, 0, 1, flowModel,1,1); //b     //3      3.56709    6.31790
				network.addLink(2, 4, 2.0, 1, 4.334, 0, 0, 1, flowModel,1,1); //c     //4      6.48572    20.7919
				network.addLink(2, 3, 1.0, 1, 1.453, 0, 0, 1, flowModel,1,1); //d     //1      1.20951    1.85582
				network.addLink(3, 2, 2.0, 1, 0.981, 0, 0, 1, flowModel,1,1); //e     //1      1.00000    1.00000
				network.addLink(3, 4, 1.0, 1, 5.314, 0, 0, 1, flowModel,1,1); //f     //3      3.77792    5.84522	
				network.addLink(2, 4, 2.0, 1, 1.820, 0, 0, 1, flowModel,1,5); //c     //4      6.48572    20.7919 */
				
				network.addLink(1, 2, 2.0, 1, 5.0, 0, 0, 1, flowModel,1,1); //a     //5      5.66462    9.75164
				network.addLink(1, 3, 1.0, 1, 3.0, 0, 0, 1, flowModel,1,1); //b     //3      3.56709    6.31790
				network.addLink(2, 4, 2.0, 1, 4.0, 0, 0, 1, flowModel,1,1); //c     //4      6.48572    20.7919
				network.addLink(2, 3, 1.0, 1, 1.0, 0, 0, 1, flowModel,1,1); //d     //1      1.20951    1.85582
				network.addLink(3, 2, 2.0, 1, 1.0, 0, 0, 1, flowModel,1,1); //e     //1      1.00000    1.00000
				network.addLink(3, 4, 1.0, 1, 3.0, 0, 0, 1, flowModel,1,1); //f     //3      3.77792    5.84522	
			  //network.addLink(2, 4, 2.0, 1, 4.0, 0, 0, 1, flowModel,1,5); //c     //4      6.48572    20.7919
				

				
		    	demand  = new Demand(network);
		    	demand.add(new ODPair(1,4), 1);
		    	demand.add(new ODPair(3,4), 1);
		    	demand.add(new ODPair(1,3), 1);
		    	demand.add(new ODPair(2,3), 1);
		    	demand.add(new ODPair(2,4), 2);
		    	demand.add(new ODPair(1,2), 1);		
	
		    	break;

		/*	case Sample4nodes6links1ODDemand:
				network = new TrafficNetwork(4, 4, 6);
		    	
		    	network.addLink(1, 2, 9.0, 1, 5.0, 0, 0, 1, flowModel,0,1);	//1
		    	network.addLink(1, 3, 7.0, 1, 3.0, 0, 0, 1, flowModel,0,1);	//2
		    	network.addLink(2, 4, 8.0, 1, 4.0, 0, 0, 1, flowModel,0,1);	//3
		    	network.addLink(2, 3, 4.0, 1, 1.0, 0, 0, 1, flowModel,0,1);	//4
		    	network.addLink(3, 2, 3.0, 1, 1.0, 0, 0, 1, flowModel,0,1);	//5
		    	network.addLink(3, 4, 6.0, 1, 3.0, 0, 0, 1, flowModel,0,1);	//6
		    	
		    	demand  = new Demand(network);
		    	demand.add(new ODPair(1,4), 1);
	
		    	break; */
				
			case Sample6nodes9links:
				network = new TrafficNetwork(6, 6, 9);
		    	
		    	//add links
		    	network.addLink(1, 2, 5.0, 1, 1.0, 0, 0, 1, flowModel,0,1);	//1
		    	network.addLink(1, 3, 5.0, 1, 2.0, 0, 0, 1, flowModel,0,1);	//2
		    	network.addLink(2, 3, 5.0, 1, 1.0, 0, 0, 1, flowModel,0,1);	//3
		    	network.addLink(2, 4, 5.0, 1, 2.0, 0, 0, 1, flowModel,0,1);	//4
		    	network.addLink(3, 4, 5.0, 1, 1.0, 0, 0, 1, flowModel,0,1);	//5
		    	network.addLink(3, 5, 5.0, 1, 2.0, 0, 0, 1, flowModel,0,1);	//6
		    	network.addLink(4, 5, 5.0, 1, 1.0, 0, 0, 1, flowModel,0,1);	//7
		    	network.addLink(4, 6, 5.0, 1, 2.0, 0, 0, 1, flowModel,0,1);	//8
		    	network.addLink(5, 6, 5.0, 1, 1.0, 0, 0, 1, flowModel,0,1);	//9
		    	
		    	demand  = new Demand(network);
		    	demand.add(new ODPair(1,6), 8);
		    	/*demand.add(new ODPair(3,4), 1);
		    	demand.add(new ODPair(1,3), 1);
		    	demand.add(new ODPair(2,3), 1);
		    	demand.add(new ODPair(2,4), 2);
		    	demand.add(new ODPair(1,2), 1);*/			
		    	break;
		    	
			case SiouxFalls:
				readInputFromFile("../JTraffic/siouxFallsInput.txt");
				break;
				
			case SiouxFalls1ODPair:
				readInputFromFile("../JTraffic/siouxFallsInput.txt");
				
		    	demand  = new Demand(network);
		    	demand.add(new ODPair(1,20), 1.0);
		    	break;
		    	
			/*case SiouxFalls16ODPair:
				readInputFromFile("./JTraffic/siouxFallsInput.txt");
				
		    	demand  = new Demand(network);
		    	demand.add(new ODPair(1,6),1.0);
		    	demand.add(new ODPair(1,7),1.0);
		    	demand.add(new ODPair(1,18),1.0);
		    	demand.add(new ODPair(1,20),1.0);
		    	demand.add(new ODPair(2,6),1.0);
		    	demand.add(new ODPair(2,7),1.0);
		    	demand.add(new ODPair(2,18),1.0);
		    	demand.add(new ODPair(2,20),1.0);
		    	demand.add(new ODPair(3,6),1.0);
		    	demand.add(new ODPair(3,7),1.0);
		    	demand.add(new ODPair(3,18),1.0);
		    	demand.add(new ODPair(3,20),1.0);
		    	demand.add(new ODPair(13,6),1.0);
		    	demand.add(new ODPair(13,7),1.0);
		    	demand.add(new ODPair(13,18),1.0);
		    	demand.add(new ODPair(13,20),1.0);
				
				break;*/
				
			case SiouxFalls16demand:
			readInputFromFile("../JTraffic/siouxFallsInput.txt");
			
	    	demand  = new Demand(network);
	    	demand.add(new ODPair(1,6),0.0);
	    	demand.add(new ODPair(1,7),1360.0);
	    	demand.add(new ODPair(1,18),1100.0);
	    	demand.add(new ODPair(1,20),1600.0);
	    	demand.add(new ODPair(2,6),1000.0);
	    	demand.add(new ODPair(2,7),0.0);
	    	demand.add(new ODPair(2,18),1200.0);
	    	demand.add(new ODPair(2,20),1250.0);
	    	demand.add(new ODPair(3,6),1500.0);
	    	demand.add(new ODPair(3,7),1200.0);
	    	demand.add(new ODPair(3,18),0.0);
	    	demand.add(new ODPair(3,20),1028.0);
	    	demand.add(new ODPair(13,6),1600.0);
	    	demand.add(new ODPair(13,7),1000.0);
	    	demand.add(new ODPair(13,18),1400.0);
	    	demand.add(new ODPair(13,20),0.0);
			
			break;
			
			case SiouxFalls16lowdemand:
				readInputFromFile("../JTraffic/siouxFallsInput.txt");
				
		    	demand  = new Demand(network);
		    	demand.add(new ODPair(1,6),0.0);
		    	demand.add(new ODPair(1,7),680.0);
		    	demand.add(new ODPair(1,18),550.0);		    	
		    	demand.add(new ODPair(1,20),800.0);
		    	demand.add(new ODPair(2,6),500.0);
		    	demand.add(new ODPair(2,7),0.0);
		    	demand.add(new ODPair(2,18),600.0);
		    	demand.add(new ODPair(2,20),625.0);
		    	demand.add(new ODPair(3,6),750.0);
		    	demand.add(new ODPair(3,7),600.0);
		    	demand.add(new ODPair(3,18),0.0);
		    	demand.add(new ODPair(3,20),514.0);
		    	demand.add(new ODPair(13,6),800.0);
		    	demand.add(new ODPair(13,7),500.0);
		    	demand.add(new ODPair(13,18),700.0);
		    	demand.add(new ODPair(13,20),0.0);
				
				break;
				
			case SiouxFalls16highdemand:
				readInputFromFile("../JTraffic/siouxFallsInput.txt");
				
		    	demand  = new Demand(network);
		    	demand.add(new ODPair(1,6),0.0);
		    	ODPair o = new ODPair(1,7);
		    	demand.add(o,2040.0);
		    	
		    	demand.add(new ODPair(1,18),1650.0);
		    	demand.add(o, 20.0);
		    	demand.add(new ODPair(1,20),2400.0);
		    	demand.add(new ODPair(2,6),1500.0);
		    	demand.add(new ODPair(2,7),0.0);
		    	demand.add(new ODPair(2,18),1800.0);
		    	demand.add(new ODPair(2,20),1875.0);
		    	demand.add(new ODPair(3,6),2250.0);
		    	demand.add(new ODPair(3,7),1800.0);
		    	demand.add(new ODPair(3,18),0.0);
		    	demand.add(new ODPair(3,20),1542.0);
		    	demand.add(new ODPair(13,6),2400.0);
		    	demand.add(new ODPair(13,7),1500.0);
		    	demand.add(new ODPair(13,18),2100.0);
		    	demand.add(new ODPair(13,20),0.0);
				
				break;	
			
			
			case Anaheim:
				readInputFromFile("../JTraffic/Anaheim_net.txt");
				
			}
    	}	
    	catch (Exception e) {
			System.err.printf(e.getMessage());
			e.printStackTrace();
		} 
		
	}

	private void readInputFromFile(String fileName){
    	try {
			NetworkReader networkReader = new NetworkReader(fileName);
			
			network = networkReader.readTopology();
			demand = networkReader.readDemand();		
	    }
    	catch (Exception e) {
			System.err.printf(e.getMessage());
			e.printStackTrace();
		} 
	}
	
}
