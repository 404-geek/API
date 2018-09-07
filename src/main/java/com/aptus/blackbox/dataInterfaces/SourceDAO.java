package com.aptus.blackbox.dataInterfaces;

import java.util.List;

import com.aptus.blackbox.datamodels.Sources;

public interface SourceDAO {
 public List<Sources> getSourceList();
 public void insert(Sources source);
 public boolean removeSource(String _id);
}
