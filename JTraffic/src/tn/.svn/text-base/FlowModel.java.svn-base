package tn;

public interface FlowModel{	
	/** calculate the flow-dependent travel time per user, i.e. f(x)
	 * , where x is the flow and f is the travel time function
	 * @param freeFlowTime travel time at x = 0
	 * @param capacity link capacity at which travel time starts to build up due to link saturation
	 * @param actualFlow
	 * @return travel time derivative 
	 */
	public double getTravelTime(double freeFlowTime, double capacity, double actualFlow);

	/**
	 * Calculate the per user travel time integral F(x)
	 * Total link travel time for all X users = 
	 * Integrate f(x) dx up to X = 
	 * X.F(X)
	 * @param freeFlowTime travel time at flow -> 0
	 * @param capacity link capacity at which travel time starts building up due to saturation
	 * @param actualFlow 
	 * @return travel time
	 */
	public double getTravelTimeIntegral(double freeFlowTime, double capacity, double actualFlow);

	public FlowModel clone();
}

