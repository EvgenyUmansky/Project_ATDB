/**
 * 
 */
package org.bgu.ise.ddb.items;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletResponse;

import org.bgu.ise.ddb.MediaItems;
import org.bgu.ise.ddb.ParentController;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;

import jdk.jshell.spi.ExecutionControl.ExecutionControlException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.sql.*;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @author Alex
 *
 */
@RestController
@RequestMapping(value = "/items")
public class ItemsController extends ParentController {
	/**
	 * The function copy all the items(title and production year) from the Oracle table MediaItems to the System storage.
	 * The Oracle table and data should be used from the previous assignment
	 */
	@RequestMapping(value = "fill_media_items", method={RequestMethod.GET})
	public void fillMediaItems(HttpServletResponse response){
		System.out.println("was here");
		//:TODO your implementation
		// get the oracle driver
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// try-with-resources
		try(
				Connection connection = DriverManager.getConnection(
						"jdbc:oracle:thin:@132.72.65.216:1521:oracle", "mohsenab", "abcd"); // open connection to oracle
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery("SELECT * FROM MediaItems"); // get results of select query
				MongoClient mongoClient = new MongoClient("localhost", 27017);// open mongodb connection
			){
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("MediaItems");
			// add the data from result to mongo
			while(result.next()) {
				// get title and year from oracle result
				String title = result.getString("TITLE");
				int year = Integer.parseInt(result.getString("PROD_YEAR"));
				
				if(isKeyValueInCollection("MediaItems", "title", title)) {
					continue;
				}
				
				// add the data to mongo
				BasicDBObject insertionDocument = new BasicDBObject();
				insertionDocument.put("title", title);
				insertionDocument.put("prod_year", year);
				collection.insert(insertionDocument);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		HttpStatus status = HttpStatus.OK;
		response.setStatus(status.value());
	}
	
	

	/**
	 * The function copy all the items from the remote file,
	 * the remote file have the same structure as the films file from the previous assignment.
	 * You can assume that the address protocol is http
	 * @throws IOException 
	 */
	@RequestMapping(value = "fill_media_items_from_url", method={RequestMethod.GET})
	public void fillMediaItemsFromUrl(@RequestParam("url")    String urladdress,
			HttpServletResponse response) throws IOException{
		// %26 instead &:
		// https://drive.google.com/uc?export=download%26id=1kKKkAJlqf0xzfOmLpX9sZC6Zfq8JZfxr
		System.out.println(urladdress);
		//:TODO your implementation
		if(urladdress == null || urladdress.isEmpty()) {
			return;
		}
		
        URL rowdata = new URL(urladdress);
        URLConnection data = rowdata.openConnection();
        
	    try (
	    		MongoClient mongoClient = new MongoClient("localhost", 27017);
	    		Scanner input = new Scanner(data.getInputStream());
	    	){
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("MediaItems");
	        
	        while (input.hasNextLine()) {
	            String line = input.nextLine();
	            String[] titleYear = line.split(",");
	            System.out.println(line);
	            
				if(isKeyValueInCollection("MediaItems", "title", titleYear[0])) {
					continue;
				}
				
				// add the data to mongo
				BasicDBObject insertionDocument = new BasicDBObject();
				insertionDocument.put("title", titleYear[0]);
				insertionDocument.put("prod_year", Integer.parseInt(titleYear[1]));
				collection.insert(insertionDocument);
	        }
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
		
		HttpStatus status = HttpStatus.OK;
		response.setStatus(status.value());
	}
	
	
	/**
	 * The function retrieves from the system storage N items,
	 * order is not important( any N items) 
	 * @param topN - how many items to retrieve
	 * @return
	 */
	@RequestMapping(value = "get_topn_items",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(MediaItems.class)
	public  MediaItems[] getTopNItems(@RequestParam("topn")    int topN){
		//:TODO your implementation
		if(topN <= 0) {
			return new MediaItems[0];
		}
		
		MediaItems[] mediaItems = new MediaItems[topN];
		try(
				MongoClient mongoClient = new MongoClient("localhost", 27017);
		   ){
			
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("MediaItems");
			Iterator<DBObject> iter = collection.find().iterator();
			
			for(int i = 0; i < topN; i++) {
				DBObject data = iter.next();
				mediaItems[i] = new MediaItems(data.get("title").toString(), Integer.parseInt(data.get("prod_year").toString()));
			}
			
		 }
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return mediaItems;
	}
	
	
	// check if the data is already exists in mongo 
	private boolean isKeyValueInCollection(String collectionName, String key, String value) {
		try(MongoClient mongoClient = new MongoClient("localhost", 27017);){
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection(collectionName);
			DBObject isExistQuery = new BasicDBObject(key, value);
			DBCursor isExistResult = collection.find(isExistQuery);
			
			if(isExistResult.size() > 0) {
				return true;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
		

}
