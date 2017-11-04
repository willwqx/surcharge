package tn.test;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import tn.Assignment;
import tn.Demand;
import tn.TrafficNetwork;
import tn.misc.LinkReadOnlyProperty;
import tn.stats.LinkNormalizedProperty;
import tn.stats.LinkPropertyStatistics;
import tn.vulnerability.FFTTGrader;
import tn.vulnerability.HybridTTGrader;
import tn.vulnerability.TTGrader;
import tn.vulnerability.UETTGrader;

public class HybridUEMethod {
	private TrafficNetwork network;
	private Demand demand;
	
	private LinkReadOnlyProperty<Double> grades[] = new LinkReadOnlyProperty[3];	
	TTGrader[] graders = new TTGrader[3];
	
	public void test(){
		
		TestNetwork sample = new TestNetwork(TestNetwork.NetworkId.Anaheim);
		network = sample.getNetwork();
		demand = sample.getDemand();
		
		PrintStream out=null;
		if(/*Log file*/true){
			try {
				out = new PrintStream("debug.txt");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		System.out.print(network);

		graders[0] = new FFTTGrader(out); 
		graders[1] = new UETTGrader(out); 
		graders[2] = new HybridTTGrader(out);
		

		if(false){
			graders[1].delayedGrade(network, demand);
			out.printf("grading link 11\n");
			out.printf("%f\n", graders[1].getGrades().get(11));

			if(false){
				out.printf("Base Assignment:\n%s\nLoaded Network:\n%s\n", 
						graders[1].getBaseAssignment().toString(),
						network.toString(new String[]
						    {"Flow", "Cpcty", "FFTTime", "TCost", "f*TCost" }));
				
				out.printf("New Assignment:\n%s\nLoaded Network:\n%s\n", 
						graders[1].getReassignment().toString(),
						network.toString(new String[]
						    {"Flow", "Cpcty", "FFTTime", "TCost", "f*TCost" }));
				
				out.printf("Difference Assignment:\n%s\n", 
						Assignment.getDifference(demand, 
								graders[1].getBaseAssignment(),
								graders[1].getReassignment()).toString());
			}
		}
		
		else
		{
			for(int i=0; i<3; i++){
				grades[i] = graders[i].grade(network, demand);
	
				System.out.printf("\n%s Method:\n%s\nRuntime = %d ms\n", 
						graders[i].getTitle(), grades[i].toString(), graders[i].getRuntimeMillis());
			}
			analyzeCorrelation(graders[1], graders[0]);
			analyzeCorrelation(graders[1], graders[2]);
		}
	}
	
	private void analyzeCorrelation(TTGrader graders1, TTGrader graders2) {
		double p=tn.stats.LinkPropertyStatistics.getCovariance(
				new tn.stats.LinkNormalizedProperty(graders1.getGrades()), 
				new tn.stats.LinkNormalizedProperty(graders2.getGrades()));
		System.out.printf("\nPearson's Correlation Coefficient between %s and %s = %f\n", 
				graders1.getTitle(), graders2.getTitle(), p);
		
		double s=tn.stats.LinkPropertyStatistics.getCovariance(
				new LinkNormalizedProperty(
						new LinkPropertyStatistics(graders1.getGrades()).getRank()),
				new LinkNormalizedProperty(		
						new LinkPropertyStatistics(graders2.getGrades()).getRank()));

		System.out.printf("\nSpearman's Correlation Coefficient between %s and %s = %f\n", 
				graders1.getTitle(), graders2.getTitle(), s);
	}


	public static void main(String[] args) {
    	HybridUEMethod networkTest = new HybridUEMethod();
    	networkTest.test();
    }	

}
