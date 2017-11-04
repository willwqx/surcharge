package tn.misc;

import tn.Network;

/** 
 * @author Saleh Ibrahim
 */

/**<b>Description:</b><br><br>
 * This class is the base class for creating a link property. As the name implies, 
 * it creates a "read-only" property. Therefore, it has only getter methods to get 
 * desired property-associated values. All property values must be convertable to 
 * {@link Double}. Instantiating this abstract class and implementing its methods 
 * enables the creation of a read-only link property of a specified type "T".  
 */


public abstract class LinkReadOnlyProperty<T> {
	/**
	 * Default value of the link property
	 */
	final protected T defaultValue;
	
	/**
	 * the network object with which the property is associated 
	 */
	final protected Network network;
	
	/**
	 * Property name
	 */
	final protected String name;

	/**Initialize {@link LinkReadOnlyProperty} object: 
	 * <ul>
	 * <li>Initialize variables.</li>
	 * <li> Add the created object "{@link LinkReadOnlyProperty}" to the link property 
	 * map of the network under consideration.</li>
	 * </ul>
	 * @param name property name
	 * @param network the network object with which the property is associated 
	 * @param defaultValue	the default value of link property
	 */
	public LinkReadOnlyProperty(String name, Network network, T defaultValue) {
		this.network = network;
		this.defaultValue = defaultValue;
		this.name = name;
		network.addLinkProperty(name, this);
	}
	
	/**Copy constructor. Copy another {@link LinkReadOnlyProperty}, 
	 *  and use its associated network as the network of this {@link LinkReadOnlyProperty}
	 * @param other source object to copy from
	 */
	public LinkReadOnlyProperty(LinkReadOnlyProperty<T> other){
		this.network = other.network;
		this.defaultValue = other.defaultValue;
		this.name = other.name;
	}
	
	/**Copy another {@link LinkReadOnlyProperty}, 
	 *  and use the given network as the network of this {@link LinkReadOnlyProperty}.
	 * @param network network object to use as the {@link LinkReadOnlyProperty}'s network.
	 * @param other source object to copy from
	 */
	
	public LinkReadOnlyProperty(Network network, LinkReadOnlyProperty<T> other) {
		this.network = network;
		this.defaultValue = other.defaultValue;
		this.name = other.name;
	}

	/**Get the value of the link property for the given linkId
	 * @param linkId index of the link to inspect
	 * @return the value of the link property corresponding to linkId
	 */
	public abstract T get(int linkId);
	
	/**
	 * @return the network containing the links to which the property is associated 
	 */
	public Network getNetwork() {
		return network;
	}
	
	/**
	 * @return the property name
	 */
	public String getName(){
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	// Returns the a copy of the LinkReadOnlyProperty object.
	public LinkReadOnlyProperty<T> clone (){
		return this;
	}
	
	/**Clone for association with a different network
	 * @param network the other network
	 * @return a clone associated with the given network
	 */
	public abstract LinkReadOnlyProperty<T> clone(Network network);

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		// for each link the set of links forming the network
		for(int i:network.getLinks()){
			/* Append the link number along with its property value to 
			 * the above created string (for display purposes)
			 */
			sb.append(String.format("#%d\t%s\n", i, get(i).toString()));
		}
		return sb.toString();
	}
	
	/**Calculate the sum of the property value for all links
	 * property value type must be convertible to Double
	 * @return total
	 */
	public double getSum(){
		double sum = 0.0;
		// for each link; get its value and add it to the total sum
		for(int i:network.getLinks()){
			sum +=(Double) get(i);
		}
		return sum;
	}
}
