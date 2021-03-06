package tn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tn.err.InvalidLinkId;
import tn.err.InvalidNodeId;
import tn.misc.LinkProperty;
import tn.misc.LinkPropertyMap;
import tn.misc.LinkReadOnlyProperty;

public class Network {

	/**
	 * Actual number of nodes.
	 * nodeId is in [1, nodeCount]
	 */
	private final int nodeCount;
	
	/**
	 * Number of zones. A zone is a subset of nodes. 
	 * Some zones are terminal zones, that is they cannot be used
	 * for routing traffic going to other zones. Other zones can.
	 */
	private final int zoneCount;
	
	/**
	 * Actual number of links.
	 * linkId is in [1, linkCount]
	 */
	private final int linkCount;
	
	/**
	 * The set of links.
	 * [1, linkCount]
	 */
	private final Links links;
	
	private List<Integer>[] outLinks;

	private List<Integer>[] inLinks;

	/**
	 * The set of nodes
	 * [1, nodeCount]
	 */
	private final Nodes nodes;
	
	/**
	 * Storage for link origin nodes
	 */
	private final LinkProperty<Integer> pFrom;
	
	/**
	 * Storage for link destination nodes
	 */
	private final LinkProperty<Integer> pTo;
	
	private final LinkProperty<Double> pOmega;
	

	/**
	 * Link property map.
	 */
	private final Map<String, LinkReadOnlyProperty<?>> linkProperties;

	/**Network constructor
	 * @param nodeCount number of nodes
	 * @param zoneCount number of zones
	 * @param linkCount	number of links
	 */
	public Network(int nodeCount, int zoneCount, int linkCount){
		this.nodeCount = nodeCount;
		this.zoneCount = zoneCount;				
		this.linkCount = linkCount;
		this.linkProperties = new HashMap<String, LinkReadOnlyProperty<?>>();
		
		links = new Links();
		nodes = new Nodes();
				
		pFrom = new LinkPropertyMap<Integer>("From", this, 0);
		pTo = new LinkPropertyMap<Integer>("To", this, 0);
		pOmega = new LinkPropertyMap<Double>("Omega", this, 0.0);
	}
	
	/**Copy constructor
	 * @param o other network to copy from
	 */
	public Network(Network o){
		nodeCount = o.nodeCount;
		zoneCount = o.zoneCount;
		linkCount = o.linkCount;
		this.linkProperties = new HashMap<String, LinkReadOnlyProperty<?>>();
		
		links = new Links();						
		nodes = new Nodes();

		pFrom = o.pFrom.clone(this);
		pTo = o.pTo.clone(this);
		pOmega = o.pOmega.clone(this);
	}
	

	/** add a link to the network
	 * @param from start-node id
	 * @param to end-node id
	 * @return added-link id
	 * @throws InvalidLinkId 
	 */
	public int addLink(int from, int to) throws InvalidNodeId, InvalidLinkId{
		
		int id = Links.firstLinkId+links.count;
		
		if(id<Links.firstLinkId || id >= Links.firstLinkId+linkCount){
			throw new InvalidLinkId(linkCount, id);				
		}else if(from<1 || from > nodeCount){
			throw new InvalidNodeId(nodeCount, from);				
		} else if(to<1 || to>nodeCount){
			throw new InvalidNodeId(nodeCount, to);				
		} else {
			this.pFrom.set(id, from);
			this.pTo.set(id, to);
		} 
	
		links.count++;
		return id;
	}
	
	/**Add link property association
	 * @param name property name
	 * @param property link property map
	 */
	public void addLinkProperty(String name, LinkReadOnlyProperty<?> property){
		linkProperties.put(name, property);
	}
	
	/**Retrieve link property by name
	 * @param name property name
	 * @return link property map
	 */
	public LinkReadOnlyProperty<?> getProperty(String name){
		return linkProperties.get(name);
	}

	/**
	 * @return number of nodes 
	 */
	public int getNodeCount() {
		return nodeCount;
	}

	/**
	 * @return number of zones
	 */
	public int getZoneCount() {
		return this.zoneCount;
	}

	/**
	 * @return number of links
	 */
	public int getLinkCount() {
		return linkCount;
	}

	public Integer getDestination(int linkId) {
		return pTo.get(linkId);
	}

	public Integer getOrigin(int linkId) {
		return pFrom.get(linkId);
	}
	
	public Double getOmega(int linkId){
		return pOmega.get(linkId);
	}

	public Nodes getNodes() {
		return nodes;
	}

	public Links getLinks() {
		return links;
	}

