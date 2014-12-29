package a.multitouch;

import java.util.Random;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.mt4j.AbstractMTApplication;
import org.mt4j.MTApplication;
import org.mt4j.components.MTComponent;
import org.mt4j.components.visibleComponents.shapes.MTRectangle.PositionAnchor;
import org.mt4j.components.visibleComponents.widgets.MTBackgroundImage;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.input.gestureAction.DefaultScaleAction;
import org.mt4j.input.inputProcessors.*;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragEvent;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.flickProcessor.*;
import org.mt4j.input.inputProcessors.componentProcessors.flickProcessor.FlickEvent.FlickDirection;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleEvent;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.*;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MTColor;
import org.mt4j.util.font.FontManager;
import org.mt4j.util.font.IFont;
import org.mt4j.util.math.ToolsMath;
import org.mt4j.util.math.Vector3D;
import org.mt4j.util.math.Vertex;

import com.sun.prism.impl.Disposer.Target;

import processing.core.PImage;
import sun.management.counter.Variability;
import advanced.physics.physicsShapes.*;
import advanced.physics.util.PhysicsHelper;
import advanced.physics.util.UpdatePhysicsAction;

public class BubbleGameScene extends AbstractScene {
	private float timeStep = 1.0f / 60.0f;
	private int constraintIterations = 10;
	
	/** THE CANVAS SCALE **/
	private float scale = 20;
	private AbstractMTApplication app;
	private World world;
	
	private MTComponent physicsContainer;
	
	/** GAME STATE INFO **/
	private final int BUBBLE_COUNT = 35;
	private final int POP_POINTS = 9999;
	private final int BIGPOP_POINTS = 99999;
	private int bubbleAmount = 0;
	private int gameScore = 0;

	private enum GameAction { POP, FLICK };
	
	/** File paths **/
	private String imagesPath = "a" + AbstractMTApplication.separator + "multitouch" + AbstractMTApplication.separator + "data" + AbstractMTApplication.separator + "images" + AbstractMTApplication.separator;
	private String soundsPath = "a" + AbstractMTApplication.separator + "multitouch" + AbstractMTApplication.separator + "data" + AbstractMTApplication.separator + "sounds" + AbstractMTApplication.separator;
	
	/** MINIM AUDIO PLAYER **/
	private AudioTrack sndBG;
	private AudioTrack sndPop1;
	private AudioTrack sndPop2;
	private AudioTrack sndPop3;
	private AudioTrack sndPop4;
	
	public BubbleGameScene(AbstractMTApplication mtApplication, String name) {
		super(mtApplication, name);
		this.app = mtApplication;
		//this.setClear(true);

		float worldOffset = 10; //Make Physics world slightly bigger than screen borders
		
		//Physics world dimensions
		AABB worldAABB = new AABB(new Vec2(-worldOffset, -worldOffset), new Vec2((app.width)/scale + worldOffset, (app.height)/scale + worldOffset));
		
		Vec2 gravity = new Vec2(0, -2.0f);
		boolean sleep = true;
		
		//Create the physics world
		this.world = new World(worldAABB, gravity, sleep);
		
		// Show touch points
		this.registerGlobalInputProcessor(new CursorTracer(app, this));
		
		//Update the positions of the components according the the physics simulation each frame
		this.registerPreDrawAction(new UpdatePhysicsAction(world, timeStep, constraintIterations, scale));


		// Load/play sound(s)
		sndBG = new AudioTrack(soundsPath + "pbround.mp3", (MTApplication) app);
		sndBG.setLoopCount(999);
		sndBG.setVolume(-5f);
		sndBG.play();
		sndPop1 = new AudioTrack(soundsPath + "pop1.wav", (MTApplication) app);
		sndPop1.setVolume(-10f);
		sndPop2 = new AudioTrack(soundsPath + "pop2.wav", (MTApplication) app);
		sndPop2.setVolume(-4f);
		sndPop3 = new AudioTrack(soundsPath + "pop3.wav", (MTApplication) app);
		sndPop3.setVolume(-5f);
		sndPop4 = new AudioTrack(soundsPath + "alaskarobotics.wav", (MTApplication) app);
		
		// Set the background Image
		PImage backgroundImage = app.loadImage(imagesPath + "puzzlebg.jpg");
		MTBackgroundImage mtBackground = new MTBackgroundImage(app, backgroundImage, false);
		this.getCanvas().addChild(mtBackground);
		
		physicsContainer = new MTComponent(app);
		//Scale the physics container. Physics calculations work best when the dimensions are small (about 0.1 - 10 units)
		//So we make the display of the container bigger and add in turn make our physics object smaller
		physicsContainer.scale(scale, scale, 1, Vector3D.ZERO_VECTOR);
		this.getCanvas().addChild(physicsContainer);
		
		//Create borders around the screen
		this.createScreenBorders(physicsContainer);

		// Show the score
		initScore();
		
		//Create bubbles
		for (int i = 0; i < 1; i++) {
			addBigBubble();
			//addBubble();
		}
		
	}
	
