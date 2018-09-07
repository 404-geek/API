package com.aptus.blackbox.dataServices;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.CategoryDAO;
import com.aptus.blackbox.dataInterfaces.DestinationDAO;
import com.aptus.blackbox.dataInterfaces.SourceDAO;
import com.aptus.blackbox.datamodels.Categories;
import com.aptus.blackbox.datamodels.Destinations;
import com.aptus.blackbox.datamodels.Sources;

@Service
public class SourceDestinationList {

	@Autowired
	private SourceDAO sourceDAO;
	
	@Autowired
	private DestinationDAO destinationDAO;
	
	@Autowired
	private CategoryDAO categoryDAO;
	
	public void insertCategory(Categories category) {
		categoryDAO.insert(category);
	}
	
	public void insertSource(Sources source) {
		sourceDAO.insert(source);
	}
	
	public void insertDestination(Destinations destination) {
		destinationDAO.insert(destination);
	}
	
	public List<String> getCategories(String _id){
		return categoryDAO.getCategoryList(_id);	
	}
	
	public List<Sources> getSourceList(){
		return sourceDAO.getSourceList();
	}
	
	public List<Destinations> getDestinationList(){
		return destinationDAO.getDestinationList();
		
	}
	
	public void fun() {
		
	}
	
	
	
	
	
}
