package tn;

import tn.err.InvalidLinkId;
import tn.err.InvalidNodeId;
import tn.misc.LinkDoublePropertyMap;
import tn.misc.LinkProperty;
import tn.misc.LinkPropertyFunction;
import tn.misc.LinkPropertyFunction.Function;
import tn.misc.LinkPropertyMap;
import tn.misc.LinkReadOnlyProperty;


/**A Network represents the directed-graph topology , i.e. nodes, directed links
 * and allows properties to be associated with links 
 * @author Saleh Ibrahim 
 */
public class TrafficNetwork extends Network{
	
	/**
	 * Storage for link capacity
	 */
	private LinkProperty<Double> pCapacity;

	/**
	 * Storage for link properties
	 */
	private LinkProperty<Double> pDistance;

	/**
	 * Storage for link properties
	 */
	private LinkProperty<Double> pSpeedLimit;

	/**
	 * Storage for link properties
	 */
	private LinkProperty<Double> pToll;
	
	private LinkProperty<FlowModel> pFlowModel;

	/**
	 * Map for link free flow travel time
	 */
	private LinkReadOnlyProperty<Double> pFreeFlowTravelTime;

	/**
	 * Storage for link properties
	 */
	private LinkProperty<Double> pOmega;

	/**
	 * Storage for link properties
	 */
	private LinkProperty<Double> pDifficulty;
	
	/**
	 * Link property functions
	 */
	private Function<Double> fTravelTime, fTravelTimeIntegral, fTravelCost, fTravelCostIntegral;

	/**
	 * Link property function maps 
	 * pTravelTime:Map for link travel time (at the given load)
	 * pTravelTimeIntegral: Map for link travel time integral (at the given load)
	 * Integrate with load to calculate travelTime
	 * pTravelCost: link travel cost map
	 * pTravelCostIntegral: Link travel cost integral map 
	 */
	private LinkReadOnlyProperty<Double> pTravelTime, pTravelTimeIntegral, pTravelCost, pTravelCostIntegral;

	/**
	 * Link cost (multiplied by flow) 
	 */
	private FlowCostFunction pTotalTravelTime, pTotalTravelTimeIntegral, pTotalTravelCost, pTotalTravelCostIntegral;

	/**
	 * Storage for link type
	 */
	private LinkProperty<Integer> pLinkType;

	/**
	 * Map for link flow
	 */
	private LinkDoublePropertyMap pFlow;
	
	/**
	 * Map for Residual capacity
	 */
	private LinkReadOnlyProperty<Double> pResidualCapcity;

	
	/**
	 * The function used for calculating the cost of each link 
	 * as a function of travel time, distance and toll.
	 */
	private CostModel costModel;

	/**
	 * Default link flow model. Used when no flowModel is assigned to a link
	 */
	private static FlowModel defaultFlowModel=new PolynomialFlowModel(0.15,4);
	
	/**
	 * Construct a network with the given number of nodes 
	 * links, demand and/or assignments are to be added later on 
	 * using addLink(), assign() 
	 */
	public TrafficNetwork(int nodeCount, int zoneCount, int linkCount){
		super(nodeCount, zoneCount, linkCount);		
		
		costModel = new CostModel(0,0);
				
		pCapacity = new LinkDoublePropertyMap("Cpcty", this);
		pFlow = new LinkDoublePropertyMap("Flow", this);
		pDistance = new LinkDoublePropertyMap("Length", this);
		pSpeedLimit = new LinkDoublePropertyMap("SpeedLmt", this);
		pToll = new LinkDoublePropertyMap("Toll", this);
		pFlowModel = new LinkPropertyMap<FlowModel>("Flow-Model", this, defaultFlowModel);
		pFreeFlowTravelTime =  new LinkDoublePropertyMap("FFTTime", this); 
		pLinkType = new LinkPropertyMap<Integer>("Type", this, 0);
		pOmega = new LinkDoublePropertyMap("ExtraCost",this);
		pDifficulty = new LinkDoublePropertyMap("AttackDifficulty", this);

		initFunctionProperties();
	}

