package com.aptus.blackbox.datamodels;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Categories {

	private String _id;
	private List<String> categories;
	
	
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	
	
	
}
