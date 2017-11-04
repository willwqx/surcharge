package tn.misc;

import java.util.HashMap;
import java.util.Set;

import tn.Network;

/**
 *<b>Description:</b><br><br>
 *This class is responsible for creating a map (hash map) for a specific link property. 
 *The map holds link IDs and the corresponding property values. It also has some 
 *map accessibility and cloning and modification functions. 
 */


public class LinkPropertyMap<T> extends LinkProperty<T> {


	/**
	 * Link property internal storage (a hash map that stores link IDs and 
	 * the corresponding properties)
	 */
	protected HashMap<java.lang.Integer, T> map;

	
	
	/**Standard constructor: Initialize {@link LinkPropertyMap} object:
	 * <ul>
	 * <li>Create a {@link LinkReadOnlyProperty}</li>
	 * <li>Create a hash map and initialize it with link IDs</li>
	 * </ul>
	 * @param network the network with which this property is associated
	 * @param defaultValue default value of the property
	 */
	public LinkPropertyMap(String name, Network network, T defaultValue) {
		super(name, network, defaultValue);
		map = new HashMap<java.lang.Integer, T>(network.getLinkCount());
	}

	/**Copy constructor: Makes a copy of the given property map and stores the 
	 * non-default-value link-property values in this new property map 
	 * @param sourceMap property to copy
	 */
	public LinkPropertyMap(LinkReadOnlyProperty<T> sourceMap) {
		super(sourceMap);
		map = new HashMap<Integer, T>(network.getLinkCount());
		/* for each link in the network, if the property value is not the default one
		 * store it in the new map
		 */
		for(int i:network.getLinks()){
			if(sourceMap.get(i)!=defaultValue)
				map.put(i, sourceMap.get(i));
		}
	}

	/**Copy constructor: Initialize {@link LinkPropertyMap} object using the 
	 * content of another {@link LinkPropertyMap} object.
	 * @param other the other {@link LinkPropertyMap} to be copied
	 */
	public LinkPropertyMap(LinkPropertyMap<T> other){
		super(other.getName(), other.network, other.defaultValue);
		map = new HashMap<java.lang.Integer, T>(other.map);
	}

	/**Copy constructor: Makes a copy of the given property map for a different network
	 *  and stores the non-default value link-property values in this new property map 
	 * @param network new network
	 * @param sourceMap property to copy
	 */
	public LinkPropertyMap(Network network, LinkReadOnlyProperty<T> sourceMap) {
		super(sourceMap.getName(), network, sourceMap.defaultValue);
		map = new HashMap<Integer, T>(network.getLinkCount());
		/* for each link in the network, if the property value is not the default one
		 * store it in the new map
		 */		
		for(int i:network.getLinks()){
			if(sourceMap.get(i)!=defaultValue)
				map.put(i, sourceMap.get(i));
		}
	}

	/* (non-Javadoc)
	 * @see tn.LinkReadOnlyProperty#get(int)
	 */
	@Override
	/* Implementation of the get method of the LinkReadOnlyProperty class.
	 * Return the property value for a given link if the link is stored in 
	 * the link property map, otherwise return the default value of that property. 
	 * The returned value is of type T. 
	 */   
	public T get(int linkId) {
		if(map.containsKey(linkId))
			return map.get(linkId);
		else
			return defaultValue;
	}

	/* (non-Javadoc)
	 * @see tn.LinkProperty#set(int, java.lang.Object)
	 */
	@Override
	/* Remove the links with default values, and store 
	 * the given values as the values for the given links
	 */
	public void set(int linkId, T value) {
		if(value.equals(defaultValue))
			map.remove(linkId);
		else
			map.put(linkId, value);
	}

	/**Update the link property values from the given map. 
	 * Links that do not have a mapping in the given map remain unchanged.
	 * @param other source of link property values
	 * @return this LinkPropertyMap after being updated     ??? void --> no return
	 */
	public void set(LinkPropertyMap<T> other){
		map.putAll(other.map);
	}
					
	/* (non-Javadoc)
	 * @see tn.misc.LinkProperty#reset()
	 */
	@Override
	// As the name implies, it clears all values in the map
	public void reset() {
		map.clear();
	}

	@Override
	// Make a copy of the LinkPropertyMap
	public LinkPropertyMap<T> clone() {
		return new LinkPropertyMap<T>(this);
	}

	@Override
	// Make a copy for another network
	public LinkPropertyMap<T> clone(Network network) {
		return new LinkPropertyMap<T>(network, this);
	}
	
	
	/**
	 * Returns the set of link IDs contained in the map
	 * @return {@link Set} the set of link IDs
	 */
	
	// 																	??? This returns all keys
	public Set<Integer> getNonDefaultLinkIdSet(){
		return map.keySet();
	}
}
