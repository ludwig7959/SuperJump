package com.tistory.hornslied.superjump;

import java.util.HashMap;
import java.util.Map;

public class MultipleJump {

	private int limit;
	
	private Map<Integer, Jump> jumps;
	
	public MultipleJump(int limit) {
		jumps = new HashMap<>();
		
		this.limit = limit;
	}
	
	public boolean hasJump(int index) {
		return jumps.containsKey(index);
	}
	
	public Jump getJump(int index) {
		return jumps.get(index);
	}
	
	public int getLimit() {
		return limit;
	}
	
	public void addJump(int index, Jump jump) {
		jumps.put(index, jump);
	}
}
