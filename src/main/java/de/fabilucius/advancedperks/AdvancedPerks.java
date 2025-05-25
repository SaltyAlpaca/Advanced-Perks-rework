package de.fabilucius.advancedperks;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.fabilucius.advancedperks.api.AdvancedPerksAPI;
import de.fabilucius.advancedperks.api.manager.PerkManager;
import de.fabilucius.advancedperks.command.PerksCommand;
import de.fabilucius.advancedperks.core.command.AbstractCommand;
import de.fabilucius.advancedperks.core.logging.APLogger;
import de.fabilucius.advancedperks.core.module.ConfigurationModule;
import de.fabilucius.advancedperks.core.module.PrimaryModule;
import de.fabilucius.advancedperks.exception.AdvancedPerksException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;


public class AdvancedPerks extends JavaPlugin {

    @Inject
    private APLogger logger;

    @Inject
    private Injector injector;
    @Inject
    private PerkManager perkManager;

    @Inject
    private AdvancedPerksAPI advancedPerksApi;

    @Override
    public void onEnable() {
        createDefaultConfigFiles();

        Injector injectorInstance = Guice.createInjector(
                new PrimaryModule(this),
                new ConfigurationModule()
        );
        injectorInstance.injectMembers(this);

        try {
            this.logger.info("Beginning the bootstrap process of the plugin.");
            this.injector.getInstance(AdvancedPerksBootstrap.class).initializePlugin();
            this.logger.info("Successfully finished the bootstrap process of the plugin.");

            // Initialize PerkManager after all injections are complete
            this.perkManager.initialize();

        } catch (AdvancedPerksException exception) {
            this.logger.log(Level.SEVERE, "An unexpected error occurred during the bootstrap process of the plugin.", exception);
        }

        // Register commands dynamically
        registerCommand("perks", injectorInstance.getInstance(PerksCommand.class));
    }
    private void createDefaultConfigFiles() {
        createFileIfMissing("database.yml");
        createFileIfMissing("messages.yml");
        createFileIfMissing("perk_gui.yml");
        createFileIfMissing("settings.yml");
        createFileIfMissing("perks.yml"); // Add perks.yml here
    }


    private void createFileIfMissing(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try (InputStream in = getResource(fileName)) {
                if (in != null) {
                    Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    getLogger().log(Level.INFO, () -> fileName + " was missing and has been created."); // Log successful creation as INFO
                } else {
                    getLogger().warning(fileName + " was missing but no default found in resources.");
                }
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, e, () -> "Could not create default " + fileName); // Log failures as SEVERE
            }
        }
    }
    public void registerCommand(String commandName, AbstractCommand executor) {
        try {
            // Access Bukkit's CommandMap using reflection
            final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            final CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            // Create a PluginCommand instance for the command
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand command = constructor.newInstance(commandName, this);

            // Set the executor and tab completer
            command.setExecutor(executor);
            command.setTabCompleter(executor);

            // Register the command with CommandMap
            commandMap.register(commandName, command);

            getLogger().log(Level.INFO, () -> "Registered command: " + commandName);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, String.format("Failed to register command: %s", commandName), e);
        }
    }
    public PerkManager getPerkManager() {
        return perkManager;
    }

    public AdvancedPerksAPI getAdvancedPerksApi() {
        return advancedPerksApi;
    }
    public Injector getInjector() {
        return injector;
    }
    @Override
    public void onDisable() {
        this.injector.getInstance(AdvancedPerksBootstrap.class).shutdownPlugin();
    }
}
