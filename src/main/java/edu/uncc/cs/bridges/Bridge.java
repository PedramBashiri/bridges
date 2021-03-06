package edu.uncc.cs.bridges;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import sun.security.pkcs11.wrapper.Functions;


/**
 * Connection to the Bridges server.
 * 
 * Initialize this class before using it, and call complete() afterward.
 * 
 * @author Sean Gallagher
 */
public class Bridge {
	
	
	private static int assignment;
	private static String key;
	private static Visualizer visualizer;
	private static BridgeNetwork backend;
	private static String userName;	
	
	// This exists to prevent duplicate error traces.
	public static String getUserName() {
		return userName;
	}

	public static void setUserName(String userName) {
		Bridge.userName = userName;
	}

	private static boolean failsafe = false;

	/**
	 * Initialize Bridges with Visualizer
	 * @param assignment  The assignment number, for grading
	 * @param visualizer  The visualizer, for assignment
	 * @param username TODO
	 */
	public static void init(int assignment, String key, Visualizer visualizer, String username) {
		Bridge.assignment = assignment;
		Bridge.key = key;
		Bridge.visualizer = visualizer;
		Bridge.backend = new BridgeNetwork();
		Bridge.userName = username;
	}
	
	/* Accessors and Mutators */

	public static int getAssignment() {
		return assignment;
	}
	public static void setAssignment(int assignment) {
		Bridge.assignment = assignment;
	}
	
	public static String getKey() {
		return key;
	}
	public static void setKey(String key) {
		Bridge.key = key;
	}

	public static String getServerURL() {
		return backend.server_url;
	}
	public static void setServerURL(String server_url) {
		Bridge.backend.server_url = server_url;
	}

	public static Visualizer getVisualizer() {
		return visualizer;
	}
	public static void setVisualizer(Visualizer visualizer) {
		Bridge.visualizer = visualizer;
	}
	
	/**
	 * Update visualization metadata. This may be called many times.
	 * This is usually an expensive operation and involves connecting to the network.
	 * Calling this method is optional provided you call complete()
	 */
	public static void update() {
        try {
        	System.out.println(visualizer.getRepresentation());
			backend.post("/assignments/" + assignment, visualizer.getRepresentation());
		} catch (IOException e) {
			System.err.println("There was a problem sending the visualization"
					+ " representation to the server. Are you connected to the"
					+ " Internet? Check your network settings. Otherwise, maybe"
					+ " the central Bridges server is down. Try again later.\n"
					+ e.getMessage());
		} catch (RateLimitException e) {
			System.err.println("There was a problem sending the visualization"
					+ " representation to the server. However, it responded with"
					+ " an impossible 'RateLimitException'. Please contact"
					+ " Bridges developers and file a bug report; this error"
					+ " should not be possible.\n"
					+ e.getMessage());
		} 
        // Return a URL to the user
        System.out.println("Check out your visuals at " + backend.prepare("/assignments/" + assignment + "/" + userName) );
	}

	/**
	 * Close down Bridges.
	 * 
	 * This only calls update() but it could conceivably do more.
	 */
	public static void complete() {
		update();
	}
	
	
	// Internal utility methods
	
	/**
	 * Internal method for JSON preparation
	 * @param in 	The original string
	 * @return a string with all but the last character
	 */
	public static StringBuilder trimComma(StringBuilder in) {
		if (in.length() > 0 && in.charAt(in.length()-1) == ',')
			in.deleteCharAt(in.length()-1);
		return in;
	}
	
	/**
	 * Internal method for JSON preparation
	 * @param in	The original String
	 * @return the string, encapsulated in quotes
	 */
	static String quote(String in) {
		return String.format("\"%s\"", in);
	}
	
	/**
	 * Internal method for JSON preparation
	 * @return a string with all but the first and last characters
	 */
	static String unquote(String in) {
		return in.substring(
				Math.min(in.length()-1, 1),
				Math.max(in.length()-1, 0));
	}
	
	/**
	 * Idiom for enabling ordered iteration on any map.
	 * The reason for this is to make the strings compare equal for testing
	 * @param values
	 * @return
	 */
	static <T extends Comparable<T>> List<T> sorted_values(
			Map<String, T> values) {
		List<T> sorted_values = new ArrayList<>(values.values());
		Collections.sort(sorted_values);
		return sorted_values;
	}
	
	/**
	 * Idiom for enabling ordered iteration on any map.
	 * The reason for this is to make the strings compare equal for testing
	 * @param values
	 * @return
	 */
	static <K extends Comparable<K>, V> List<Entry<K, V>> sorted_entries(
			Map<K, V> map) {
		List<Entry<K, V>> sorted_entries = new ArrayList<>(map.entrySet());
		Collections.sort(sorted_entries, new Comparator<Entry<K, V>>() {
			public int compare(Entry<K, V> t, Entry<K, V> o) {
				return t.getKey().compareTo(o.getKey());
			}
		});
		return sorted_entries;
	}
	
