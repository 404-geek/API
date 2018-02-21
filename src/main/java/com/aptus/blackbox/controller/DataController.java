package com.aptus.blackbox.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aptus.blackbox.Service.Credentials;
import com.aptus.blackbox.index.DestObject;
import com.github.opendevl.JFlat;




/*
 * boolean null text number/integer
 */






@RestController
public class DataController {
	private String  tableName;
	private Connection con = null;

	
	@Value("${abc}")
	private String mongoUrl;

	@Autowired
	private Credentials credentials;
	
	private DestObject destObj;
	private Map<String,String> destToken;

	@RequestMapping(method = RequestMethod.GET, value = "/authdestination")
	private void destination() throws SQLException { // @RequestParam("data") Map<String,String> data
		try {

			HashMap<String, String> destCred = new HashMap<>();

			destCred.put("database_name", "test");
			destCred.put("db_username", "root");
			destCred.put("db_password", "blackbox");
			destCred.put("server_host", "192.168.1.40");
			destCred.put("server_port", "3306");
			//tableName = "user";
			credentials.setDestToken(destCred);
			
			this.destObj = credentials.getDestObj();
			this.destToken = credentials.getDestToken();
			
			if (!checkDB(destToken.get("database_name"))) {
				// invalid
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			con.close();
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/pushToDB")
	public boolean jsontodatabase(@RequestBody String  jsonString,@RequestParam("tableName") String tableName) throws SQLException {
		System.out.println("pushDBController-driver: "+destObj.getDrivers());
		this.tableName=tableName;
		try {
			if (con == null || con.isClosed())
				connection();
			System.out.println("TABLENAME: "+tableName);
			//System.out.println("JSONSTRING: "+jsonString);
			String host = destToken.get("server_host");
			String port = destToken.get("server_port");
			String dbase = destToken.get("database_name");
			String user = destToken.get("db_username");
			String pass = destToken.get("db_password");
			PreparedStatement preparedStmt;
			if (checkDB(destToken.get("database_name"))) {
				JFlat x = new JFlat(jsonString);
				List<Object[]> json2csv = x.json2Sheet().headerSeparator("_").getJsonAsSheet();
				// System.out.println(json2csv);
				
				String statement = "";
				
				int i = 0;
				for (Object[] row : json2csv) {
					if (i == 0) {
						 statement="CREATE TABLE "+
								 destObj.getIdentifier_quote_open()+ tableName +destObj.getIdentifier_quote_close()+
								 "(";
						 for(Object t : row)
						 statement+=t.toString()+" TEXT,";
						
						 statement=statement.substring(0,statement.length()-1)+");";
						 System.out.println("-----"+statement);
						 preparedStmt = con.prepareStatement(statement);
						 preparedStmt.execute();
					} else {
						int k;						
						String instmt = "INSERT INTO " +
								 destObj.getIdentifier_quote_open()+ tableName +destObj.getIdentifier_quote_close()+
								  " VALUES(";
						
						for(k=0;k<row.length;k++)
							instmt+="?,";
						
						instmt = instmt.substring(0,instmt.length()-1)+");";
						PreparedStatement stmt = con.prepareStatement(instmt);
						
						k=1;
						for (Object attr : row) {
							stmt.setString( k++,attr==null?null:attr.toString());
						}
						System.out.println(instmt);
						stmt.execute();
					}
					i++;
				}
				con.close();
				return true;
			}
		} catch (Exception e) {
			System.err.println("Got an exception!");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
				
		
		return false;
	}

	
	public void connection() throws SQLException {
		try {
			
			System.out.println("DataController-driver: "+destObj.getDrivers());
			Class.forName(destObj.getDrivers());
			String url = destObj.getUrlprefix() + destToken.get("server_host") + ":"
					+ destToken.get("server_port") + destObj.getDbnameseparator()
					+ destToken.get("database_name");
			System.out.println(url);
			con = DriverManager.getConnection(url, destToken.get("db_username"),
					destToken.get("db_password"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean checkDB(String dbase) throws SQLException {

		try {
			if (con == null || con.isClosed())
				connection();
			Statement stmt = con.createStatement();
			String query = "SELECT count(*) FROM information_schema.tables WHERE table_schema =" 
			+ destObj.getValue_quote_open()+ dbase+destObj.getValue_quote_close()+";";
			System.out.println(query);
			ResultSet res = stmt.executeQuery(query);
			if (res.next())
				return true;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}



}
