package tn.misc;

import org.apache.commons.collections15.Transformer;

/**
 * Encapsulate a {@link LinkProperty<Double>} in a {@link Transformer<Integer, Number>} wrapper
 * So that it can be passed to JUNG functions
 */
public class LinkPropertyTransformer implements Transformer<Integer, Number> {

	final LinkReadOnlyProperty<Double> property;
	
	public LinkPropertyTransformer(LinkReadOnlyProperty<Double> property){
		
		this.property = property;
	}
	
	@Override
	public Double transform(Integer linkId) {
		
		return property.get(linkId);
	}
}

