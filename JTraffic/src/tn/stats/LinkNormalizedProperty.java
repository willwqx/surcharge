package tn.stats;

import tn.Network;
import tn.misc.LinkReadOnlyProperty;

public class LinkNormalizedProperty extends LinkReadOnlyProperty<Double>{
	double mean, stdDev;
	LinkReadOnlyProperty<Double> grades;
	
	public LinkNormalizedProperty(LinkReadOnlyProperty<Double> grades){
		super(grades);
		this.grades = grades;
		LinkPropertyStatistics stats = new LinkPropertyStatistics(grades);
		mean = stats.getMean();
		stdDev = stats.getStdDev();
	}

	@Override
	public LinkReadOnlyProperty<Double> clone(Network network) {
		return new LinkNormalizedProperty(grades.clone(network)); 
	}

	@Override
	public Double get(int linkId) {
		return (grades.get(linkId)-mean)/stdDev;
	}
}

