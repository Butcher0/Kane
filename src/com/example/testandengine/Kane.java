package com.example.testandengine;

import java.io.IOException;
import java.util.Vector;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.Engine.EngineLock;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;
import org.andengine.util.debug.Debug.DebugLevel;

import android.hardware.SensorManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;


public class Kane extends SimpleBaseGameActivity implements
IOnSceneTouchListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mPlayerTextureRegion;
	private TiledTextureRegion mFireButtonTexureRegion;

	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;

	private ITextureRegion mParallaxLayerBack;
	private ITextureRegion mParallaxLayerMid;
	private ITextureRegion mParallaxLayerFront;
	
	private Sound arrowShootSnd;
	private Sound duckFlyingSnd;
	private PhysicsWorld mPhysicsWorld;
	private Vector<Arrow> arrowList = new Vector<Arrow>();
	private Vector<Duck> duckList = new Vector<Duck>();
	static EngineLock engineLock;
	private Scene scene;
	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	
	// Arrow med textures
	private Arrow a;
	private TiledTextureRegion arrowTexture;
	
	// Duck med textures
	private Duck duck;
	private TiledTextureRegion duckTexture;
	
	// Crosshair med texture
	private Xair xAir;
	private TiledTextureRegion xAirTexture;
	private int xAirdegree = 45;
	
	
	// Controls for xAir
	private TiledTextureRegion UpBtnTexture;
	private TiledTextureRegion DwnBtnTexture;
	
	// digital controls
	private BitmapTextureAtlas mOnScreenControlTexture;
	private ITextureRegion mOnScreenControlBaseTextureRegion;
	private ITextureRegion mOnScreenControlKnobTextureRegion;

	private DigitalOnScreenControl mDigitalOnScreenControl;
	
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		engineOptions.getAudioOptions().setNeedsSound(true);
		return engineOptions;
		
	}
	

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 512, TextureOptions.DEFAULT);
		this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "minplayer.png", 0, 0, 3, 2);
		this.mFireButtonTexureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "firebutton.png", 73, 0, 1, 1);
		this.arrowTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "arrow.png", 0, 72, 1, 1);
		this.duckTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "duck.png", 0, 144, 2, 2);
		this.xAirTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "xair.png", 146, 0, 1, 1);
		// 2 textures for controls av xAir
		this.UpBtnTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "xairUp.png", 146, 229, 1, 1);
		this.DwnBtnTexture = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "xairDown.png", 146, 266, 1, 1);
		this.mBitmapTextureAtlas.load();

		this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024);
		this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_front.png", 0, 0);
		this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_back_2.png", 0, 188);
		this.mParallaxLayerMid = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_mid.png", 0, 669);
		
		this.mAutoParallaxBackgroundTexture.load();
		engineLock = this.mEngine.getEngineLock();
		
		// Soundeffects
		SoundFactory.setAssetBasePath("mfx/");
		try {
			this.arrowShootSnd = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "fire.ogg");
			this.duckFlyingSnd = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "duckfly.ogg");
		} catch (final IOException e) {
			Debug.e(e);
		}
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

	    scene = new Scene();
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(this.mParallaxLayerBack.getWidth() - CAMERA_WIDTH, CAMERA_HEIGHT - this.mParallaxLayerBack.getHeight(), this.mParallaxLayerBack, vertexBufferObjectManager)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 80, this.mParallaxLayerMid, vertexBufferObjectManager)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, vertexBufferObjectManager)));
		scene.setBackground(autoParallaxBackground);

		/* Calculate the coordinates for the face, so its centered on the camera. */
		final float playerX = 25;
		final float playerY = CAMERA_HEIGHT - this.mPlayerTextureRegion.getHeight() - 3;
		
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		scene.attachChild(ground);
		scene.attachChild(roof);
		scene.attachChild(left);
		scene.attachChild(right);

		scene.registerUpdateHandler(this.mPhysicsWorld);

		/* Create two sprits and add it to the scene. */
		final AnimatedSprite player = new AnimatedSprite(playerX, playerY, this.mPlayerTextureRegion, vertexBufferObjectManager);
		final AnimatedSprite fireButton = new AnimatedSprite(550, 420, this.mFireButtonTexureRegion, vertexBufferObjectManager);
		final AnimatedSprite upButton = new AnimatedSprite(680, 350, this.UpBtnTexture, vertexBufferObjectManager);
		final AnimatedSprite dwnButton = new AnimatedSprite(680, 400, this.DwnBtnTexture, vertexBufferObjectManager);
		
	
		player.setScaleCenterY(this.mPlayerTextureRegion.getHeight());
		player.setScale(2);
		player.animate(new long[]{200, 200, 200}, 0, 2, true);
		createXair();

		scene.attachChild(player);
		scene.attachChild(fireButton);
		scene.attachChild(upButton);
		scene.attachChild(dwnButton);
		
		// 

		
		
	//	new Arrow(100, 200, 0, scene, mPhysicsWorld, mBitmapTextureAtlas, this, vertexBufferObjectManager);
	

		
		scene.registerTouchArea(fireButton);
		
		scene.registerTouchArea(upButton);
		scene.registerTouchArea(dwnButton);
		scene.setOnAreaTouchListener(new IOnAreaTouchListener() {
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if(pSceneTouchEvent.isActionDown() && pTouchArea.equals(fireButton)) {
					Kane.this.arrowShootSnd.play();
						
//					Arrow arrow = new Arrow(25, 425, 0, scene, mPhysicsWorld, mBitmapTextureAtlas, Kane.this, vertexBufferObjectManager);
//					Arrow2 arrow = new Arrow2(pTouchAreaLocalY, pTouchAreaLocalY, mFireButtonTexureRegion, vertexBufferObjectManager, mFireButtonTexureRegion, mPhysicsWorld, scene, null);
//					arrowList.add(arrow);
					createArrow();
					createDuck();
	//				Duck duck = new Duck(CAMERA_WIDTH - 50, 250, 0, scene, mPhysicsWorld, mBitmapTextureAtlas, Kane.this, vertexBufferObjectManager);
	//				duckList.add(duck);
//					Debug.log(DebugLevel.INFO, "X er no " + arrow.getxVal());
//					Debug.log(DebugLevel.INFO, "Y er no " + arrow.getyVal());
//					Debug.log(DebugLevel.INFO, "Antall piler skutt: " + arrowList.size());
 
				}
				if(pSceneTouchEvent.isActionDown() && pTouchArea.equals(upButton)){

					xAirdegree += 1;
					xAir.setY((float) (Math.cos(Math.toRadians(xAirdegree)) * 450));
					xAir.setX((float) (Math.cos(Math.toRadians(xAirdegree)) * 150));
					Debug.log(DebugLevel.INFO, "Vinkel i grader er: " + xAirdegree);
					Debug.log(DebugLevel.INFO, "X - verdi - Cosinus er " + (float) (Math.cos(Math.toRadians(xAirdegree)) * 150));
					Debug.log(DebugLevel.INFO, "Y - verdi - Sinus er " + (float) (Math.cos(Math.toRadians(xAirdegree)) * 450));
					Debug.log(DebugLevel.INFO, "min X - verdi - Cosinus er " + Math.cos(Math.toRadians(0)));
					Debug.log(DebugLevel.INFO, "min Y - verdi - Sinus er " + Math.sin(Math.toRadians(0)));
					
				//	xAir.setX(pX)
					// Flytt sikte i bue oppover & oppdater x+y
				}
				if(pSceneTouchEvent.isActionDown() && pTouchArea.equals(dwnButton)){
					xAirdegree -= 1;
					xAir.setY((float) (Math.cos(Math.toRadians(xAirdegree)) * 450));
					xAir.setX((float) (Math.cos(Math.toRadians(xAirdegree)) * 150));
					Debug.log(DebugLevel.INFO, "Vinkel i grader er: " + xAirdegree);
					Debug.log(DebugLevel.INFO, "X - verdi - Cosinus er " + (float) (Math.cos(Math.toRadians(xAirdegree)) * 150));
					Debug.log(DebugLevel.INFO, "Y - verdi - Sinus er " + (float) (Math.cos(Math.toRadians(xAirdegree)) * 450));
					// Flytt sikte i bue nedover & oppdater x+y
				}
				return true;
				
			}
		});
		
		scene.registerUpdateHandler(new IUpdateHandler(){
			public void reset(){
				
			}
			public void onUpdate(float pSecondsElapsed) {

				for(int i = 0; i < arrowList.size(); i++){
					for(int j=0; j< duckList.size(); j++){
						if(arrowList.get(i).collidesWith(duckList.get(j))){
							Debug.log(DebugLevel.INFO, "Size arrowList: " + arrowList.size());
							Debug.log(DebugLevel.INFO, "Size duckList: " + duckList.size());
							Debug.log(DebugLevel.INFO, "Collision between arrow: " + i + " and duck: " + j);
							Debug.log(DebugLevel.INFO, "Trying to remove arrow: " + i + "at " + arrowList.get(i).getY());
						
							removeArrow(arrowList.get(i));

							Debug.log(DebugLevel.INFO, "Trying to remove duck: " + j);
							removeDuck(duckList.get(j));
						}
					}
				}
				for(int i = 0; i < arrowList.size(); i++){
					if(arrowList.get(i).collidesWith(ground)){
						Debug.log(DebugLevel.INFO, "Trying to remove arrow: " + i);
						engineLock.lock();
						removeArrow(arrowList.get(i));
						engineLock.unlock();
					}
				}
				for(int i = 0; i < duckList.size(); i++){
					if(duckList.get(i).collidesWith(ground) || duckList.get(i).collidesWith(left)){
						Debug.log(DebugLevel.INFO, "Trying to remove duck: " + i);
						engineLock.lock();
						removeDuck(duckList.get(i));
						engineLock.unlock();
					}
				}
				
			}
		});

		return scene;
	}
	
	
	private void removeArrow(final AnimatedSprite arrow) {

		this.runOnUpdateThread(new Runnable() {
		//	this.mFaceToRemove = new Sprite(centerX, centerY, this.mFaceTextureRegion, this.getVertexBufferObjectManager());
			public void run() {
				final PhysicsConnector arrowPhysicsConnector = mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(arrow);
				engineLock.lock();
				Debug.log(DebugLevel.INFO, "KJØRER REMOVEARROW: ");
				mPhysicsWorld.unregisterPhysicsConnector(arrowPhysicsConnector);
				
				scene.unregisterTouchArea(arrow);
				scene.detachChild(arrow);
				arrowList.remove(arrow);
				
				engineLock.unlock();
			}
		});

	}
	
	private void removeDuck(final AnimatedSprite duck) {

		this.runOnUpdateThread(new Runnable() {

			public void run() {
				final PhysicsConnector duckPhysicsConnector = mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(duck);
				engineLock.lock();
				mPhysicsWorld.unregisterPhysicsConnector(duckPhysicsConnector);
				
				scene.unregisterTouchArea(duck);
				scene.detachChild(duck);
				duckList.remove(duck);
				
				engineLock.unlock();
			}
		});

	}
	
	private void createXair(){
		
		xAir = new Xair(Math.cos(Math.toRadians(xAirdegree)) * 143, Math.sin(Math.toRadians(xAirdegree)) * 500, xAirTexture,getVertexBufferObjectManager(), arrowTexture,mPhysicsWorld, scene, engineLock);
		scene.attachChild(xAir);
		scene.registerUpdateHandler(xAir);
		
	}


	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private Arrow createArrow() {
		a = new Arrow(25, 425, arrowTexture,getVertexBufferObjectManager(), arrowTexture,mPhysicsWorld, scene, engineLock);
		final Rectangle arrowShape = new Rectangle(25, 425, 50, 2, getVertexBufferObjectManager());
		
		final Body body;
		body = PhysicsFactory.createBoxBody(mPhysicsWorld, arrowShape, BodyType.DynamicBody, FIXTURE_DEF);

		a.setbody(body);
		body.setLinearVelocity(100f, -50f);
		body.setTransform(body.getWorldCenter(), 100);
		
		scene.attachChild(a);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(a, body,true, true));
		scene.registerUpdateHandler(a);
		arrowList.add(a);
		return a;

	}
	private Duck createDuck() {
		duck = new Duck(CAMERA_WIDTH - 50, 250, duckTexture,getVertexBufferObjectManager(), duckTexture,mPhysicsWorld, scene, engineLock);
		final Rectangle duckShape = new Rectangle(CAMERA_WIDTH - 50, 250, 25, 2, getVertexBufferObjectManager());
		
		final Body body;
		body = PhysicsFactory.createBoxBody(mPhysicsWorld, duckShape, BodyType.DynamicBody, FIXTURE_DEF);
		Kane.this.duckFlyingSnd.play();
		duck.setbody(body);
				
		body.setLinearVelocity(-15f, -7.5f);
		duck.animate(new long[]{200, 200}, 0, 1, true);
		
		scene.attachChild(duck);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(duck, body,true, true));
		scene.registerUpdateHandler(duck);
		duckList.add(duck);
		return duck;

	}
	


	
}
	
	
	
