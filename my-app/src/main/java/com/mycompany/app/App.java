package com.mycompany.app;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.geotools.geometry.jts.GeometryClipper;

import java.awt.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import processing.core.PVector;

//javac -cp .;* calculateFeatures.java
//java -cp .;* calculateFeatures

public class App {
	//indication of the amount of frames per match, edited in getmatchdetails
	public static int frames = 138570;
	public static Connection con;
	public static Statement stmt;
	
	public static Connection getConnection() {
		String username = "root";
		String password = "";
		String host = "127.0.0.1";
		String port = "3306";

		System.out.println("connecting to database using:"+ username +" "+ password + " "+ host+" "+ port);

		Properties props = new Properties();
		props.put("user", username);
		props.put("password", password);

		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(
			"jdbc:mysql://" + host + ":" + port + "/psv2?autoReconnect=true&useSSL=false",
			props);
			System.out.println("connected to database");
			return conn;
		} catch (ClassNotFoundException|SQLException e) {
			System.out.println("couldn't connect to database. exiting...");
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
	
	//posession per frame, team 1 or team 2
	public static int[] ballpossession = new int[frames];
	public static double[][] ballDistanceToTeam = new double[2][frames];
	public static double[] PlayersTotalDistance = new double[frames];
	public static double[] directOpponentDistance = new double[frames];
	public static double[] directOpponentWholeMatchDistance = new double[frames];
	public static double[][] ballDistanceTo1ClossestPlayers = new double[2][frames];
	public static double[][] ballDistanceTo2ClossestPlayers = new double[2][frames];
	public static double[][] ballDistanceTo3ClossestPlayers = new double[2][frames];
	public static double[][] ballDistanceTo4ClossestPlayers = new double[2][frames];
	public static double[][] ballDistanceTo5ClossestPlayers = new double[2][frames];
	public static double[][] ballDistanceTo6ClossestPlayers = new double[2][frames];
	public static double[] playerDistanceTo1ClossestPlayers = new double[frames];
	public static double[] playerDistanceTo2ClossestPlayers = new double[frames];
	public static double[] playerDistanceTo3ClossestPlayers = new double[frames];
	public static double[] playerDistanceTo4ClossestPlayers = new double[frames];
	public static double[] playerDistanceTo5ClossestPlayers = new double[frames];
	public static double[] playerDistanceTo6ClossestPlayers = new double[frames];
	public static double[] ballDistanceToOpponentGoal = new double[frames];
	public static int[] defendingPlayersWithin5Meters = new int[frames];
	public static int[] defendingPlayersWithin10Meters = new int[frames];
	public static int[] defendingPlayersWithin15Meters = new int[frames];
	public static int[] defendingPlayersWithin20Meters = new int[frames];
	public static int[] freePlayers2meters = new int[frames];
	public static int[] freePlayers3meters = new int[frames];
	public static int[] freePlayers4meters = new int[frames];
	public static int[] freePlayers5meters = new int[frames];
	public static int[] freePlayers6meters = new int[frames];
	public static int[] freePlayers7meters = new int[frames];
	public static double[] vonoroiSurfaceTeam1 = new double[frames];
	public static double[] vonoroiSurfaceTeam2 = new double[frames];
	public static double[] vonoroiSurfaceBall = new double[frames];
	public static double[] playerInPossessionPossition = new double[frames];
	public static String[] targetB = new String[frames];
	public static String[] targetB25= new String[frames];
	public static int[] target1 = new int[frames]; //als balverovering = 1, anders 0
	public static int[] target10 = new int[frames]; //tot 10/25e seconden voor balverovering = 1, anders 0
	public static int[] target25 = new int[frames]; //tot 1seconden voor balverovering = 1 anders 0
	public static int[] target50 = new int[frames]; //tot 2seconden voor balverovering = 1 anders 0
	public static int[] target100= new int[frames]; //tot 4 seconden voor balverovering = 1 anders 0
	public static int[] target25_100 = new int[frames]; //van 1 tot 4 seconden voor balverovering = 1 anders 0
	
	
	public static String getValues(int frame){
		String line = ""+(frame+1);
		line += "," + ballpossession[frame];
		line += "," + (float)ballDistanceToTeam[0][frame];
		line += "," + (float)ballDistanceToTeam[1][frame];
		line += "," + (float)ballDistanceTo1ClossestPlayers[0][frame];
		line += "," + (float)ballDistanceTo1ClossestPlayers[1][frame];
		line += "," + (float)ballDistanceTo2ClossestPlayers[0][frame];
		line += "," + (float)ballDistanceTo2ClossestPlayers[1][frame];
		line += "," + (float)ballDistanceTo3ClossestPlayers[0][frame];
		line += "," + (float)ballDistanceTo3ClossestPlayers[1][frame];
		line += "," + (float)ballDistanceTo4ClossestPlayers[0][frame];
		line += "," + (float)ballDistanceTo4ClossestPlayers[1][frame];
		line += "," + (float)ballDistanceTo5ClossestPlayers[0][frame];
		line += "," + (float)ballDistanceTo5ClossestPlayers[1][frame];
		line += "," + (float)ballDistanceTo6ClossestPlayers[0][frame];
		line += "," + (float)ballDistanceTo6ClossestPlayers[1][frame];
		line += "," + (float)PlayersTotalDistance[frame];
		line += "," + (float)directOpponentDistance[frame];
		line += "," + (float)directOpponentWholeMatchDistance[frame];
		line += "," + (float)playerDistanceTo1ClossestPlayers[frame];
		line += "," + (float)playerDistanceTo2ClossestPlayers[frame];
		line += "," + (float)playerDistanceTo3ClossestPlayers[frame];
		line += "," + (float)playerDistanceTo4ClossestPlayers[frame];
		line += "," + (float)playerDistanceTo5ClossestPlayers[frame];
		line += "," + (float)playerDistanceTo6ClossestPlayers[frame];
		line += "," + (float)ballDistanceToOpponentGoal[frame];
		line += "," + defendingPlayersWithin5Meters[frame];
		line += "," + defendingPlayersWithin10Meters[frame];
		line += "," + defendingPlayersWithin15Meters[frame];
		line += "," + defendingPlayersWithin20Meters[frame];
		line += "," + freePlayers2meters[frame];
		line += "," + freePlayers3meters[frame];
		line += "," + freePlayers4meters[frame];
		line += "," + freePlayers5meters[frame];
		line += "," + freePlayers6meters[frame];
		line += "," + freePlayers7meters[frame];
		line += "," + (float)vonoroiSurfaceTeam1[frame];
		line += "," + (float)vonoroiSurfaceTeam2[frame];
		line += "," + (float)vonoroiSurfaceBall[frame];
		line += "," + (float)playerInPossessionPossition[frame];
		line += "," + targetB[frame];
		line += "," + targetB25[frame];
		line += "," + target1[frame];
		line += "," + target10[frame];
		line += "," + target25[frame];
		line += "," + target50[frame];
		line += "," + target100[frame];
		line += "," + target25_100[frame];
		return line;
	}
	
	//insert players_distances into database
	public static void insertPressureMeasurements() {
		System.out.println("insertPressureMeasurements");
		try {
			con = getConnection();
			stmt = con.createStatement();
			for (int frame = 0; frame<frames; frame++) {
				String sql = "INSERT INTO pressure_measurements2 VALUES("+getValues(frame)+")";
				//System.out.println(sql);
				stmt.executeUpdate(sql);
			}
			stmt.close();
			con.close();
		} catch (SQLException err) {
			System.out.println( err.getMessage());
		}
	}
	
	//position of the ball per frame
	public static PVector[] ballpositions = new PVector[frames];
	public static void getball() {
		System.out.println("Get ball");
		try {
			con = getConnection();
			stmt = con.createStatement();

			String sql = "SELECT ball_measurements.x, ball_measurements.y, ball_measurements.z, ball_measurements.possession, frames.frame_id " +
			"FROM ball_measurements, frames " +
			"WHERE ball_measurements.frame_id = frames.frame_id " +
			"ORDER BY frame_id";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				float data_x = rs.getFloat("x");
				float data_y = rs.getFloat("y");
				float data_z = rs.getFloat("z");
				int i = rs.getInt("frame_id") - 1;

				ballpositions[i] = convert(data_x, data_y, data_z);
				ballpossession[i] = rs.getInt("possession");
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (SQLException err) {
			System.out.println(err.getMessage());
		}
	}
	
	/* NOTE: player positions are stored as player-then-frame instead of the intuitive frame-then-player!! */
	//possitions of the players in the field. this is in meters. left upper corner = 0.0 PVectors have an x and y
	public static PVector[][] playersPos = new PVector[22][frames];
	//get players data from the database
	public static void getPlayers() {
		System.out.println("Get players");
		try {
			con = getConnection();
			stmt = con.createStatement();
			String sql = "SELECT player_measurements.player_id, player_measurements.x, player_measurements.y, frames.frame_id " +
			"FROM player_measurements, frames " +
			"WHERE player_measurements.frame_id = frames.frame_id " +
			"ORDER BY frames.frame_id";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				float data_x = rs.getFloat("x");
				float data_y = rs.getFloat("y");
				int data_player_id = rs.getInt("player_id");
				int i = rs.getInt("frame_id") - 1;
				playersPos[data_player_id - 1][i] = convert(data_x, data_y);
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (SQLException err) {
			System.out.println(err.getMessage());
		}
	}
	
	//get match details like frames, and how manny sections
	public static void getMatchDetails() {
		System.out.println("match details");
		try {
			con = getConnection();
			stmt = con.createStatement();
			String sql = "select max(frame_id) as framess from frames";
			ResultSet rs = stmt.executeQuery(sql);
			frames = 0;
			while (rs.next()) {
				frames = rs.getInt("framess")-1;
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (SQLException err) {
			System.out.println(err.getMessage());
		}
	}
	
	//To translate the data to real meters in the field. -1<~data_x<~1 & -1<~data_y<~1
	//Returns a pvector which indicates a position on the field.
	public static PVector convert(float data_x, float data_y) {
		float x = 0f;
		float y = 0f;
		if (data_x > 0) {
			x = 52.5f + 52.5f * data_x;
		} else {
			x = 52.5f + 52.5f * data_x;
		}
		if (data_y > 0) {
			y = 34f + 34f * data_y;
		} else {
			y = 34f + 34f * data_y;
		}
		return new PVector(x, y);
	}

	//To translate the data to real meters in the field. -1<~data_x<~1 & -1<~data_y<~1
	//Returns a pvector which indicates a position on the field. ball can have a height.
	public static PVector convert(float data_x, float data_y, float data_z) {
		float x = 0f;
		float y = 0f;
		if (data_x > 0) {
			x = 52.5f + 52.5f * data_x;
		} else {
			x = 52.5f + 52.5f * data_x;
		}
		if (data_y > 0) {
			y = 34f + 34f * data_y;
		} else {
			y = 34f + 34f * data_y;
		}
		return new PVector(x, y, data_z);
	}
	
	//checks if the player got a red card.
	private static boolean redcard(PVector pos1, PVector pos2) {
		PVector rodeKaart = convert(-10, -10);
		if ((rodeKaart.x == pos1.x && rodeKaart.y == pos1.y) || (rodeKaart.x == pos2.x && rodeKaart.y == pos2.y)) {
			return true;
		}
		return false;
	}

	//calculate the distance between two positions.
	//if distance between two frames from a player or ball it is also meters per 1/25th second
	//if someone has a red card the distance is 0 because he is out of the game
	private static double calculateDistance(PVector pos1, PVector pos2) {
		//only needed for players....
		if (redcard(pos1, pos2)) {
			return Math.sqrt(105*105 + 68*68);
		}
		float x = pos1.x;
		float y = pos1.y;
		float x2 = pos2.x;
		float y2 = pos2.y;
		return Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2)); //also meters per 1/25 second;
	}
	
	//distance from ball to team 1, and team 2
	public static int[] playerInPossession = new int[frames];
	public static void setPlayerInPossession(int frame, double[][] individualdistance){
		double smallestDistance = Double.MAX_VALUE; 
		int index = 23;
		if(ballpossession[frame]==1){
			for(int player = 0; player<11; player++){
				if(individualdistance[0][player] < smallestDistance && individualdistance[0][player]<2){
					smallestDistance = individualdistance[0][player];
					index = player;
				} 
			}
		}else{
			for(int player = 0; player<11; player++){
				if(individualdistance[1][player] < smallestDistance && individualdistance[1][player]<2){
					smallestDistance = individualdistance[0][player];
					index = player+11;
				} 
			}
		}
		if(index == 23){
			index = playerInPossession[frame-1];
		}
		playerInPossession[frame] = index; //speler die het dichts bij de bal is en in het team zit van de possession heeft de bal
	}
	
	public static void setBallDistanceToClossestPlayers(int frame, double[][] individualdistance){
		Arrays.sort(individualdistance[0]);
		Arrays.sort(individualdistance[1]);
		for(int player = 0; player<7; player++){
			if(player<1){
				ballDistanceTo1ClossestPlayers[0][frame] += individualdistance[0][player];
				ballDistanceTo1ClossestPlayers[1][frame] += individualdistance[1][player];
			}
			if(player<2){
				ballDistanceTo2ClossestPlayers[0][frame] += individualdistance[0][player];
				ballDistanceTo2ClossestPlayers[1][frame] += individualdistance[1][player];
			}
			if(player<3){
				ballDistanceTo3ClossestPlayers[0][frame] += individualdistance[0][player];
				ballDistanceTo3ClossestPlayers[1][frame] += individualdistance[1][player];
			}
			if(player<4){
				ballDistanceTo4ClossestPlayers[0][frame] += individualdistance[0][player];
				ballDistanceTo4ClossestPlayers[1][frame] += individualdistance[1][player];
			}
			if(player<5){
				ballDistanceTo5ClossestPlayers[0][frame] += individualdistance[0][player];
				ballDistanceTo5ClossestPlayers[1][frame] += individualdistance[1][player];
			}
			if(player<6){
				ballDistanceTo6ClossestPlayers[0][frame] += individualdistance[0][player];
				ballDistanceTo6ClossestPlayers[1][frame] += individualdistance[1][player];
			}
		}
	}
	
	//public double[][] ballDistanceToTeam = new int[2][frames];
	public static void setBallTeamDistance(){
		System.out.println("setBallTeamDistance");
		for(int frame = 1; frame<frames; frame++){
			double[][] individualdistance = new double[2][11];
			double[][] individualdistance2 = new double[2][11];
			for(int player = 0; player<22; player++){
				int team = player / 11;
				double distance = calculateDistance(playersPos[player][frame], ballpositions[frame]);
				individualdistance[team][player-team*11] = distance;
				individualdistance2[team][player-team*11] = distance;
				ballDistanceToTeam[team][frame] += distance;
			}
			setPlayerInPossession(frame, individualdistance);
			//niet omwisselen! hij sorteert de individualdistance!
			setBallDistanceToClossestPlayers(frame, individualdistance2);
		}
	}
	
	public static double[][] sumDistanceMatrices = new double[10][10];
	//optimal direct opponent per frame for 10 players, this is visa versa
	public static int[][] DirectOpponent = new int[10][frames];
	//optimal direct opponent over the whole match for 10 players, this is visa versa
	public static int[] DirectOpponentWholeMatch = new int[10];
	//make a matrix of the 10 players with their distance between the opponent team.
	//m[0][0] = team 1 player 2 to team 2 player 2
	//m[0][1] = team 1 player 2 to team 2 player 3
	//m[1][2] = team 1 player 3 to team 2 player 4
	//ect
	//keepers are not in this matrix
	public static double[][] makeDistanceMatrix(int frame) {
		double[][] distanceMatrix = new double[10][10];
		for (int i = 1; i < 11; i++) {
			for (int j = 12; j < 22; j++) {
				double distance = calculateDistance(playersPos[i][frame], playersPos[j][frame]);
				distanceMatrix[i-1][j-12] = distance;
				PlayersTotalDistance[frame] += distance;
			}
		}
		return distanceMatrix;
	}
	
	public static void setDirectOpponent(){
		System.out.println("SetDirectOpponent");
		for(int frame = 0; frame < frames; frame++){
			double[][] distancematrix = makeDistanceMatrix(frame);
			for(int i = 0; i<10; i++){
				for(int j = 0; j<10; j++){
					sumDistanceMatrices[i][j] += distancematrix[i][j];
				}
			}
			HungarianAlgorithm hungar = new HungarianAlgorithm(distancematrix);
			int[] result = hungar.execute();
			for(int player = 0; player<10; player++){
				int opponent = result[player];
				DirectOpponent[player][frame] = opponent+12; //skip the keeper and first team
				directOpponentDistance[frame] += distancematrix[player][opponent];
			}
		}
		HungarianAlgorithm hungar = new HungarianAlgorithm(sumDistanceMatrices);
		int[] result = hungar.execute();
		for(int player = 0; player<10; player++){
			DirectOpponentWholeMatch[player] = result[player]+12; //skip the keeper and first team
		}
	}
	
	public static void setWholeMatchOpponentDistance(){
		System.out.println("setWholeMatchOpponentDistance");
		for(int frame = 0; frame < frames; frame++){
			for(int player = 0; player<10; player++){
				int opponent = DirectOpponentWholeMatch[player];
				directOpponentWholeMatchDistance[frame] += calculateDistance(playersPos[player+1][frame], playersPos[opponent][frame]);
			}
		}
	}
	
	public static void setTarget(){
		System.out.println("setTarget");
		for(int frame = 1; frame<frames; frame++){
			if(ballpossession[frame] != ballpossession[frame-1]){
				target1[frame] = 1;
				targetB[frame] = "'a'";
				targetB25[frame] = "'a'";
				for(int i=0; i<10; i++){
					target10[frame-i] = 1;
				}
				for(int i=0; i<25; i++){
					target25[frame-i] = 1;
					targetB25[frame-i] = "'a'";
				}
				for(int i=0; i<50; i++){
					target50[frame-i] = 1;
				}
				for(int i=0; i<100;i++){
					target100[frame-i] = 1;
				}
				for(int i=25; i<100; i++){
					target25_100[frame-i] = 1;
				}
			}else{
				target1[frame] = 0;
				targetB[frame] = "'b'";
				targetB25[frame] = "'b'";
			}
		}
	}
	
	public static int getOpponent(int index, int frame){
		if(index>=12){
			for(int i = 0; i<10; i++){
				if(DirectOpponent[i][frame] == index){
					return i+1;
				}
			}
		}
		return DirectOpponent[index-1][frame];
	}
	
	public static void setPlayerToPlayerDistance(){
		System.out.println("setPlayerToPlayerDistance");
		for(int frame = 0; frame<frames; frame++){
			int possessionplayer = playerInPossession[frame];
			int correct = 1;
			if(possessionplayer<11){
				correct = 12;
			}
			double distance[] = new double[10];
			double distance2[] = new double[10];
			for(int player=correct; player<correct+10; player++){
				double x = calculateDistance(playersPos[player][frame], ballpositions[frame]);
				distance[player-correct] = x;
				distance2[player-correct] = x;
			}
			Arrays.sort(distance);
			int index1 = 0, index2 = 0, index3 = 0, index4 = 0, index5 = 0, index6 = 0;
			for(int i = 0; i<10; i++){
				if(distance2[i] == distance[0])
					index1 = i+correct;
				if(distance2[i] == distance[1])
					index2 = i+correct;
				if(distance2[i] == distance[2])
					index3 = i+correct;
				if(distance2[i] == distance[3])
					index4 = i+correct;
				if(distance2[i] == distance[4])
					index5 = i+correct;
				if(distance2[i] == distance[5])
					index6 = i+correct;
			}
			int opponent = getOpponent(index1, frame);
			playerDistanceTo1ClossestPlayers[frame] = calculateDistance(playersPos[index1][frame], playersPos[opponent][frame]);
			opponent = getOpponent(index2, frame);
			playerDistanceTo2ClossestPlayers[frame] = playerDistanceTo1ClossestPlayers[frame] + calculateDistance(playersPos[index2][frame], playersPos[opponent][frame]);
			opponent = getOpponent(index3, frame);
			playerDistanceTo3ClossestPlayers[frame] = playerDistanceTo2ClossestPlayers[frame] + calculateDistance(playersPos[index3][frame], playersPos[opponent][frame]);
			opponent = getOpponent(index4, frame);
			playerDistanceTo4ClossestPlayers[frame] = playerDistanceTo3ClossestPlayers[frame] + calculateDistance(playersPos[index4][frame], playersPos[opponent][frame]);
			opponent = getOpponent(index5, frame);
			playerDistanceTo5ClossestPlayers[frame] = playerDistanceTo4ClossestPlayers[frame] + calculateDistance(playersPos[index5][frame], playersPos[opponent][frame]);
			opponent = getOpponent(index6, frame);
			playerDistanceTo6ClossestPlayers[frame] = playerDistanceTo5ClossestPlayers[frame] + calculateDistance(playersPos[index6][frame], playersPos[opponent][frame]);
		}
	}
	
	public static void setDistanceToOpponentGoal(){
		System.out.println("setDistanceToOpponentGoal");
		for(int frame = 0; frame<frames; frame++){
			PVector goal1 = convert(1,0);
			PVector goal2 = convert(-1,0);
			double distance1 = calculateDistance(goal1, playersPos[0][frame]);
			double distance2 = calculateDistance(goal2, playersPos[0][frame]);
			if(ballpossession[frame] == 1){ //wie is in balbezit
				if (distance1 < distance2){ //verdedigt team1 goal1?
					ballDistanceToOpponentGoal[frame] = calculateDistance(goal2, ballpositions[frame]);
					//balbezit = team1,
					//team1 verdedigt goal1,
				}
				else{ 
					ballDistanceToOpponentGoal[frame] = calculateDistance(goal1, ballpositions[frame]);
					//balbezit = team1,
					//team1 verdedigt goal2,
				}
			}
			else{//wie is in balbezit
				if (distance1 < distance2){ //verdedigt team1 goal1?
					ballDistanceToOpponentGoal[frame] = calculateDistance(goal1, ballpositions[frame]);
					//balbezit = team2,
					//team1 verdedigt goal1,
				}
				else{ 
					ballDistanceToOpponentGoal[frame] = calculateDistance(goal2, ballpositions[frame]);
					//balbezit = team2,
					//team1 verdedigt goal2,
				}
			}
		}
	}
	
	public static void setAantalSpelersBinnenMeter(double distance, int frame){
		if(distance<5){
			defendingPlayersWithin5Meters[frame] += 1;
		}
		if(distance<10){
			defendingPlayersWithin10Meters[frame] += 1;
		}
		if(distance<15){
			defendingPlayersWithin15Meters[frame] += 1;
		}
		if(distance<20){
			defendingPlayersWithin20Meters[frame] += 1;
		}
	}
	
	public static void setAantalSpelersBinnenXxMeter(){
		System.out.println("setAantalSpelersBinnenXxMeter");
		for(int frame = 0; frame<frames; frame++){
			if(ballpossession[frame] == 1){
				for(int player = 0; player < 11; player++){
					double distance = calculateDistance(playersPos[player][frame], ballpositions[frame]);
					setAantalSpelersBinnenMeter(distance, frame);				
				}
			}
			else{
				for(int player = 11; player < 22; player++){
					double distance = calculateDistance(playersPos[player][frame], ballpositions[frame]);
					setAantalSpelersBinnenMeter(distance, frame);	
				}
			}
		}
	}
	
	public static void setNumFreePlayers(){
		for(int frame = 0; frame<frames; frame++){
			double[][] distanceMatrix = makeDistanceMatrix(frame);
			if(ballpossession[frame] == 1){
				for(int player = 0; player<10; player++){
					boolean two = true;
					boolean tree = true;
					boolean four = true;
					boolean five = true;
					boolean six = true;
					boolean seven = true;
					for(int opponent = 0; opponent<10; opponent++){
						if(distanceMatrix[player][opponent]<=2)
							two = false;
						if(distanceMatrix[player][opponent]<=3)
							tree = false;
						if(distanceMatrix[player][opponent]<=4)
							four = false;
						if(distanceMatrix[player][opponent]<=5)
							five = false;
						if(distanceMatrix[player][opponent]<=6)
							six = false;
						if(distanceMatrix[player][opponent]<=7)
							seven = false;
					}
					if(two)
						freePlayers2meters[frame] += 1;
					if(tree)
						freePlayers3meters[frame] += 1;
					if(four)
						freePlayers4meters[frame] += 1;
					if(five)
						freePlayers5meters[frame] += 1;
					if(six)
						freePlayers6meters[frame] += 1;
					if(seven)
						freePlayers7meters[frame] += 1;
				}
			}
			else{
				for(int player = 0; player<10; player++){
					boolean two2 = true;
					boolean tree2 = true;
					boolean four2 = true;
					boolean five2 = true;
					boolean six2 = true;
					boolean seven2 = true;
					for(int opponent = 0; opponent<10; opponent++){
						if(distanceMatrix[opponent][player]<=2)
							two2 = false;
						if(distanceMatrix[opponent][player]<=3)
							tree2 = false;
						if(distanceMatrix[opponent][player]<=4)
							four2 = false;
						if(distanceMatrix[opponent][player]<=5)
							five2 = false;
						if(distanceMatrix[opponent][player]<=6)
							six2 = false;
						if(distanceMatrix[opponent][player]<=7)
							seven2 = false;
					}
					if(two2)
						freePlayers2meters[frame] += 1;
					if(tree2)
						freePlayers3meters[frame] += 1;
					if(four2)
						freePlayers4meters[frame] += 1;
					if(five2)
						freePlayers5meters[frame] += 1;
					if(six2)
						freePlayers6meters[frame] += 1;
					if(seven2)
						freePlayers7meters[frame] += 1;
				}
			}
		}
	}

    
	public static GeometryCollection computeVoronoi(int frame) {
        List<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < playersPos.length; i++) {
            double x = playersPos[i][frame].x;
            double y = playersPos[i][frame].y;
            coordinates.add(new Coordinate(x, y));
        }

        VoronoiDiagramBuilder vdbuilder = new VoronoiDiagramBuilder();
        vdbuilder.setSites(coordinates);
        GeometryCollection geometries = (GeometryCollection)vdbuilder.getDiagram(new GeometryFactory());

        float margin = 0.0f;
        Coordinate lowerleft = new Coordinate(margin, margin);
        Coordinate upperright = new Coordinate(105 - margin, 68 - margin);
        Envelope envelope = new Envelope(lowerleft, upperright);
        GeometryClipper gc = new GeometryClipper(envelope);

        Geometry[] newGeometries = new Geometry[geometries.getNumGeometries()];
        for (int i = 0; i < geometries.getNumGeometries(); i++) {
            Geometry geometry = gc.clipSafe(geometries.getGeometryN(i), false, 0);
            if (geometry == null) {
                System.out.println("geometry clipper returned null, skipped frame" + frame);
                return new GeometryCollection(new Geometry[0], new GeometryFactory());
            } else {
                newGeometries[i] = geometry;
            }
        }

        return new GeometryCollection(newGeometries, new GeometryFactory());
    }
	
	public static void computeAllVoronoi(){
		System.out.println("Voronoicellen berekenen");
		for(int frame = 0; frame<frames; frame++){
			if(frame%10000==0)
				System.out.println(frame);
			GeometryCollection voronoiCells = computeVoronoi(frame);
			int numCells = voronoiCells.getNumGeometries();
			
			//in welke polygon zit de bal
			double ballX = ballpositions[frame].x;
            double ballY = ballpositions[frame].y;
            Point point = new GeometryFactory().createPoint(new Coordinate(ballX, ballY));
			for(int cell=0; cell<numCells; cell++){
				Polygon polygon = (Polygon) voronoiCells.getGeometryN(cell);
				boolean ball = false;
				Coordinate[] coordinates = polygon.getCoordinates();
				if (polygon.contains(point)){
					ball = true;
				}
				double surface = 0;
				int j = coordinates.length-2;
				for(int i = 0; i<coordinates.length-1; i++){
					surface += ((coordinates[j].x + coordinates[i].x) * (coordinates[j].y-coordinates[i].y));
					j=i;
				}
				if(cell<11){
					vonoroiSurfaceTeam1[frame] += surface/2;
				}
				else{
					vonoroiSurfaceTeam2[frame] += surface/2;
				}
				if(ball){
					vonoroiSurfaceBall[frame] = surface/2;
				}
			}
		}
	}
		
	public static double[] distanceToLine = new double[22];
	public static void playerLabels(){
		PVector goal1 = convert(1,0);
		PVector goal2 = convert(-1,0);
		for(int frame = 0; frame<frames; frame++){
			double distance1 = calculateDistance(goal1, playersPos[0][frame]);
			double distance2 = calculateDistance(goal2, playersPos[0][frame]);
			for(int player = 0; player<22; player++){
				if(distance1<distance2 && player<11){
					distanceToLine[player] += 105-playersPos[player][frame].x;
				}
				if(distance1>distance2 && player<11){
					distanceToLine[player] += playersPos[player][frame].x;
				}
				if(distance1<distance2 && player>=11){
					distanceToLine[player] += playersPos[player][frame].x;
				}
				if(distance1>distance2 && player>=11){
					distanceToLine[player] += 105-playersPos[player][frame].x;
				}
			}
		}
		for(int frame = 0; frame<frames; frame++){
			playerInPossessionPossition[frame] = distanceToLine[playerInPossession[frame]]/frames;
		}
	}
	
	public static void main(String[] args) {
		getMatchDetails();
		getball();
		getPlayers();
		setBallTeamDistance();
		setDirectOpponent();
		setWholeMatchOpponentDistance();
		setPlayerToPlayerDistance();
		setDistanceToOpponentGoal();
		setAantalSpelersBinnenXxMeter();
		setNumFreePlayers();
		computeAllVoronoi();
		playerLabels();
		setTarget();
		insertPressureMeasurements();
	}
}
