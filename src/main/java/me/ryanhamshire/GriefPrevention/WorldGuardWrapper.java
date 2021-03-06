package me.ryanhamshire.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

class WorldGuardWrapper
{
    private WorldGuardPlugin worldGuard = null;
    private WorldGuardPlatform worldGuardPlatform = null;

    public WorldGuardWrapper() throws ClassNotFoundException
    {
        this.worldGuard = (WorldGuardPlugin)GriefPrevention.instance.getServer().getPluginManager().getPlugin("WorldGuard");
        this.worldGuardPlatform = WorldGuard.getInstance().getPlatform();
    }

    public boolean canBuild(Location lesserCorner, Location greaterCorner, Player creatingPlayer)
    {
        World world = lesserCorner.getWorld();
        com.sk89q.worldedit.world.World weWorld = worldGuardPlatform.getWorldByName(world.getName());

        if (worldGuard == null)
        {
            GriefPrevention.AddLogEntry("WorldGuard is out of date and not enabled. Please update or remove WorldGuard.", CustomLogEntryTypes.Debug, false);
            return true;
        }
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(creatingPlayer);

        if(new RegionPermissionModel(localPlayer).mayIgnoreRegionProtection(weWorld)) return true;

        RegionManager manager = worldGuardPlatform.getRegionContainer().get(weWorld);
        
        if(manager != null)
        {
            ProtectedCuboidRegion tempRegion = new ProtectedCuboidRegion(
                "GP_TEMP",
                new BlockVector(lesserCorner.getX(), 0, lesserCorner.getZ()),
                new BlockVector(greaterCorner.getX(), world.getMaxHeight(), greaterCorner.getZ()));

			ApplicableRegionSet overlaps = manager.getApplicableRegions(tempRegion);

            for (ProtectedRegion r : overlaps.getRegions()) {
                if (!manager.getApplicableRegions(r).testState(localPlayer, Flags.BUILD)) {
                    return false;
                }
            }
            return true;
        }
        
        return true;
    }
}
