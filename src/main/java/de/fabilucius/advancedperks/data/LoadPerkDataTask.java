package de.fabilucius.advancedperks.data;

import de.fabilucius.advancedperks.AdvancedPerks;
import de.fabilucius.advancedperks.core.database.Database;
import de.fabilucius.advancedperks.core.logging.APLogger;
import de.fabilucius.advancedperks.data.state.PerkStateController;
import de.fabilucius.advancedperks.perk.Perk;
import de.fabilucius.advancedperks.registry.PerkRegistryImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class LoadPerkDataTask {

    private final AdvancedPerks advancedPerks;
    private final APLogger logger;
    private final PerkRegistryImpl perkRegistryImpl;
    private final Database database;
    private final PerkStateController perkStateController;
    private final UUID uniqueId;

    public LoadPerkDataTask(AdvancedPerks advancedPerks, APLogger logger, PerkRegistryImpl perkRegistryImpl, Database database, PerkStateController perkStateController, UUID uniqueId) {
        this.advancedPerks = advancedPerks;
        this.logger = logger;
        this.perkRegistryImpl = perkRegistryImpl;
        this.database = database;
        this.perkStateController = perkStateController;
        this.uniqueId = uniqueId;
    }

    public CompletableFuture<PerkData> execute() {
        return CompletableFuture.supplyAsync(() -> {
            PerkData perkData = new PerkData(uniqueId);
            String query = "SELECT * FROM ap_data WHERE unique_id = ?";

            try (PreparedStatement loadStatement = database.createPreparedStatement(query)) {
                loadStatement.setString(1, uniqueId.toString());
                ResultSet resultSet = loadStatement.executeQuery();

                if (resultSet.next()) {
                    List<Perk> enabledPerks = Arrays.stream(resultSet.getString("enabled_perks").split(","))
                            .map(perkRegistryImpl::getPerkByIdentifier)
                            .filter(Objects::nonNull)
                            .toList();

                    Bukkit.getScheduler().runTask(advancedPerks, () -> {
                        Player player = Bukkit.getPlayer(uniqueId);
                        if (player != null) {
                            enabledPerks.forEach(perk -> perkStateController.enablePerk(player, perk));
                        }
                    });
                    perkData.getBoughtPerks().addAll(Arrays.stream(resultSet.getString("bought_perks").split(",")).toList());
                    perkData.setDataHash(resultSet.getBytes("data_hash"));
                } else {
                    perkData.setLoaded();
                }
            } catch (Exception exception) {
                logger.log(Level.WARNING, "An error occurred while loading the PerkData for uniqueId %s.".formatted(perkData.getUuid().toString()), exception);
            }
            return perkData;
        });
    }
}
