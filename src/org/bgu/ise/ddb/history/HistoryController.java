/**
 * 
 */
package org.bgu.ise.ddb.history;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.bgu.ise.ddb.ParentController;
import org.bgu.ise.ddb.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * @author Alex
 *
 */
@RestController
@RequestMapping(value = "/history")
public class HistoryController extends ParentController{
	
	
	
	/**
	 * The function inserts to the system storage triple(s)(username, title, timestamp). 
	 * The timestamp - in ms since 1970
	 * Advice: better to insert the history into two structures( tables) in order to extract it fast one with the key - username, another with the key - title
	 * @param username
	 * @param title
	 * @param response
	 */
	@RequestMapping(value = "insert_to_history", method={RequestMethod.GET})
	public void insertToHistory (@RequestParam("username")    String username,
			@RequestParam("title")   String title,
			HttpServletResponse response){
		System.out.println(username+" "+title);
		//:TODO your implementation
		
		try {
			if(! (isExistUser(username) && isExistTitle(title))) {
				HttpStatus status = HttpStatus.CONFLICT;
				response.setStatus(status.value());
				return;
			}
			
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("History");
			BasicDBObject searchQuery = new BasicDBObject();
			
			//TODO: timestamp??????
			searchQuery.put("username", username);
			searchQuery.put("title", title);
			searchQuery.put("timestamp", new Date().getTime());
			
			collection.insert(searchQuery);
			
			mongoClient.close();
		}catch(Exception ex) {
			System.out.println(ex);
		}
		
		
		HttpStatus status = HttpStatus.OK;
		response.setStatus(status.value());
	}
	
	
	
	/**
	 * The function retrieves  users' history
	 * The function return array of pairs <title,viewtime> sorted by VIEWTIME in descending order
	 * @param username
	 * @return
	 */
	@RequestMapping(value = "get_history_by_users",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(HistoryPair.class)
	public  HistoryPair[] getHistoryByUser(@RequestParam("entity")    String username){
		//:TODO your implementation
		HistoryPair hp = new HistoryPair("aa", new Date());
		System.out.println("ByUser "+hp);
		
		
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("History");
			BasicDBObject searchQuery = new BasicDBObject();
			
			//TODO: timestamp??????
			searchQuery.put("username", username);
			
			DBCursor cursor = collection.find(searchQuery); //find history per user
			DBObject timeObj = new BasicDBObject("timestamp", -1);
		    cursor.sort(timeObj); //sort results (a value of 1 or -1 to specify an ascending or descending sort respectively.)

		    HistoryPair[] historyPairs = new HistoryPair[cursor.size()];
			int i = 0;
			while (cursor.hasNext()) {
				DBObject tempObj = cursor.next();
				HistoryPair temp = new HistoryPair((String)tempObj.get("title"),
						new Date((long)tempObj.get("timestamp")));
				historyPairs[i] = temp;
				i++;
			}
			

			mongoClient.close();
			return historyPairs;
			
		}catch(Exception ex) {
			System.out.println(ex);
		}
		
		
		return new HistoryPair[]{hp};
	}
	
	
	/**
	 * The function retrieves  items' history
	 * The function return array of pairs <username,viewtime> sorted by VIEWTIME in descending order
	 * @param title
	 * @return
	 */
	@RequestMapping(value = "get_history_by_items",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(HistoryPair.class)
	public  HistoryPair[] getHistoryByItems(@RequestParam("entity")    String title){
		//:TODO your implementation
		HistoryPair hp = new HistoryPair("aa", new Date());
		System.out.println("ByItem "+hp);
		
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("History");
			BasicDBObject searchQuery = new BasicDBObject();
			
			//TODO: timestamp??????
			searchQuery.put("title", title);
			
			DBCursor cursor = collection.find(searchQuery); //find history per user
			DBObject timeObj = new BasicDBObject("timestamp", -1);
		    cursor.sort(timeObj); //sort results (a value of 1 or -1 to specify an ascending or descending sort respectively.)

		    HistoryPair[] historyPairs = new HistoryPair[cursor.size()];
			int i = 0;
			while (cursor.hasNext()) {
				DBObject tempObj = cursor.next();
				HistoryPair temp = new HistoryPair((String)tempObj.get("username"),
						new Date((long)tempObj.get("timestamp")));
				historyPairs[i] = temp;
				i++;
			}
			

			mongoClient.close();
			return historyPairs;
			
		}catch(Exception ex) {
			System.out.println(ex);
		}
		
		return new HistoryPair[0];
	}
	
