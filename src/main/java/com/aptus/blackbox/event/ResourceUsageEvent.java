package com.aptus.blackbox.event;

import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

public class ResourceUsageEvent implements  ResolvableTypeProvider {
	private String data;
	public ResourceUsageEvent(String data){
		this.setData(data);
		System.out.println("Ressource USage Event Model constructor called for listener");
		System.out.println(data);
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	@Override
	public ResolvableType getResolvableType() {
		// TODO Auto-generated method stub
		return null;
	}

}
