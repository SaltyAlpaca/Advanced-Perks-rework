package de.fabilucius.advancedperks.data;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import de.fabilucius.advancedperks.AdvancedPerks;
import de.fabilucius.advancedperks.core.database.Database;
import de.fabilucius.advancedperks.core.logging.APLogger;
import de.fabilucius.advancedperks.data.state.PerkStateController;
import de.fabilucius.advancedperks.registry.PerkRegistryImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class PerkDataRepository implements Listener {

    private final AdvancedPerks advancedPerks;

    @Inject
    private APLogger logger;

    @Inject
    private Database database;

    @Inject
    private PerkRegistryImpl perkRegistryImpl;

    @Inject
    private PerkStateController perkStateController;

    private final Cache<UUID, PerkData> perkDataCache = CacheBuilder.newBuilder().build();

    @Inject
    public PerkDataRepository(AdvancedPerks advancedPerks) {
        this.advancedPerks = advancedPerks;
        Bukkit.getPluginManager().registerEvents(this, this.advancedPerks);
    }

    public void setupDatabase() {
        this.database.connectToDatabase();
        this.database.runSqlScript("sql/2023.sql");
    }

    public boolean migratePerkData() {
        return this.database.runPerkDataMigrateScript();
    }

    public void loadOnlinePlayer() {
        Bukkit.getOnlinePlayers().forEach(player -> this.getPerkDataByUuid(player.getUniqueId()));
    }

    @NotNull
    public CompletableFuture<PerkData> getPerkDataByUuid(UUID uuid) {
        PerkData perkData = this.perkDataCache.getIfPresent(uuid);
        if (perkData != null) {
            return CompletableFuture.supplyAsync(() -> perkData);
        } else {
            return this.loadPerkDataAsync(uuid);
        }
    }

    @NotNull
    public PerkData getPerkDataByPlayer(Player player) {
        PerkData perkData = this.perkDataCache.getIfPresent(player.getUniqueId());
        if (perkData == null) {
            UnloadedPerkData createdPerkData = new UnloadedPerkData(player.getUniqueId());
            this.perkDataCache.put(player.getUniqueId(), createdPerkData);
            this.loadPerkDataAsync(player.getUniqueId()).thenAcceptAsync(perkData1 ->
                    this.perkDataCache.put(perkData1.getUuid(), perkData1));
            return createdPerkData;
        }
        return perkData;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UnloadedPerkData unloadedPerkData = new UnloadedPerkData(event.getPlayer().getUniqueId());
        this.perkDataCache.put(unloadedPerkData.getUuid(), unloadedPerkData);
        this.loadPerkDataAsync(unloadedPerkData.getUuid()).thenAcceptAsync(perkData ->
                this.perkDataCache.put(perkData.getUuid(), perkData));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        PerkData perkData = this.perkDataCache.getIfPresent(playerUUID);

        if (perkData != null) {
            // Save perk data asynchronously on player quit
            this.savePerkDataAsync(perkData);

            this.perkDataCache.invalidate(playerUUID);
        }
    }




    public CompletableFuture<PerkData> loadPerkDataAsync(UUID uniqueId) {
        LoadPerkDataTask loadTask = new LoadPerkDataTask(
                this.advancedPerks,
                this.logger,
                this.perkRegistryImpl,
                this.database,
                this.perkStateController,
                uniqueId
        );
        return loadTask.execute();
    }



    public void savePerkDataAsync(PerkData perkData) {
        Bukkit.getScheduler().runTaskAsynchronously(this.advancedPerks, () -> {
            try {
                this.savePerkDataSync(perkData);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to save perk data for UUID: " + perkData.getUuid(), e);
            }
        });
    }


    private void savePerkDataSync(PerkData perkData) {

        if (!perkData.isLoaded() || Arrays.equals(perkData.getDataHash(), perkData.calculateDataHash())) {
            return;
        }
        this.database.savePerkData(perkData);
    }


    public void handleShutdown() {
        this.perkDataCache.asMap().values().forEach(this::savePerkDataSync);
    }
}