	/**
	 * The function retrieves all the  users that have viewed the given item
	 * @param title
	 * @return
	 */
	@RequestMapping(value = "get_users_by_item",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(HistoryPair.class)
	public  User[] getUsersByItem(@RequestParam("title") String title){
		//:TODO your implementation
		if(title == null ||title.isEmpty()) {
			return new User[0];
		}
		
		HistoryPair[] historyPairs = getHistoryByItems(title);
		if(historyPairs.length == 0) {
			return new User[0];
		}
		
		try(
				MongoClient mongoClient = new MongoClient("localhost", 27017);
		   ){
			
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("Registration");
			User[] users = new User[historyPairs.length];
			
			for(int i = 0; i < historyPairs.length; i++) {
				BasicDBObject searchQuery = new BasicDBObject();
				searchQuery.put("username", historyPairs[i].getCredentials());
				DBCursor cursor = collection.find(searchQuery); //find user per title
				
				DBObject userData = cursor.next();
				users[i] = new User((String)userData.get("username"), (String)userData.get("firstname"), 
						(String)userData.get("lastname"));
			
			}
			return users;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		
//		User hp = new User("aa","aa","aa");
//		System.out.println(hp);
		return new User[0];
	}
	
	/**
	 * The function calculates the similarity score using Jaccard similarity function:
	 *  sim(i,j) = |U(i) intersection U(j)|/|U(i) union U(j)|,
	 *  where U(i) is the set of usernames which exist in the history of the item i.
	 * @param title1
	 * @param title2
	 * @return
	 */
	@RequestMapping(value = "get_items_similarity",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	public double  getItemsSimilarity(@RequestParam("title1") String title1,
			@RequestParam("title2") String title2){
		//:TODO your implementation
		double ret = 0.0;
		
		User[] usersObj1 = getUsersByItem(title1);
		User[] usersObj2 = getUsersByItem(title2);
		
		ArrayList<String> users1 = new ArrayList<String>();
		ArrayList<String> users2 = new ArrayList<String>();
		
		// get usernames to arrays of string names
		for (User user : usersObj1){
			users1.add(user.getUsername());
		}
		
		for (User user : usersObj2){
			users2.add(user.getUsername());
		}
		
		// Intersection list
		Set<String> intersectionSet = users1.stream()
				  .distinct()
				  .filter(users2::contains)
				  .collect(Collectors.toSet());
		
		// get sets of users by two titles 
		Set<String> U1 = new HashSet<String>(users1);
		Set<String> U2 = new HashSet<String>(users2);
		
		U1.addAll(U2); // union
		
		if(U1.size() == 0) {
			return ret;
		}
		
		ret = ((double) intersectionSet.size()) / ((double) U1.size());
		
		return ret;
	}
	
	
	private boolean isExistUser(String username){
		System.out.println(username);
		//:TODO your implementation
		//https://www.baeldung.com/java-mongodb
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("Registration");
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("username", username);
			DBCursor cursor = collection.find(searchQuery);

			while (cursor.hasNext()) {
			    return true;
			}
			
			mongoClient.close();
		}catch(Exception ex) {
			System.out.println(ex);
		}
		
		return false;
		
	}
	
	private boolean isExistTitle(String title){
		System.out.println(title);
		//:TODO your implementation
		//https://www.baeldung.com/java-mongodb
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("MediaItems");
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("title", title);
			DBCursor cursor = collection.find(searchQuery);

			while (cursor.hasNext()) {
			    return true;
			}
			
			mongoClient.close();
		}catch(Exception ex) {
			System.out.println(ex);
		}
		
		return false;
		
	}
	

}
