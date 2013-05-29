package pack;

public class VectorSlider {
		
	double x;
	double y;
	
	public VectorSlider(){
		this.x = 0;
		this.y = 0;
	}
	
	public double getDistance(){
		double distance = Math.sqrt((Math.pow(x, 2))+(Math.pow(y, 2)));
		return distance;
	}
}