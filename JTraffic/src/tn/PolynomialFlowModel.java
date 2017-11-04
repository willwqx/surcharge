package tn;

public class PolynomialFlowModel implements FlowModel{
	double B;		//increase in travel time when link reaches capacity
	double power;	//rate of increase in travel time beyond capacity

	
	public PolynomialFlowModel(double B, double power){
		this.B = B;
		this.power = power;
	}
	


	@Override
	public double getTravelTime(double freeFlowTime,
			double capacity, double actualFlow){
		return innerGetTravelTime(freeFlowTime, capacity, actualFlow);
	}
	
	@Override
	public double getTravelTimeIntegral(double freeFlowTime, double capacity, 
			double actualFlow){
		return innerGetTravelTimeIntegral(freeFlowTime, capacity, actualFlow);
	}
	
	@Override
	public FlowModel clone() {		
		return new PolynomialFlowModel(this.B, this.power);
	}

	public class WithCutoff implements FlowModel{

		@Override
		public double getTravelTimeIntegral(double freeFlowTime, double capacity,
				double actualFlow) {
			if(actualFlow<capacity)
				return innerGetTravelTimeIntegral(freeFlowTime, capacity, actualFlow);
			else
				return Double.POSITIVE_INFINITY;
		}
		
		@Override
		/*
		 * MUST CHANGE
		 */
		public double getTravelTime(double freeFlowTime, double capacity, 
				double actualFlow){
			//if(actualFlow<capacity)
				return innerGetTravelTime(freeFlowTime, capacity, 
						actualFlow); 
			//else 
				//return Double.POSITIVE_INFINITY;
		}

		@Override
		public FlowModel clone() {
			return new PolynomialFlowModel(B, power).new WithCutoff();
		}
	}
	
	/*
	 * CHECK VALIDITY
	 */
	private double innerGetTravelTimeIntegral(double freeFlowTime, double capacity,
			double actualFlow){
		return freeFlowTime*(1+B*Math.pow(actualFlow/capacity, power));
	}
	
	private double innerGetTravelTime(double freeFlowTime,
			double capacity, double actualFlow) {
		return freeFlowTime*(1+B*Math.pow(actualFlow/capacity, power));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("(%.2f,%.2f)", B, power);
	}
}
	
