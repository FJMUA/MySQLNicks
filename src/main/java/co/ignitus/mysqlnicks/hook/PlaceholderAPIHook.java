package co.ignitus.mysqlnicks.hook;

import co.ignitus.mysqlnicks.MySQLNicks;
import co.ignitus.mysqlnicks.util.DataUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

import static co.ignitus.mysqlnicks.util.MessageUtil.format;
import static co.ignitus.mysqlnicks.util.MessageUtil.stripColor;

// TODO
public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final MySQLNicks plugin;

    public PlaceholderAPIHook(MySQLNicks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "Ignitus Co; IllTamer";
    }

    @Override
    public String getIdentifier() {
        return "mysqlnicks";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.equalsIgnoreCase("nickname")) {
            Optional<String> nickname = plugin.getNameCacheService().getCache(player.getUniqueId());
            if (!nickname.isPresent()) {
                return player.getName();
            }
            return format(plugin.getConfig().getString("nickname-prefix", "") + nickname);
        }

        if (identifier.equalsIgnoreCase("nocolor") || identifier.equalsIgnoreCase("nocolour")) {
            Optional<String> nickname = plugin.getNameCacheService().getCache(player.getUniqueId());
            if (!nickname.isPresent()) {
                return player.getName();
            }
            String message = plugin.getConfig().getString("nickname-prefix", "") + nickname;
            return player.hasPermission("mysqlnicks.bypass.nocolor") ? format(message) : stripColor(message);
        }
        return null;
    }

}
