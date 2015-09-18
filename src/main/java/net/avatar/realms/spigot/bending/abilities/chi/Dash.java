package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;

@BendingAbility(name = "Dash", element = BendingType.ChiBlocker)
public class Dash extends ActiveAbility {

	@ConfigurationParameter("Length")
	private static double LENGTH = 1.9;

	@ConfigurationParameter("Height")
	private static double HEIGHT = 0.7;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 6000;

	private Vector direction;

	public Dash(Player player) {
		super(player, null);
	}

	@Override
	public boolean sneak() {
		if (this.state.isBefore(AbilityState.CanStart)) {
			return true;
		}

		if (this.state == AbilityState.CanStart) {
			AbilityManager.getManager().addInstance(this);
			setState(AbilityState.Preparing);
		}

		return false;
	}

	public static boolean isDashing(Player player) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.Dash);
		if ((instances == null) || instances.isEmpty()) {
			return false;
		}
		return instances.containsKey(player);
	}

	public static Dash getDash(Player pl) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.Dash);
		return (Dash) instances.get(pl);
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		if (this.state != AbilityState.Progressing) {
			return true;
		}
		System.out.println(this.player.getLocation().toString());
		dash();
		return false;
	}

	public void dash() {
		Vector dir = new Vector(this.direction.getX() * LENGTH, HEIGHT, this.direction.getZ() * LENGTH);
		this.player.setVelocity(dir);
		setState(AbilityState.Ended);
	}

	// This should be called in OnMoveEvent to set the direction dash the same
	// as the player
	public void setDirection(Vector d) {
		if (this.state != AbilityState.Preparing) {
			return;
		}
		if (Double.isNaN(d.getX()) || Double.isNaN(d.getY()) || Double.isNaN(d.getZ())
				|| (((d.getX() < 0.005) && (d.getX() > -0.005)) && ((d.getZ() < 0.005) && (d.getZ() > -0.005)))) {
			this.direction = this.player.getLocation().getDirection().clone().normalize();
		} else {
			this.direction = d.normalize();
		}
		System.out.println(this.player.getLocation().toString());
		setState(AbilityState.Progressing);
	}

	@Override
	public void remove() {
		long cd = COOLDOWN;
		if (ComboPoints.getComboPointAmount(player) >= 1) {
			ComboPoints.consume(player, 1);
		} else {
			cd *= 2;
		}
		this.bender.cooldown(Abilities.Dash, cd);
		super.remove();
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.Dash;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}