	/**
	 * @return an array of lists, element i is a list of links leaving node i. 
	 * Element 0 is unused
	 */
	@SuppressWarnings("unchecked")
	public List<Integer>[] getOutLinks() {
		if(outLinks==null){
			outLinks = new List[getNodeCount()+1];
			for(int i:getNodes()){
				outLinks[i] = new LinkedList<Integer>();				
			}
			for (int e : getLinks())
				outLinks[getOrigin(e)].add(e);
		}
		return outLinks;
	}

	public List<Integer> getOutLinks(int nodeId) {
		return getOutLinks()[nodeId];
	}

	/**
	 * @return an array of lists, element i is a list of links entering node i 
	 * element 0 is unused
	 */
	@SuppressWarnings("unchecked")
	public List<Integer>[] getInLinks() {
		if(inLinks==null){
			inLinks = new List[getNodeCount()+1];
			for(int i:getNodes()){
				inLinks[i] = new LinkedList<Integer>();				
			}
			for (int e : getLinks())
				inLinks[getDestination(e)].add(e);
		}
		return inLinks;
	}

	public List<Integer> getInLinks(int nodeId) {
		return getInLinks()[nodeId];
	}

	public int getFirstNodeId() {
		return TrafficNetwork.Nodes.firstNodeId;
	}

	public int getLastNodeId() {
		return TrafficNetwork.Nodes.firstNodeId+nodeCount-1;
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(toStringHeader());
		
		sb.append(links.toString());		
		sb.append("\n  ");
		
		return sb.toString();		
	}
	
	private String toStringHeader(){
		return String.format("Network has %d nodes and %d links\n", 
				getNodeCount(), getLinkCount());
	}
	
	public String toString(String[] columns) {
		
		//TODO: Clean up this code 
		StringBuilder sb = new StringBuilder();
		
		sb.append(toStringHeader());
		
		sb.append(links.toString(columns));		
		sb.append("\n  ");
		
		return sb.toString();		
	}	

	/** Set of links, indexed by linkId in [1, links.size()]
	 */
	public class Links implements Iterable<Integer>{

		public static final int firstLinkId = 1;
		
		int count = 0;
		
		public Links() {
		}

		public boolean contains(int linkId){
			return linkId>= firstLinkId && linkId<firstLinkId+count;
		}

		public String toString(String[] columns){
			StringBuilder sb = new StringBuilder("LinkId\tFrom\tTo");
			for(String column: columns){
				sb.append('\t'); sb.append(column);
			}
			sb.append('\n'); 
			
			
			for(int i:links){
				sb.append(String.format("#%d\t%d\t%d", 
						i, getOrigin(i), getDestination(i)));
				
				for(String column: columns){
					LinkReadOnlyProperty property = linkProperties.get(column);
					sb.append('\t'); sb.append(property.get(i).toString());					 
				}
				sb.append('\n'); 
			}
			return sb.toString();
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("LinkId\tFrom\tTo");
			for(Entry<String, LinkReadOnlyProperty<?>> property : linkProperties.entrySet()){
				sb.append('\t'); sb.append(property.getKey());
			}
			sb.append('\n'); 
			
			
			for(int i:links){
				sb.append(String.format("#%d\t%d\t%d", 
						i, getOrigin(i), getDestination(i)));
				
				for(Entry<String, LinkReadOnlyProperty<?>> property : linkProperties.entrySet()){
					sb.append('\t'); sb.append(property.getValue().get(i).toString());					 
				}
				sb.append('\n'); 
			}
			return sb.toString();
		}

		@Override
		public Iterator<Integer> iterator() {
			class LinkIdIterator implements Iterator<Integer>{
				int i;
				
				public LinkIdIterator(){
					i=firstLinkId-1;
				}
				
				@Override
				public boolean hasNext() {
					return i<linkCount;
				}

				@Override
				public Integer next() {
					i++;
					return i;
				}

				@Override
				public void remove() {
					throw new IllegalArgumentException();
				}
				
			}

			return new LinkIdIterator();
		}
		
	}
	
	public class Nodes implements Iterable<Integer>{

		public static final int firstNodeId = 1;
		
		int count = 0;
		
		public Nodes() {
		}

		public boolean contains(int nodeId){
			return nodeId>= firstNodeId && nodeId<firstNodeId+count;
		}


		@Override
		public Iterator<Integer> iterator() {
		
			class NodeIdIterator implements Iterator<Integer>{

				int i;
				
				public NodeIdIterator(){
					i=firstNodeId-1;
				}
				
				@Override
				public boolean hasNext() {
					return i<nodeCount;
				}

				@Override
				public Integer next() {
					i++;
					return i;
				}

				@Override
				public void remove() {
					throw new IllegalArgumentException();
				}
			}
			
			return new NodeIdIterator();
		}
	}
		
}
