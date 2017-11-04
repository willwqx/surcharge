package tn.stats;

import java.util.Arrays;

import tn.Network;
import tn.misc.LinkReadOnlyProperty;

public class LinkPropertyStatistics {
	
	private LinkReadOnlyProperty<Double> grades;
	double mean, stdDev;
	Network network;
	
	int rank[];

	public LinkPropertyStatistics(LinkReadOnlyProperty<Double> grades){
		this.grades  = grades;
		this.network = grades.getNetwork();
		
		mean = 0;
		for(int linkId:network.getLinks()){
			mean+=grades.get(linkId);
		}
		mean/=network.getLinkCount();

		stdDev = 0;
		for(int linkId:network.getLinks()){
			double dev = grades.get(linkId)-mean;
			stdDev +=dev*dev;
		}
		stdDev = Math.sqrt(stdDev/network.getLinkCount());
	}

	public double getMean() {
		return mean;
	}

	public double getStdDev() {
		return stdDev;
	}
	
	RankProperty rankProperty=null;
	
	public RankProperty getRank(){
		
		if(rankProperty==null){
			
			double[] gradesArray =  new double[network.getLinkCount()+1];
			for(int i:network.getLinks()){
				gradesArray[i] = grades.get(i);
			}
			
			rank = new int[network.getLinkCount()+1];
			
			Arrays.sort(gradesArray, 1, network.getLinkCount()+1);
			for(int i:network.getLinks()){
				rank[i] = Arrays.binarySearch(gradesArray, 1, network.getLinkCount()+1, grades.get(i));
			}
			rankProperty = new RankProperty();
		}
		
		return rankProperty;
	}
	
	class RankProperty extends LinkReadOnlyProperty<Double>{

		public RankProperty() {
			super("Rank", LinkPropertyStatistics.this.network, 0.0);
		}

		@Override
		public LinkReadOnlyProperty<Double> clone(Network network) {
			return this;
		}

		@Override
		public Double get(int linkId) {			
			return (double)(rank[linkId]);
		}		
	}
	
	public static double getCovariance(LinkReadOnlyProperty<Double> p1, LinkReadOnlyProperty<Double> p2){
		double cov = 0;
		Network network = p1.getNetwork();
		
		for(int i:network.getLinks()){
			cov+=p1.get(i) * p2.get(i);
		}
		cov/=network.getLinkCount();
		
		return cov;		
	}
}
