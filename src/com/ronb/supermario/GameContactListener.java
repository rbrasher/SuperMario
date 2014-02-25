package com.ronb.supermario;

import android.util.Log;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class GameContactListener implements ContactListener {

	Game game;
	
	public GameContactListener(Game pGame) {
		super();
		this.game = pGame;
	}
	
	@Override
	public void beginContact(Contact contact) {
		Log.v("GAMECONTACTLISTENER", "beginContact()");
		
		this.fireballAction(contact.getFixtureA(), contact.getFixtureB());
		
		if(contact.getFixtureB().getUserData() != null) {
			Log.v("GAMECONTACTLISTENER", contact.getFixtureB().getUserData().toString());
			
			if(contact.getFixtureB().getUserData().toString() == "EnemyLeftSensor") {
				Log.v("GAMECONTACTLISTENER", ((Enemy)contact.getFixtureB().getBody().getUserData()).enemyID + ": EnemyLeftSensor");
				
				((Enemy) contact.getFixtureB().getBody().getUserData()).changeDirection();
			} else if (contact.getFixtureB().getUserData().toString() == "EnemyRightSensor") {
				Log.v("GAMECONTACTLISTENER", ((Enemy)contact.getFixtureB().getBody().getUserData()).enemyID + ": EnemyRightSensor");
				
				((Enemy) contact.getFixtureB().getBody().getUserData()).changeDirection();
			}
			
			if(contact.getFixtureB().getUserData().toString() == "PlayerFeet")
				game.numFootContacts++;
		}
		
		if(contact.getFixtureA().getUserData() != null) {
			if(contact.getFixtureA().getUserData().toString() == "EnemyLeftSensor") {
				Log.v("GAMECONTACTLISTENER", ((Enemy)contact.getFixtureA().getBody().getUserData()).enemyID + ": EnemyLeftSensor");
				
				((Enemy) contact.getFixtureA().getBody().getUserData()).changeDirection();
			} else if (contact.getFixtureA().getUserData().toString() == "EnemyRightSensor") {
				Log.v("GAMECONTACTLISTENER", ((Enemy)contact.getFixtureA().getBody().getUserData()).enemyID + ": EnemyRightSensor");
				
				((Enemy) contact.getFixtureA().getBody().getUserData()).changeDirection();
			}
			
			Log.v("GAMECONTACTLISTENER", contact.getFixtureA().getUserData().toString());
			if(contact.getFixtureA().getUserData().toString() == "PlayerFeet")
				game.numFootContacts++;
		}
		
		if(game.numFootContacts > 0)
			game.jumpingEnd();
	}
	
	private void fireballAction(Fixture pFixtureA, Fixture pFixtureB) {
		if(pFixtureA.getBody().getUserData() != null) {
			
			if(pFixtureB.getBody().getUserData() != null) {
				if(pFixtureB.getBody().getUserData().toString() == "player") {
					return;
				}
			}
			
			if(pFixtureA.getBody().getUserData() instanceof Fireball) {
				Fireball fb = (Fireball) pFixtureA.getBody().getUserData();
				pFixtureA.getBody().setUserData(null);
				game.fireballContainer.add(fb);
			}
		}
		
		if(pFixtureB.getBody().getUserData() != null) {
			
			if(pFixtureA.getBody().getUserData() != null) {
				if(pFixtureA.getBody().getUserData().toString() == "player")
					return;
			}
			
			if(pFixtureB.getBody().getUserData() instanceof Fireball) {
				Fireball fb = (Fireball) pFixtureB.getBody().getUserData();
				pFixtureB.getBody().setUserData(null);
				game.fireballContainer.add(fb);
			}
		}
	}

	@Override
	public void endContact(Contact contact) {
		Log.v("GAMECONTACTLISTENER", "endContact()");
		
		if(contact.getFixtureB().getUserData() != null) {
			if(contact.getFixtureB().getUserData().toString() == "PlayerFeet")
				game.numFootContacts--;
		}
		
		if(contact.getFixtureA().getUserData() != null) {
			if(contact.getFixtureA().getUserData().toString() == "PlayerFeet")
				game.numFootContacts--;
		}
		if(game.numFootContacts < 1)
			game.jumpingStart();
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}

}
