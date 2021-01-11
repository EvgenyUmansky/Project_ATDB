/**
 * 
 */
package org.bgu.ise.ddb.registration;



import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletResponse;
import org.bgu.ise.ddb.ParentController;
import org.bgu.ise.ddb.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.MongoClient;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;









import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.swing.text.Document;

import org.apache.tomcat.jni.Local;
import org.bgu.ise.ddb.ParentController;
import org.bgu.ise.ddb.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author Alex
 *
 */
@RestController
@RequestMapping(value = "/registration")
public class RegistarationController extends ParentController{
	
	
	/**
	 * The function checks if the username exist,
	 * in case of positive answer HttpStatus in HttpServletResponse should be set to HttpStatus.CONFLICT,
	 * else insert the user to the system  and set to HttpStatus in HttpServletResponse HttpStatus.OK
	 * @param username
	 * @param password
	 * @param firstName
	 * @param lastName
	 * @param response
	 */
	@RequestMapping(value = "register_new_customer", method={RequestMethod.POST})
	public void registerNewUser(@RequestParam("username") String username,
			@RequestParam("password")    String password,
			@RequestParam("firstName")   String firstName,
			@RequestParam("lastName")  String lastName,
			HttpServletResponse response){
		System.out.println(username+" "+password+" "+lastName+" "+firstName);
		
		try {
			if(isExistUser(username)) {
				HttpStatus status = HttpStatus.CONFLICT;
				response.setStatus(status.value());
				return;
			}
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("Registration");
			
			BasicDBObject addQuery = new BasicDBObject();
			addQuery.put("username", username);
			addQuery.put("firstname", firstName);
			addQuery.put("lastname", lastName);
			addQuery.put("password", password);
			addQuery.put("date", LocalDate.now()); // to use for retrieve users from last n days
			
			collection.insert(addQuery);

			
			mongoClient.close();
			
			
			HttpStatus status = HttpStatus.OK;
			response.setStatus(status.value());
			
		}catch(Exception ex) {
			System.out.println(ex);
		}
	
		
	}
	
	/**
	 * The function returns true if the received username exist in the system otherwise false
	 * @param username
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "is_exist_user", method={RequestMethod.GET})
	public boolean isExistUser(@RequestParam("username") String username) throws IOException{
		System.out.println(username);
		//:TODO your implementation
		//https://www.baeldung.com/java-mongodb
		try (MongoClient mongoClient = new MongoClient("localhost", 27017);){
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("Registration");
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("username", username);
			DBCursor cursor = collection.find(searchQuery);

			while (cursor.hasNext()) {
			    return true;
			}
		}catch(Exception ex) {
			System.out.println(ex);
		}
		
		return false;
		
	}
	
	/**
	 * The function returns true if the received username and password match a system storage entry, otherwise false
	 * @param username
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "validate_user", method={RequestMethod.POST})
	public boolean validateUser(@RequestParam("username") String username,
			@RequestParam("password")    String password) throws IOException{
		System.out.println(username+" "+password);
		//:TODO your implementation
		
		try(MongoClient mongoClient = new MongoClient("localhost", 27017);) {
			
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("Registration");
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("username", username);
			searchQuery.put("password", password);
			DBCursor cursor = collection.find(searchQuery);

			while (cursor.hasNext()) {
			    return true;
			}
		}catch(Exception ex) {
			System.out.println(ex);
		}
		
		return false;
		
	}
	
	/**
	 * The function retrieves number of the registered users in the past n days
	 * @param days
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "get_number_of_registred_users", method={RequestMethod.GET})
	public int getNumberOfRegistredUsers(@RequestParam("days") int days) throws IOException{
		System.out.println(days+"");
		int result = 0;
		//:TODO your implementation
		try(MongoClient mongoClient = new MongoClient("localhost", 27017);){
			
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime date = now.minusDays(days);
			
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("Registration");
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("date", new BasicDBObject("$gt", date));
			DBCursor cursor = collection.find(searchQuery);

			while (cursor.hasNext()) {
				result++;
				cursor.next();
			}	
		}catch(Exception ex) {
			System.out.println(ex);
		}
	
		return result;
		
	}
	
	/**
	 * The function retrieves all the users
	 * @return
	 */
	@RequestMapping(value = "get_all_users",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(User.class)
	public  User[] getAllUsers(){
		//:TODO your implementation
		User u = new User("alex", "alex", "alex");
		System.out.println(u);
		
		try(MongoClient mongoClient = new MongoClient("localhost", 27017);){
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("Registration");
			DBCursor cursor = collection.find(); // all users
			User[] users = new User[cursor.size()];
			int i = 0;
			while (cursor.hasNext()) {
				DBObject tempObj = cursor.next();
				User temp = new User((String)tempObj.get("username"),
						(String)tempObj.get("firstname"),(String)tempObj.get("lastname"));
				users[i] = temp;
				i++;
			}
		
			return users;
			
		}catch(Exception ex) {
			System.out.println(ex);
		}
		
		return new User[]{u};
	}

}