	private void initFunctionProperties() {
		fTravelTime = new TravelTimeFunction();
		fTravelTimeIntegral = new TravelTimeIntegralFunction();
		fTravelCost = new TravelCostFunction();
		fTravelCostIntegral = new TravelCostIntegralFunction();
		
		pTravelTime = new LinkPropertyFunction<Double>("TTime", this, fTravelTime);
		pTravelTimeIntegral = new LinkPropertyFunction<Double>("TTimeI", this, fTravelTimeIntegral);
		pTravelCost = new LinkPropertyFunction<Double>("TCost", this, fTravelCost);
		pTravelCostIntegral = new LinkPropertyFunction<Double>("TCostI", this, fTravelCostIntegral);
		pResidualCapcity = new LinkPropertyFunction<Double>("Residue", this, new ResidualCapacityFunction());

		pTotalTravelTime = new FlowCostFunction("f*TTime", fTravelTime);
		pTotalTravelTimeIntegral = new FlowCostFunction("f*TTimeI", fTravelTimeIntegral);
		pTotalTravelCost = new FlowCostFunction("f*TCost", fTravelCost);
		pTotalTravelCostIntegral = new FlowCostFunction("f*TCostI", fTravelCostIntegral);		
	}

	/**
	 * Construct a network with the same number of nodes
	 * and the same links and cost model of a given network
	 * @param o network to copy
	 */
	public TrafficNetwork(TrafficNetwork o){
		super(o);

		costModel = new CostModel(o.costModel);

		pCapacity = o.pCapacity.clone(this);
		pFlow = o.pFlow.clone(this);
		pDistance = o.pDistance.clone(this);
		pSpeedLimit = o.pSpeedLimit.clone(this);
		pToll = o.pToll.clone(this);
		pFlowModel = o.pFlowModel.clone(this);
		pFreeFlowTravelTime =  o.pFreeFlowTravelTime.clone(this);
		pTravelTime = o.pTravelTime.clone(this);
		pTravelTimeIntegral = o.pTravelTimeIntegral.clone(this);
		pTravelCost = o.pTravelCost.clone(this);
		pTravelCostIntegral = o.pTravelCostIntegral.clone(this);
		pLinkType = o.pLinkType.clone(this);
		pResidualCapcity = o.pResidualCapcity.clone(this);
		pOmega = o.pOmega.clone(this);
		pDifficulty = o.pDifficulty.clone(this);
		
		
	}
	
	/**Change the cost calculation model
	 * @see tn.CostModel CostModel
	 * @param costModel cost model object
	 */
	public void setCostModel(CostModel costModel) {
		this.costModel = costModel;
	}
	
	public void setTravelCost(LinkReadOnlyProperty<Double> travelCost){
		this.pTravelCost = travelCost;
	}
	
	public LinkReadOnlyProperty<Double> getTravelCost(){
		return pTravelCost;
	}
	
	/**Assign a given traffic load to the network, assigning each user to the best path
	 */
	public void loadAssignment(Assignment assignment) {		
		setFlow(assignment.getFlow());
	}

	public void addFlow(int linkId, double additionalFlow){
		pFlow.set(linkId, pFlow.get(linkId).doubleValue() + additionalFlow);
	}