	private void addBigBubble() { 
		Vector3D center = new Vector3D(ToolsMath.getRandom(60, app.width-60), ToolsMath.getRandom(60, app.height-60));
		final PhysicsCircle c = new PhysicsCircle(app, center, ToolsMath.getRandom(190, 220), world, 1.0f, 0.3f, 0.4f, scale);
		
		MTColor fillColor = new MTColor(ToolsMath.getRandom(60, 255),ToolsMath.getRandom(60, 255),ToolsMath.getRandom(60, 255), 150);
		c.setFillColor(fillColor);
		c.setStrokeColor(new MTColor(0, 0, 0, 150));
		c.setStrokeWeight(6);
		c.unregisterAllInputProcessors();
		c.registerInputProcessor(new ScaleProcessor(app, true));
		c.addGestureListener(ScaleProcessor.class, new IGestureEventListener() {
			public boolean processGestureEvent(MTGestureEvent ge) {
				ScaleEvent te = (ScaleEvent) ge;

				switch (te.getId()) {
				case MTGestureEvent.GESTURE_UPDATED:
					break;
				case MTGestureEvent.GESTURE_ENDED:
					double area = c.get2DPolygonArea();
					System.out.println(area);
					if (area < 50000 || area > 250000) {
						playPop(sndPop4);
						bubbleAmount--;
						updateScore(GameAction.POP);
						for (int i = 0; i < 10; i++) {
							addBubble();
						}
						//checkBubbleAmount();
						c.destroy();
					}
					break;
				}
				return false;
			}
		});
		//c.setAnchor(PositionAnchor.CENTER);
		physicsContainer.addChild(c);
		bubbleAmount++; 	// Update amount, used for "refilling" bubbles when low.
		/*
		final PhysicsRectangle physRect = new PhysicsRectangle(new Vector3D(300,300), 400, 400, app, world, 1f, 0.4f, 0.4f, scale);
		MTColor col = new MTColor(ToolsMath.getRandom(60, 255),ToolsMath.getRandom(60, 255),ToolsMath.getRandom(60, 255), 150);
		physRect.setFillColor(col);
		physRect.setStrokeColor(new MTColor(255, 255, 255, 150));
		physRect.setStrokeWeight(3);
		physRect.unregisterAllInputProcessors();
		physRect.registerInputProcessor(new ScaleProcessor(app, true));
		physRect.addGestureListener(ScaleProcessor.class, new IGestureEventListener() {
			public boolean processGestureEvent(MTGestureEvent ge) {
				ScaleEvent te = (ScaleEvent) ge;

				switch (te.getId()) {
				case MTGestureEvent.GESTURE_UPDATED:
					break;
				case MTGestureEvent.GESTURE_ENDED:
					double area = physRect.get2DPolygonArea();
					
					if (area < 200 || area > 2000) {
						playPop(sndPop4);
						bubbleAmount--;
						updateScore(GameAction.POP);
						checkBubbleAmount();
						physRect.destroy();
					}
					break;
				}
				return false;
			}
		});
		physRect.setAnchor(PositionAnchor.CENTER);
		physRect.setPositionGlobal(new Vector3D(0, app.getHeight()/ 2,0));
		physicsContainer.addChild(physRect);*/
		
	}
	
