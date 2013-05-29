package pack;

public class GravityObject {
		
	double xP;
	double yP;
	int size;
	float color[];
	double force;
	double xV;
	double yV;
	final double[] xGhosts = new double[30];
	final double[] yGhosts = new double[30];
	
	long lastGhostTime = System.currentTimeMillis();
	
	public GravityObject(double x, double y, int size, float color[]){
		this.xP = x;
		this.yP = y;
		this.size = size;
		this.color = color;
		/*this.force = 0;
		this.direction = 0;*/
	}
	
	public double getAcceleration(){
		double acceleration = force/getMass();
		return acceleration;
	}
	
	public double getMass(){
		double mass = Math.PI * Math.pow(size, 2.5); // For realism: (size, 2)
		//double mass = Math.pow(size, 1.6);
		return mass;
	}
		
	public void move(){
			xP = xP + xV;
			yP = yP + yV;
			
			if(lastGhostTime+20 < System.currentTimeMillis()){
				for(int i = 29; i > 0; i--){
					
					xGhosts[i] = xGhosts[i-1];
					yGhosts[i] = yGhosts[i-1];
					
				}
				lastGhostTime = System.currentTimeMillis();
				
				xGhosts[0] = xP;
				yGhosts[0] = yP;
				
				//System.out.println("("+xP+", "+yP+")");
			}
	}

	public void remove() {
		xV = 0;
		yV = 0;
		xP = -1200;
		yP = -1200;
		size = 0;
		force = 0;
	}

	public void eat(GravityObject GO) {
		size += GO.size;
		
		
			color[0] =  (float) ((color[0]*1.5+GO.color[0]*0.5) / 2);
		
			color[1] =  (float) ((color[1]*1.5+GO.color[1]*0.5) / 2);
		
			color[2] =  (float) ((color[2]*1.5+GO.color[2]*0.5) / 2);
		
		double XVTmp = GO.getXForce() / getMass();
		double YVTmp = GO.getYForce() / getMass();
			
		xV += XVTmp;
		yV += YVTmp;

	}
	
	public double getXForce(){
		double GOxForce = this.xV * this.getMass();
		return GOxForce;
	}
	
	public double getYForce(){
		double GOyForce = this.yV * this.getMass();
		return GOyForce;
	}
}