package com.aptus.blackbox.dataInterfaces;

import java.util.List;

import com.aptus.blackbox.datamodels.Categories;

public interface CategoryDAO {

	 public List<String> getCategoryList(String _id);
	 public void insert(Categories category);
	 public boolean removeCategory(String _id);
	 public boolean removeCategoryItem(String _id,String item);
	 boolean addCategoryItem(String _id, String item);

	
}
