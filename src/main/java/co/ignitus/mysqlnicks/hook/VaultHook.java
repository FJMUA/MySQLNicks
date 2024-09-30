package co.ignitus.mysqlnicks.hook;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private static Economy economy = null;

    public static boolean init(Plugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public static boolean withdraw(double money, OfflinePlayer player) {
        EconomyResponse resp = economy.withdrawPlayer(player, money);
        return resp.transactionSuccess();
    }

}
