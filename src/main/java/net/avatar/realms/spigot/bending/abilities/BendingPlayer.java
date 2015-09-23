package net.avatar.realms.spigot.bending.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.event.AbilityCooldownEvent;

public class BendingPlayer {

	private UUID player;

	private Map<Integer, BendingAbilities> slotAbilities = new HashMap<Integer, BendingAbilities>();
	private String currentDeck = "default";
	private Map<String, Map<Integer, BendingAbilities>> decks = new HashMap<String, Map<Integer, BendingAbilities>>();

	private List<BendingElement> bendings = new LinkedList<BendingElement>();
	private List<BendingAffinity> affinities = new LinkedList<BendingAffinity>();
	private List<BendingPath> paths = new LinkedList<BendingPath>();

	private Map<BendingAbilities, Long> cooldowns = new HashMap<BendingAbilities, Long>();

	private long paralyzeTime = 0;
	private long slowTime = 0;

	private long lastTime = 0;

	public BendingPlayer(UUID id) {
		this.player = id;
		this.lastTime = System.currentTimeMillis();
	}

	public String getCurrentDeck() {
		return this.currentDeck;
	}

	public Set<String> getDecksNames() {
		return this.decks.keySet();
	}

	public Map<String, Map<Integer, BendingAbilities>> getDecks() {
		return this.decks;
	}

	public BendingPlayer(BendingPlayerData data) {
		this.player = data.getPlayer();

		this.bendings = data.getBendings() != null ? data.getBendings() : this.bendings;
		this.slotAbilities = data.getSlotAbilities() != null ? data.getSlotAbilities() : this.slotAbilities;

		this.affinities = data.getAffinities() != null ? data.getAffinities() : this.affinities;
		this.paths = data.getPaths() != null ? data.getPaths() : this.paths;

		this.lastTime = data.getLastTime();
	}

	public static BendingPlayer getBendingPlayer(Player player) {
		return Bending.database.get(player.getUniqueId());
	}

	public boolean isOnGlobalCooldown() {
		return (System.currentTimeMillis() <= (this.lastTime + Settings.GLOBAL_COOLDOWN));
	}

	public boolean isOnCooldown(BendingAbilities ability) {

		if (isOnGlobalCooldown()) {
			return true;
		}

		if (this.cooldowns.containsKey(ability)) {
			long time = System.currentTimeMillis();
			return (time <= this.cooldowns.get(ability));

		} else {
			return false;
		}
	}

	public void cooldown() {
		cooldown(null, 0);
	}

	public void cooldown(BendingAbilities ability, long cooldownTime) {
		long time = System.currentTimeMillis();
		if (ability != null) {
			this.cooldowns.put(ability, time + cooldownTime);
		}
		this.lastTime = time;
		if (ability != null) {
			Bending.callEvent(new AbilityCooldownEvent(this, ability));
		}
	}

	public Map<BendingAbilities, Long> getCooldowns() {
		Map<BendingAbilities, Long> cooldowns = new HashMap<BendingAbilities, Long>();
		long now = System.currentTimeMillis();
		List<BendingAbilities> toRemove = new LinkedList<BendingAbilities>();
		for (BendingAbilities ab : this.cooldowns.keySet()) {
			long remain = this.cooldowns.get(ab) - now;
			if (remain <= 0) {
				toRemove.add(ab);
			} else {
				cooldowns.put(ab, remain);
			}
		}

		for (BendingAbilities ab : toRemove) {
			cooldowns.remove(ab);
		}

		return cooldowns;
	}

	public UUID getPlayerID() {
		return this.player;
	}

	public boolean isBender() {
		return !this.bendings.isEmpty();
	}

	public boolean isBender(BendingElement type) {
		if (type == BendingElement.Energy) {
			return true;
		}
		return this.bendings.contains(type);
	}

	public boolean hasAffinity(BendingAffinity specialization) {
		return this.affinities.contains(specialization);
	}

	public boolean hasPath(BendingElement type) {
		if (this.paths == null) {
			return false;
		}
		for (BendingPath path : this.paths) {
			if (path.getElement() == type) {
				return true;
			}
		}
		return false;
	}

	public boolean hasPath(BendingPath path) {
		if (this.paths == null) {
			return false;
		}
		return this.paths.contains(path);
	}

	public void setBender(BendingElement type) {
		removeBender();
		this.bendings.add(type);
		Bending.database.save(this.player);
	}

	public void addBender(BendingElement type) {
		if (!this.bendings.contains(type)) {
			this.bendings.add(type);
			Bending.database.save(this.player);
		}
	}

	public void setAffinity(BendingAffinity affinity) {
		this.clearAffinity(affinity.getElement());
		this.affinities.add(affinity);
		Bending.database.save(this.player);
	}

