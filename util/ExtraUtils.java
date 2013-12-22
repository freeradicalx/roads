package freeradicalx.util;

import net.minecraft.world.World;

//This class provides general purpose tools and utilities for my mods and for debugging
public class ExtraUtils {
	
	//Find the height of terra firma, the ground itself, as opposed to a tree or wood that might bug with the results of world.getHeightValue()
	public static int groundHeight(World world, int x, int z){
		
		int y = world.getHeightValue(x,z);
		
		while (world.getBlockId(x,y,z) != 1
				&& world.getBlockId(x,y,z) != 2
				&& world.getBlockId(x,y,z) != 3
				&& world.getBlockId(x,y,z) != 7
				&& world.getBlockId(x,y,z) != 8
				&& world.getBlockId(x,y,z) != 9
				&& world.getBlockId(x,y,z) != 10
				&& world.getBlockId(x,y,z) != 11
				&& world.getBlockId(x,y,z) != 12
				&& world.getBlockId(x,y,z) != 13
				&& world.getBlockId(x,y,z) != 24) {
			y -= 1;
		}
		
		//This if statement will set ground height 3 blocks above any water or lava, to give roads using this method stilts instead of sitting on the surface.
		if (world.getBlockId(x,y,z) != 8
			|| world.getBlockId(x,y,z) != 9
			|| world.getBlockId(x,y,z) != 10
			|| world.getBlockId(x,y,z) != 11) {
			y = y + 3;
		}
		
		return y;
	}
	
	public static int groundHeightSea(World world, int x, int z){
		
		int y = world.getHeightValue(x,z);
		
		while ((world.getBlockId(x,y,z) != 1
				&& world.getBlockId(x,y,z) != 2
				&& world.getBlockId(x,y,z) != 3
				&& world.getBlockId(x,y,z) != 7
				&& world.getBlockId(x,y,z) != 12
				&& world.getBlockId(x,y,z) != 13
				&& world.getBlockId(x,y,z) != 24) && y > 1) {
			y -= 1;
		}
		//System.out.println("groundHeightSea: "+y);
		return y;
	}

	//Draw a line in wool between two points, useful for debugging worldgen pathfinding
	public static void drawLine(World world, int x1, int z1, int x2, int z2){
		
		//Assign differences
		int diffX = Math.abs(x1 - x2);
		if (x1 > x2){
			diffX = -diffX;
		}
		int diffZ = Math.abs(z1 - z2);
		if (z1 > z2){
			diffZ = -diffZ;
		}
	
		//Find the angle of the line to be drawn from the difference
		float angle = 0;
		if (diffX != 0){
			if (diffX > 0){
				angle = (float) Math.atan(diffZ/diffX);
			}
			if (diffX < 0){
				angle = (float) (Math.atan(diffZ/diffX) + Math.PI);
			}
		}
		if (diffX == 0) {
			if (diffZ > 0){
				angle = (float) 1.57;
			}
			if (diffZ <= 0){
				angle = (float) -1.57;
			}
		}
		
		//Calculate the length of the line to be drawn
		int length = (int) Math.sqrt((diffX*diffX)+(diffZ*diffZ));
		
		int x,y,z;
		y = 100;
		for (int progress = 0; progress <= length; ++progress){
			x = (int)Math.round(x1 + (progress * Math.cos(angle)) - (diffZ * Math.sin(angle)));
			z = (int)Math.round(x1 + (progress * Math.cos(angle)) + (diffZ * Math.sin(angle)));
			world.setBlock(x,y,z,43,1,2);
		}
	}

	//TODO: MAKE THIS METHOD
	public static float findAngleInRadians(int x1, int z1, int x2, int z2){
		return (float) 0;
	}

}