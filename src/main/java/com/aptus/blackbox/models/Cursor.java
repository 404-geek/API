package com.aptus.blackbox.models;

import java.io.Serializable;

public class Cursor implements Serializable{
private String key,param,type;

public String getKey() {
	return key;
}

public void setKey(String key) {
	this.key = key;
}

public String getParam() {
	return param;
}

public void setParam(String param) {
	this.param = param;
}

public String getType() {
	return type;
}

public void setType(String type) {
	this.type = type;
}

}
