package pack;

import java.io.IOException; //le impor
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;

public class ScreenDisplay {
	
	final int screenWidth = Display.getDesktopDisplayMode().getWidth();
	final int screenHeight = Display.getDesktopDisplayMode().getHeight();
	final int maxAmountGravityObjects = 64;
	
	//Create objects
	VectorSlider vecSlider = new VectorSlider();
	
	// Create vars DON'T YET DEFINE VALUE
	int frameNumber;
	int FPS;
	int sizeSliderVal;
	int sizeSliderMultiplier;
	int slices;
	
	double gravConstant;
	double vecSliderMultiplier;
	
	boolean drawVector;
	
	long time;
	
	GravityObject[] gravityObjectArray = new GravityObject[maxAmountGravityObjects];
	final float[][] colorArray = new float[5][3];
	private int sizeSliderMultipier;
	// [0]=red
	// [1]=green
	// [2]=blue
	// [3]=white
	// [4]=black
	
	ScreenDisplay(){ // CONSTRUCTOR
		initGL(screenWidth, screenHeight);
		load();
		startDisplayLoop();
	}


	private void initGL(int width, int height){
	
		try {
			//DisplayMode mode = new DisplayMode(width,height);
			//DisplayMode mode = Display.getDesktopDisplayMode(); Old code, for not-fullscreen stuff
			Display.setDisplayMode(Display.getDesktopDisplayMode());
			Display.setFullscreen(true);
			Display.create();
			Display.setVSyncEnabled(true);
		} catch (LWJGLException e) {
			System.out.println("Catched an error in intiGL");
			e.printStackTrace();
			System.exit(0);	
		}
 
		// init OpenGL
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, width, 0, height, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
	
	private void load(){
		frameNumber = 61;
		FPS = 0;
		sizeSliderVal = 10;
		sizeSliderMultiplier = 12;
		slices = 100;
		gravConstant = 0.1;
		vecSliderMultiplier = 0.15;
		drawVector = false;
		
		colorArray[0][0] = 1;
		colorArray[0][1] = 0;
		colorArray[0][2] = 0;
		
		colorArray[1][0] = 0.2f;
		colorArray[1][1] = 1;
		colorArray[1][2] = 0.2f;
		
		colorArray[2][0] = 0.3f;
		colorArray[2][1] = 0.3f;
		colorArray[2][2] = 1;
		
		colorArray[3][0] = 1;
		colorArray[3][1] = 1;
		colorArray[3][2] = 1;
		
		colorArray[4][0] = 0;
		colorArray[4][1] = 0;
		colorArray[4][2] = 0;
		
		/*loadTexture("tmpNaam", "TX_test.png");*/
	}

	
	private float[] randomColorArray(){
		float[] array = new float[3];
		
		array[0] = (float) Math.random();
		array[1] = (float) Math.random();
		array[2] = (float) Math.random();
		
		return array;
	}
	
	private void startDisplayLoop(){
		while(!Display.isCloseRequested()){
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			checkInput();
			applyPhysics();
						
			drawStuff();
			
			endThisLoop();
		}
		endProgram();
		Display.destroy();
	}
	
	private void endProgram() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		System.exit(0);
	}