    /**
     * 	Automatically choose whether to open a node identifier with:
     *  Twitter via followers(),
     *  TMDb with movies(), or
     *  RottenTomatoes with actors()
     * 
     * @param identifier
     * @param max
     * @returns a list of identifiers
     * @throws QueryLimitException
     */
   
    /**
     * This Method returns the list of followers 
     * @param identifier holds the name of the 
     * @param max holds the max number of followers
     * @return
     */
    	public static List<Follower> getAssociations(Follower identifier, int max){
        	try {
        		return followers(identifier, max);
    	    }
        		catch (RateLimitException e) {
        		return new ArrayList<>();
        	}
    }
    	

    	
    	/**
         * This Method returns the list of tweets
         * @param identifier holds the name of the 
         * @param max holds the max number of tweets
         * @return
         */
        	public static List<Tweet> getAssociations(TwitterAccount identifier, int max){
            	try {
            		return getTwitterTimeline(identifier, max);
        	    }
            		catch (RateLimitException e) {
            		return new ArrayList<>();
            	}
        }
        	
        	public static List<EarthquakeTweet> convertTweet(List<Tweet> aList){
        		List<EarthquakeTweet> earthquakes = new ArrayList<>();
        		for (int i =0; i<aList.size();i++){
        			Tweet aTweet = aList.get(i);
        			EarthquakeTweet anEarthquake = new EarthquakeTweet(aTweet);
        			earthquakes.add(anEarthquake);
        			}
        		return earthquakes;
        	} 
    
    	/**
         * This Method returns the list of actors 
         * @param identifier holds the name of the movie
         * @param max holds the max number of actors
         * @return
         */
        	public static List<Actor> getAssociations(Actor identifier, int max){
            	try {
            		return actors(identifier, max);
        	    }
            		catch (RateLimitException e) {
            		return new ArrayList<>();
            	}
        }
        	
        	
    	/**
         * This Method returns the list of movies 
         * @param identifier holds the name of the movie
         * @param max holds the max number of movies
         * @return
         */
        	public static List<Movie> getAssociations(Movie identifier, int max){
            	try {
            		return movies(identifier, max);
        	    }
            		catch (RateLimitException e) {
            		return new ArrayList<>();
            	}
        }
	
    /** List the user's followers as more FollowGraphNodes.
        Limit the result to `max` followers. Note that results are batched, so
        a large `max` (as high as 200) _may_ only count as one request.
        See Bridges.followgraph() for more about rate limiting. 
     * @throws IOException */
    static List<Follower> followers(Follower id, int max)
    		throws RateLimitException {
    	if (failsafe) {
    		// Don't contact Twitter, use sample data
    		return SampleDataGenerator.getFriends(id.getName(), max);
    	} else {
	    	try {
	    		//either timeline or followers
		    	String resp = backend.get("/streams/twitter.com/followers/"
		    			+ id.getName() + "/" + max);
		    		//System.out.println("the resp: "+resp);
		        JSONObject response = backend.asJSONObject(resp);
		        JSONArray followers = (JSONArray) backend.safeJSONTraverse(
		        		"['followers']", response, JSONArray.class);
		        List<Follower> results = new ArrayList<>();
		    	for (Object follower : followers) {
		    		String name = (String) backend.safeJSONTraverse(
		    				"", follower, String.class);
		    		results.add(new Follower(name));
		    	}
		    	return results;
	    	} catch (IOException e) {
	    		// Trigger failsafe.
	    		System.err.println("Warning: Trouble contacting Bridges. Using "
	    				+ "sample data instead.\n"
	    				+ e.getMessage());
	    		failsafe = true;
	    		return followers(id, max);
	    	}
    	}
    }
    
    /**
     * List the user's followers as more FollowGraphNodes.
     * Limit the result to `max` followers. Note that results are batched, so 
     * a large `max` (as high as 200) _may_ only count as one request.
     * See Bridges.followgraph() for more about rate limiting. 
     * @throws IOException */
	static List<Tweet> getTwitterTimeline(TwitterAccount id, int max)
			throws RateLimitException {
		if (failsafe) {
			// Don't contact Twitter, use sample data
			return SampleDataGenerator.getTwitterTimeline(id.getName(), max);
		} else {
	    	try {
		    	String resp = backend.get("/streams/twitter.com/timeline/"
		    			+ id.getName() + "/" + max);
		        JSONObject response = backend.asJSONObject(resp);
		        JSONArray tweets_json = (JSONArray) backend.safeJSONTraverse(
		        		"['tweets']", response, JSONArray.class);
		        
		        List<Tweet> results = new ArrayList<>();
		    	for (Object tweet_json : tweets_json) {
		    		String content = (String) backend.safeJSONTraverse(
		    				"['tweet']", tweet_json, String.class);
		    		String date_str = (String) backend.safeJSONTraverse(
		    				"['date']", tweet_json, String.class);
		    		
		    		// TODO: When Java 8 is common enough, switch this to ZonedDateTime.parse()
		    		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		    		Date date;
		    		try {
						date = df.parse(date_str);
					} catch (ParseException e) {
						date = new Date();
					}
		    		results.add(new Tweet(content, date));
		    	}
		    	return results;
	    	} catch (IOException e) {
	    		// Trigger failsafe.
	    		System.err.println("Warning: Trouble contacting Bridges. Using "
	    				+ "sample data instead.\n"
	    				+ e.getMessage());
	    		failsafe = true;
	    		return getTwitterTimeline(id, max);
	    	}
		}
	}
    
