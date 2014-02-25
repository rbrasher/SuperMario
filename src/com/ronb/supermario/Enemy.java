package com.ronb.supermario;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class Enemy {

	public static final float ENEMY_VELOCITY = .6f;
	public static final float ENEMY_MASS = 1f;
	private int enemyDirection = -1;
	public boolean EnemyTrigger = false;
	public int enemyID = 0;
	private Body mEnemyBody;
	private AnimatedSprite enemy;
	
	public Body getEnemyBody() {
		return mEnemyBody;
	}
	
	public AnimatedSprite getEnemy() {
		return enemy;
	}
	
	public Enemy(Vector2 v, TiledTextureRegion pTiledTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld pPhysicsWorld) {
		this.enemyID = 1 + (int) (Math.random() * ((100 - 1) + 1));
		
		this.enemy = new AnimatedSprite(v.x, v.y, pTiledTextureRegion, pVertexBufferObjectManager);
		
		final FixtureDef playerFixtureDef = PhysicsFactory.createFixtureDef(1f, 0f, 0f);
		mEnemyBody = PhysicsFactory.createBoxBody(pPhysicsWorld, enemy, BodyType.DynamicBody, playerFixtureDef);
		mEnemyBody.setFixedRotation(true);
		mEnemyBody.setUserData(this);
		
		PolygonShape mPoly = new PolygonShape();
		mPoly.setAsBox(.1f, .1f, new Vector2(-.3f, -.04f), 0);
		FixtureDef pFixtureDef = PhysicsFactory.createFixtureDef(0f, 0f, 0f, true);
		pFixtureDef.shape = mPoly;
		Fixture leftSensor = mEnemyBody.createFixture(pFixtureDef);
		leftSensor.setUserData("EnemyLeftSensor");
		mPoly.dispose();
		
		PolygonShape mPoly2 = new PolygonShape();
		mPoly2.setAsBox(.1f, .1f, new Vector2(.3f, -.04f), 0);
		FixtureDef pFixtureDef2 = PhysicsFactory.createFixtureDef(0f, 0f, 0f, true);
		pFixtureDef2.shape = mPoly2;
		Fixture rightSensor = mEnemyBody.createFixture(pFixtureDef2);
		rightSensor.setUserData("EnemyRightSensor");
		mPoly2.dispose();
		
		PolygonShape mPoly3 = new PolygonShape();
		mPoly3.setAsBox(.1f, .1f, new Vector2(0f, -.28f), 0);
		FixtureDef pFixtureDef3 = PhysicsFactory.createFixtureDef(0f, 0f, 0f, true);
		pFixtureDef3.shape = mPoly3;
		Fixture topSensor = mEnemyBody.createFixture(pFixtureDef3);
		topSensor.setUserData("EnemyTopSensor");
		mPoly3.dispose();
		
		PolygonShape mPoly4 = new PolygonShape();
		mPoly4.setAsBox(.1f, .1f, new Vector2(0f, .25f), 0);
		FixtureDef pFixtureDef4 = PhysicsFactory.createFixtureDef(0f, 0f, 0f, true);
		pFixtureDef4.shape = mPoly4;
		Fixture bottomSensor = mEnemyBody.createFixture(pFixtureDef4);
		bottomSensor.setUserData("EnemyBottomSensor");
		mPoly4.dispose();
		
		pPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(enemy, mEnemyBody, true, false));
	}
	
	public void startMoving() {
		if(!EnemyTrigger) {
			enemy.animate(new long[]{200, 200}, 0, 1, true);
			mEnemyBody.setLinearVelocity(enemyDirection * ENEMY_VELOCITY, 0);
		}
		EnemyTrigger = true;
	}
	
	public void changeDirection() {
		Log.v("ENEMY", "changeDirection()");
		Log.v("ENEMY", String.valueOf(mEnemyBody.getLinearVelocity().x));
		enemyDirection = -(enemyDirection);
		
		mEnemyBody.setLinearVelocity(enemyDirection * ENEMY_VELOCITY, 0);
	}
}
