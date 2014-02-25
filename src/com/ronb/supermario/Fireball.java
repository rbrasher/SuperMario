package com.ronb.supermario;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.ronb.supermario.Game.PlayerDirection;
import com.ronb.supermario.Game;

public class Fireball extends AnimatedSprite {

	private Body mFireballBody;
	private Game game;
	
	public Body getFireballBody() {
		return mFireballBody;
	}
	
	public Fireball(float pX, float pY, ITiledTextureRegion pTiledTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager, Game pGame) {
		super(pX, pY, pTiledTextureRegion, pVertexBufferObjectManager);
		this.game = pGame;
		
		mFireballBody = PhysicsFactory.createBoxBody(game.getPhysicsWorld(), this, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(.5f, .5f, .7f));
		mFireballBody.setFixedRotation(false);
		game.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(this, mFireballBody, true, false));
		
		game.scene.attachChild(this);
		this.animate(100);
		mFireballBody.setLinearVelocity((game.lastDirection == PlayerDirection.RIGHT) ? 10 : -10, 1);
		mFireballBody.setUserData(this);
	}
	
	public void destroy() {
		game.getPhysicsWorld().unregisterPhysicsConnector(game.getPhysicsWorld().getPhysicsConnectorManager().findPhysicsConnectorByShape(this));
		mFireballBody.setActive(false);
		game.getPhysicsWorld().destroyBody(mFireballBody);
		game.scene.detachChild(this);
		
		//perform system garbage collection
		System.gc();
	}

}
