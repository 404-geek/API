package com.aptus.blackbox.datamodels;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.aptus.blackbox.models.srcdestObj;


@Document
public class SrcDestList implements Serializable{
	@Id
	private  String _id ;
	private List<String>  destCategories,srcCategories;
    private List<srcdestObj> destinations,sources;
    
	
	
	
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}

	
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