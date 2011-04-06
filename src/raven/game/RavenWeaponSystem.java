package raven.game;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import raven.game.armory.*;
import raven.math.Transformations;
import raven.math.Vector2D;
import raven.ui.GameCanvas;

public class RavenWeaponSystem {
	private RavenBot owner;
	
	/** map of weapons the bot is carrying (a bot may only carry one instance
	 * of each weapon) */
	private Map<RavenObject, RavenWeapon> weaponMap;
	
	/** the weapon the bot is currently holding */
	private RavenWeapon currentWeapon;
	
	/** this is the minimum amount of time a bot needs to see an opponent
	 * before it can react to it. This variable is used to prevent a bot
	 * shooting at an opponent the instant it becomes visible. */
	private double reactionTime;
	
	/** each time the current weapon is fired a certain amount of random noise
	 * is added to the the angle of the shot. This prevents the bots from
	 * hitting their opponents 100% of the time. The lower this value the more
	 * accurate a bot's aim will be. Recommended values are between 0 and 0.2
	 * (the value represents the max deviation in radians that can be added to
	 * each shot). */
	private double aimAccuracy;
	
	/** the amount of time a bot will continue aiming at the position of the
	 * target even if the target disappears from view. */
	private double aimPersistance;
	
	/** predicts where the target will be by the time it takes the current
	 * weapon's projectile type to reach it. Used by TakeAimAndShoot */
	private Vector2D predictFuturePositionOfTarget() {
		double maxSpeed = getCurrentWeapon().getMaxProjectileSpeed();
		
		// if the target is ahead and facing the bot shoot at its current pos
		Vector2D toEnemy = owner.getTargetBot().pos().sub(owner.pos());
		
		// the lookahead time is proportional to the distance between the
		// enemy and the persuer; and is inversely proportional to the sum of
		// the agents' velocities
		double lookAheadTime = toEnemy.length() / (maxSpeed + owner.getTargetBot().getMaxSpeed());
		
		// return the predicted future position of the enemy
		return owner.getTargetBot().pos().add(owner.getTargetBot().velocity().mul(lookAheadTime));
	}
	
	/** adds a random deviation to the firing angle not greater than
	 * aimAccuracy radians */
	private void addNoiseToAim(Vector2D aimingPos) {
		Vector2D toPos = aimingPos.sub(owner.pos());
		
		Transformations.Vec2DRotateAroundOrigin(toPos, Math.random() * 2.0 * aimAccuracy - aimAccuracy);
		
		aimingPos = toPos.add(owner.pos());
	}

	public RavenWeaponSystem(RavenBot owner, double reactionTime, double aimAccuracy, double aimPersistence) {
		this.owner = owner;
		this.reactionTime = reactionTime;
		this.aimAccuracy = aimAccuracy;
		this.aimPersistance = aimPersistence;
		
		initialize();
	}
	
	/** sets up the weapon map with just one weapon: the blaster */
	public void initialize() {
		weaponMap = new HashMap<RavenObject, RavenWeapon>();
		
		// set up the container
		currentWeapon = new Blaster(owner);
		weaponMap.put(RavenObject.BLASTER, currentWeapon);
	}

