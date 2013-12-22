ROADS MOD by FREERADICALX for MINECRAFT 1.6.4

Use: Add procedurally generated two-lane roads to the Minecraft landscape that ebb, flow and cut through the existing terrain, meant to behave as much like actual engineered roads as possible.

This is my first mod. This is also my first (serious) Java project. It’s messy, but I’ve done my best to keep it more or less organized, commented and readable. But as the golden retriever with the beakers says: “LAWL I HAVE NO IDEA WHAT I’M DOING”. All suggestions / pointers / contributions / forks are welcome and encouraged! If you like what this mod does you’re welcome to integrate it into your own but please let me know about any improvements you make and give me credit for the original!

Development on this mod was begun in 1.5.2 and was ported to 1.6.4 and tested for upload to Git. Developed with Forge and MCP in Eclipse Juno on Mac OS X Mavericks (10.9).

To add to your mod: Add /freeradicalx/roads and freeradicalx/util to /mcp/src/minecraft.

Some explanation of the code:

/roads/WorldGenRoads.java: The actual world generator that rolls for a new road and checks for nearby roads to link to. Does all the heavy lifting of figuring out what road segments will be drawn where.

/roads/RoadData.java: Uses NBTTagCompounds to store lists of chunks that have roads in them and keeps track of road coordinates and road IDs (Roads have floating point numbers as identifiers so a road doesn’t, for example, loop back into itself)

/roads/Roads.java: Hook file required by FML

/roads/lib/Reference.java: Stores version info on the mod

/util/ExtraUtils.java: A toolbox. Stores various trigonometric methods used to draw the roads and methods used to determine proper ground height to build the roads on.

-freeradicalx
nathanscottrosenquist at gmail dot com