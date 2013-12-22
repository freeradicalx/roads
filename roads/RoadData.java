package freeradicalx.roads;

import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapStorage;

public class RoadData extends WorldSavedData {

	public RoadData(String par1Str) {
		super(par1Str);
	}

	final static String key = "roads";
	
	NBTTagCompound roadChunks = new NBTTagCompound();
	NBTTagCompound roadIDs = new NBTTagCompound();
	NBTTagCompound roadPoints = new NBTTagCompound();
	NBTTagCompound roadDirections = new NBTTagCompound();

	public static RoadData forWorld(World world) {		
		MapStorage storage = world.perWorldStorage;
		RoadData result = (RoadData)storage.loadData(RoadData.class, key);
		if (result == null) {
			result = new RoadData("roads");
			storage.setData(key, result);
		}
		return result;
	}
	
	public void addRoad(Chunk chunk){
		ChunkCoordIntPair pair = chunk.getChunkCoordIntPair();
		int x = pair.chunkXPos;
		int z = pair.chunkZPos;
		roadChunks.setBoolean(x+","+z, true);
	}
	
	//Check to make sure the chunk exists before calling this or the result could be null and probably crash stuff.
	public boolean hasRoad(Chunk chunk){
		ChunkCoordIntPair pair = chunk.getChunkCoordIntPair();
		int x = pair.chunkXPos;
		int z = pair.chunkZPos;
		boolean hasRoad = roadChunks.getBoolean(x+","+z);
		return hasRoad;
	}
	
	public void addPoints(Chunk chunk, int[] curve){
		//System.out.println("Entered the addPoints method");
		ChunkCoordIntPair pair = chunk.getChunkCoordIntPair();
		int x = pair.chunkXPos;
		int z = pair.chunkZPos;
		roadPoints.setIntArray(x+","+z, curve);
	}
	
	//Use hasRoad on the chunk before calling this, otherwise potential null errors
	public int[] getPoints(Chunk chunk){
		ChunkCoordIntPair pair = chunk.getChunkCoordIntPair();
		int x = pair.chunkXPos;
		int z = pair.chunkZPos;
		return roadPoints.getIntArray(x+","+z);
	}
	
	public void setHeading(Chunk chunk, float heading){
		double curviness = 0.25;	// Higher value == curvier roads
		float variance = (float) (curviness - (Math.random() * (curviness * 2)));
		float direction = heading + variance;
		ChunkCoordIntPair pair = chunk.getChunkCoordIntPair();
		int x = pair.chunkXPos;
		int z = pair.chunkZPos;
		//System.out.println("variance("+variance+") + heading("+heading+") = direction ("+direction+")");
		roadDirections.setFloat(x+","+z, direction);
	}
	
	public void setDirection(Chunk chunk, float direction){
		ChunkCoordIntPair pair = chunk.getChunkCoordIntPair();
		int x = pair.chunkXPos;
		int z = pair.chunkZPos;
		roadDirections.setFloat(x+","+z, direction);
	}
	
	public float getDirection(Chunk chunk){
		ChunkCoordIntPair pair = chunk.getChunkCoordIntPair();
		int x = pair.chunkXPos;
		int z = pair.chunkZPos;
		return roadDirections.getFloat(x+","+z);
	}
	
	public void setID(Chunk chunk, double roadID){
		ChunkCoordIntPair pair = chunk.getChunkCoordIntPair();
		int x = pair.chunkXPos;
		int z = pair.chunkZPos;
		roadIDs.setDouble(x+","+z, roadID);
	}
	
	public double getID(Chunk chunk){
		ChunkCoordIntPair pair = chunk.getChunkCoordIntPair();
		int x = pair.chunkXPos;
		int z = pair.chunkZPos;
		double roadID = roadIDs.getDouble(x+","+z);
		return roadID;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		// TODO Auto-generated method stub
		
	}
	
}
