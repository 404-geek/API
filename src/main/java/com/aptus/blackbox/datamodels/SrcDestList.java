package com.aptus.blackbox.datamodels;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
public class SrcDestList {
	@Id
	private final String _id = "srcdestlist";
	private List<String>  destCategories,srcCategories;
    private List<srcdestObj> destinations,sources;
	
	public List<String> getDestCategories() {
		return destCategories;
	}
	public void setDestCategories(List<String> destCategories) {
		this.destCategories = destCategories;
	}
	public List<String> getSrcCategories() {
		return srcCategories;
	}
	public void setSrcCategories(List<String> srcCategories) {
		this.srcCategories = srcCategories;
	}
	public List<srcdestObj> getDestinations() {
		return destinations;
	}
	public void setDestinations(List<srcdestObj> destinations) {
		this.destinations = destinations;
	}
	public List<srcdestObj> getSources() {
		return sources;
	}
	public void setSources(List<srcdestObj> sources) {
		this.sources = sources;
	}
	
 	
}

class srcdestObj{
	
	private String id,name,logo;
	private List<String> categories;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
}
