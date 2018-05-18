	package com.aptus.blackbox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import com.aptus.blackbox.dataService.ApplicationCredentials;
import com.aptus.blackbox.dataService.Credentials;
import com.aptus.blackbox.models.DestObject;

abstract public class DestinationAuthorisation {
	
	private String tableName;
	private Connection con = null;

	@Value("${spring.mongodb.ipAndPort}")
	private String mongoUrl;
	@Value("${homepage.url}")
	private String homeUrl;
	@Value("${base.url}")
	private String baseUrl;
	@Value("${access.control.allow.origin}")
	private String rootUrl;

	@Autowired
	private Credentials credentials;
	@Autowired
	private ApplicationCredentials applicationCredentials;
	@Autowired
	private ApplicationContext Context;
	
	
	protected void connection(Map<String, String> destToken, DestObject destObj) throws SQLException {
		try {

			System.out.println("DataController-driver: " + destObj.getDrivers());
			Class.forName(destObj.getDrivers());
			String url = destObj.getUrlprefix() + destToken.get("server_host") + ":" + destToken.get("server_port")
					+ destObj.getDbnameseparator() + destToken.get("database_name");
			System.out.println(url);
			con = DriverManager.getConnection(url, destToken.get("db_username"), destToken.get("db_password"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected boolean checkDB(String dbase, Map<String, String> destToken, DestObject destObj) throws SQLException {

		try {
			if (con == null || con.isClosed())
				connection(destToken, destObj);
			Statement stmt = con.createStatement();
			String query = "SELECT count(*) FROM information_schema.tables WHERE table_schema ="
					+ destObj.getValue_quote_open() + dbase + destObj.getValue_quote_close() + ";";
			System.out.println(query);
			ResultSet res = stmt.executeQuery(query);
			if (res.next()) {
				con.close();
				return true;
			}
			con.close();
			return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	protected boolean truncateAndPush(DestObject destObj,Map<String, String> destToken) {
		try {
			if (con == null || con.isClosed())
				connection(destToken, destObj);
			PreparedStatement stmt;
			stmt = con.prepareStatement("SELECT count(*) AS COUNT FROM information_schema.tables WHERE table_name ="
					+ destObj.getValue_quote_open() + credentials.getCurrConnId().getConnectionId()
					+ destObj.getValue_quote_close() + ";");
			ResultSet res = stmt.executeQuery();
			res.first();
			if (res.getInt("COUNT") != 0) {
				stmt = con.prepareStatement("DROP TABLE " + credentials.getCurrConnId().getConnectionId() + ";");
				stmt.execute();
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}
