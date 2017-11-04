package tn.misc;

import java.util.Set;

import tn.Network;

/**<b>Description:</b><br><br>
 * This class creates a {@link LinkReadOnlyProperty} that can be scaled by a factor stored in
 * a factor map of type {@link LinkPropertyMap}. The scaled property is needed to account for 
 * disruption of a specific property by multiplying its value by that "disruption" factor. The class 
 * provides useful methods to set the factor for the property of a given link, get disrupted links  
 * , reset the factors  of all links to one, etc ...
 */
public class ScaledLinkProperty extends LinkReadOnlyProperty<Double>{

	/**
	 * The factor map
	 */
	LinkPropertyMap<Double> factorMap;
	
	/**
	 * The original property that is to be disrupted
	 */
	LinkReadOnlyProperty<Double> originalProperty;

	/**<ul>
	 * <li>Store original {@link LinkReadOnlyProperty}</li>
	 * <li>Create a copy of the property to scale, and initialize it to a zero default value</li>
	 * <li>Create a factor map (of type: {@link LinkPropertyMap}) to use in scaling the created 
	 * copy, and initialize it to a one default value</li>
	 * </ul>
	 * @param originalProperty The {@link LinkReadOnlyProperty} to be scaled
	 */
	
	public ScaledLinkProperty(LinkReadOnlyProperty<Double> originalProperty) {
		super(originalProperty.getName().concat("-Scaled"), originalProperty.getNetwork(), 0.0);
		factorMap = new LinkPropertyMap<Double>(originalProperty.getName().concat("-Factor"), originalProperty.getNetwork(), 1.0);
		this.originalProperty = originalProperty;
	}

	/* (non-Javadoc)
	 * @see tn.misc.LinkPropertyMap#get(int)
	 */
	// Multiply the factor by the property of the specified link and return the result
	@Override
	public Double get(int linkId) {
		return factorMap.get(linkId) * originalProperty.get(linkId);     // I have a concern here
	}

	
	/* (non-Javadoc)
	 * @see tn.misc.LinkReadOnlyProperty#clone()
	 */
	// Make a scaled copy of the original property
	@Override
	public ScaledLinkProperty clone() {
		return new ScaledLinkProperty(originalProperty);
	}

	/*
	 * (non-Javadoc)
	 * @see tn.misc.LinkReadOnlyProperty#clone(tn.Network)
	 */
	// Make a scaled copy of the original property and associate it with the given network
	@Override
	public ScaledLinkProperty clone(Network network) {
		return new ScaledLinkProperty(originalProperty.clone(network));
	}
	
	
	/**
	 * @return the set of links whose factor is not 1.0 
	 */
	public Set<Integer> getScaledLinks() {
		return factorMap.getNonDefaultLinkIdSet();
	}
	
	/**
	 * Set the factor for the given link to the given factor.
	 * It is called disrupt because it is used to set the factor to be multiplied by the link property 
	 * to account for the amount of disruption to the property of that link (caused by the tester). 
	 * @param linkId link id
	 * @param factor the factor to be multiplied
	 */
	public void disrupt(int linkId, double factor){
		factorMap.set(linkId, factor);
	}
	
	/**
	 * Reset the factor of the given link to 1.0
	 * @param linkId link id
	 */
	public void reset(int linkId){
		factorMap.set(linkId, 1.0);
	}
	
	/**
	 * Reset the factor of all links to 1.0
	 */
	public void reset(){
		factorMap.reset();
	}
}

