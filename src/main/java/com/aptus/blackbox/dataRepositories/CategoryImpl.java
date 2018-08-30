package com.aptus.blackbox.dataRepositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.aptus.blackbox.dataInterfaces.CategoryDAO;
import com.aptus.blackbox.datamodels.Categories;

@Repository
public class CategoryImpl implements CategoryDAO {

	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public void insert(Categories category) {
		mongoTemplate.save(category);
		
	}
	
	@Override
	public List<String> getCategoryList(String _id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(_id));
		Categories data =  mongoTemplate.findOne(query, Categories.class);
		return data.getCategories();
		
	}

	@Override
	public boolean addCategoryItem(String _id, String item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeCategory(String _id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeCategoryItem(String _id, String item) {
		// TODO Auto-generated method stub
		return false;
	}



	
}
