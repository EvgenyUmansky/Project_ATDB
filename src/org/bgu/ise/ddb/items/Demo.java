package org.bgu.ise.ddb.items;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class Demo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("ProjectATDB");
			DBCollection collection = db.getCollection("MediaItems");
			int x = 0;
		}
		catch(Exception e) {
			System.out.print(e);
		}
	}

}