	/** add a link to the network
	 * 		id: link id
	 * 		from: node id
	 * 		to: node id
	 * 		distance: travel distance of the link
	 * 		capacity: maximum throughput
	 * 		travelTime: ideal travel time
	 * 		speed: posted speed limit
	 * 		toll: toll associated with link
	 * 		flowModel: determines the actual travelTime as a function of flow 
	 * 		linkType: unknown
	 * @throws InvalidLinkId 
	 */
	public void addLink(int from, int to, double capacity, double distance,
			double freeFlowTravelTime, double speed, double toll, int linkType,
			FlowModel flowModel, double omega, double difficulty) throws InvalidNodeId, InvalidLinkId{
		
		int id = addLink(from, to);
		
		this.pCapacity.set(id, capacity);
		this.pDistance.set(id, distance);
		((LinkPropertyMap<Double>) this.pFreeFlowTravelTime).set(id, freeFlowTravelTime);
		this.pSpeedLimit.set(id, speed);
		this.pToll.set(id, toll);
		this.pLinkType.set(id , linkType);
		this.pFlow.set(id, 0.0);
		this.pFlowModel.set(id, flowModel);
		this.pOmega.set(id, omega);
		this.pDifficulty.set(id, difficulty);
	}

	public LinkDoublePropertyMap resetFlow(){
		LinkDoublePropertyMap oldFlow = pFlow;
		pFlow = new LinkDoublePropertyMap("Flow", this);
		return oldFlow;
	}

	/** Replace the current flow map with the give one
	 * @param flowMap the new flow map
	 * @return the old flow map
	 */
	public LinkDoublePropertyMap resetFlow(LinkDoublePropertyMap flowMap){
		LinkDoublePropertyMap oldFlow = pFlow;
		setFlow(flowMap);
		return oldFlow;
	}

	/** Makes the given flow map the flow map of the network
	 * @param flowMap the new flow map
	 */
	public void setFlow(LinkDoublePropertyMap flowMap){
		pFlow = flowMap; 
		addLinkProperty("Flow", flowMap);
	}

	public void addFlow(LinkDoublePropertyMap flowMap) {
		pFlow.add(flowMap);
	}

	public void addFlow(Path path, double additionalFlow) {
		pFlow.add(path.getLinks(), additionalFlow);
	}

	public LinkDoublePropertyMap cloneFlow() {
		return pFlow.clone();
	}

	public double getResidualCapacity(int linkId) {
		return pResidualCapcity.get(linkId);
	}

	public LinkReadOnlyProperty<Double> getFreeFlowTravelTime() {
		return pFreeFlowTravelTime;
	}

	public void setFreeFlowTravelTime(LinkReadOnlyProperty<Double> freeFlowTravelTime) {
		pFreeFlowTravelTime = freeFlowTravelTime;
		
	}

	public Assignment combineAssignment(double alpha, Assignment assignment1, double beta,
			Assignment assignment2) {
	
		Assignment result = new Assignment(this, assignment1.getSize());
		if(alpha>0){
			for(ODPairAssignment e: assignment1){
				result.add(e.route, alpha * e.volume);
			}			
		}
		
		if(beta>0){
			for(ODPairAssignment e: assignment2){
				result.add(e.route, beta * e.volume);
			}			
		}
		return result;
	}

	public double getCapacity(int linkId){
		return pCapacity.get(linkId).doubleValue();
	}

	public double getDistance(int linkId) {
		return pDistance.get(linkId); 
	}

	public LinkDoublePropertyMap getFlow() {
		return pFlow;
	}

	public double getFlow(int linkId){
		return pFlow.get(linkId).doubleValue();			
	}

	public double getFreeFlowTravelTime(int linkId) {
		return pFreeFlowTravelTime.get(linkId);
	}

	public Number getMonitoringCost(int linkId, int sensorClass) {
		//TODO: implement link monitoring cost property 
		return 1;
	}

	public double getToll(int linkId) {
		return pToll.get(linkId).doubleValue();
	}
	
	public Double getOmega(int linkId) {
		return pOmega.get(linkId).doubleValue();
	}
	
	public Double getDifficulty(int linkId) {
		return pDifficulty.get(linkId).doubleValue();
	}

	public LinkReadOnlyProperty<Double> getTotalTravelCost(){
		
		return pTotalTravelCost;
	}

	public LinkReadOnlyProperty<Double> getTotalTravelTime(){
		
		return pTotalTravelTime;
	}
	