	private void addBubble() {
		Vector3D center = new Vector3D(ToolsMath.getRandom(60, app.width-60), ToolsMath.getRandom(60, app.height-60));
		final PhysicsCircle c = new PhysicsCircle(app, center, ToolsMath.getRandom(70, 90), world, 1.0f, 0.3f, 0.4f, scale);
		
		MTColor fillColor = new MTColor(ToolsMath.getRandom(60, 255),ToolsMath.getRandom(60, 255),ToolsMath.getRandom(60, 255), 150);
		c.setFillColor(fillColor);
		c.setStrokeColor(new MTColor(255, 255, 255, 150));
		c.setStrokeWeight(3);
		c.unregisterAllInputProcessors();
		c.registerInputProcessor(new FlickProcessor(300, 5));
		c.addGestureListener(FlickProcessor.class, new IGestureEventListener() {
		public boolean processGestureEvent(MTGestureEvent ge) {
			FlickEvent e = (FlickEvent)ge;
			if (e.getId() == MTGestureEvent.GESTURE_ENDED)
			{
				FlickDirection dir = e.getDirection();
				if (! dir.equals(FlickDirection.UNDETERMINED)) { // Valid Flick
					System.out.println("FLICK: " + e.getDirection());
					playPop(sndPop4);
					bubbleAmount--;
					updateScore(GameAction.POP);
					checkBubbleAmount();
					c.destroy();
				}
				else {
					System.out.println("TAP (FLICK = UNDETERMINED)");
					playPop(sndPop4);
					bubbleAmount--;
					updateScore(GameAction.POP);
					checkBubbleAmount();
					c.destroy();
				}
			}
			return false;
		}
		});
		physicsContainer.addChild(c);
		bubbleAmount++; 	// Update amount, used for "refilling" bubbles when low.
	}
	
	private void createScreenBorders(MTComponent parent){
		//Left border 
		float borderWidth = 50f;
		float borderHeight = app.height;
		Vector3D pos = new Vector3D(-(borderWidth/2f) , app.height/2f);
		PhysicsRectangle borderLeft = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderLeft.setName("borderLeft");
		parent.addChild(borderLeft);
		
		//Right border
		pos = new Vector3D(app.width + (borderWidth/2), app.height/2);
		PhysicsRectangle borderRight = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderRight.setName("borderRight");
		parent.addChild(borderRight);
		
		//Top border
		borderWidth = app.width;
		borderHeight = 50f;
		pos = new Vector3D(app.width/2, -100);
		PhysicsRectangle borderTop = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderTop.setName("borderTop");
		parent.addChild(borderTop);
		
		//Bottom border
		pos = new Vector3D(app.width/2 , app.height + (borderHeight/2));
		PhysicsRectangle borderBottom = new PhysicsRectangle(pos, borderWidth, borderHeight, app, world, 0,0,0, scale);
		borderBottom.setName("borderBottom");
		parent.addChild(borderBottom);
	}
	
	
	private void playRandomPop() {
		Random r = new Random();
		int n = 1 + r.nextInt(3);
		if (n == 1) playPop(sndPop1);
		if (n == 2) playPop(sndPop2);
		if (n == 3) playPop(sndPop3);
	}
	
	private void playPop(AudioTrack t) {
		t.rewind();
		t.play();
	}
	
	private void checkBubbleAmount() {
		if (bubbleAmount < BUBBLE_COUNT / 2) {
			for (int i = 0; i < BUBBLE_COUNT / 2; i++) {
				addBubble();
			}
			double r = ToolsMath.getRandom(0,  2);
			if (r <= 1) addBigBubble();
			System.out.println(r);
		}
	}
	
	/** Score Stuff **/

	private MTTextArea scoreField;
	
	private void initScore() {
		IFont fontArial = FontManager.getInstance().createFont(app, "arial.ttf", 50, MTColor.WHITE);
		scoreField = new MTTextArea(app, fontArial);
		scoreField.setNoStroke(true);
		scoreField.setFillColor(new MTColor(0, 0, 0, 100));
		scoreField.setPositionGlobal(new Vector3D(0, app.height - 30));
		drawScore();
		scoreField.removeAllGestureEventListeners();
		this.getCanvas().addChild(scoreField);
	}

	private void drawScore() {
		scoreField.setText("SCORE: " + String.format("%05d", gameScore));
	}
	
	private void updateScore(GameAction ga) {
		if (ga.equals(GameAction.POP))
			gameScore += POP_POINTS;
		else if (ga.equals(GameAction.FLICK))
			gameScore += BIGPOP_POINTS;
		
		drawScore();
	}

}
