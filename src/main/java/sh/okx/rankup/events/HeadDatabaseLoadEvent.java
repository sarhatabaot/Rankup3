package sh.okx.rankup.events;

import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import sh.okx.rankup.RankupPlugin;


public class HeadDatabaseLoadEvent implements Listener {
    private final RankupPlugin plugin;

    public HeadDatabaseLoadEvent(final RankupPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent e) {
        plugin.setHeadDatabaseAPI(new HeadDatabaseAPI());
    }
}
