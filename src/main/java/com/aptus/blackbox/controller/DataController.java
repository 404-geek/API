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
import org.springframework.core.env.Environment;
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
	private String mongoUrl, tableName;
	private Connection con = null;

	public DataController() {

	}

	public DataController(Environment env) {
		mongoUrl = env.getProperty("spring.mongodb.ipAndPort");
	}

	@Autowired
	private Credentials credentials;

	@RequestMapping(method = RequestMethod.GET, value = "/authdestination")
	private void destination() throws SQLException { // @RequestParam("data") Map<String,String> data
		try {

			HashMap<String, String> destCred = new HashMap<>();

			destCred.put("database_name", "test");
			destCred.put("db_username", "root");
			destCred.put("db_password", "blackbox");
			destCred.put("server_host", "192.168.1.9");
			destCred.put("server_port", "3306");
			//tableName = "user";
			credentials.setDestToken(destCred);

			if (!checkDB(credentials.getDestToken().get("database_name"))) {
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

	public boolean jsontodatabase(String jsonString,String tableName) throws SQLException {

		this.tableName=tableName;
		try {
			if (con == null)
				connection();

			JFlat x = new JFlat(jsonString);
			List<Object[]> json2csv = x.json2Sheet().getJsonAsSheet();
			Map<String, String> dt = credentials.getDestToken();
			String host = dt.get("server_host");
			String port = dt.get("server_port");
			String dbase = dt.get("database_name");
			String user = dt.get("db_username");
			String pass = dt.get("db_password");
			PreparedStatement stmt;
			if (checkDB(credentials.getDestToken().get("database_name"))) {
				String query = "CREATE TABLE " + credentials.getDestObj().getValue_quote_open() + tableName
						+ credentials.getDestObj().getValue_quote_close() + "(id "
						+ credentials.getDestObj().getType_integer() + "," + "name "
						+ credentials.getDestObj().getType_varchar() + "(100));";
				System.out.println(query);
				stmt = con.prepareStatement(query);
				//stmt.executeUpdate();

				query = "INSERT INTO " + credentials.getDestObj().getValue_quote_open() + tableName
						+ credentials.getDestObj().getValue_quote_close() + " VALUES(?,?)";
				stmt = con.prepareStatement(query);
				stmt.setInt(1, 2);
				stmt.setString(2, "jj");

				//stmt.executeUpdate();
				System.out.println(query);

				int i = 0;
				int count = 0;
				String statement= "CREATE TABLE `"+ tableName + "`("  ;
				
				Object[] type; 
				if(json2csv.size()>0)
					type = json2csv.get(1);
				else
					System.out.println("No Data");
				
//				for (Object[] o : json2csv) {
//					if (i > -1) {
//						if (con == null)
//							connection();
//						if (i == 0) {
//							for (Object t : o) {
//								statement += "`" + t.toString()+":"+t.getClass()+"," ;
//							}
//							System.out.println(statement.substring(0, statement.length() - 1) + ");");
//						stmt = con.prepareStatement(statement.substring(0, statement.length() - 1) + ");");
////							stmt.executeUpdate();
//						} else {
//							String insstmt = "INSERT INTO `" + tableName + "` VALUES(";
//							for (Object t : o) {
//								if (t == null) {
//									insstmt += "" + "NULL ,";
//								} else {
//									count++;
//									insstmt += t.toString()+":"+t.getClass() + ",";
//								}
//								insstmt = insstmt.substring(0, insstmt.length() - 1) + "(";
//								for (int j = 0; j < count; j++) {
//								insstmt += "?,";
//								}
//
//							}
//							 stmt = con.prepareStatement(insstmt.substring(0, statement.length() - 1) + ");");
//							System.out.println(insstmt.substring(0, insstmt.length() - 1) + ");");
//							//stmt.executeUpdate();
//						}
//					}
//					i = i + 1;
//				}

			}
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

	public void connection() throws SQLException {
		try {
			DestObject destObj = credentials.getDestObj();
			System.out.println(destObj.getDrivers());
			Class.forName(destObj.getDrivers());
			String url = destObj.getUrlprefix() + credentials.getDestToken().get("server_host") + ":"
					+ credentials.getDestToken().get("server_port") + destObj.getDbnameseparator()
					+ credentials.getDestToken().get("database_name");
			System.out.println(url);
			con = DriverManager.getConnection(url, credentials.getDestToken().get("db_username"),
					credentials.getDestToken().get("db_password"));
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
			if (con == null)
				connection();
			Statement stmt = con.createStatement();
			String query = "SELECT count(*) FROM information_schema.tables WHERE table_schema =" + "'" + dbase;
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
