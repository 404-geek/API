package com.aptus.blackbox.datamodels;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class DestinationConfig implements Serializable{

	@Id
	private String _id;
	private String dbnameseparator,drivers,identifier_quote_close,identifier_quote_open;
	private String type_date,type_dateTime,type_integer,type_real,type_text,type_time,type_varchar;
	private String select_statement,insert_statement,create_statement,drop_statement;
	private String information_table,database_schema,table_schema,urlprefix,value_quote_close,value_quote_open;

	public String getSelect_statement() {
		return select_statement;
	}

	public void setSelect_statement(String select_statement) {
		this.select_statement = select_statement;
	}

	public String getInsert_statement() {
		return insert_statement;
	}

	public void setInsert_statement(String insert_statement) {
		this.insert_statement = insert_statement;
	}

	public String getCreate_statement() {
		return create_statement;
	}

	public void setCreate_statement(String create_statement) {
		this.create_statement = create_statement;
	}

	public String getDrop_statement() {
		return drop_statement;
	}

	public void setDrop_statement(String drop_statement) {
		this.drop_statement = drop_statement;
	}

	public String getInformation_table() {
		return information_table;
	}

	public void setInformation_table(String information_table) {
		this.information_table = information_table;
	}

	public String getDatabase_schema() {
		return database_schema;
	}

	public void setDatabase_schema(String database_schema) {
		this.database_schema = database_schema;
	}

	public String getTable_schema() {
		return table_schema;
	}

	public void setTable_schema(String table_schema) {
		this.table_schema = table_schema;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getDrivers() {
		return drivers;
	}

	public void setDrivers(String drivers) {
		this.drivers = drivers;
	}

	public String getUrlprefix() {
		return urlprefix;
	}

	public void setUrlprefix(String urlprefix) {
		this.urlprefix = urlprefix;
	}

	public String getDbnameseparator() {
		return dbnameseparator;
	}

	public void setDbnameseparator(String dbnameseparator) {
		this.dbnameseparator = dbnameseparator;
	}

	public String getType_varchar() {
		return type_varchar;
	}

	public void setType_varchar(String type_varchar) {
		this.type_varchar = type_varchar;
	}

	public String getType_text() {
		return type_text;
	}

	public void setType_text(String type_text) {
		this.type_text = type_text;
	}

	public String getType_integer() {
		return type_integer;
	}

	public void setType_integer(String type_integer) {
		this.type_integer = type_integer;
	}

	public String getType_real() {
		return type_real;
	}

	public void setType_real(String type_real) {
		this.type_real = type_real;
	}

	public String getType_time() {
		return type_time;
	}

	public void setType_time(String type_time) {
		this.type_time = type_time;
	}

	public String getType_date() {
		return type_date;
	}

	public void setType_date(String type_date) {
		this.type_date = type_date;
	}

	public String getType_dateTime() {
		return type_dateTime;
	}

	public void setType_dateTime(String type_dateTime) {
		this.type_dateTime = type_dateTime;
	}

	public String getIdentifier_quote_open() {
		return identifier_quote_open;
	}

	public void setIdentifier_quote_open(String identifier_quote_open) {
		this.identifier_quote_open = identifier_quote_open;
	}

	public String getIdentifier_quote_close() {
		return identifier_quote_close;
	}

	public void setIdentifier_quote_close(String identifier_quote_close) {
		this.identifier_quote_close = identifier_quote_close;
	}

	public String getValue_quote_open() {
		return value_quote_open;
	}

	public void setValue_quote_open(String value_quote_open) {
		this.value_quote_open = value_quote_open;
	}

	public String getValue_quote_close() {
		return value_quote_close;
	}

	public void setValue_quote_close(String value_quote_close) {
		this.value_quote_close = value_quote_close;
	}
	

}
