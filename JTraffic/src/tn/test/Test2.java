package tn.test;

public class Test2 {
	
	void print(){
	System.out.println("this is another class");
	}
	
	
	//private is the most restrictive
	//default
	//protected
	//public
	int value;
	public void setvalue (int x) {
		if (x > 0) {
		this.value = x;
		}
		else System.out.println("Need non-negative number. ");
	}
	
	public int getvalue() {
		return value;
	}
	
	
	// public visibility means that this method can be accessed anywhere in the program so long as you have instance of this class to call it from
	public void dopublic(int x) {
		 x = 10;
		System.out.println("x=" + x);
	}
	
	// private visibility means that this method can't be accessed anywhere other than inside this class
	private void doprivate() {
		
	}
	
	// protected visibility means this method can only be accessed inside of this package, and from subclasses of this class
	protected void doprotected() {
		
	}
	
	// default visibility means that this method can only be accessed inside of this package
	void dosomthing() {
		
	}
	
	
	
	
	
	
	
	
}
