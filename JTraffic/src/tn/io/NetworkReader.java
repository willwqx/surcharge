package tn.io;

import java.io.FileNotFoundException;
import java.io.FileReader;

import tn.Demand;
import tn.FlowModel;
import tn.ODPair;
import tn.PolynomialFlowModel;
import tn.TrafficNetwork;
import tn.err.InvalidDemand;
import tn.err.InvalidInputException;

public class NetworkReader {
	
	private String fileName;
	private FileReader inputFile;
	private LineNumberScanner lineScanner;
	private TrafficNetwork network;
	
	/*
	 * Open a text file for reading network topology and/or demand
	 * 
	 * fileName: file to open
	 */
	public NetworkReader(String fileName) throws FileNotFoundException{
		this.fileName = fileName;
		inputFile = new FileReader(fileName);
		lineScanner = new LineNumberScanner(inputFile);		
	}
	
	/*
	 * read network topology, i.e. links and link properties
	 */
	public TrafficNetwork readTopology() throws InvalidInputException{
		network = null;
		
		try{
			//Read network specifications
			//Number of zones
			int zoneCount = lineScanner.nextInt();
			
			//Number of nodes
			int nodeCount = lineScanner.nextInt();

			//First through node
			//TODO: handle non-through nodes
			@SuppressWarnings("unused")
			int firstThroughNode = lineScanner.nextInt();
			
			//read number of links
			int linkCount = lineScanner.nextInt();						

			//construct network
			network = new TrafficNetwork(nodeCount, zoneCount, linkCount);

			//read and construct links			
			for(int i=0; i<linkCount; i++){
				int 	from = lineScanner.nextInt();
				int 	to = lineScanner.nextInt();
				double 	capacity = lineScanner.nextDouble();
				double 	distance = lineScanner.nextDouble();
				double 	travelTime = lineScanner.nextDouble();
				double 	B = lineScanner.nextDouble();
				double 	power = lineScanner.nextDouble();
				double 	speed=lineScanner.nextDouble();
				double 	toll = lineScanner.nextDouble();
				int 	linkType = lineScanner.nextInt();
				double omega = lineScanner.nextDouble();
				double difficulty = lineScanner.nextDouble();
				
				
				//FlowModel flowModel = new PolynomialFlowModel(B, power).new WithCutoff();
				FlowModel flowModel = new PolynomialFlowModel(B, power);
				
				network.addLink(from, to, capacity, distance, travelTime, speed, toll, linkType, flowModel, omega, difficulty);
			}
			
			return network;
			
		}
		catch(Exception e){
			
			throw new InvalidInputException(fileName, lineScanner.getLineNumber(), e);
			
		}
	}
	/*
	 * network: network topology for the reader to assign demand to 
	 */
	public void readDemand(TrafficNetwork network)throws InvalidInputException{
		this.network = network;
		readDemand();
	}
	
	/**Reads demand from the input file.
	 * @return Demand object that maps O-D pairs and their corresponding demand 
	 * @throws InvalidInputException
	 */
	public Demand readDemand() throws InvalidInputException{
		if(network==null){
			throw new NullPointerException("Can't read demand because network topology is null!\nYou must first call readTopology or call setTopology with a valid Network object.");
		}
				
		try {
			int srcCount = lineScanner.nextInt();
			@SuppressWarnings("unused")
			int pairCount = 
					lineScanner.nextInt();
			Demand demand = new Demand(network);
			for(int i=0; i<srcCount; i++){
				int dstCount = lineScanner.nextInt();
				int initNode = lineScanner.nextInt();
				for(int j=0; j<dstCount; j++){
					int termNode = lineScanner.nextInt();
					double volume = lineScanner.nextDouble();
					ODPair od = new ODPair(initNode, termNode);
					if(initNode==termNode){
						if(volume!=0){
							throw new InvalidDemand(od, volume);
						}
					}else{
						demand.add(od, volume);						
					}
				}
			}

			return demand;
		}
		catch(Exception e){
			
			throw new InvalidInputException(fileName, lineScanner.getLineNumber(), e);
			
		}
	}
	
}