	private void checkInput(){
		if(Mouse.isButtonDown(0)){
			leftHold(Mouse.getX(), Mouse.getY());
		}
		while(Mouse.next()){
			if(Mouse.getEventButtonState() && Mouse.getEventButton() == 0){
				leftClick(Mouse.getX(), Mouse.getY());
			}
			
			if(Mouse.getEventButtonState() && Mouse.getEventButton() == 1){
				rightClick();
			}			
		}
		while(Keyboard.next()){
			if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE){
				System.exit(0);
			}
			
			if(Keyboard.getEventKey() == Keyboard.KEY_SPACE){
				if(drawVector){
					drawVector = false;
				}else{
					drawVector = true;
				}
			}			
		}		
			if(Keyboard.isKeyDown(Keyboard.KEY_UP)){
				if(vecSlider.y < 85){
					vecSlider.y++;
				}
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)){
				if(vecSlider.x > -90){
					vecSlider.x--;
				}
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)){
				if(vecSlider.y > -85){
					vecSlider.y--;
				}
			}
			if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)){
				if(vecSlider.x < 85){
					vecSlider.x++;
				}
			}
	}
	private void leftHold(int x, int y){
		if(y > 5 && y < 55 && x > 10 && x < screenWidth-200){ //Within sizeSlider region
			sizeSliderVal = x/sizeSliderMultiplier;
			if(x > screenWidth - 250){
				sizeSliderVal = (screenWidth-250)/sizeSliderMultiplier;
			} 
		}else if(y < 190 && x > screenWidth-190 && y > 10 && x < screenWidth-10){ //Within vecSlider region
			vecSlider.x = x-(screenWidth-100);
			vecSlider.y = y-100;
		}
	}
	
	private void leftClick(int x, int y) {
		if((y > 70 && x < screenWidth-200) || (y > 210)){
				drawCircle(x, y, sizeSliderVal+4, colorArray[0]);
				createGravityObject(x, y);
		}
	}
	
	private void rightClick(){
		vecSlider.x = 0;
		vecSlider.y = 0;
		
		for(int i = 0; i < gravityObjectArray.length; i++){
			gravityObjectArray[i] = null;
		}
		
	}
	
	private void applyPhysics() {
		GravityObject[] GOA = gravityObjectArray;
		
		for(int i = 0; i < GOA.length; i++){
			int c = 0;
			
			if(GOA[i] != null ){
				
				double[] xVelAr = new double[128];
				double[] yVelAr = new double[128];
				
				for(int j = 0; j < GOA.length; j++){
					if(GOA[j] != null){
						if(GOA[j].size != 0 && GOA[i].size != 0){
							if(i != j){
								double distance = Math.sqrt((Math.pow((GOA[j].xP - GOA[i].xP), 2))+(Math.pow((GOA[j].yP - GOA[i].yP), 2)));
								double gravityForce = gravConstant * (GOA[i].getMass() * GOA[j].getMass()) / Math.pow(distance, 2);
								//System.out.println("Gravitational force between "+i+" and "+ j+" is "+ gravityForce);
								
								double xDistance = GOA[j].xP - GOA[i].xP;
								if(xDistance < 0){
									xDistance = GOA[i].xP - GOA[j].xP;
								}
								double yDistance = GOA[j].yP - GOA[i].yP;
								if(yDistance < 0){
									yDistance = GOA[i].yP - GOA[j].yP;
								}
								//System.out.println("X, Y Distance: "+xDistance+" & "+yDistance);
								double acceleration = gravityForce / GOA[i].getMass();
								//System.out.println("Acceleration of object "+i+" is equal to "+acceleration);
								
								//System.out.println("Object "+i+" moved towards "+j+" with a speed of: "+acceleration);
								xVelAr[c] = (acceleration / (xDistance + yDistance)) * (GOA[j].xP - GOA[i].xP);
								//System.out.println("xVelAr["+j+"] = ("+acceleration+" / ("+xDistance+" + "+yDistance+")) * ("+GOA[j].xP+" - "+GOA[i].xP+") En dat is: "+ xVelAr[j]);
								yVelAr[c] = (acceleration / (xDistance + yDistance)) * (GOA[j].yP - GOA[i].yP);
								
								c++; // C, C# etc...
							}
						}
					}else{
						break;
					}
				}
				
				for(int k = 0; k < xVelAr.length; k++){
					double xTotalVel = 0;
					if(xVelAr[k] != 0){
							xTotalVel+=xVelAr[k];
					}else{
						break;
					}
					GOA[i].xV += xTotalVel;
				}
				
				for(int l = 0; l < xVelAr.length; l++){
					double yTotalVel = 0;
					if(yVelAr[l] != 0){
							yTotalVel+=yVelAr[l];
					}else{
						break;
					}
					GOA[i].yV += yTotalVel;
				}
				
				if(!collision(GOA[i])){
					GOA[i].move();
				}else{
					GOA[i].move();
					//colliding is handled in collision()
				}
				
			}else{
				break;
			}
		}
		
		// Delete out of bounds spheres
		for(int i = 0; i < gravityObjectArray.length; i++){
			if(gravityObjectArray[i] != null){
				if(gravityObjectArray[i].xP > screenWidth || gravityObjectArray[i].xP < 0 || gravityObjectArray[i].yP > screenHeight || gravityObjectArray[i].yP < 0){
					gravityObjectArray[i].remove();
				}
			}
		}
	}

	private boolean collision(GravityObject GO) {
		for(int i = 0; i < gravityObjectArray.length; i++){
			if(gravityObjectArray[i] != null){
				if(GO != gravityObjectArray[i]){
					double centerDistance = Math.sqrt((Math.pow((GO.xP - gravityObjectArray[i].xP), 2))+(Math.pow((GO.yP - gravityObjectArray[i].yP), 2))); // <3 Pytagoras
					if(centerDistance < GO.size + gravityObjectArray[i].size){
							
						if(GO.size > gravityObjectArray[i].size){
							GO.eat(gravityObjectArray[i]);
						}else{
							gravityObjectArray[i].eat(GO);
						}
						if(GO.size < gravityObjectArray[i].size){
							GO.remove();
						}else{
							gravityObjectArray[i].remove();
						}
						return true;
					}
				}
			}else{
				break;
			}
		}
		return false;
	}

	private void drawStuff() {
		drawVector();
		drawGlow();
		drawGhosts();
		drawGravityObject();
		
		/*drawQuad(300, 300, 110, 110, getTexture("tmpNaam")); // Textured stuff
		GL11.glColor3f(1, 1, 1);*/
		SimpleText.drawString("Simulating gravity!" +
				"                   Try the arrow keys!                   Right mouse to clear                   Spacebar for vectors                   FPS: "+FPS+"                   Size: "+(sizeSliderVal+1), 5, 65);
		
		drawSizeSlider();
		drawVecSlider();
	}
	
	private void endThisLoop(){
		//FPS meter
		if (time <= System.currentTimeMillis() - 1000) {			
			Display.setTitle("Simulating gravity @ " + frameNumber + " FPS"); //Normal
			FPS = frameNumber;
			frameNumber = 0;
			time = System.currentTimeMillis();
		} else {
			frameNumber++;
		}
		Display.update();
		Display.sync(60);
	}
	
	private boolean gravityObjectCollision(int x, int y, int size) {
		for(int i = 0; i < gravityObjectArray.length; i++){
			if(gravityObjectArray[i] != null){
				
				double centerDistance = Math.sqrt((Math.pow((x - gravityObjectArray[i].xP), 2))+(Math.pow((y - gravityObjectArray[i].yP), 2))); // <3 Pytagoras
				
				if(centerDistance <= size + gravityObjectArray[i].size){
					return true;
				}
			}else{
				break;
			}
		}
		
		return false;
	}
	
	private void drawSizeSlider() {
		drawQuad(0, 0, 60, screenWidth-200, colorArray[3]);
		drawQuad(5, 5, 50, screenWidth-210, colorArray[0]);
		drawCircle((sizeSliderVal*sizeSliderMultiplier) + 23, 30, 25, colorArray[2]);
	}
	
	private void createGravityObject(int x, int y){
		int size = sizeSliderVal+3;
		float[] color = randomColorArray();
			
		if(!gravityObjectCollision(x, y, size)){
			for(int i = 0; i < gravityObjectArray.length - 1; i++){
				if(gravityObjectArray[i] == null || gravityObjectArray[i].size == 0){
					System.out.println("Created a gravityObject NR: "+ i + " Coords: ("+ x + ", "+ y+") size: "+size);
					gravityObjectArray[i] = new GravityObject(x, y, size, color);
					gravityObjectArray[i].xV = vecSlider.x*vecSliderMultiplier;
					gravityObjectArray[i].yV = vecSlider.y*vecSliderMultiplier;
					gravityObjectArray[i].force = vecSlider.getDistance();
					break;
				}
			}
		}
	}
	
	private void drawVecSlider(){
		drawQuad(screenWidth - 205, 0, 200, 205, colorArray[3]);
		drawQuad(screenWidth - 200, 5, 190, 195, colorArray[0]);
		
		drawQuad(screenWidth-101, 70, 60, 2, colorArray[4]);
		drawQuad(screenWidth-130, 99, 2, 60, colorArray[4]);
		
		GL11.glTranslatef(screenWidth-100, 100, 0);
		
		drawCircle(vecSlider.x, vecSlider.y, 11, colorArray[2]);
				
		GL11.glLoadIdentity();
		
		//drawCircle((sizeSliderVal*7) + 23, 30, 25, colorArray[2]);
	}

	private void drawGravityObject() {
		for(int i = 0; i < gravityObjectArray.length; i++){
			if(gravityObjectArray[i] != null){
				drawCircle(gravityObjectArray[i].xP, gravityObjectArray[i].yP, gravityObjectArray[i].size, gravityObjectArray[i].color);
			}else{
				break;
			}
		}
	}
	
	private void drawGhosts(){
		for(int i = 0; i < gravityObjectArray.length; i++){
			if(gravityObjectArray[i] != null){
				for(int j = 0; j < 30; j++){
					
					drawCircle(gravityObjectArray[i].xGhosts[j], gravityObjectArray[i].yGhosts[j], gravityObjectArray[i].size, gravityObjectArray[i].color, (float) (0.7/(j + 0.1)));
					//System.out.println("Draw a ghost: ("+gravityObjectArray[i].ghostsX[j]+", "+gravityObjectArray[i].ghostsY[j]+")");
				}
				
			}else{
				break;
			}
		}
	}
	
	private void drawVector(){
		if(drawVector){
			for(int i = 0; i < gravityObjectArray.length; i++){
				if(gravityObjectArray[i] != null){
					GravityObject[] GOA = gravityObjectArray;
					GravityObject GO = gravityObjectArray[i];
				
					for(int j = 0; j < GOA.length; j++){
						if(GOA[j] != null && GOA[j].xP > 0 && GOA[j].yP > 0 && GOA[i].xP > 0 && GOA[i].yP > 0){
							double distance = Math.sqrt((Math.pow((GOA[j].xP - GOA[i].xP), 2))+(Math.pow((GOA[j].yP - GOA[i].yP), 2)));
							double gravityForce = gravConstant * (GOA[i].getMass() * GOA[j].getMass()) / Math.pow(distance, 2);
						
							float[] gColAr = new float[3];
							gColAr[0] = (float) (gravityForce*1.3);
							gColAr[1] = (float) (gravityForce*1);
							gColAr[2] = (float) (gravityForce*1);					
							drawLine((int)(GOA[i].xP), (int)(GOA[i].yP), (int)(GOA[j].xP), (int)(GOA[j].yP), gColAr);
						}
					}
					float[] sColAr = new float[3];
					sColAr[0] = (float) (0.2);
					sColAr[1] = (float) (0.2);
					sColAr[2] = (float) (0);
					drawLine( ((int) GO.xP), ((int) GO.yP), ((int) (GO.xP + GO.xV*(25))), ((int) (GO.yP + GO.yV*(25))), sColAr);
				}
			}
		}
	}
	
	private void drawGlow(){
		for(int i = 0; i < gravityObjectArray.length; i++){
			if(gravityObjectArray[i] != null){
				
				float[] tmpColor = gravityObjectArray[i].color;
				drawCircle(gravityObjectArray[i].xP, gravityObjectArray[i].yP, gravityObjectArray[i].size+1, tmpColor, 0.5f);
				drawCircle(gravityObjectArray[i].xP, gravityObjectArray[i].yP, gravityObjectArray[i].size+2, tmpColor, 0.4f);
				drawCircle(gravityObjectArray[i].xP, gravityObjectArray[i].yP, gravityObjectArray[i].size+3, tmpColor, 0.3f);
				drawCircle(gravityObjectArray[i].xP, gravityObjectArray[i].yP, gravityObjectArray[i].size+5, tmpColor, 0.2f);
				drawCircle(gravityObjectArray[i].xP, gravityObjectArray[i].yP, gravityObjectArray[i].size+7, tmpColor, 0.1f);
			}else{
				break;
			}
		}
	}
	
	private void drawLine(int X1, int Y1, int X2, int Y2, float color[]){
		GL11.glColor3f(color[0], color[1], color[2]);
		
		GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex2f(X1, Y1);
			GL11.glVertex2f(X2, Y2);
		GL11.glEnd();
	}
	
	private void drawQuad(int x, int y, int h, int w, float color[]){
		GL11.glColor3f(color[0], color[1], color[2]);

		// draw quad
		GL11.glBegin(GL11.GL_QUADS);
		    GL11.glVertex2f(x, y);
		    GL11.glVertex2f(x+w,y);
		    GL11.glVertex2f(x+w,y+h);
		    GL11.glVertex2f(x,y+h);
		GL11.glEnd();
	}
	
	private void drawQuad(int x, int y, int h, int w, float color[], float alpha){
		GL11.glColor4f(color[0], color[1], color[2], alpha);

		// draw quad
		GL11.glBegin(GL11.GL_QUADS);
		    GL11.glVertex2f(x, y);
		    GL11.glVertex2f(x+w,y);
		    GL11.glVertex2f(x+w,y+h);
		    GL11.glVertex2f(x,y+h);
		GL11.glEnd();
	}
	
	private void drawCircle(double x, double y, int radius, float color[]){
		GL11.glColor3f(color[0], color[1], color[2]);
		
		float incr = (float) (2 * Math.PI / slices);
		/*xCoord = xCoord + radius;
		yCoord = yCoord + radius;*/
		
    GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        for(int i = 0; i < slices; i++)
        {
              float angle = incr * i;

              float Xc = (float) (x +  Math.cos(angle) * radius);
              float Yc = (float) (y +  Math.sin(angle) * radius);

              GL11.glVertex2f(Xc, Yc);
        }
     GL11.glEnd();	
	}


	private void drawCircle(double x, double y, int radius, float color[], float alpha){		
		GL11.glColor4f(color[0], color[1], color[2], alpha);
	
		float incr = (float) (2 * Math.PI / slices);
		/*xCoord = xCoord + radius;
		yCoord = yCoord + radius;*/
	
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		for(int i = 0; i < slices; i++){
			float angle = incr * i;

			float Xc = (float) (x +  Math.cos(angle) * radius);
			float Yc = (float) (y +  Math.sin(angle) * radius);

          GL11.glVertex2f(Xc, Yc);
		}
		GL11.glEnd();	
	}
}







