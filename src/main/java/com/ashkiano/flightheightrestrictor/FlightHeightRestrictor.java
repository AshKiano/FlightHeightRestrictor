package com.ashkiano.flightheightrestrictor;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class FlightHeightRestrictor extends JavaPlugin {

    private int maxHeight;
    private int teleportDistance;
    private int checkInterval;
    private List<String> worldWhitelist;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        Metrics metrics = new Metrics(this, 19367);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    World world = player.getWorld();
                    if (!worldWhitelist.contains(world.getName())) continue;

                    int playerHeight = player.getLocation().getBlockY();
                    if (playerHeight > maxHeight) {
                        if (player.isInsideVehicle() && player.getVehicle().getType() != EntityType.PLAYER) {
                            player.leaveVehicle();
                        }

                        player.teleport(player.getLocation().subtract(0, teleportDistance, 0));
                    }
                }
            }
        }.runTaskTimer(this, 0, checkInterval);

        this.getLogger().info("Thank you for using the FlightHeightRestrictor plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://donate.ashkiano.com");
    }

    private void loadConfig() {
        maxHeight = getConfig().getInt("max-height");
        teleportDistance = getConfig().getInt("teleport-distance");
        checkInterval = getConfig().getInt("check-interval");
        worldWhitelist = getConfig().getStringList("world-whitelist");
    }
}