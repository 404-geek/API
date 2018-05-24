package com.aptus.blackbox.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImplicitDataNode implements Serializable {
	private String label;
	private List<ImplicitDataNode> childs = new ArrayList<>();
	private Map<String,String> data=new HashMap<>();
	private boolean isVisited=false;
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	

	public boolean isVisited() {
		return isVisited;
	}
	public void setVisited(boolean isVisited) {
		this.isVisited = isVisited;
	}
	public Map<String,String> getData() {
		return data;
	}
	public void setData(String key,String value) {
		this.data.put(key, value);
	}
	public List<ImplicitDataNode> getChilds() {
		return childs;
	}
	public void setChilds(ImplicitDataNode childs) {
		this.childs.add(childs);
	}
	public void setChilds(List<ImplicitDataNode> childs) {
		this.childs = childs;
	}

}
