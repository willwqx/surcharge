package tn.misc;

import java.util.Map;

import tn.Network;

public class LinkDoublePropertyMap extends LinkPropertyMap<java.lang.Double>{

	public LinkDoublePropertyMap(LinkPropertyMap<Double> other) {
		super(other);
	}

	public LinkDoublePropertyMap(LinkReadOnlyProperty<Double> other) {
		super(other);
	}

	public LinkDoublePropertyMap(String name, Network network, Double defaultValue) {
		super(name, network, defaultValue);
	}

	public LinkDoublePropertyMap(Network network, LinkReadOnlyProperty<Double> other) {
		super(network, other);
	}

	public LinkDoublePropertyMap(Network network, LinkPropertyMap<Double> other) {
		super(network, other);
	}

	public LinkDoublePropertyMap(String name, Network network){
		super(name, network, 0.0);
	}
	
	public LinkDoublePropertyMap(String name, Network network, double defaultValue) {
		super(name, network, defaultValue);
	}

	public java.lang.Double add(int i, double flow){
		double sum = this.get(i)+flow;
		this.set(i, sum);
		return sum;
	}

	public LinkDoublePropertyMap add(LinkDoublePropertyMap flows){
		for(int i: flows.map.keySet()){
			double sum = this.get(i)+flows.get(i);
			this.set(i, sum);
		}
		return this;
	}
	
	public LinkDoublePropertyMap add(Iterable<java.lang.Integer> keySet, double value){
		for(int link: keySet) 
			this.add(link, value);
		return this;
	}

	public LinkDoublePropertyMap combine(LinkDoublePropertyMap other, double otherFactor, double thisFactor){
		for(Map.Entry<java.lang.Integer, java.lang.Double> e: other.map.entrySet()){
			double thisFlow = get(e.getKey());
			double otherFlow = e.getValue();
			double newFlow = otherFactor*otherFlow+ thisFactor*thisFlow;
			map.put(e.getKey(), newFlow);
		}
		return this;
	}
	
	/**Generate a linear combination of two properties
	 * @param a coefficient of the first property
	 * @param A first property
	 * @param b coefficient of the second property
	 * @param B second property
	 * @return combined property
	 */
	static public LinkDoublePropertyMap combine(double a, LinkDoublePropertyMap A, double b, LinkDoublePropertyMap B){

		LinkDoublePropertyMap result = new LinkDoublePropertyMap(A.getName(), A.getNetwork(), 0.0);
										
		for(Map.Entry<java.lang.Integer, java.lang.Double> e: A.map.entrySet()){
			Integer l = e.getKey();
			result.add(l, a*A.get(l));
		}
		
		for(Map.Entry<java.lang.Integer, java.lang.Double> e: B.map.entrySet()){
			Integer l = e.getKey();
			result.add(l, b*B.get(l));
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see tn.misc.LinkPropertyMap#clone()
	 */
	@Override
	public LinkDoublePropertyMap clone() {
		return new LinkDoublePropertyMap(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();

		for(int i: network.getLinks()){
			
			b.append(String.format("#%d\t%10f\n", i, this.get(i)));
		}
		return b.toString();
	}

	public String toRowString() {
		StringBuilder b = new StringBuilder();
		for(int i: network.getLinks()){	
			b.append(String.format(",%10f", this.get(i)));
		}
		return b.toString();
	}
	
	/* (non-Javadoc)
	 * @see tn.misc.LinkPropertyMap#clone(tn.Network)
	 */
	@Override
	public LinkDoublePropertyMap clone(Network network) {
		return new LinkDoublePropertyMap(network, this);
	}
}