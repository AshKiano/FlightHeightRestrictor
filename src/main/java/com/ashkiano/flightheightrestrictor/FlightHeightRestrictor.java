package com.ashkiano.flightheightrestrictor;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

        boolean showDonateMessage = getConfig().getBoolean("ShowDonateMessage", true);
        if (showDonateMessage) {
            this.getLogger().info("Thank you for using the FlightHeightRestrictor plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://donate.ashkiano.com");
        }
        checkForUpdates();
    }

    private void loadConfig() {
        maxHeight = getConfig().getInt("max-height");
        teleportDistance = getConfig().getInt("teleport-distance");
        checkInterval = getConfig().getInt("check-interval");
        worldWhitelist = getConfig().getStringList("world-whitelist");
    }

    private void checkForUpdates() {
        try {
            String pluginName = this.getDescription().getName();
            URL url = new URL("https://plugins.ashkiano.com/version_check.php?plugin=" + pluginName);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String jsonResponse = response.toString();
                JSONObject jsonObject = new JSONObject(jsonResponse);
                if (jsonObject.has("error")) {
                    this.getLogger().warning("Error when checking for updates: " + jsonObject.getString("error"));
                } else {
                    String latestVersion = jsonObject.getString("latest_version");

                    String currentVersion = this.getDescription().getVersion();
                    if (currentVersion.equals(latestVersion)) {
                        this.getLogger().info("This plugin is up to date!");
                    } else {
                        this.getLogger().warning("There is a newer version (" + latestVersion + ") available! Please update!");
                    }
                }
            } else {
                this.getLogger().warning("Failed to check for updates. Response code: " + responseCode);
            }
        } catch (Exception e) {
            this.getLogger().warning("Failed to check for updates. Error: " + e.getMessage());
        }
    }
}