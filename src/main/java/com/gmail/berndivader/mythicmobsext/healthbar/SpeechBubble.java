package com.gmail.berndivader.mythicmobsext.healthbar;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.util.Vector;

import com.gmail.berndivader.mythicmobsext.utils.math.MathUtils;


public class SpeechBubble {
	protected TextDisplay display;
	protected LivingEntity entity;
	protected UUID uuid;
	protected double offset;
	protected double sOffset;
	protected double fOffset;
	protected String[] template;
	protected int counter;
	protected int maxlines;
	protected int il1 = 0;
	protected int ll;
	protected boolean useOffset;
	protected boolean uc1;
	protected String id;

	public SpeechBubble(LivingEntity entity, String[] text) {
		this(entity, "bubble", entity.getLocation(), 0d, -1, text, 0d, 0d, false, 30, true);
	}

	public SpeechBubble(LivingEntity entity, String[] text, int ll) {
		this(entity, "bubble", entity.getLocation(), 0d, -1, text, 0d, 0d, false, ll, true);
	}

	public SpeechBubble(LivingEntity entity, String s1, Location l1, double offset, int showCounter, String[] text,
			double sOffset, double fOffset, boolean b1, int ll, boolean b2) {
		this.id = s1;
		this.ll = ll;
		this.fOffset = fOffset;
		this.sOffset = sOffset;
		this.maxlines = -1;
		this.useOffset = fOffset != 0d || sOffset != 0d;

		Location spawnLoc = l1.clone();
		if (this.useOffset && b1) {
			Vector soV = MathUtils.getSideOffsetVectorFixed(entity.getLocation().getYaw(), this.sOffset, false);
			Vector foV = MathUtils.getFrontBackOffsetVector(entity.getLocation().getDirection(), this.fOffset);
			spawnLoc.add(soV).add(foV);
		}
		this.uc1 = b2;
		this.uuid = entity.getUniqueId();
		HealthbarHandler.speechbubbles.put(this.uuid.toString() + this.id, this);
		this.counter = showCounter < 1 ? 60 : showCounter * 20;
		this.counter = showCounter;
		this.entity = entity;
		this.offset = offset;
		this.template = text;

		// Spawn TextDisplay
		this.display = entity.getWorld().spawn(spawnLoc, TextDisplay.class, td -> {
			td.setBillboard(Billboard.CENTER);
			td.setSeeThrough(false);
			td.setShadowed(true);
			td.setDefaultBackground(false);
			td.setBackgroundColor(org.bukkit.Color.fromARGB(160, 0, 0, 0));
			td.setVisibleByDefault(true);
		});
		lines();
	}

	public boolean update() {
		if (display == null || display.isDead())
			return false;
		Location l = this.entity.getLocation();
		World w = l.getWorld();
		double dx = l.getX();
		double dy = l.getY();
		double dz = l.getZ();
		// Approximate line height offset similar to the old HD logic
		int lineCount = this.template != null ? this.template.length : 1;
		double do1 = (lineCount * 0.25) + (il1 * 0.5) + this.offset;
		if (this.useOffset) {
			Vector soV = MathUtils.getSideOffsetVectorFixed(entity.getLocation().getYaw(), this.sOffset, false);
			Vector foV = MathUtils.getFrontBackOffsetVector(entity.getLocation().getDirection(), this.fOffset);
			dx += soV.getX() + foV.getX();
			dz += soV.getZ() + foV.getZ();
		}
		this.display.teleport(new Location(w, dx, dy + do1, dz));
		if (uc1) {
			this.counter--;
			if (this.counter < 0)
				this.remove();
		}
		return true;
	}

	public void remove() {
		HealthbarHandler.speechbubbles.remove(this.uuid.toString() + this.id);
		if (display != null && !display.isDead()) {
			display.remove();
		}
	}

	public void lines() {
		this.il1 = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.template.length; i++) {
			String line = this.template[i];
			// Strip <additem.XXX> tags - TextDisplay can't render items inline,
			// just show the material name as text instead
			if (line.contains("<additem.")) {
				String matName = line.split("<additem\\.")[1].split(">")[0];
				line = line.replaceAll("<additem\\.[^>]+>", "[" + matName + "]");
				il1++;
			}
			if (i > 0) sb.append("\n");
			sb.append(line);
		}
		if (display != null && !display.isDead()) {
			display.setText(sb.toString());
		}
	}


}