    /**
     * Return a list of movies an actor played in.
     * 
     * The data comes courtesy of RottenTomatoes.
     * 
     * The quota for this resource is about 10k actors/day but is shared by all
     * students. So if you consume all 10k, it will be a bad day. Please make
     * sure you limit your queries appropriately.
     * 
     */
    static List<Movie> movies(Movie id, int max)
    		throws RateLimitException {

    	if (failsafe) {
    		// Don't contact Bridges, use sample data
    		return SampleDataGenerator.getMovies(id.getName(), max);
    	} else {
	    	try {
		    	String resp = backend.get("/streams/actors/" + id.getName());
		    	JSONArray movies = backend.asJSONArray(resp);
		    	
		        // Get (in JS) movies_json.map(function(m) { return m.title; })
		        List<Movie> results = new ArrayList<>();
		        for (Object movie : movies) {
		        	String title = (String) backend.safeJSONTraverse("['title']",
		        			movie, String.class);
		        	results.add(new Movie(title));
		        }
		        return results;
	    	} catch (IOException e) {
	    		// Trigger failsafe.
	    		System.err.println("Warning: Trouble contacting Bridges. Using "
	    				+ "sample data instead.\n"
	    				+ e.getMessage());
	    		failsafe = true;
	    		return movies(id, max);
	    	}
    	}
    }
    
    /**
     * Return the actors that played in a movie.
     * 
     * The data comes courtesy of TMDb.
     * 
     * This resource has unlimited queries but has caveats. Not every extra
     * that played in every movie ever is listed in the database and some
     * movies are documented rather sparsely. Expect some to be missing.
     * @throws IOException 
     * @throws RateLimitException 
     */
    static List<Actor> actors(Actor id, int max)
    		throws RateLimitException {

    	if (failsafe) {
    		// Don't contact Bridges, use sample data
    		return SampleDataGenerator.getCast(id.getName(), max);
    	} else {
	    	try {
		    	String resp = backend.get("/streams/rottentomatoes.com/" + id.getName());
		    	JSONArray movies = backend.asJSONArray(resp);
		    	
		        // We will assume that the first movie is the right one
		    	JSONArray abridged_cast = (JSONArray) backend.safeJSONTraverse(
		    			"[0]['abridged_cast']", movies, JSONArray.class);
		    	List<Actor> results = new ArrayList<>();
		    	for (Object cast_member : abridged_cast) {
		    		if (results.size() == max)
		    			break;
		    		String name = (String) backend.safeJSONTraverse("['name']",
		    				cast_member, String.class);
					results.add(new Actor(name));
		    	}
		    	return results;
	    	} catch (IOException e) {
	    		// Trigger failsafe.
	    		System.err.println("Warning: Trouble contacting Bridges. Using "
	    				+ "sample data instead.\n"
	    				+ e.getMessage());
	    		failsafe = true;
	    		return actors(id, max);
	    	}
    	}
    }
    
    /**
     * Generate a sample Edge weight for two nodes
     * @param source
     * @param target
     * @return
     */
    public static double getEdgeWeight(String source, String target) {
    	int h = source.hashCode() + target.hashCode();
    	if (h < 0) h = -h;
    	return h % 10;
    }
}

class Ident {
	public String provider;
	public String name;
	
	/**
	 * Create a new Ident from an identifier string with a provider
	 * @param identifier
	 */
	public Ident(String identifier) {
    	if (identifier.contains("/")) {
    		String[] halves = identifier.split("/", 2);
    		provider = halves[0];
    		name = halves[1];
    	} else {
    		throw new RuntimeException("Provider or screenname missing in "
    				+ identifier + ". Should look like: example.com/username");
    	}
	}
	
	/**
	 * Create a plain, straightforward Ident with the provider and name as given.
	 * @param provider
	 * @param name
	 */
	public Ident(String provider, String name) {
		this.provider = provider;
		this.name = name;
	}
	
	public static Ident fromAnyString(String identifier) {
    	if (identifier.contains("/")) {
    		return new Ident(identifier);
    	} else {
    		return new Ident("", identifier);
    	}
	}
}
