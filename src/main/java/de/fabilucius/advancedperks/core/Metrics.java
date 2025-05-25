package de.fabilucius.advancedperks.core;

import de.fabilucius.advancedperks.AdvancedPerks;
import de.fabilucius.advancedperks.core.configuration.type.SettingsConfiguration;
import de.fabilucius.advancedperks.core.logging.APLogger;
import me.clip.placeholderapi.metrics.MetricsBase;
import me.clip.placeholderapi.metrics.json.JsonObjectBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;


import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class Metrics {

    private final Plugin plugin;

    public Metrics(AdvancedPerks plugin, int serviceId) {
        this.plugin = plugin;
        // Get the config file
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        if (!config.isSet("serverUuid")) {
            config.addDefault("enabled", true);
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            config.addDefault("logFailedRequests", false);
            config.addDefault("logSentData", false);
            config.addDefault("logResponseStatusText", false);
            config.options().header(
                    "bStats (https://bStats.org) collects some basic information for plugin authors, like how\n" +
                            "many people use their plugin and their total player count. It's recommended to keep bStats\n" +
                            "enabled, but if you're not comfortable with this, you can turn this setting off. There is no\n" +
                            "performance penalty associated with having metrics enabled, and data sent to bStats is fully\n" +
                            "anonymous.").copyDefaults(true);
            try {
                config.save(configFile);
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
        // Load the data
        boolean enabled = config.getBoolean("enabled", true);
        String serverUUID = config.getString("serverUuid");
        boolean logErrors = config.getBoolean("logFailedRequests", false);
        boolean logSentData = config.getBoolean("logSentData", false);
        boolean logResponseStatusText = config.getBoolean("logResponseStatusText", false);
        new MetricsBase(
                "bukkit",
                serverUUID,
                serviceId,
                enabled,
                this::appendPlatformData,
                this::appendServiceData,
                submitDataTask -> Bukkit.getScheduler().runTask(plugin, submitDataTask),
                plugin::isEnabled,
                (message, error) -> this.plugin.getLogger().log(Level.WARNING, message, error),
                message -> this.plugin.getLogger().log(Level.INFO, message),
                logErrors,
                logSentData,
                logResponseStatusText);
    }

    public static void load(APLogger logger, SettingsConfiguration settingsConfiguration, AdvancedPerks advancedPerks) {
        if (settingsConfiguration.shouldMetricsBeCollected()) {
            logger.info("Metrics collection has started, thanks for providing useful and anonymous metrics " +
                    "data to improve my software.");
            new Metrics(advancedPerks, 12771);
        } else {
            logger.info("Collecting of metrics was disabled in the config, please consider enabling it " +
                    "as it would help me improve my software with no additional cost for you.");
        }
    }


    private void appendPlatformData(JsonObjectBuilder builder) {
        builder.appendField("playerAmount", Bukkit.getOnlinePlayers().size());
        builder.appendField("onlineMode", Bukkit.getOnlineMode() ? 1 : 0);
        builder.appendField("bukkitVersion", Bukkit.getVersion());
        builder.appendField("bukkitName", Bukkit.getName());
        builder.appendField("javaVersion", System.getProperty("java.version"));
        builder.appendField("osName", System.getProperty("os.name"));
        builder.appendField("osArch", System.getProperty("os.arch"));
        builder.appendField("osVersion", System.getProperty("os.version"));
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
    }

    private void appendServiceData(JsonObjectBuilder builder) {
        builder.appendField("pluginVersion", plugin.getDescription().getVersion());
    }


}
