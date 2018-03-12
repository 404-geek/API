JsonObject o=new JsonObject();
					JsonArray each=new JsonArray();
					JsonArray endp=new JsonArray();
					endp.add(endpnts);
					JsonObject obj=new JsonObject();
					obj.addProperty("sourceName", "");
					obj.addProperty("destName", "");
					obj.addProperty("connectionId", "");
					obj.add("endPoints", endp);
					each.add(obj);
					o.add("$each" , each);
					
					JsonObject sdId=new JsonObject();
					sdId.add("srcdestId", o);
					JsonObject atset=new JsonObject();
					atset.add("$addToSet", atset);
					
					System.out.println(atset);