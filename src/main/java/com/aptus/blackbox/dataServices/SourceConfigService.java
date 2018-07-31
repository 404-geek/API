package com.aptus.blackbox.dataServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aptus.blackbox.dataInterfaces.SourceConfigDAO;
import com.aptus.blackbox.datamodels.SourceConfig;

@Service
public class SourceConfigService {

	@Autowired
	private SourceConfigDAO sourceConfigDAO;
	
	public void createSourceConfig(SourceConfig sourceConfig) {
		sourceConfigDAO.createSourceConfig(sourceConfig);
	}
	
	public SourceConfig getSourceConfig(String source) {
		return sourceConfigDAO.getSourceConfig(source);
	}
	public boolean updateSourceConfig(String source,SourceConfig sourcConfig) {
		return sourceConfigDAO.updateSourceConfig(source, sourcConfig);
	}
	public boolean deleteSourceConfig(String source) {
		return sourceConfigDAO.deleteSourceConfig(source);
	}
	
}
