package tn.algorithm;

import tn.Assignment;
import tn.Demand;
import tn.TrafficNetwork;

/**A router takes the {@link Demand} applied to a {@link TrafficNetwork} and 
 * generates a corresponding {@link Assignment} that satisfies
 * the given demand
 */
public interface Router{
	
	/**
	 * @param demand Demand object containing a map of 
	 * OD pairs and the corresponding demand volume
	 * @return Assignment object containing a map of paths 
	 * and the corresponding flow in each path.
	 * Upon return, the actual flow in the network must remain unchanged
	 */
	public Assignment route(Demand demand); 
}
