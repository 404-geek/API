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

@RestController
public class DataController {
	private String mongoUrl,connectionID;
	private Connection con=null;
	
	
	public DataController(Environment env) {
		mongoUrl = env.getProperty("spring.mongodb.ipAndPort");
	}
	@Autowired
	private Credentials credentials;
	
	@RequestMapping(method = RequestMethod.GET, value = "/authdesination")
	private void destination(@RequestParam("data") Map<String,String> data) {
		try {
			HashMap<String,String> destCred = new HashMap<>();
			destCred.put("dbName", "mysql");
			destCred.put("userName", "mysql");
			destCred.put("password", "mysql");
			destCred.put("host", "mysql");
			destCred.put("port", "mysql");
			credentials.setDestToken(destCred);
			 
			
			if(!checkDB(credentials.getDestToken().get("database_name")))
			{
				//invalid
			}
		  }
		catch(Exception e) {
			e.printStackTrace();
		}
}
	
	public boolean jsontodatabase(String jsonString) throws SQLException
	{
		
		try {
			
			JFlat x = new JFlat(jsonString);
			List<Object[]> json2csv = x.json2Sheet().getJsonAsSheet();
			Map<String,String> dt = credentials.getDestToken();
			String host = dt.get("server_host");
			String port = dt.get("server_port");
			String dbase = dt.get("database_name");
			String user = dt.get("db_username");
			String pass = dt.get("db_password");	
			
			if (checkDB(credentials.getDestToken().get("database_name"))) {
				String statement =  "CREATE TABLE `" + connectionID + "`(";
				int i = 0;
				int count=0;
				
				for (Object[] o : json2csv) {
					if (i > -1) {
						if(con==null)
							connection();
						if (i == 0) {
							for (Object t : o) {
								statement += "`" + t.toString() + "` TEXT," ;
							}
							System.out.println(statement.substring(0, statement.length() - 1) + ");");
							PreparedStatement stmt=con.prepareStatement(statement.substring(0, statement.length() - 1) + ");");  
							stmt.executeUpdate();
						} else {
							String insstmt = "INSERT INTO `"+connectionID+"` VALUES(";
							for (Object t : o) {
								if (t == null) {
									insstmt +=""
											+ "NULL ,";
								} else {
									count++;
									insstmt+=t.toString()+",";
								}
								insstmt=insstmt.substring(0,insstmt.length()-1)+"(";
								for(int j=0;j<count;j++)
								{
									insstmt+="?,";
								}
								
							}
							PreparedStatement stmt=con.prepareStatement(insstmt.substring(0, statement.length() - 1) + ");");
							System.out.println(insstmt.substring(0,insstmt.length()-1)+");");
							stmt.executeUpdate();
						}
					}
					i = i + 1;
				}
				}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		finally {if(con==null)
			connection();
			con.close();
		}
	}
	public void connection() throws SQLException
	{
		try {
			DestObject destObj=credentials.getDestObj();
			Class.forName(destObj.getDrivers());
			String url = destObj.getUrlprefix() + credentials.getDestToken().get("server_host")
					+":" + credentials.getDestToken().get("server_port")+  
					destObj.getDbnameseparator() + credentials.getDestToken().get("database_name");
			con = DriverManager.getConnection(url, credentials.getDestToken().get("db_username"), credentials.getDestToken().get("db_password"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkDB(String dbase) throws SQLException
	{
		
		try {
			if(con==null)
				connection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM information_schema.tables WHERE table_schema =" + "'"
					+ dbase + "'" + " AND table_name =" + "'" + connectionID + "'");
			while (rs.next()) {
				System.out.println(rs.getInt(1));
				if (rs.getInt(1) == 1) {
					stmt.executeUpdate("DROP " + "`" + connectionID + "`");
					con.close();
					return true;
				}
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
	
}
