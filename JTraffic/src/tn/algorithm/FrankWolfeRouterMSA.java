package tn.algorithm;

import java.io.PrintStream;

import org.apache.commons.collections15.Transformer;

import tn.Assignment;
import tn.Demand;
import tn.TrafficNetwork;
import tn.misc.LinkDoublePropertyMap;

//import tn.TrafficNetwork.Graph;

/**
 * Implements the {@link Router} interface. Assumes infinite link capacities,
 * and assumes that link cost is constant at its value when flow is zero. It
 * then routes each user to the initially shortest path. Starting from the
 * initial assignment, new costs are calculated and the corresponding new
 * shortest paths are found.
 * 
 * @author Sherif Tolba
 * 
 */
public class FrankWolfeRouterMSA implements Router {
	
	private LinkDoublePropertyMap T;  

	double previousSysTravelCost;
	
	/**
	 * Output stream for printing trace information
	 * 
	 */
	protected PrintStream out;

	/**
	 * Flags to control what gets printed in the trace output
	 * 
	 */
	boolean printAssignment = false, printFlows = false,
			printTravelTime = true, printLineSearch = false;

	/**
	 * The network in which the given demand is being routed
	 * 
	 */
	TrafficNetwork network;

	/**
	 * The acceptable relative error between iterations to signal convergence
	 * 
	 */
	double tolerance;

	/**
	 * Used when {@link tolerance} is not set (or set to zero).
	 * 
	 */
	static final double defaultTolerance = 10.0;

	/**
	 * The transformer passed to the shortest path algorithm step Returns the
	 * link travel cost
	 */
	Transformer<Integer, Number> costTransformer;

	/**
	 * Previous iteration flow
	 */
	double x_1;
	
	/**
	 * Current iteration flow
	 */
	double x;	
	
	/**
	 * 
	 */
	int iteration = 1;
	
	
	LinkDoublePropertyMap oldFlow;
	
	LinkDoublePropertyMap MSAFlow;
	
	Assignment currentAssignment;

	Router shortestPathRouter, router2;


	
	/**
	 * Construct a {@link FrankWolfeRouter} object
	 * 
	 * @param tolerance
	 *            The acceptable relative error between iterations to signal
	 *            convergence
	 * @param out
	 *            Output stream for printing trace information
	 */
	public FrankWolfeRouterMSA(double tolerance, PrintStream out, LinkDoublePropertyMap t) {
		this.T = t;
	    this.out = out;
	    
		if (tolerance == 0.0)
			this.tolerance = defaultTolerance;
		else
			this.tolerance = tolerance;
	
	}


	/**
	 * Calculate the initial assignment using shortest path router
	 * 
	 * @param demand
	 */
	private LinkDoublePropertyMap init(Demand demand) {

		network = demand.getNetwork();
		
		oldFlow = new LinkDoublePropertyMap("oldFlow", network);

		for(int link: network.getLinks()){
			oldFlow.set(link, 0.0);
		}
		
		shortestPathRouter = new ShortestPathRouter(network, T, null/* System.out */);

		// Calculate initial shortest path assignment
		currentAssignment = reroute(demand);

		if (printAssignment && out != null) {
			out.print("\n ------------ Frank-Wolfe Initialization ---------------\n");
			out.print(currentAssignment.toString());
		}

		LinkDoublePropertyMap currentFlow = new LinkDoublePropertyMap("currentFlow", network);
		//LinkDoublePropertyMap MSAFlow = new LinkDoublePropertyMap("MSAFlow", network);
		MSAFlow = new LinkDoublePropertyMap("MSAFlow", network);
		
		currentFlow = currentAssignment.getFlow();
		
		for(int link: network.getLinks()){
			
			x = currentFlow.get(link);
			x_1 = oldFlow.get(link);
			
			double doubleIteration = Double.parseDouble(Integer.toString(iteration)); 
			
			MSAFlow.set(link, (1/doubleIteration)*x+(1-1/doubleIteration)*x_1);
		}
		oldFlow = MSAFlow.clone();
		
		return MSAFlow;
		
	}

	/**
	 * Route the given demand using the shortest path router, fixing the link
	 * costs at their current values
	 * 
	 * @param demand
	 *            the demand to be routed
	 * @return the resulting assignment
	 */
	private Assignment reroute(Demand demand) {
		return shortestPathRouter.route(demand);
	}