	public void takeAimAndShoot() {
		// aim the weapon only if the current target is shootable or if it has
		// only	very recently gone out of view (this latter condition is to
		// ensure the weapon is aimed at the target even if it temporarily
		// dodges behind a wall or other cover)
		if (owner.getTargetSys().isTargetShootable() || owner.getTargetSys().getTimeTargetHasBeenOutOfView() < aimPersistance) {
			// the position the weapon will be aimed at
			Vector2D aimingPos = owner.getTargetBot().pos();
			
			// if the current weapon is not an instant hit type gun the target
			// position	must be adjusted to take into account the predicted
			// movement of the target
			if (owner.rotateFacingTowardPosition(aimingPos)
					&& owner.getTargetSys().getTimeTargetHasBeenVisible() > reactionTime
					&& owner.hasLOSto(aimingPos)) {
				aimingPos = predictFuturePositionOfTarget();
				
				// if the weapon is aimed correctly, there is line of sight
				// between the bot and the aiming position and it has been in
				// view for a period longer than the bot's reaction time,
				// shoot the weapon
				if (owner.rotateFacingTowardPosition(aimingPos)
						&& owner.getTargetSys().getTimeTargetHasBeenVisible() > reactionTime
						&& owner.hasLOSto(aimingPos)) {
					addNoiseToAim(aimingPos);
					
					getCurrentWeapon().shootAt(aimingPos);

				}
			}
			// no need to predict movement, aim directly at target
			else {
				// if the weapon is aimed correctly and it has been in view for a
				// period longer than the bot's reaction time, shoot the weapon
				if (owner.rotateFacingTowardPosition(aimingPos)
						&& owner.getTargetSys().getTimeTargetHasBeenVisible() > reactionTime
						&& owner.hasLOSto(aimingPos)) {
					addNoiseToAim(aimingPos);
					
					getCurrentWeapon().shootAt(aimingPos);
				}
			}
		}
		// no target to shoot at so rotate facing to be parallel with the
		// bot's heading direction
		else {
			owner.rotateFacingTowardPosition(owner.pos().add(owner.heading()));
		}
	}

	public void selectWeapon() {
		// if a target is present use fuzzy logic to determine the most
		// desirable weapon
		if (owner.getTargetSys().isTargetPresent()) {
			// calculate the distance to the target
			double distToTarget = owner.pos().distance(owner.getTargetSys().getTarget().pos());
			
			// for each weapon in the inventory calculate its desirability
			// given the current situation. The most desirable weapon is
			// selected
			double bestSoFar = Double.MIN_VALUE;
			for (RavenWeapon weapon : weaponMap.values()) {
				// grab the desirability of this weapon (desirability is based
				// upon distance to target and ammo remaining)
				double score = weapon.getDesirability(distToTarget);
				
				if (score > bestSoFar) {
					bestSoFar = score;
					currentWeapon = weapon;
				}
			}
		} else {
			currentWeapon = weaponMap.get(RavenObject.BLASTER);
		}
	}

	public void addWeapon(RavenObject weaponType) {
		RavenWeapon newWeap;
		
		switch (weaponType) {
		case RAIL_GUN:
			newWeap = new Railgun(owner);
			break;
		case SHOTGUN:
			newWeap = new Shotgun(owner);
			break;
		case ROCKET_LAUNCHER:
			newWeap = new RocketLauncher(owner);
			break;
		}
		
		RavenWeapon present = getWeaponFromInventory(weaponType);
		if (present != null) {
			present.incrementRounds(newWeap.numRoundsRemaining());
		} else {
			weaponMap.put(weaponType, newWeap);
		}
		
	}

	public void changeWeapon(RavenObject type) {
		if (weaponMap.containsKey(type)) {
			currentWeapon = weaponMap.get(type);
		}
		
	}

	public void shootAt(Vector2D pos) {
		getCurrentWeapon().shootAt(pos);
	}
	
	/** returns a pointer to the current weapon */
	public RavenWeapon getCurrentWeapon() { return currentWeapon; }

	public RavenWeapon getWeaponFromInventory(RavenObject weaponType) {
		return weaponMap.get(weaponType);
	}
	
	public int getAmmoRemainingForWeapon(RavenObject weaponType) {
		if (weaponMap.containsKey(weaponType)) {
			return weaponMap.get(weaponType).numRoundsRemaining();
		}
		
		return 0;
	}
	
	public double reactionTime() { return reactionTime; }
	
	public void renderCurrentWeapon() {
		getCurrentWeapon().render();
	}

	public void renderDesirabilities() {
		Vector2D p = owner.pos();
		
		int offset = 15 * weaponMap.size();
		
		for (RavenObject weaponKey : weaponMap.keySet()) {
			double score = weaponMap.get(weaponKey).getLastDesirabilityScore();
			final DecimalFormat formatter = new DecimalFormat("0.00")
			GameCanvas.textAtPos(p.x + 10, p.y - offset, formatter.format(score) + " " + weaponKey.getDescription());
		}
	}
}
