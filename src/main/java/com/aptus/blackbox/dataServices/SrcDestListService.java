package com.aptus.blackbox.dataServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.SrcDestListDAO;
import com.aptus.blackbox.datamodels.SrcDestList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

@Service
public class SrcDestListService {

	@Autowired
	private SrcDestListDAO srcdestDAO;
	
	
	public String getSrcDestList() {
		SrcDestList response = srcdestDAO.getSrcDestList("srcdestlist");
		String data = new Gson().toJson(response, SrcDestList.class); 
		return data;
	}


	public void insertData(SrcDestList srcdestList) {
		 srcdestDAO.insertData(srcdestList);		
	}


	
}