	/**
	 * Set the link flow values and calculate the corresponding objective
	 * function and system travel time
	 * 
	 * @param newFlow
	 *            the new flow to be applied to the links (replacing the current
	 *            flow)
	 */
	private LinkDoublePropertyMap updateLinkIterationCost(LinkDoublePropertyMap currentFlow) {
		network.setFlow(currentFlow);

		if (printFlows && out != null) {
			out.print("Link flows:\n");
			out.print(currentFlow.toString());
		}

		//systemTravelCost = network.getTotalTravelCost().getSum();
		 
		//systemTravelCost = computeBPRTravelCost(newFlow);
		
		//network.passT(T);
		
		return computeBPRTravelCost(currentFlow);
		
	}
	
	
	private LinkDoublePropertyMap computeBPRTravelCost(LinkDoublePropertyMap newFlow){
		
		LinkDoublePropertyMap LinkBPRValues = new LinkDoublePropertyMap("LinkBPRValues", network);
		for(int link: network.getLinks()){
			LinkBPRValues.set(link, T.get(link)*(1.0+0.15*Math.pow(network.getFlow(link)/network.getCapacity(link),4)));
		}
		return LinkBPRValues;
		
	}

	
	private double findSystemTravelCost(LinkDoublePropertyMap linkIterationCost, LinkDoublePropertyMap currentFlow){
		
		LinkDoublePropertyMap flowCostProduct = new LinkDoublePropertyMap("flowCostProduct", network);
		
		for(int link: network.getLinks()){
			double linkFlow = currentFlow.get(link);
			double linkCost = linkIterationCost.get(link);
			double product = linkFlow*linkCost;
			flowCostProduct.set(link, product);
		}
		
		return flowCostProduct.getSum();
		
	}


	
	//**************************************************************************************************************************************
	//######################################################################################################################################
	//**************************************************************************************************************************************
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see tn.Network.Router#route(tn.Network.Demand)
	 */
	@Override
	public Assignment route(Demand demand) {
		
		
		network = demand.getNetwork();
		
		double difference;

		//LinkDoublePropertyMap flowBackup = network.cloneFlow();
		
		// 0) Initialize: Find the shortest path assignment and return the corresponding flow:
		
		LinkDoublePropertyMap currentFlow = init(demand);

		// 1) Updates link iteration costs and return the result:
		
		LinkDoublePropertyMap linkBPRValues = updateLinkIterationCost(currentFlow);
		
		// 2) Find the current total system cost:
		
		double currentSysTravelCost = findSystemTravelCost(linkBPRValues, currentFlow);
		
		// 3) Store the total system cost for next iteration:
		
		previousSysTravelCost = currentSysTravelCost;
		
		
		double stop_criterion = tolerance;
		

		do {
			
			shortestPathRouter = new ShortestPathRouter(network, linkBPRValues, null/* System.out */);

			// Calculate initial shortest path assignment
			currentAssignment = reroute(demand);

			if (printAssignment && out != null) {
				out.print("\n ------------ Frank-Wolfe Initialization ---------------\n");
				out.print(currentAssignment.toString());
			}

			//LinkDoublePropertyMap MSAFlow = new LinkDoublePropertyMap("MSAFlow", network);
			
			currentFlow = currentAssignment.getFlow();
			
			for(int link: network.getLinks()){
				
				x = currentFlow.get(link);
				x_1 = oldFlow.get(link);
				
				double doubleIteration = Double.parseDouble(Integer.toString(iteration)); 
				
				MSAFlow.set(link, (1/doubleIteration)*x+(1-1/doubleIteration)*x_1);
			}
			oldFlow = MSAFlow.clone();
			
			linkBPRValues = updateLinkIterationCost(MSAFlow);
			currentSysTravelCost = findSystemTravelCost(linkBPRValues, MSAFlow);

			
			difference = currentSysTravelCost - previousSysTravelCost;
			
			previousSysTravelCost = currentSysTravelCost;

			iteration++;
			
		} while (iteration <= 1000);     // difference > stop_criterion

		return currentAssignment;
	}
	
	
	//**************************************************************************************************************************************
	//######################################################################################################################################
	//**************************************************************************************************************************************

}
