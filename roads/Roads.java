package freeradicalx.roads;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import freeradicalx.roads.lib.Reference;

@Mod(
		modid = Reference.MOD_ID,
		name = Reference.MOD_NAME,
		version = Reference.MOD_VERSION)

@NetworkMod(
		serverSideRequired = false,
		clientSideRequired = true)

public class Roads {
	
	WorldGenRoads roadGen = new WorldGenRoads();

	@PreInit
	public void preInit(FMLPreInitializationEvent event){
		
		GameRegistry.registerWorldGenerator(roadGen);
		
	}
	
	@Init
	public void Init(FMLInitializationEvent event){
		
		GameRegistry.removeBiome(BiomeGenBase.iceMountains);
		GameRegistry.removeBiome(BiomeGenBase.extremeHillsEdge);
		GameRegistry.removeBiome(BiomeGenBase.extremeHills);
		GameRegistry.removeBiome(BiomeGenBase.taiga);
		GameRegistry.removeBiome(BiomeGenBase.ocean);
		GameRegistry.removeBiome(BiomeGenBase.desert);
		GameRegistry.removeBiome(BiomeGenBase.jungle);
		GameRegistry.removeBiome(BiomeGenBase.jungleHills);
		GameRegistry.removeBiome(BiomeGenBase.swampland);
		GameRegistry.removeBiome(BiomeGenBase.icePlains);;
		
	}
	
	@PostInit
	public void PostInit(FMLPostInitializationEvent event){}
	
}
