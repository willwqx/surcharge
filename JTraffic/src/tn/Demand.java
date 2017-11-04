package tn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Demand specifications, i.e. volume of traffic between each pair of Origin-Destination 
 */
public class Demand implements Iterable<Map.Entry<ODPair, Double>>{
	
	TrafficNetwork network;
	
	/**A Map to store the demand elements:
	 * For each ODPair, the corresponding demand volume 
	 */
	private Map<ODPair, Double> demand;
	
	public Demand(TrafficNetwork network){
		this.network = network;
		demand = new HashMap<ODPair, Double>();
	}
	
	public TrafficNetwork getNetwork(){
		return network;
	}

	
	/**get the demand associated with the given origin-destination pair
	*/
	public double get(ODPair od){
		if(demand.containsKey(od)){
			return demand.get(od);
		} else {
			return 0;
		}
	}
	
	/*public ODPair getODPair(int orignal, int destination){

	}*/

	public Set<ODPair> ODSet(){
		return demand.keySet();
	}
	
	public int count(){
		return demand.size();
	}
	
	/**
	 * Sets the demand for the given o-d pair.
	 * @param od Origin-Destination pair
	 * @param volume the traffic volume to assign to this pair
	 * @return the new total volume associated with the given od pair
	 */
	public double set(ODPair od, double volume) {
		demand.put(od, volume);
		return volume;
	}
	
	/**Adds the given volume to the demand corresponding to the given o-d pair. 
	 * You can use negative numbers to subtract. If the given volume is zero
	 * the call has no effect whatsoever other than returning the 
	 * previous demand volume, if one exists. 
	 * @param od Origin-Destination pair
	 * @param volume the traffic volume to add (use negative numbers to subtract)
	 * @return the new total volume associated with the given od pair
	 */
	public double add(ODPair od, double volume){
		if(demand.containsKey(od)){
			//If the given volume is zero, just return the already existing demand volume for the given od pair
			if(volume==0) return demand.get(od);
			
			//Add the old demand volume to the new volume 
			volume+= demand.get(od);

			//update the demand volume
			demand.put(od, volume);
			
			//and return the updated volume
			return volume;
			
		}else{//There is no pre-existing demand corresponding to this od pair
			//If the given volume is zero, just return zero
			if(volume==0) return 0.0;

			//Update the map with the new volume only if it is greater than zero. Otherwise, return zero.
			demand.put(od, volume);
			return volume;				
		}
	}
	
	public double sub(ODPair od, double volume){
		if(demand.containsKey(od)){
			//If the given volume is zero, just return the already existing demand volume for the given od pair
			if(volume==0) return demand.get(od);
			
			//Add the old demand volume to the new volume 
			volume-= demand.get(od);

			//update the demand volume
			demand.put(od, volume);
			
			//and return the updated volume
			return volume;
			
		}else{//There is no pre-existing demand corresponding to this od pair
			//If the given volume is zero, just return zero
			if(volume==0) return 0.0;

			//Update the map with the new volume only if it is greater than zero. Otherwise, return zero.
			demand.put(od, volume);
			return volume;				
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Demand clone = new Demand(network);
		clone.demand.putAll(demand);
		return clone;
	}


	/**Get the set of O-D pairs with assigned demand
	 * @return iterator of (ODPair, volume) demand entries
	 */
	@Override
	public Iterator<Entry<ODPair, Double>> iterator() {
		return demand.entrySet().iterator();
	}


	/**
	 * @return total volume of all demand
	 */
	public double getTotal() {
		double total = 0.0;
		for(Double volume: demand.values()) total+=volume;
		return total;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Demand [" + demand + "]";
	}		
}
