package com.aptus.blackbox.dataInterfaces;

import com.aptus.blackbox.datamodels.SourceConfig;

public interface SourceConfigDAO {
	public void createSourceConfig(SourceConfig sourceConfig);
	public SourceConfig getSourceConfig(String source);
	public boolean updateSourceConfig(String source,SourceConfig sourcConfig);
	public boolean deleteSourceConfig(String source);
}
