package co.ignitus.mysqlnicks;

import co.ignitus.mysqlnicks.commands.NickCMD;
import co.ignitus.mysqlnicks.hook.VaultHook;
import co.ignitus.mysqlnicks.util.DataUtil;
import co.ignitus.mysqlnicks.util.PlaceholderUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class MySQLNicks extends JavaPlugin {

    @Getter
    private static MySQLNicks instance;

    private final CommandSender cs = Bukkit.getConsoleSender();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        if (!DataUtil.createDatabases()) {
            cs.sendMessage(ChatColor.RED + "[MySQLNicks] Unable to connect to the database. Disabling plugin...");
            getPluginLoader().disablePlugin(this);
            return;
        }
        cs.sendMessage(ChatColor.GREEN + "[MySQLNicks] Successfully established a connection with the database.");

        if (!VaultHook.init(instance)) {
            cs.sendMessage(ChatColor.RED + "[MySQLNicks] Vault hooks failed. Disabling plugin...");
            getPluginLoader().disablePlugin(this);
            return;
        }
        cs.sendMessage(ChatColor.GREEN + "[MySQLNicks] Hooked to Vault");

        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            cs.sendMessage(ChatColor.RED + "[MySQLNicks] PlaceholderAPI not found. Disabling plugin...");
            getPluginLoader().disablePlugin(this);
            return;
        }
        new PlaceholderUtil(this).register();
        cs.sendMessage(ChatColor.GREEN + "[MySQLNicks] Hooked to PlaceholderAPI");

        getCommand("nick").setExecutor(new NickCMD());
    }

    @Override
    public void onDisable() {
        instance = null;
    }

}
