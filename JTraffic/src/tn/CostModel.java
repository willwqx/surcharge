package tn;

public class CostModel{
	
	private double distanceFactor;
	private double tollFactor;
	
	public CostModel(double distanceFactor, double tollFactor){
		this.distanceFactor=distanceFactor;
		this.tollFactor=tollFactor;
	}

	public CostModel(CostModel costModel) {
		this.distanceFactor=costModel.distanceFactor;
		this.tollFactor=costModel.tollFactor;
	}

	public double getCost(double travelTime, double distance, double toll) {
		
		return travelTime + distanceFactor* distance + tollFactor*toll;
	}
	
	public double getCostIntegral(double travelTimeIntegral, double distance, double toll, double flow){
		return travelTimeIntegral + flow  * (distanceFactor* distance + tollFactor*toll);
	}
}
