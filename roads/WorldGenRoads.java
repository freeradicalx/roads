package freeradicalx.roads;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;
import freeradicalx.util.ExtraUtils;

public class WorldGenRoads implements IWorldGenerator{
	
	World world;
	Chunk chunk;
	IChunkProvider chunkProvider;
	RoadData roadData;
	Random random = new Random();					//Random number gen
	int segmentLength = 30 + random.nextInt(15);	//Length of a new road segment
	int chanceOfNewRoad = 100;						//Higher value = fewer roads (1 per every chanceOfNewRoad chunks)
	int width = -5; 								//change this line for wider or narrower highways. Width of highway == abs(width * 2)
	int maxIncline = 4;								//Maximum altitude change between road points
	boolean canGoBackwards = true;
	int playerX = 0;
	int playerY = 0;
	int playerZ = 0;
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		switch(world.provider.dimensionId){
			case 0: generateSurface(world, chunkProvider, random, chunkX, chunkZ);
			case 1: generateEnd(world, random, chunkX, chunkZ);
			case -1: generateNether(world, random, chunkX, chunkZ);
		}
		
	}
	
	public void generateSurface(World theWorld, IChunkProvider theChunkProvider, Random random, int chunkX, int chunkZ){
		
		//THIS CODE ISN'T WORKING BECAUSE EVENTHOOKS NEVER DETECTS THE PLAYER
		//if (EventHooks.playerIsSpawned()){
		//	System.out.println("<<<<<<<<<<<<<<<<<PLAYER SPAWNED>>>>>>>>>>>>>>>>>");
		//	playerX = (int) Minecraft.getMinecraft().thePlayer.posX;
		//	playerY = (int) Minecraft.getMinecraft().thePlayer.posY;
		//	playerZ = (int) Minecraft.getMinecraft().thePlayer.posZ;
		//}
		
		try{playerX = (int) Minecraft.getMinecraft().thePlayer.posX;}
		catch(NullPointerException n){playerX = 0;}
		try{playerY = (int) Minecraft.getMinecraft().thePlayer.posY;}
		catch(NullPointerException n){playerY = 0;}
		try{playerZ = (int) Minecraft.getMinecraft().thePlayer.posZ;}
		catch(NullPointerException n){playerZ = 0;}
		
		world = theWorld;
		chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
		chunkProvider = theChunkProvider;
		roadData = RoadData.forWorld(world);
		
		//Roll for a new road being started regardless of connecting roads
		if (random.nextInt(chanceOfNewRoad) == 0){
			roadData.addRoad(chunk);
		}
        
		//Count up surrounding chunks with roads connecting to this chunk and list them
	    List<Chunk> roadChunks = new ArrayList<Chunk>();
	    roadChunks = this.checkForRoads(roadChunks, chunkX, chunkZ);
	    
	    //If there are no road chunks nearby but this chunk rolled a road, begin new road
        if (roadData.hasRoad(chunk) && roadChunks.size() == 0){
        	newRoad();
        }  
        
        //If there are chunks with roads connecting into this chunk, draw them
        int roadChunkNumber = 0;
        if (roadChunks.size() > 0){
        	while (roadChunkNumber < roadChunks.size()){
        		continueRoad(roadChunks.get(roadChunkNumber), chunkX, chunkZ);
        		roadChunkNumber++;
        	}
        }
        
	}
	      
    // Checks a 10 x 10 grid of chunks around the current chunk for highways, returns a list of chunks with highways ending in THIS chunk.
    public List<Chunk> checkForRoads(List<Chunk> roadChunks, int xo, int zo){
    	
    	for(int xcount = -5; xcount <= 5; xcount++){
    		for(int zcount = -5; zcount <= 5; zcount++){
    			boolean center = ((xcount == 0) && (zcount == 0)); //checks to see if we're examining our own current chunk
    			if(chunkProvider.chunkExists(xcount+xo, zcount+zo)){
	    			if(roadData.hasRoad(world.getChunkFromChunkCoords(xcount+xo, zcount+zo)) == true && (center == false)){
	    				int[] points = roadData.getPoints(world.getChunkFromChunkCoords(xcount+xo, zcount+zo));
	    				if(points.length > 0){
	    					if (((points[0] >> 4) == xo && (points[2] >> 4) == zo) || ((points[6] >> 4) == xo && (points[8] >> 4) == zo)){
	    						//System.out.println((xo) +","+(zo)+" connects to "+(points[3] >> 4)+","+(points[5] >> 4));
	    						roadChunks.add(world.getChunkFromChunkCoords(xcount+xo, zcount+zo));
	    					}
	    				}
	    			}
    			}
    		}
    	}
    	return roadChunks;
	}
    
    // Checks a 10 x 10 grid of chunks around the current chunk for highways, returns a list of chunks with roads that don't end on this one.
    public List<Chunk> checkForOtherRoads(List<Chunk> roadOtherChunks, int xo, int zo){
    	
    	for(int xcount = -5; xcount <= 5; xcount++){
    		for(int zcount = -5; zcount <= 5; zcount++){
    			boolean center = ((xcount == 0) && (zcount == 0)); //checks to see if we're examining our own current chunk
    			if(chunkProvider.chunkExists(xcount+xo, zcount+zo)){
	    			if(roadData.hasRoad(world.getChunkFromChunkCoords(xcount+xo, zcount+zo)) == true && (center == false)){
	    				int[] points = roadData.getPoints(world.getChunkFromChunkCoords(xcount+xo, zcount+zo));
	    				if(points.length > 0){
	    					//System.out.println("found ID: "+roadData.getID(world.getChunkFromChunkCoords(xcount+xo, zcount+zo))+" this ID: "+roadData.getID(chunk));
	    					if (((points[0] >> 4) == xo && (points[2] >> 4) == zo) == false 
	    					&& ((points[6] >> 4) == xo && (points[8] >> 4) == zo) == false
	    					&& roadData.getID(world.getChunkFromBlockCoords(points[3], points[5])) != roadData.getID(chunk)
	    					&& roadData.getID(world.getChunkFromBlockCoords(points[3], points[5])) != 0.0){
	    						roadOtherChunks.add(world.getChunkFromBlockCoords(points[3], points[5]));
	    						//System.out.println("FOUND NEARBY ROAD THAT DOESN'T INTERSECT THIS ROAD!");
	    						//System.out.println(roadData.getID(world.getChunkFromBlockCoords(points[3], points[5]))+" at "+points[3]+","+points[5]+" and "+roadData.getID(chunk)+" at "+chunk.xPosition*16+","+chunk.zPosition*16);
	    					}
	    				}
	    			}
    			}
    		}
    	}
    	return roadOtherChunks;
	}
     
	public void newRoad(){
		
    	float direction = 0;
		boolean foundEmptyChunk = false;
    	//boolean canGoBackwards = true;
    	int newCurve[] = new int[9];
    	newCurve[0] = playerX;
    	newCurve[1] = playerY;
    	newCurve[2] = playerZ;
    	newCurve[3] = (chunk.xPosition*16) + 8;
    	newCurve[5] = (chunk.zPosition*16) + 8;
    	newCurve[4] = (ExtraUtils.groundHeight(world, newCurve[3], newCurve[5])+
				ExtraUtils.groundHeight(world, newCurve[3]+1, newCurve[5])+
				ExtraUtils.groundHeight(world, newCurve[3]+1, newCurve[5]+1)+
				ExtraUtils.groundHeight(world, newCurve[3], newCurve[5]+1)) / 4;
    	int searchCount = 0;
    	int searchExtender = 0;
    	int playerDiffX = Math.abs(newCurve[3]-newCurve[0]);
    	int playerDiffZ = Math.abs(newCurve[5]-newCurve[2]);
		
	    List<Chunk> roadOtherChunks = new ArrayList<Chunk>();
        roadOtherChunks = this.checkForOtherRoads(roadOtherChunks, chunk.xPosition, chunk.zPosition);
        if (roadOtherChunks.size() > 0){
        	//System.out.println("ABORTING NEW ROAD: There are already roads within five chunks of this chunk.");
        	return;
        }
    	
    	if (newCurve[0] > newCurve[3]){
    		playerDiffX = -playerDiffX;
    	}
    	if (newCurve[2] > newCurve[5]){
    		playerDiffZ = -playerDiffZ;
    	}
    	
		//Calculate and assign direction, the angle that the new chunk uses to look for empty chunks to extend it's first segment into later.
    	if (playerDiffX != 0){
    		direction = (float) (Math.atan(playerDiffZ / playerDiffX));
    		if (playerDiffX < 0){
    			direction = (float)(direction + Math.PI);
    		}		
    	}
    	else {
    		if (playerDiffZ > 0){
    			direction = (float)1.57;
    		}
    		if (playerDiffZ < 0){
    			direction = (float)-1.57;
    		}
    	}
    	
    	roadData.setHeading(chunk, direction);
    	direction = roadData.getDirection(chunk);
    	
		newCurve[6] = 0; newCurve[7] = 0;newCurve[8] = 0;
    	
    	while ((foundEmptyChunk == false) && (searchCount < 3)){
        	if (!chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender)))) && !foundEmptyChunk){
        		newCurve[6] = newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender)));	
            	newCurve[7] = newCurve[4];
            	newCurve[8] = newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender)));
            	foundEmptyChunk = true;
        	}
        	if (!chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction-0.5) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction-0.5) * (segmentLength+searchExtender)))) && !foundEmptyChunk){
        		newCurve[6] = newCurve[3]+((int)(Math.cos(direction-0.5) * (segmentLength+searchExtender)));	
            	newCurve[7] = newCurve[4];
            	newCurve[8] = newCurve[5]+((int)(Math.sin(direction-0.5) * (segmentLength+searchExtender)));
            	foundEmptyChunk = true;
            	//System.out.ln("Direction -0.5");
        	}
        	if (!chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction+0.5) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction+0.5) * (segmentLength+searchExtender)))) && !foundEmptyChunk){
        		newCurve[6] = newCurve[3]+((int)(Math.cos(direction+0.5) * (segmentLength+searchExtender)));	
            	newCurve[7] = newCurve[4];
            	newCurve[8] = newCurve[5]+((int)(Math.sin(direction+0.5) * (segmentLength+searchExtender)));
        		foundEmptyChunk = true;
        		//System.out.println("Direction +0.5");
    		}
        	searchExtender += 16;
    		++searchCount;
    	}
    	
    	if (foundEmptyChunk == false){
    		//System.out.println("ABORTING NEW ROAD: No empty chunks nearby to build road into.");
    		//System.out.println("NEW CHUNK COULDN'T FIND A FURTHER AWAY CHUNK TO DRAW INTO, UH OH");
    		return;
    	}
		
		int diffX = Math.abs(newCurve[3] - newCurve[6]);
		int diffY = Math.abs(newCurve[4] - newCurve[7]);
		int diffZ = Math.abs(newCurve[5] - newCurve[8]);
		
		//A test to see if the chunk two road segments ahead of this one is already loaded, to cancel this road altogehter if so to avoid ugly stunted segments spawning.
		if (newCurve[3] < newCurve[6]){
			if (newCurve[5] < newCurve[8]){
				if (chunkProvider.chunkExists(newCurve[6]+diffX,newCurve[8]+diffZ)){
					return;
				}
			}
			if (newCurve[5] >= newCurve[8]){
				if (chunkProvider.chunkExists(newCurve[6]+diffX,newCurve[8]-diffZ)){
					return;
				}
			}
		}
		if (newCurve[3] >= newCurve[6]){
			if (newCurve[5] < newCurve[8]){
				if (chunkProvider.chunkExists(newCurve[6]-diffX,newCurve[8]+diffZ)){
					return;
				}
			}
			if (newCurve[5] >= newCurve[8]){
				if (chunkProvider.chunkExists(newCurve[6]-diffX,newCurve[8]-diffZ)){
					return;
				}
			}
		}
		
		/*
		//These tests see if paving backward two road segments will run this road into loaded chunks, stunting the road's backward continuation.
		//Sets the start of the road to the player's loaded chunk to avoid this happening - stopping all backward paving -  if this is the case.
		if (newCurve[3] >= newCurve[6]) {
			if (newCurve[5] >= newCurve[8]){
				if (chunkProvider.chunkExists(newCurve[3]+diffX*2, newCurve[5]+diffZ*2)){
					System.out.println("Setting back of this new roadchunk to the player to avoid backward cutoff road (+diffX,+diffZ)");
			    	newCurve[0] = playerX;
			    	newCurve[1] = playerY;
			    	newCurve[2] = playerZ;
			    	canGoBackwards = false;
				}
			}
			if (newCurve[5] < newCurve[8]){
				if (chunkProvider.chunkExists(newCurve[3]+diffX*2, newCurve[5]-diffZ*2)){
					System.out.println("Setting back of this new roadchunk to the player to avoid backward cutoff road (+diffX,-diffZ)");
			    	newCurve[0] = playerX;
			    	newCurve[1] = playerY;
			    	newCurve[2] = playerZ;
			    	canGoBackwards = false;
				}
			}
		}
		if (newCurve[3] < newCurve[6]) {
			if (newCurve[5] >= newCurve[8]){
				if (chunkProvider.chunkExists(newCurve[3]-diffX*2, newCurve[5]+diffZ*2)){
					System.out.println("Setting back of this new roadchunk to the player to avoid backward cutoff road (-diffX,+diffZ)");
			    	newCurve[0] = playerX;
			    	newCurve[1] = playerY;
			    	newCurve[2] = playerZ;
			    	canGoBackwards = false;
				}
			}
			if (newCurve[5] < newCurve[8]){
				if (chunkProvider.chunkExists(newCurve[3]-diffX*2, newCurve[5]-diffZ*2)){
					System.out.println("Setting back of this new roadchunk to the player to avoid backward cutoff road (-diffX,-diffZ)");
			    	newCurve[0] = playerX;
			    	newCurve[1] = playerY;
			    	newCurve[2] = playerZ;
			    	canGoBackwards = false;
				}
			}
		}
		
		//If the chunk can go backward still after testing for backward loaded chunks, this block assigns it's backward [0,1,2] values.
		if (canGoBackwards == true){
			//System.out.println("Chunk can go backwards.");
			if (newCurve[3] >= newCurve[6]){ newCurve[0] = newCurve[3]+diffX;}
			else {
				newCurve[0] = newCurve[3]-diffX;
 
			}
			if (newCurve[4] >= newCurve[7]){ newCurve[1] = newCurve[4]+diffY;}
			else {
				newCurve[1] = newCurve[4]-diffY;
				//diffY = -diffY;  //obsolete cause it doesn't get used, but whatever just keeping things consistent.
			}
			if (newCurve[5] >= newCurve[8]){ newCurve[2] = newCurve[5]+diffZ;}
			else {
				newCurve[2] = newCurve[5]-diffZ;
				//diffZ = -diffZ;
			}
		} */
		
		//Set the direction of this road segment based on whatever forward chunk was found to be empty.
    	diffX = (int)(Math.round(Math.abs(newCurve[6] - newCurve[3])));
    	diffZ = (int)(Math.round(Math.abs(newCurve[8] - newCurve[5])));
    	if (newCurve[6] < newCurve[3]){
    		diffX = -diffX;
    	}
    	if (newCurve[8] < newCurve[5]){
    		diffZ = -diffZ;
    	}
    	if (diffX != 0){
    		direction = (float) (Math.atan(diffZ / diffX));
    		if (diffX < 0){
    			direction = (float)(direction + Math.PI);
    		}		
    	}
    	if (diffX == 0) {
    		if (diffZ > 0){
    			direction = (float)1.57;
    		}
    		if (diffZ < 0){
    			direction = (float)-1.57;
    		}
    	}
    	
		roadData.addRoad(chunk);
    	roadData.setDirection(chunk, direction);
		roadData.addPoints(chunk, newCurve);
		double newID = Math.random();
		roadData.setID(chunk, newID);
		
		//System.out.println("NEW ROAD - ID: "+roadData.getID(chunk)+" DIRECTION: "+roadData.getDirection(chunk));
		//System.out.println("[0,2]: "+newCurve[0]+","+newCurve[2]+" [3,5]: "+newCurve[3]+","+newCurve[5]+" [6,8]: "+newCurve[6]+","+newCurve[8]);
	}

    public void continueRoad(Chunk roadChunk, int chunkX, int chunkZ){
    	
		int oldCurve[] = roadData.getPoints(roadChunk);
		int newCurve[] = new int[9];
		boolean foundEmptyChunk = false;
    	int searchExtender = 0;
    	int searchCount = 0;
    	float length = 0;
    	float incrementY = 0;
    	int eventualX;
    	int eventualZ;
		
		//Set this roadID to be the same as the segment it continues
		roadData.setID(chunk, roadData.getID(roadChunk));
    	
		//Set this segment's heading based on the previous segment's heading.
		roadData.setHeading(chunk, roadData.getDirection(roadChunk));
		float direction = roadData.getDirection(chunk);
		
		//Set this road segment's road points
		newCurve[0] = oldCurve[3];
	    newCurve[1] = oldCurve[4];
	    newCurve[2] = oldCurve[5];
		if((oldCurve[0] >> 4) == chunkX && (oldCurve[2] >> 4) == chunkZ){
			newCurve[3] = roadData.getPoints(roadChunk)[0];
			newCurve[5] = roadData.getPoints(roadChunk)[2];
			newCurve[4] = (ExtraUtils.groundHeight(world, newCurve[3], newCurve[5])+
						ExtraUtils.groundHeight(world, newCurve[3]+1, newCurve[5])+
						ExtraUtils.groundHeight(world, newCurve[3]+1, newCurve[5]+1)+
						ExtraUtils.groundHeight(world, newCurve[3], newCurve[5]+1)) / 4;
		}
		if((oldCurve[6] >> 4) == chunkX && (oldCurve[8] >> 4) == chunkZ){
			newCurve[3] = roadData.getPoints(roadChunk)[6];
			newCurve[5] = roadData.getPoints(roadChunk)[8];
			newCurve[4] = (ExtraUtils.groundHeight(world, newCurve[3], newCurve[5])+
						ExtraUtils.groundHeight(world, newCurve[3]+1, newCurve[5])+
						ExtraUtils.groundHeight(world, newCurve[3]+1, newCurve[5]+1)+
						ExtraUtils.groundHeight(world, newCurve[3], newCurve[5]+1)) / 4;
		}
		
		//If the incline is extreme, bring it within the allowed maximum
		if (newCurve[4] > (oldCurve[4]+maxIncline)){
			newCurve[4] = oldCurve[4]+maxIncline;
		}
		if (newCurve[4] < (oldCurve[4]-maxIncline)){
			newCurve[4] = oldCurve[4]-maxIncline;
		}
		
		
		//This block of code will re-route the road into a nearby road to end it, if close enough (Proximity determined by checkForOtherRoads() )
	    List<Chunk> roadOtherChunks = new ArrayList<Chunk>();
        roadOtherChunks = this.checkForOtherRoads(roadOtherChunks, chunkX, chunkZ);
    	if (roadOtherChunks.size() > 0){
    		//System.out.println("ABORTING ROAD: There is another road nearby. Merging into that road.");
    		
        	int closestChunk = 0;
        	int loop = 0;
        	int closest = 5000;
        	int diffX = 0;
        	int diffZ = 0;
        	float angle = 0;
        	
    		while (loop <= roadOtherChunks.size()-1){
    			//TODO: I get an index out of bounds / ticking memory connection exception here sometimes... Can't figure out what's wrong in the loop.
    			diffX = Math.abs(oldCurve[3] - roadData.getPoints(roadOtherChunks.get(loop))[3]);
    			diffZ = Math.abs(oldCurve[5] - roadData.getPoints(roadOtherChunks.get(loop))[5]);
    			
    			if (diffX + diffZ < closest){
    				closestChunk = loop;
    				closest = diffX + diffZ;
    			}
    			
        		//this.drawSegment(roadData.getPoints(roadOtherChunks.get(loop))[3], roadData.getPoints(roadOtherChunks.get(loop))[4], roadData.getPoints(roadOtherChunks.get(loop))[5], 
    			//		newCurve[0], newCurve[1], newCurve[2]);
        		//roadData.setID(roadOtherChunks.get(loop), roadData.getID(chunk));
    			
    			//If a nearby road terminates in a roadless chunk, draw a road from this segment to it to make it seem like it terminates at this road
    			if (chunkProvider.chunkExists(roadData.getPoints(roadOtherChunks.get(loop))[6], roadData.getPoints(roadOtherChunks.get(loop))[8])){
    				if (!roadData.hasRoad(world.getChunkFromBlockCoords(roadData.getPoints(roadOtherChunks.get(loop))[6], roadData.getPoints(roadOtherChunks.get(loop))[8]))){
    		    		this.drawSegment(roadData.getPoints(roadOtherChunks.get(loop))[3], roadData.getPoints(roadOtherChunks.get(loop))[4], roadData.getPoints(roadOtherChunks.get(loop))[5], 
    							newCurve[0], newCurve[1], newCurve[2]);
    		    		//roadData.setID(roadOtherChunks.get(loop), roadData.getID(chunk));
    		    		
    				}
    			}
    			
    			++loop;
    		}
    		
    		//this.drawSegment(oldCurve[3], oldCurve[4], oldCurve[5], roadData.getPoints(closestChunk)[3], roadData.getPoints(closestChunk)[4], roadData.getPoints(closestChunk)[5]);
    		this.drawSegment(roadData.getPoints(roadOtherChunks.get(closestChunk))[3], roadData.getPoints(roadOtherChunks.get(closestChunk))[4], roadData.getPoints(roadOtherChunks.get(closestChunk))[5], 
					newCurve[0], newCurve[1], newCurve[2]);
    		roadData.setID(chunk, roadData.getID(roadOtherChunks.get(closestChunk)));
    		//System.out.println("These chunkIDs should be the same: " + roadData.getID(roadOtherChunks.get(closestChunk)) + " and " + roadData.getID(chunk));
    		return;
    	}
    	

		//Forward search for an empty chunk to run into
    	while ((foundEmptyChunk == false) && (searchCount < 3)){
    		//System.out.println("checking: "+(newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender))) >> 4)+","+(newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender))) >> 4)+" - exists?: "+!chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender))))+","+foundEmptyChunk);
        	if (!chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender)))) && !foundEmptyChunk){
        		newCurve[6] = newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender)));	
            	newCurve[7] = newCurve[4];
            	newCurve[8] = newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender)));
            	foundEmptyChunk = true;
        	}
        	//System.out.println("checking: "+(newCurve[3]+((int)(Math.cos(direction-0.5) * (segmentLength+searchExtender))) >> 4)+","+(newCurve[5]+((int)(Math.sin(direction-0.5) * (segmentLength+searchExtender))) >> 4)+" - exists?: "+!chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender))))+","+foundEmptyChunk);
        	if (!chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction-0.5) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction-0.5) * (segmentLength+searchExtender)))) && !foundEmptyChunk){
        		newCurve[6] = newCurve[3]+((int)(Math.cos(direction-0.5) * (segmentLength+searchExtender)));	
            	newCurve[7] = newCurve[4];
            	newCurve[8] = newCurve[5]+((int)(Math.sin(direction-0.5) * (segmentLength+searchExtender)));
            	foundEmptyChunk = true;
            	//System.out.println("Direction -0.5");
        	}
        	//System.out.println("checking: "+(newCurve[3]+((int)(Math.cos(direction+0.5) * (segmentLength+searchExtender))) >> 4)+","+(newCurve[5]+((int)(Math.sin(direction+0.5) * (segmentLength+searchExtender))) >> 4)+" - exists?: "+!chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender))))+","+foundEmptyChunk);
        	if (!chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction+0.5) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction+0.5) * (segmentLength+searchExtender)))) && !foundEmptyChunk){
        		newCurve[6] = newCurve[3]+((int)(Math.cos(direction+0.5) * (segmentLength+searchExtender)));	
            	newCurve[7] = newCurve[4];
            	newCurve[8] = newCurve[5]+((int)(Math.sin(direction+0.5) * (segmentLength+searchExtender)));
        		foundEmptyChunk = true;
        		//System.out.println("Direction +0.5");
    		}
        	searchExtender += 16;
    		++searchCount;
    	}
    	
    	//If the forward search fails, then this block will do a search relative to the player (Away from them relative this chunk) for an empty chunk to run into.
    	if(foundEmptyChunk == false){
    		//System.out.println("Next chunk in line already exists, looking for alternate chunk to head to...");
    		
	    	searchExtender = 0;
	    	searchCount = 0;
    		
	    	int playerDiffX = (int)(Math.round(Math.abs(newCurve[3]-playerX)));
	    	int playerDiffZ = (int)(Math.round(Math.abs(newCurve[5]-playerZ)));
	    	
	    	if (playerX < newCurve[3]){
	    		playerDiffX = -playerDiffX;
	    	}
	    	if (playerZ < newCurve[5]){
	    		playerDiffZ = -playerDiffZ;
	    	}
	    	
	    	if (playerDiffX != 0){
	    		direction = (float) (Math.atan(playerDiffZ / playerDiffX));
	    		if (playerDiffX < 0){
	    			direction = (float)(direction + Math.PI);
	    		}		
	    	}
	    	if (playerDiffX == 0) {
	    		if (playerDiffZ > 0){
	    			direction = (float)1.57;
	    		}
	    		if (playerDiffZ < 0){
	    			direction = (float)-1.57;
	    		}
	    	}
	    	
	    	//Up to this point direction is the direction from [3],[5] to the player; This statement should flip it 180 to look away from the player.
	    	direction = (float) (direction + Math.PI);
	    	
	    	while ((foundEmptyChunk == false) && (searchCount < 3)){
	    		//System.out.println("checking: "+(newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender))) >> 4)+","+(newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender))) >> 4)+" - exists?: "+chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender)))));
	        	if (!chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender)))) && !foundEmptyChunk){
	        		newCurve[6] = newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender)));	
	            	newCurve[7] = newCurve[4];
	            	newCurve[8] = newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender)));
	            	foundEmptyChunk = true;
	        	}
	        	//System.out.println("checking: "+(newCurve[3]+((int)(Math.cos(direction-0.5) * (segmentLength+searchExtender))) >> 4)+","+(newCurve[5]+((int)(Math.sin(direction-0.5) * (segmentLength+searchExtender))) >> 4)+" - exists?: "+chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender)))));
	        	if (!chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction-0.5) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction-0.5) * (segmentLength+searchExtender)))) && !foundEmptyChunk){
	        		newCurve[6] = newCurve[3]+((int)(Math.cos(direction-0.5) * (segmentLength+searchExtender)));	
	            	newCurve[7] = newCurve[4];
	            	newCurve[8] = newCurve[5]+((int)(Math.sin(direction-0.5) * (segmentLength+searchExtender)));
	            	foundEmptyChunk = true;
	            	//System.out.println("Direction -0.5");
	        	}
	        	//System.out.println("checking: "+(newCurve[3]+((int)(Math.cos(direction+0.5) * (segmentLength+searchExtender))) >> 4)+","+(newCurve[5]+((int)(Math.sin(direction+0.5) * (segmentLength+searchExtender))) >> 4)+" - exists?: "+chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction) * (segmentLength+searchExtender)))));
	        	if (!chunkProvider.chunkExists(newCurve[3]+((int)(Math.cos(direction+0.5) * (segmentLength+searchExtender))), newCurve[5]+((int)(Math.sin(direction+0.5) * (segmentLength+searchExtender)))) && !foundEmptyChunk){
	        		newCurve[6] = newCurve[3]+((int)(Math.cos(direction+0.5) * (segmentLength+searchExtender)));	
	            	newCurve[7] = newCurve[4];
	            	newCurve[8] = newCurve[5]+((int)(Math.sin(direction+0.5) * (segmentLength+searchExtender)));
	        		foundEmptyChunk = true;
	        		//System.out.println("Direction +0.5");
	    		}
	        	searchExtender += 16;
	    		++searchCount;
	    	}	
    	}
    	
    	//If no empty chunks have been found to spawn into at this point, search for a nearby road to re-route into it if possible.
    	if (!foundEmptyChunk){
    		//System.out.println("ABORTING ROAD: No empty chunks found to continue into. Looking to bail end of road into nearby road.");
    	    List<Chunk> roadOtherChunks2 = new ArrayList<Chunk>();
            roadOtherChunks2 = this.checkForOtherRoads(roadOtherChunks2, chunkX, chunkZ);
        	if (roadOtherChunks2.size() > 0){
        		
            	int closestChunk = 0;
            	int loop = 0;
            	int closest = 5000;
            	int diffX = 0;
            	int diffZ = 0;
            	float angle = 0;
            	
        		while (loop <= roadOtherChunks2.size()-1){
        			//TODO: I get an index out of bounds / ticking memory connection exception here sometimes... Can't figure out what's wrong in the loop.
        			diffX = Math.abs(oldCurve[3] - roadData.getPoints(roadOtherChunks2.get(loop))[3]);
        			diffZ = Math.abs(oldCurve[5] - roadData.getPoints(roadOtherChunks2.get(loop))[5]);
        			
        			if (diffX + diffZ < closest){
        				closestChunk = loop;
        				closest = diffX + diffZ;
        			}
        			
        			//If a nearby road terminates in a roadless chunk, draw a road from this segment to it to make it seem like it terminates at this road
        			if (chunkProvider.chunkExists(roadData.getPoints(roadOtherChunks2.get(loop))[6], roadData.getPoints(roadOtherChunks2.get(loop))[8])){
        				if (!roadData.hasRoad(world.getChunkFromBlockCoords(roadData.getPoints(roadOtherChunks2.get(loop))[6], roadData.getPoints(roadOtherChunks2.get(loop))[8]))){
        		    		this.drawSegment(roadData.getPoints(roadOtherChunks2.get(loop))[3], roadData.getPoints(roadOtherChunks2.get(loop))[4], roadData.getPoints(roadOtherChunks2.get(loop))[5], 
        							newCurve[0], newCurve[1], newCurve[2]);
        		    		
        				}
        			}
        			
        			++loop;
        		}
        		
        		//this.drawSegment(oldCurve[3], oldCurve[4], oldCurve[5], roadData.getPoints(closestChunk)[3], roadData.getPoints(closestChunk)[4], roadData.getPoints(closestChunk)[5]);
        		this.drawSegment(roadData.getPoints(roadOtherChunks2.get(closestChunk))[3], roadData.getPoints(roadOtherChunks2.get(closestChunk))[4], roadData.getPoints(roadOtherChunks2.get(closestChunk))[5], 
    					newCurve[0], newCurve[1], newCurve[2]);
        		roadData.setID(roadOtherChunks2.get(closestChunk), roadData.getID(chunk));
        		return;
        	}
    	}
    	
    	//Extrapolate the eventual chunk two segments forward that this road will spawn into
	    eventualX = Math.abs(newCurve[6] - newCurve[3]);
	    eventualZ = Math.abs(newCurve[8] - newCurve[5]);
	    if (newCurve[3] > newCurve [6]){ eventualX = -eventualX; }
	    if (newCurve[5] > newCurve [8]){ eventualZ = -eventualZ; }
	    eventualX = newCurve[6] + eventualX;
	    eventualZ = newCurve[8] + eventualZ;
	    
	    //If the extrapolated chunk exists, either cancel the road completely or, if it exists AND has a road, extend directly over to it.
		if (chunkProvider.chunkExists(eventualX, eventualZ) && (roadData.hasRoad(world.getChunkFromBlockCoords(eventualX, eventualZ)) == false)){
			//System.out.println("Ending road at "+newCurve[3]+","+newCurve[5]+" because it's next segments ends in a loaded chunk.");
			roadData.setID(chunk, 0.0);
			//System.out.println("ABORTING ROAD: The eventual next chunk in line on this road already exists, and has no road on it.");
			return;
		}
		if (chunkProvider.chunkExists(eventualX, eventualZ) && (roadData.hasRoad(world.getChunkFromBlockCoords(eventualX, eventualZ)) == true)){
			//System.out.println("Extending road segment centered at "+newCurve[3]+","+newCurve[5]+" to "+roadData.getPoints(world.getChunkFromBlockCoords(eventualX, eventualZ))[3]+","+roadData.getPoints(world.getChunkFromBlockCoords(eventualX, eventualZ))[5]+" to meet another road");
			newCurve[6] = roadData.getPoints(world.getChunkFromBlockCoords(eventualX, eventualZ))[3];
			newCurve[7] = roadData.getPoints(world.getChunkFromBlockCoords(eventualX, eventualZ))[4];
			newCurve[8] = roadData.getPoints(world.getChunkFromBlockCoords(eventualX, eventualZ))[5];
		}
		
		//Set the direction of this road segment based on whatever forward chunk was found to be empty.
    	float diffX = (int)(Math.round(Math.abs(newCurve[6] - newCurve[3])));
    	float diffZ = (int)(Math.round(Math.abs(newCurve[8] - newCurve[5])));
    	if (newCurve[6] < newCurve[3]){
    		diffX = -diffX;
    	}
    	if (newCurve[8] < newCurve[5]){
    		diffZ = -diffZ;
    	}
    	if (diffX != 0){
    		direction = (float) Math.atan(diffZ / diffX);
    		if (diffX < 0){
    			direction = (float)(direction + Math.PI);
    		}
    	}
    	if (diffX == 0) {
    		if (diffZ >= 0){
    			direction = (float)1.57;
    		}
    		if (diffZ < 0){
    			direction = (float)-1.57;
    		}
    	}
    	roadData.setDirection(chunk, direction);
    	
		//Draw the road segment
		this.drawSegment(newCurve[0], newCurve[1], newCurve[2], newCurve[3], newCurve[4], newCurve[5]);
		
		//Save created curve to this chunk's point array
		roadData.addPoints(chunk, newCurve);
		
		//Since this chunk will have a road, make sure it gets added to the list of chunks with roads
		if(roadData.hasRoad(chunk) == false){
			roadData.addRoad(chunk);
		}
		//System.out.println("CONTINUING ROAD - ID: "+roadData.getID(chunk)+" DIRECTION: "+roadData.getDirection(chunk));
	}

    //Draws a road segment from one x,z point to another. Does it's own y (height) calculations.
    public void drawSegment(int x1, int y1, int z1, int x2, int y2, int z2){
    	
    	float angle = 0;
    	float length = 0;
    	float diffX = Math.abs(x1 - x2);
    	float diffZ = Math.abs(z1 - z2);
    	float diffY = Math.abs(y1 - y2);
    	
    	if (x1 > x2){
    		diffX = -diffX;
    	}
    	if (y1 > y2){
    		diffY = -diffY;
    	}
    	if (z1 > z2){
    		diffZ = -diffZ;
    	}
    	
    	if (diffX != 0){
    		angle = (float) Math.atan(diffZ/diffX);
    		if (diffX <= 0){
    			angle = (float) (angle + Math.PI);
    		}
    	}
    	if (diffX == 0){
    		if (diffZ > 0){
    			angle = (float) 1.57;
    		}
    		if (diffZ <= 0){
    			angle = (float) -1.57;
    		}
    	}
    	
    	length = (float) (Math.sqrt((diffX * diffX) + (diffZ * diffZ)));
    	
		float incrementY = diffY / length;
		
		for (int progress = 0; progress <= Math.round(length); progress++){
			for (int crosspave = width; crosspave <= (Math.abs(width)); crosspave++){
	
				int rotatedX = (int)Math.round(x1 + (progress * Math.cos(angle)) - (crosspave * Math.sin(angle)));
				int rotatedZ = (int)Math.round(z1 + (progress * Math.sin(angle)) + (crosspave * Math.cos(angle)));
				int rotatedY = Math.round(y1 + incrementY*progress);
				
				for(int ycount = rotatedY; ycount <= (rotatedY+10); ycount++){ //overhead clearance to create above road
					if (ycount == rotatedY){
						int ground = ExtraUtils.groundHeightSea(world, rotatedX, rotatedZ);
						//An explaination of the long block ID check lists here:
						//In this for loop the road gen clears out all "underbrush"-like blocks that one would not find under an elevated roaday,
						//like leaves, wood, and any other non-"ground" blocks. It does this in a column of 2x2 blocks to account for holes
						//that would be left behind if the roadway is going diagonal (Floating point math + integer blocks = some blocks getting missed).
						//However if needs to check EACH of these four columns for ground vs underbrush, lest there be one column that's in a ravine
						//and one that's not, which if it didn't check each column would start clearing out a cavern below the road in a runaway
						//chain reaction til the end of the segment (At the least).
						for (int count = ycount-1; count >= ground; --count){
							if (world.getBlockId(rotatedX, count, rotatedZ) != 1
									&& world.getBlockId(rotatedX, count, rotatedZ) != 2
									&& world.getBlockId(rotatedX, count, rotatedZ) != 3
									&& world.getBlockId(rotatedX, count, rotatedZ) != 4
									&& world.getBlockId(rotatedX, count, rotatedZ) != 7
									&& world.getBlockId(rotatedX, count, rotatedZ) != 8
									&& world.getBlockId(rotatedX, count, rotatedZ) != 9
									&& world.getBlockId(rotatedX, count, rotatedZ) != 10
									&& world.getBlockId(rotatedX, count, rotatedZ) != 11
									&& world.getBlockId(rotatedX, count, rotatedZ) != 12
									&& world.getBlockId(rotatedX, count, rotatedZ) != 13
									&& world.getBlockId(rotatedX, count, rotatedZ) != 24){
								world.setBlock(rotatedX, count, rotatedZ, 0, 0, 2);
								if (world.getBlockId(rotatedX+1, count, rotatedZ) != 1
										&& world.getBlockId(rotatedX+1, count, rotatedZ) != 2
										&& world.getBlockId(rotatedX+1, count, rotatedZ) != 3
										&& world.getBlockId(rotatedX+1, count, rotatedZ) != 4
										&& world.getBlockId(rotatedX+1, count, rotatedZ) != 7
										&& world.getBlockId(rotatedX+1, count, rotatedZ) != 8
										&& world.getBlockId(rotatedX+1, count, rotatedZ) != 9
										&& world.getBlockId(rotatedX+1, count, rotatedZ) != 10
										&& world.getBlockId(rotatedX+1, count, rotatedZ) != 11
										&& world.getBlockId(rotatedX+1, count, rotatedZ) != 12
										&& world.getBlockId(rotatedX+1, count, rotatedZ) != 13
										&& world.getBlockId(rotatedX+1, count, rotatedZ) != 24){
									world.setBlock(rotatedX+1, count, rotatedZ, 0, 0, 2);
								}
								if (world.getBlockId(rotatedX+1, count, rotatedZ+1) != 1
										&& world.getBlockId(rotatedX+1, count, rotatedZ+1) != 2
										&& world.getBlockId(rotatedX+1, count, rotatedZ+1) != 3
										&& world.getBlockId(rotatedX+1, count, rotatedZ+1) != 4
										&& world.getBlockId(rotatedX+1, count, rotatedZ+1) != 7
										&& world.getBlockId(rotatedX+1, count, rotatedZ+1) != 8
										&& world.getBlockId(rotatedX+1, count, rotatedZ+1) != 9
										&& world.getBlockId(rotatedX+1, count, rotatedZ+1) != 10
										&& world.getBlockId(rotatedX+1, count, rotatedZ+1) != 11
										&& world.getBlockId(rotatedX+1, count, rotatedZ+1) != 12
										&& world.getBlockId(rotatedX+1, count, rotatedZ+1) != 13
										&& world.getBlockId(rotatedX+1, count, rotatedZ+1) != 24){
									world.setBlock(rotatedX+1, count, rotatedZ+1, 0, 0, 2);
								}
								if (world.getBlockId(rotatedX, count, rotatedZ+1) != 1
										&& world.getBlockId(rotatedX, count, rotatedZ+1) != 2
										&& world.getBlockId(rotatedX, count, rotatedZ+1) != 3
										&& world.getBlockId(rotatedX, count, rotatedZ+1) != 4
										&& world.getBlockId(rotatedX, count, rotatedZ+1) != 7
										&& world.getBlockId(rotatedX, count, rotatedZ+1) != 8
										&& world.getBlockId(rotatedX, count, rotatedZ+1) != 9
										&& world.getBlockId(rotatedX, count, rotatedZ+1) != 10
										&& world.getBlockId(rotatedX, count, rotatedZ+1) != 11
										&& world.getBlockId(rotatedX, count, rotatedZ+1) != 12
										&& world.getBlockId(rotatedX, count, rotatedZ+1) != 13
										&& world.getBlockId(rotatedX, count, rotatedZ+1) != 24){
									world.setBlock(rotatedX, count, rotatedZ+1, 0, 0, 2);
								}
							}
						}
						//Generate berm
						if (Math.abs(ycount - ground) <= 2 && (Math.abs(ycount - ground) > 0 && ycount > ground)){
							for (int count = ground; count < ycount; ++count){
								world.setBlock(rotatedX,count,rotatedZ,4,0,2);
								world.setBlock(rotatedX+1,count,rotatedZ,4,0,2);
								world.setBlock(rotatedX+1,count,rotatedZ+1,4,0,2);
								world.setBlock(rotatedX,count,rotatedZ+1,4,0,2);
							}
						}
						//Generate supporting pillars
						if (Math.abs(ycount - ground) > 2 && ycount > ground && progress % 9 == 0 && (crosspave == width+1 || crosspave == Math.abs(width)-1)){
							for (int count = ground; count < ycount; ++count){
								world.setBlock(rotatedX,count,rotatedZ,4,0,2);
								world.setBlock(rotatedX+1,count,rotatedZ,4,0,2);
								world.setBlock(rotatedX+1,count,rotatedZ+1,4,0,2);
								world.setBlock(rotatedX,count,rotatedZ+1,4,0,2);
							}
						}
						//Generate road base layer (under asphalt)
						world.setBlock(rotatedX,ycount-1,rotatedZ,4,0,2);
						world.setBlock(rotatedX+1,ycount-1,rotatedZ,4,0,2);
						world.setBlock(rotatedX+1,ycount-1,rotatedZ+1,4,0,2);
						world.setBlock(rotatedX,ycount-1,rotatedZ+1,4,0,2);
						
						//Generate asphalt
						world.setBlock(rotatedX,ycount,rotatedZ,35,15,2);
						world.setBlock(rotatedX+1,ycount,rotatedZ,35,15,2);
						world.setBlock(rotatedX+1,ycount,rotatedZ+1,35,15,2);
						world.setBlock(rotatedX,ycount,rotatedZ+1,35,15,2);
					}
					
					//Generate lane lines
					if (ycount == rotatedY && crosspave == 0 && progress % 6 != 0){
						world.setBlock(rotatedX,ycount,rotatedZ,35,4,2);
						world.setBlock(rotatedX+1,ycount,rotatedZ,35,4,2);
						world.setBlock(rotatedX+1,ycount,rotatedZ+1,35,4,2);
						world.setBlock(rotatedX,ycount,rotatedZ+1,35,4,2);
					}
					
					//Carve out empty space for road to travel through
					if (ycount > rotatedY){
						world.setBlock(rotatedX,ycount,rotatedZ,0,0,2);
						world.setBlock(rotatedX+1,ycount,rotatedZ,0,0,2);
						world.setBlock(rotatedX+1,ycount,rotatedZ+1,0,0,2);
						world.setBlock(rotatedX,ycount,rotatedZ+1,0,0,2);
					}
				}
				for(int ycount = rotatedY+10; ycount <= (rotatedY+20); ycount++){
					int groundcount = 0;
					if (world.getBlockId(rotatedX,ycount,rotatedZ) == 1
							|| world.getBlockId(rotatedX,ycount,rotatedZ) == 2
							|| world.getBlockId(rotatedX,ycount,rotatedZ) == 3
							|| world.getBlockId(rotatedX,ycount,rotatedZ) == 7
							|| world.getBlockId(rotatedX,ycount,rotatedZ) == 12
							|| world.getBlockId(rotatedX,ycount,rotatedZ) == 13
							|| world.getBlockId(rotatedX,ycount,rotatedZ) == 24){
						++groundcount;
					}
					else {
						world.setBlock(rotatedX,ycount,rotatedZ,0,0,2);
						world.setBlock(rotatedX+1,ycount,rotatedZ,0,0,2);
						world.setBlock(rotatedX+1,ycount,rotatedZ+1,0,0,2);
						world.setBlock(rotatedX,ycount,rotatedZ+1,0,0,2);
					}
					if (groundcount >= 2){
						ycount = 21;
					}
				}
			}
		}
    }

	
	public void generateEnd(World world, Random random, int chunkX, int chunkZ){		
		return;
	}
	
	public void generateNether(World world, Random random, int chunkX, int chunkZ){
		return;
	}

}