	public LinkReadOnlyProperty<Double> getTotalTravelTimeIntegral(){
		
		return pTotalTravelTimeIntegral;
	}

	public LinkReadOnlyProperty<Double> getTravelCost(int linkId){
		
		return pTotalTravelCost;
	}

	public LinkReadOnlyProperty<Double> getTravelCostIntegral(int linkId){
		return pTotalTravelCostIntegral;
	}

	public double getTravelTime(int linkId){
		return pTravelTime.get(linkId);
	}
	
	public LinkReadOnlyProperty<Double> getTravelTime(){	
		return pTravelTime;
	}
	
	public void setTravelTime(LinkReadOnlyProperty<Double> travelTime){
		this.pTravelTime = travelTime;
	}

	public double getTravelTimeIntegral(int linkId){
		return pTravelTimeIntegral.get(linkId);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new TrafficNetwork(this);
	}


	class FlowCostFunction extends LinkReadOnlyProperty<Double>{
		final Function<Double> cost;
		
		public FlowCostFunction(String name, Function<Double> perUnitCost){
			super(name, TrafficNetwork.this, 0.0);
			cost = perUnitCost;
		}

		@Override
		public Double get(int linkId) {
			
			return pFlow.get(linkId)*cost.get(linkId);

		}

		@Override
		public LinkReadOnlyProperty<Double> clone(Network network) {
			return ((TrafficNetwork)network).new FlowCostFunction(this.name, cost.clone(network));
		}
		

	}

	class TravelTimeFunction implements Function<Double>{

		@Override
		public Double get(int linkId) {
			return pFlowModel.get(linkId).getTravelTime(
					TrafficNetwork.this.pFreeFlowTravelTime.get(linkId),
					TrafficNetwork.this.pCapacity.get(linkId), 
					TrafficNetwork.this.pFlow.get(linkId));
		}

		@Override
		public Function<Double> clone(Network network) {
			TrafficNetwork trafficNetwork = (TrafficNetwork) network;
			return trafficNetwork.new TravelTimeFunction();
		}
		
	}
	
	class TravelTimeIntegralFunction implements Function<Double>{
		
		@Override
		public Double get(int linkId) {			
			return pFlowModel.get(linkId).getTravelTimeIntegral(pTravelCost.get(linkId),
					pCapacity.get(linkId), pFlow.get(linkId));
		}

		@Override
		public Function<Double> clone(Network network) {
			TrafficNetwork trafficNetwork = (TrafficNetwork) network;
			return trafficNetwork.new TravelTimeIntegralFunction();
		}
	}

	class TravelCostFunction implements Function<Double>{

		@Override
		public Double get(int linkId) {
			return costModel.getCost(pTravelTime.get(linkId),
					pDistance.get(linkId), pToll.get(linkId));
		}

		@Override
		public Function<Double> clone(Network network) {
			TrafficNetwork trafficNetwork = (TrafficNetwork) network;
			return trafficNetwork.new TravelCostFunction();
		}
	}

	class TravelCostIntegralFunction implements Function<Double>{
		
		@Override
		public Double get(int linkId) {
			return costModel.getCostIntegral(pTravelTime.get(linkId),
					pDistance.get(linkId), pToll.get(linkId), pFlow.get(linkId));
		}

		@Override
		public Function<Double> clone(Network network) {
			TrafficNetwork trafficNetwork = (TrafficNetwork) network;
			return trafficNetwork.new TravelCostIntegralFunction();
		}
	}
	
	class ResidualCapacityFunction implements Function<Double>{		
		@Override
		public Double get(int linkId) {
			return pCapacity.get(linkId).doubleValue() - pFlow.get(linkId).doubleValue();
		}

		@Override
		public Function<Double> clone(Network network) {
			TrafficNetwork trafficNetwork = (TrafficNetwork) network;
			return trafficNetwork.new ResidualCapacityFunction();
		}		
	}	
}



