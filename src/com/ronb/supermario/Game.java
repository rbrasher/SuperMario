package com.ronb.supermario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.debugdraw.DebugRenderer;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.extension.tmx.TMXLoader;
import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.andengine.extension.tmx.TMXObject;
import org.andengine.extension.tmx.TMXObjectGroup;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.extension.tmx.util.exception.TMXLoadException;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;


public class Game extends SimpleBaseGameActivity {
	
	// ==========================================
	// CONSTANTS
	// ==========================================
	private static final int CAMERA_WIDTH = 960;
	private static final int CAMERA_HEIGHT = 540;
	private static final int PLAYER_VELOCITY = 1;
	
	// ==========================================
	// VARIABLES 
	// ==========================================
	AnimatedSprite player;
	boolean isPlayerJumping = false;
	private BoundCamera mCamera;
	Scene scene;
	private ArrayList<Enemy> arrayEnemies = new ArrayList<Enemy>();
	public CopyOnWriteArrayList<Fireball> fireballContainer = new CopyOnWriteArrayList<Fireball>();
	
	private TMXTiledMap mTMXTiledMap;
	private PhysicsWorld mPhysicsWorld;
	
	private Body mPlayerBody;
	
	public enum PlayerDirection {
		LEFT, RIGHT, UP, NONE;
	}
	
	PlayerDirection lastDirection = PlayerDirection.UP;
	
	private BitmapTextureAtlas leftArrowButtonTA;
	private TextureRegion leftArrowTR;
	private BitmapTextureAtlas rightArrowButtonTA;
	private TextureRegion rightArrowTR;
	private BitmapTextureAtlas jumpButtonTA;
	private TextureRegion jumpButtonTR;
	private BitmapTextureAtlas shootButtonTA;
	private TextureRegion shootButtonTR;
	
	private BitmapTextureAtlas mPlayerTA;
	private TiledTextureRegion mPlayerTR;
	private BitmapTextureAtlas mEnemyTA;
	private TiledTextureRegion mEnemyTTR;
	private BitmapTextureAtlas mFireballTA;
	private TiledTextureRegion mFireballTTR;
	protected Body mFireballBody;
	
	public int numFootContacts = 0;
	private boolean isMoving = false;
	private boolean isJumping = false;
	
	private Music mMusic;
	private Sound mJumpSound;
	

