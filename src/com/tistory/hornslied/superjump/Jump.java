package com.tistory.hornslied.superjump;

import org.bukkit.Sound;

public class Jump {

	public Sound sound;
	public float volume;
	public float pitch;
	public float angle;
	public double power;
	
	public Jump(Sound sound, float volume, float pitch, float angle, double power) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.angle = angle;
		this.power = power;
	}
}
