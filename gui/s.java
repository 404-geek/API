@RequestMapping(method = RequestMethod.POST, value = "/createdatasource")
	private ResponseEntity<String> createDataSource(@RequestParam Map<String,String> filteredEndpoints,HttpSession session)
	{
		ResponseEntity<String> ret = null;
		//System.out.println(srcname+" "+destname);
		try	{
			System.out.println(filteredEndpoints.getClass());
			System.out.println(filteredEndpoints.get("filteredendpoints")+" "+filteredEndpoints.keySet());
			HttpHeaders headers = new HttpHeaders();
			headers.add("Cache-Control", "no-cache");
			headers.add("access-control-allow-origin", rootUrl);
			headers.add("access-control-allow-credentials", "true");
			if(Utilities.isSessionValid(session,credentials)) {
				if(validateCredentials==null||endPoints==null||refreshToken==null) {
					init();
				}
				String body="",b1="",endpnts="",conId;
				conId=credentials.getUserId()+"_"+credentials.getSrcName()+"_"+credentials.getDestName()
						+"_"+String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
				for(Map.Entry<String,String> mp:credentials.getSrcToken().entrySet()) {
					b1+="{\"key\":\""+String.valueOf(mp.getKey())+"\",\"value\":\""+String.valueOf(mp.getValue())+"\"},";
				}
				Gson gson =new Gson();
				JsonArray endpoints = gson.fromJson(filteredEndpoints.get("filteredendpoints"), JsonElement.class).getAsJsonObject().get("endpoints").getAsJsonArray();
				ConnObj currobj = new ConnObj();
				for(JsonElement obj:endpoints) {
					endpnts+="\""+obj.getAsString()+"\",";
					currobj.setEndPoints(obj.getAsString());
				}
				currobj.setConnectionId(conId);
				currobj.setSourceName(credentials.getSrcName());
				currobj.setDestName(credentials.getDestName());
				credentials.setCurrConnId(currobj);
				endpnts = endpnts.substring(0, endpnts.length()-1).toLowerCase();
				System.out.println(b1);
				if(!b1.isEmpty())
					b1=b1.substring(0,b1.length()-1);

				if(credentials.isUserExist())
				{
					//sourceCredentials
					body= "{\"credentials\":["+b1+"]}";

					postpatchMetaData(body,"source","PATCHapp");

					//userCredentials
					body = "{\"$addToSet\":"
							+ "{\"srcdestId\":"
							+ "{\"$each\":["
							+ "{\"sourceName\": \""+credentials.getSrcName().toLowerCase()+"\","
							+ "\"destName\":\""+credentials.getDestName().toLowerCase()+"\","
							+ "\"connectionId\": \""+conId.toLowerCase()+"\","
							+ "\"endPoints\":["+endpnts+"]}"
							+ "]}}}";

					postpatchMetaData(body,"user","PATCH");

					//destCredentials
					body= "{\"credentials\":["+b1+"]}";

					postpatchMetaData(body,"destination","PATCHapp");

				}
				else {
					//userCredentials
					body = "{ \"_id\" : \""+credentials.getUserId().toLowerCase()+"\","
							+ "\"srcdestId\" : [ "
							+ "{ \"sourceName\" : \""+credentials.getSrcName().toLowerCase()+"\" , "
							+ "\"destName\" : \""+credentials.getDestName().toLowerCase()+"\","
							+ "\"connectionId\":\""+conId.toLowerCase()+"\","
							+ "\"endPoints\":["+endpnts+"]}"
							+ "]}";

					postpatchMetaData(body,"user","POST");

					//sourceCredentials
					body= "{\n" +
							"	\"_id\" : \""+credentials.getUserId().toLowerCase()
							+"_"+credentials.getSrcName().toLowerCase()+"\",\n" +
							"	\"credentials\":["+b1+"]\n" +
							"}";

					postpatchMetaData(body,"source","POST");

					//destCredentials
					body= "{\n" +
							"	\"_id\" : \""+credentials.getUserId().toLowerCase()
							+"_"+credentials.getDestName().toLowerCase()+"\",\n" +
							"				\"credentials\":["+b1+"]\n" +
							"}";

					postpatchMetaData(body,"destination","POST");
				}
				//ret = data(credentials.getSrcName());
				//System.out.println(ret.getBody());
				String url=baseUrl;
				headers.setLocation(URI.create(url));
				return new ResponseEntity<String>("",headers ,HttpStatus.OK);
			}
			else {
				System.out.println("Session expired!");
				String url=baseUrl;
				headers.setLocation(URI.create(url));
				return new ResponseEntity<String>("Sorry! Your session has expired",headers ,HttpStatus.OK);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return ret;
	}
