package tn.misc;

import tn.Network;

/**
 *<b>Description:</b><br><br>
 * This class creates a wrapper for a function to be applied to a certain link property. It
 * enables accessing the function, and making a copy of it. 
 */


public class LinkPropertyFunction<T> extends LinkReadOnlyProperty<T> {
	
	/** An interface that when implemented represents a function that can be applied 
	 * on a property. It enables getting the function itself and making a copy 
	 * of that function.
	 */
	static public interface Function<T>{
		public T get(int linkId);
		public Function<T> clone(Network network);
	}

	
	/**
	 * A property's function holder (holds a function that can be applied on a certain property) 
	 */
	Function<T> function;
	
	/** Constructor - Creates a {@link LinkPropertyFunction} object by creating a
	 * {@link LinkReadOnlyProperty} object with the given name and network, and a 
	 * default value of null, and then setting the function of the 
	 * {@link LinkPropertyFunction} object to be the given function
	 * 
	 * @param name property name
	 * @param network the network that the link belongs to
	 * @param function the function to be applied to the property
	 */
	public LinkPropertyFunction(String name, Network network, Function<T> function) {
		super(name, network, (T)null);
		this.function = function;
	}

	
	/**
	 * Gets the {@link Function} that is applied to a specific link 
	 * @return T the function applied to the specified link 
	 */
	@Override
	public T get(int linkId) {
		return function.get(linkId);
	}

	
	/**
	 * Makes a copy of the {@link LinkReadOnlyProperty} for use in the given network
	 * @return LinkPropertyFunction the copy of the current {@link LinkReadOnlyProperty}
	 */
	@Override
	public LinkReadOnlyProperty<T> clone(Network network) {
		return new LinkPropertyFunction<T>(name, network, function);
	}
}