	public void setPath(BendingPath path) {
		this.clearPath(path.getElement());
		this.paths.add(path);
		Bending.database.save(this.player);
	}

	public void addAffinity(BendingAffinity affinity) {
		if (!this.affinities.contains(affinity)) {
			this.affinities.add(affinity);
			Bending.database.save(this.player);
		}
	}

	public void removeAffinity(BendingAffinity affinity) {
		this.affinities.remove(affinity);
		clearAbilities();
		// clear abilities will save for us
	}

	public void clearAffinity(BendingElement element) {
		List<BendingAffinity> toRemove = new LinkedList<BendingAffinity>();
		for (BendingAffinity spe : this.affinities) {
			if (spe.getElement().equals(element)) {
				toRemove.add(spe);
			}
		}
		for (BendingAffinity spe : toRemove) {
			removeAffinity(spe);
		}
		// clear abilities will save for us
		clearAbilities();
	}

	public void clearPath(BendingElement element) {
		List<BendingPath> toRemove = new LinkedList<BendingPath>();
		for (BendingPath path : this.paths) {
			if (path.getElement().equals(element)) {
				toRemove.add(path);
			}
		}
		for (BendingPath path : toRemove) {
			this.paths.remove(path);
		}
	}

	public void clearAffinities() {
		this.affinities.clear();
		Bending.database.save(this.player);
	}

	public void clearAbilities() {
		this.slotAbilities = new HashMap<Integer, BendingAbilities>();
		Bending.database.save(this.player);
	}

	public void removeBender() {
		clearAbilities();
		this.affinities.clear();
		this.bendings.clear();
		this.paths.clear();
		Bending.database.save(this.player);
	}

	public BendingAbilities getAbility() {
		Player player = getPlayer();
		if (player == null) {
			return null;
		}
		if (!player.isOnline() || player.isDead()) {
			return null;
		}

		int slot = player.getInventory().getHeldItemSlot();
		return getAbility(slot);
	}

	public Map<Integer, BendingAbilities> getAbilities() {
		return this.slotAbilities;
	}

	public BendingAbilities getAbility(int slot) {
		return this.slotAbilities.get(slot);
	}

	public void setAbility(int slot, BendingAbilities ability) {
		this.slotAbilities.put(slot, ability);
		Bending.database.save(this.player);
	}

	public void removeSelectedAbility() {
		Player p = getPlayer();
		if (p == null) {
			return;
		}
		if (!p.isOnline() || p.isDead()) {
			return;
		}

		int slot = p.getInventory().getHeldItemSlot();
		removeAbility(slot);

		Bending.database.save(this.player);
	}

	public void removeAbility(int slot) {
		setAbility(slot, null);
		Bending.database.save(this.player);
	}

	public Player getPlayer() {
		return Bukkit.getServer().getPlayer(this.player);
	}

	public List<BendingElement> getBendingTypes() {
		List<BendingElement> list = new ArrayList<BendingElement>();
		for (BendingElement index : this.bendings) {
			list.add(index);
		}
		return list;
	}

	public String bendingsToString() {

		Player pl = getPlayer();
		if (pl != null) {
			String str = pl.getName() + " : \n";
			for (BendingElement type : this.bendings) {
				str += type.toString() + "\n";
			}
			return str;
		}
		return "This player seems not to exist.";

	}

	public boolean canBeParalyzed() {
		return (System.currentTimeMillis() > this.paralyzeTime);
	}

	public boolean canBeSlowed() {
		return (System.currentTimeMillis() > this.slowTime);
	}

	public void paralyze(long cooldown) {
		this.paralyzeTime = System.currentTimeMillis() + cooldown;
	}

	public void slow(long cooldown) {
		this.slowTime = System.currentTimeMillis() + cooldown;
	}

	public long getLastTime() {
		return this.lastTime;
	}

	public void delete() {
		Bending.database.remove(this.player);
	}

	@Override
	public String toString() {
		String string = "BendingPlayer{";
		string += "Player=" + this.player.toString();
		string += ", ";
		string += "Bendings=" + this.bendings;
		string += ", ";
		string += "Binds=" + this.slotAbilities;
		string += "}";
		return string;
	}

	public List<BendingAffinity> getAffinities() {
		return this.affinities;
	}

	public List<BendingPath> getPath() {
		return this.paths;
	}

	public BendingPlayerData serialize() {
		BendingPlayerData result = new BendingPlayerData();
		result.setBendings(this.bendings);
		result.setLastTime(this.lastTime);
		result.setAffinities(this.affinities);
		result.setPlayer(this.player);
		result.setSlotAbilities(this.slotAbilities);
		result.setPaths(this.paths);
		return result;
	}

}
