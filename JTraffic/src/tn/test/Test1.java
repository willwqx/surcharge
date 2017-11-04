package tn.test;

public class Test1 {
	public static void main(String args[]){
		
	//	groupSum(0, {2, 4, 8}, 10);
	//	int num[] = {2,4,8};
	//System.out.println(groupSum(0, [2, 4, 8 ], 10));
	//groupSum(0, [2, 4, 8 ], 10);
		
	/*	int num[] = new int[3];
		
		num[1] = 10;
		num[2] = 20; 
		
		System.out.println(num[1]); */
		
		/*	
		int nums[] = {5,6,3,3};
		int sum = 0;
		for (int x: nums){
			sum += x;
	//	System.out.println("Cumulate Sum:" + sum);
		}
		System.out.println("Total Sum:" + sum);
		*/
		
		int x = sumtwo(22215,12132);
		System.out.println(x);
		
		say("David");
		
		System.out.println("square value: " + square(15));
		
		Test2 run1 = new Test2();
		run1.print();
		
		Test2 run2 = new Test2();
		run2.setvalue(1); 
		System.out.println(run2.getvalue());

		//public void setHour(int h){
	//		hour = ((h>=0 && h<24) ? h:0);
	//	}
		
		//run2.dopublic();
		// run2.setDopublic(11);
		//System.out.println(run2.getClass());
		
		
	}	
	// this method return two int number sum.
	static int sumtwo(int a, int b) {
		return a + b;
	}
	
	// this method will say hello to any name is passed when called
	private static void say(String name) {
		System.out.println("Hello " + name + "!");
	}
	
	// this method will make the selected numbers' sum equal to target 
	//public boolean groupSum(int start, int[] nums, int target) {
    static boolean groupSum(int start, int[] nums, int target) {
			  if (start >= nums.length) {
			    return target == 0;
			  }
			  else if (groupSum(start+1, nums, target - nums[start])){
			    return true;
			  }
			  else if (groupSum(start+1, nums, target)){
			    return true;
			  } return target == 0;
	}
    
    //this method return the square value of input number
    static int square(int a) {
    	return a*a;
    }
	 
}

