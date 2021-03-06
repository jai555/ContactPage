package com.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.cloud.sql.jdbc.Connection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.Contact;

public class CreateContact extends HttpServlet {

	public boolean checkContact( String key,String con)
	{
		boolean contactFlag=false;
PersistenceManager pm = JDOHelper.getPersistenceManagerFactory("transactions-optional").getPersistenceManager();
		
		javax.jdo.Query q = pm.newQuery(Contact.class);
		q.setFilter("userKey == ukey && contactName == contactname");
		q.declareParameters("String ukey,String contactname");
		
		String userName = con;

		List<Contact> results = null;
		String json1;
		System.out.println(userName);
		Gson a = new GsonBuilder().disableHtmlEscaping().create();

		try {
			results = (List<Contact>) q.execute(key,con);
			if(results.size()==0)
			{
			    contactFlag=true;	
			}
			else
			{
				contactFlag=false;
			}
		}
		catch(Exception e)
		{
			
		}

		return contactFlag;
	}
	
	private static Map getMap(JSONObject object) {
		Map<String, Object> map = new HashMap<String, Object>();

		Object jsonObject = null;
		String key = null;
		Object value = null;

		try {
			Iterator<String> keys = object.keys();
			while (keys.hasNext()) {

				key = null;
				value = null;

				key = keys.next();

				if (null != key && !object.isNull(key)) {
					value = object.get(key);
				}

				if (value instanceof JSONObject) {
					map.put(key, getMap((JSONObject) value));
					continue;
				}

				if (value instanceof JSONArray) {
					JSONArray array = ((JSONArray) value);
					List list = new ArrayList();
					for (int i = 0; i < array.length(); i++) {
						jsonObject = array.get(i);
						if (jsonObject instanceof JSONObject) {
							list.add(getMap((JSONObject) jsonObject));
						} else {
							list.add(jsonObject);
						}
					}
					map.put(key, list);
					continue;
				}

				map.put(key, value);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return map;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("init");

		String data = "";
		HashMap<String, String> MobileNo = new HashMap<String, String>();
		HashMap<String, String> EmailID = new HashMap<String, String>();

		StringBuilder builder = new StringBuilder();
		BufferedReader reader = req.getReader();
		String line,sessionkey = null;
		
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		data = builder.toString();
		try {
			System.out.println(data);
			JSONObject object = new JSONObject(data);
			for (int i = 0; i < object.length(); i++) {
				System.out.println(object.getJSONObject("mobile"));
			}
			MobileNo = (HashMap<String, String>) getMap(object.getJSONObject("mobile"));
			EmailID = (HashMap<String, String>) getMap(object.getJSONObject("email"));
			System.out.println("Hello" + MobileNo.toString());
			Contact contact = new Contact();

			System.out.println("EMail LIST ");
			System.out.println(EmailID.toString());
			System.out.println("EMail via MAP" + EmailID.values());
			String jsonText = object.toString();
			System.out.println(jsonText);
			Gson s = new Gson();
			contact = s.fromJson(jsonText, Contact.class);
			
			if(checkContact(contact.getUserKey(),contact.getContactName()) == false)
			{
	           			
			}
	
			contact.setEmail(EmailID);
			contact.setMobileNo(MobileNo);
			System.out.println("From Contact EMail" + contact.getEmail().values());
			System.out.println("From Contact MobileNo" + contact.getMobileNo().values());
			sessionkey =contact.getUserKey();
			contact.makeString();
			System.out.println(contact.getContactName());
			PersistenceManager pm = JDOHelper.getPersistenceManagerFactory("transactions-optional")
					.getPersistenceManager();
			try {
		pm.makePersistent(contact);
				System.out.println("ADDED");
				resp.getWriter().write("200");

			} finally {
				pm.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	  
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		
		System.out.println("Trigger from CreateContact Servlet");
	}
}
