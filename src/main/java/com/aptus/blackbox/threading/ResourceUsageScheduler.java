package com.aptus.blackbox.threading;

import java.io.File;
import java.text.NumberFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import com.google.gson.JsonObject;

public class ResourceUsageScheduler implements Runnable {
	
	 public ResourceUsageScheduler() {
		// TODO Auto-generated constructor stub
		 System.out.println("Resource Usage Scheduler started");
		 
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try {
			JSONObject details = new JSONObject();
			JSONObject disk = new JSONObject();
			JSONObject os = new JSONObject();
			JSONObject mem = new JSONObject();
			JSONArray arr = new JSONArray();
			File[] roots = File.listRoots();
			Runtime runtime = Runtime.getRuntime();
			NumberFormat format = NumberFormat.getInstance();
			os.put("OS name: ", System.getProperty("os.name"));
			os.put("OS version: ", System.getProperty("os.version"));
			os.put("OS architecture: ", System.getProperty("os.arch"));
			os.put("Available processors (cores): ", runtime.availableProcessors());
			details.put("OS info: ", os);
			
			mem.put("Free memory: ", format.format(runtime.freeMemory() / 1024));
			mem.put("Allocated memory: ", format.format(runtime.totalMemory() / 1024));
			mem.put("Max memory: ", format.format(runtime.maxMemory() / 1024));
			mem.put("Total free memory: ", format.format((runtime.freeMemory() + 
					(runtime.maxMemory() - runtime.totalMemory())) / 1024));
			details.put("Memory info: ", mem);
			
			for (File root : roots) {
				disk.put("File system root: ",root.getAbsolutePath());
				disk.put("Total space (bytes): ", root.getTotalSpace());
				disk.put("Free space (bytes): ", root.getFreeSpace());
				disk.put("Usable space (bytes): ", root.getUsableSpace());
				arr.put(disk);						
			}
			
			details.put("Disk info: ",arr);
			
			System.out.println("USage data printing"+ details.toString());
			
			
		} 
		catch(HttpClientErrorException e) {
            JsonObject respBody = new JsonObject();
            respBody.addProperty("data", "Error");
            respBody.addProperty("status", "404");
            System.out.println(e.getMessage());
            
           System.out.println("***Usage data Exception error Respbody printing"+respBody.toString());
        }
		
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("***Usage data exception 2nD catch");
		}

		
	}

}