	@Override
	public EngineOptions onCreateEngineOptions() {
		mCamera = new BoundCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
		
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
		engineOptions.getAudioOptions().setNeedsMusic(true);
		engineOptions.getAudioOptions().setNeedsSound(true);
		
		return engineOptions;
	}
	
	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) {
		return new LimitedFPSEngine(pEngineOptions, 60);
	}

	@Override
	protected void onCreateResources() {
		//load up our BitmapTextureAtlas' and TextureRegions
		try {
			
			//Mario
			mPlayerTA = new BitmapTextureAtlas(this.getTextureManager(), 108, 68, TextureOptions.DEFAULT);
			mPlayerTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mPlayerTA, this, "mario2.png", 0, 0, 6, 2);
			mPlayerTA.load();
			
			//fireball
			mFireballTA = new BitmapTextureAtlas(this.getTextureManager(), 72, 9, TextureOptions.DEFAULT);
			mFireballTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mFireballTA, this, "fireball.png", 0, 0, 8, 1);
			mFireballTA.load();
			
			//enemy
			mEnemyTA = new BitmapTextureAtlas(this.getTextureManager(), 54, 16, TextureOptions.DEFAULT);
			mEnemyTTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mEnemyTA, this, "mario_dog_sprite.png", 0, 0, 3, 1);
			mEnemyTA.load();
			
			//left arrow button
			leftArrowButtonTA = new BitmapTextureAtlas(this.getTextureManager(), 50, 50, TextureOptions.BILINEAR);
			leftArrowTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(leftArrowButtonTA, this, "leftarrow.png", 0, 0);
			leftArrowButtonTA.load();
			
			//right arrow button
			rightArrowButtonTA = new BitmapTextureAtlas(this.getTextureManager(), 50, 50, TextureOptions.BILINEAR);
			rightArrowTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(rightArrowButtonTA, this, "rightarrow.png", 0, 0);
			rightArrowButtonTA.load();
			
			//jump button
			jumpButtonTA = new BitmapTextureAtlas(this.getTextureManager(), 96, 96, TextureOptions.BILINEAR);
			jumpButtonTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(jumpButtonTA, this, "jumpbutton.png", 0, 0);
			jumpButtonTA.load();
			
			//shoot button
			shootButtonTA = new BitmapTextureAtlas(this.getTextureManager(), 96, 96, TextureOptions.BILINEAR);
			shootButtonTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(shootButtonTA, this, "shootbutton.png", 0, 0);
			shootButtonTA.load();
			
		} catch (IllegalArgumentException e1) {
			Debug.e(e1);
		}
		
		//load up our music and sounds
		try {
			mMusic = MusicFactory.createMusicFromAsset(mEngine.getMusicManager(), this, "track1.ogg");
			mJumpSound = SoundFactory.createSoundFromAsset(mEngine.getSoundManager(), this, "smb_jump-super.ogg");
			
			mMusic.setLooping(true);
		} catch (final IOException e) {
			Debug.e(e);
		}
	}

	@Override
	protected Scene onCreateScene() {
		mEngine.registerUpdateHandler(new FPSLogger());
		
		scene = new Scene();
		
		scene.setBackground(new Background(0.18f, 0.74f, 0.98f));
		scene.setBackgroundEnabled(true);
		
		final HUD hud = new HUD();
		hud.setTouchAreaBindingOnActionDownEnabled(true);
		hud.setTouchAreaBindingOnActionMoveEnabled(true);
		
		mPhysicsWorld = new PhysicsWorld(new Vector2(0f, 9.8f), false);
		mPhysicsWorld.setContactListener(new GameContactListener(this));
		mPhysicsWorld.setAutoClearForces(true);
		
		scene.registerUpdateHandler(mPhysicsWorld);
		
		try {
			final TMXLoader tmxLoader = new TMXLoader(this.getAssets(), mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, this.getVertexBufferObjectManager(), new ITMXTilePropertiesListener() {
				@Override
				public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {
					
				}
			});
			
			this.mTMXTiledMap = tmxLoader.loadFromAsset("world1.tmx");
		} catch (final TMXLoadException e) {
			Debug.e(e);
		}
		
		for(int i = 0; i < mTMXTiledMap.getTMXLayers().size(); i++) {
			TMXLayer layer = mTMXTiledMap.getTMXLayers().get(i);
			scene.attachChild(layer);
		}
		
		this.createUnwalkableObjects(mTMXTiledMap);
		this.createEnemyObjects(mTMXTiledMap);
		
		player = new AnimatedSprite(200, 400, mPlayerTR, this.getVertexBufferObjectManager());
		mCamera.setChaseEntity(player);
		
		mMusic.play();
		
		final PhysicsHandler physicsHandler = new PhysicsHandler(player);
		player.registerUpdateHandler(physicsHandler);
		
		final Sprite jumpButton = new Sprite(CAMERA_WIDTH - 50 - jumpButtonTR.getWidth(), CAMERA_HEIGHT - jumpButtonTR.getHeight() - 30, jumpButtonTR, this.getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if(pSceneTouchEvent.isActionDown()) {
					if(Game.this.numFootContacts > 0)
						Game.this.jump();
				} else if (pSceneTouchEvent.isActionUp()) {
					
				}
				return true;
			};
		};
		
		final Sprite shootButton = new Sprite(CAMERA_WIDTH - 50
				- this.shootButtonTR.getWidth(), CAMERA_HEIGHT
				- this.jumpButtonTR.getHeight()
				- this.shootButtonTR.getHeight() - 60,
				this.shootButtonTR, this.getVertexBufferObjectManager()) {
				@Override
				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {

				if (pSceneTouchEvent.isActionDown()) {

					Log.v("FEBI2", String.valueOf(Game.this.player.getX()) + ' ' + String.valueOf(Game.this.player.getY()));
					if (Game.this.lastDirection == PlayerDirection.RIGHT || Game.this.lastDirection == PlayerDirection.LEFT) {
						if (Game.this.lastDirection == PlayerDirection.RIGHT) {
							Game.this.getPhysicsWorld().postRunnable(
									new Runnable() {
										@Override
										public void run() {
											Fireball fireball = new Fireball(
													(Game.this.player.getX() + 18),
													(Game.this.player.getY() + 5),
													Game.this.mFireballTTR,
													Game.this.getVertexBufferObjectManager(),
													Game.this);
										}
									});
	
						} else if (Game.this.lastDirection == PlayerDirection.LEFT) {
							Game.this.getPhysicsWorld().postRunnable(
									new Runnable() {
										@Override
										public void run() {
											Fireball fireball = new Fireball(
													(Game.this.player.getX() - 8),
													(Game.this.player.getY() + 5),
													Game.this.mFireballTTR,
													Game.this.getVertexBufferObjectManager(),
													Game.this);
										}
									});
						}
	
						Log.v("FEBI2", "fireballContainer size: " + String.valueOf(Game.this.fireballContainer.size()));
					}

				}

				return true;
			};
		};
		
		final Sprite leftArrowButton = new Sprite(15, CAMERA_HEIGHT - leftArrowTR.getHeight() - 30, leftArrowTR, this.getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if(pSceneTouchEvent.isActionDown()) {
					isMoving = true;
					lastDirection = PlayerDirection.LEFT;
					
					if(player.isAnimationRunning())
						player.animate(new long[]{200, 200, 200}, 7, 9, true);
					mPlayerBody.setLinearVelocity(-1 * PLAYER_VELOCITY, 0);
					
				} else if (pSceneTouchEvent.isActionUp()) {
					if(isJumping)
						player.stopAnimation(11);
					else
						player.stopAnimation(6);
					
					mPlayerBody.setLinearVelocity(0, 0);
					isMoving = false;
				}
				
				return true;
			};
		};
		
		final Sprite rightArrowButton = new Sprite(leftArrowTR.getWidth() + (60 + 15), CAMERA_HEIGHT - leftArrowTR.getHeight() - 30, rightArrowTR, this.getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if(pSceneTouchEvent.isActionDown()) {
					isMoving = true;
					lastDirection = PlayerDirection.RIGHT;
					
					if(player.isAnimationRunning())
						player.animate(new long[]{200, 200, 200}, 1, 3, true);
					mPlayerBody.setLinearVelocity(1 * PLAYER_VELOCITY, 0);
					
				} else if (pSceneTouchEvent.isActionUp()) {
					isMoving = false;
					if(isJumping)
						player.stopAnimation(5);
					else
						player.stopAnimation(0);
					mPlayerBody.setLinearVelocity(0, 0);
				}
				
				return true;
			};
		};
		//test
		hud.attachChild(jumpButton);
		hud.attachChild(shootButton);
		hud.attachChild(leftArrowButton);
		hud.attachChild(rightArrowButton);
		
		hud.registerTouchArea(jumpButton);
		hud.registerTouchArea(shootButton);
		hud.registerTouchArea(leftArrowButton);
		hud.registerTouchArea(rightArrowButton);
		
		mCamera.setHUD(hud);
		
		final FixtureDef playerFixtureDef = PhysicsFactory.createFixtureDef(0f, 0f, 0f);
		mPlayerBody = PhysicsFactory.createBoxBody(mPhysicsWorld, player, BodyType.DynamicBody, playerFixtureDef);
		mPlayerBody.setUserData("player");
		
		final PolygonShape mPoly = new PolygonShape();
		mPoly.setAsBox(.2f, .2f, new Vector2(0, .5f), 0);
		final FixtureDef pFixtureDef = PhysicsFactory.createFixtureDef(0f, 0f, 0f, true);
		pFixtureDef.shape = mPoly;
		Fixture mFeet = mPlayerBody.createFixture(pFixtureDef);
		mFeet.setUserData("PlayerFeet");
		
		mPoly.dispose();
		
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(player, mPlayerBody, true, false));
		
		scene.attachChild(player);
		
		scene.registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void onUpdate(float pSecondsElapsed) {
				mPhysicsWorld.onUpdate(pSecondsElapsed);
				
				Entity e = new Entity();
				
				int tileMapHeight = mTMXTiledMap.getTileHeight() * mTMXTiledMap.getTileRows();
				float y = (tileMapHeight - (CAMERA_HEIGHT / 2));
				if(player.getX() > (CAMERA_WIDTH / 2))
					e.setPosition(player.getX(), y);
				else
					e.setPosition((CAMERA_WIDTH / 2), y);
				
				for(final Fireball dFireball : fireballContainer) {
					if(dFireball instanceof Fireball && dFireball != null) {
						runOnUpdateThread(new Runnable() {

							@Override
							public void run() {
								dFireball.destroy();
								fireballContainer.remove(dFireball);
							}
							
						});
					}
				}
				
				//check nearby enemies
				for(Enemy enemy : arrayEnemies) {
					Log.v("ENEMY", String.valueOf(enemy.getEnemy().getY()));
					//Log.v("ENEMY", areaSums + " " + cameraArea);
					
					if((e.getX() + (CAMERA_WIDTH / 2)) > enemy.getEnemy().getX())
						enemy.startMoving();
					
					if(enemy.EnemyTrigger) {
						Log.v("ENEMY", enemy.enemyID + " - Speed: " + String.valueOf(enemy.getEnemyBody().getLinearVelocity().x));
						
						if(enemy.getEnemyBody().getLinearVelocity().x == 0) {
							enemy.changeDirection();
							enemy.getEnemyBody().setLinearVelocity((float)(Math.round(enemy.getEnemyBody().getLinearVelocity().x * 100.0) / 100.0), 0);
						}
					}
					
				}
				
				mCamera.setChaseEntity(e);
				
				final MoveModifier modifier = new MoveModifier(30, e.getX(), player.getX(), e.getY(), y) {
					@Override
					protected void onModifierFinished(IEntity pItem) {
						super.onModifierFinished(pItem);
						mCamera.setChaseEntity(null);
					}
				};
				
				e.registerEntityModifier(modifier);
			}

			@Override
			public void reset() {

			}
			
		});
		scene.attachChild(new DebugRenderer(mPhysicsWorld, getVertexBufferObjectManager()));
		
		return scene;
	}
	
	private void createEnemyObjects(TMXTiledMap mTMXTiledMap2) {
		for(final TMXObjectGroup group : mTMXTiledMap.getTMXObjectGroups()) {
			Log.v("createEnemyObjects", group.getName());
			if(!group.getName().equals("Enemies"))
				continue;
			
			for(final TMXObject object : group.getTMXObjects()) {
				Enemy enemy = new Enemy(new Vector2(object.getX(), object.getY()), mEnemyTTR, this.getVertexBufferObjectManager(), mPhysicsWorld);
				arrayEnemies.add(enemy);
				scene.attachChild(enemy.getEnemy());
			}
		}
	}

	private void createUnwalkableObjects(TMXTiledMap map) {
		for(final TMXObjectGroup group : mTMXTiledMap.getTMXObjectGroups()) {
			Log.v("createUnwalkableObjects", group.getName());
			if(!group.getName().equals("Unwalkable"))
				continue;
			
			for(final TMXObject object : group.getTMXObjects()) {
				
				final Rectangle rect = new Rectangle(object.getX(), object.getY(), object.getWidth(), object.getHeight(), this.getVertexBufferObjectManager());
				
				final FixtureDef boxFixtureDef = PhysicsFactory.createFixtureDef(0.0f, 0.0f, 0.0f);
				PhysicsFactory.createBoxBody(getPhysicsWorld(), rect, BodyType.StaticBody, boxFixtureDef);
				
				rect.setVisible(false);
				
				final PhysicsHandler physicsHandler = new PhysicsHandler(rect);
				rect.registerUpdateHandler(physicsHandler);
				
				scene.attachChild(rect);
				
			}
		}
	}
	
	private void jump() {
		mJumpSound.play();
		
		if(lastDirection == PlayerDirection.RIGHT) {
			player.animate(new long[]{100}, new int[]{5});
			mPlayerBody.setLinearVelocity(new Vector2(((this.isMoving) ? (PLAYER_VELOCITY + 1) : mPlayerBody.getLinearVelocity().x), -9));
			//player.stopAnimation(5);
		} else if (lastDirection == PlayerDirection.LEFT) {
			player.animate(new long[]{100}, new int[]{11});
			mPlayerBody.setLinearVelocity(new Vector2(((this.isMoving) ? (-PLAYER_VELOCITY - 1) : mPlayerBody.getLinearVelocity().x), -9));
			//player.stopAnimation(11);
		}
	}
	
	public void jumpingStart() {
		isJumping = true;
	}
	
	public void jumpingEnd() {
		isJumping = false;
		if(isMoving) {
			if(lastDirection == PlayerDirection.LEFT) {
				player.animate(new long[]{200, 200, 200}, 7, 9, true);
				mPlayerBody.setLinearVelocity(new Vector2(-PLAYER_VELOCITY, 0));
			} else if (lastDirection == PlayerDirection.RIGHT) {
				player.animate(new long[]{200, 200, 200}, 1, 3, true);
				mPlayerBody.setLinearVelocity(new Vector2(PLAYER_VELOCITY, 0));
			}
		} else {
			if(lastDirection == PlayerDirection.LEFT) {
				player.stopAnimation(6);
			} else if (lastDirection == PlayerDirection.RIGHT) {
				player.stopAnimation(0);
			}
		}
	}
	
	public PhysicsWorld getPhysicsWorld() {
		return mPhysicsWorld;
	}
	
	public void setPhysicsWorld(PhysicsWorld pPhysicsWorld) {
		this.mPhysicsWorld = pPhysicsWorld;
	}

}
