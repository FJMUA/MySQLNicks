package co.ignitus.mysqlnicks.commands;

import co.ignitus.mysqlnicks.MySQLNicks;
import co.ignitus.mysqlnicks.Pair;
import co.ignitus.mysqlnicks.hook.VaultHook;
import co.ignitus.mysqlnicks.util.DataUtil;
import co.ignitus.mysqlnicks.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class NickCMD implements CommandExecutor {

    final MySQLNicks mySQLNicks = MySQLNicks.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ("nickhistory".equalsIgnoreCase(label)) {
            if (args.length < 1) {
                sender.sendMessage(MessageUtil.getMessage("nickhis.insufficient-arguments"));
                return true;
            }

            if (!sender.hasPermission("mysqlnicks.nickhis")) {
                sender.sendMessage(MessageUtil.getMessage("nickhis.no-permission"));
                return true;
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            Bukkit.getScheduler().runTaskAsynchronously(MySQLNicks.getInstance(), () -> {
                List<Pair<String, String>> oldNewHisNickList = DataUtil.getSavedHistoryNicknames(player.getUniqueId());
                if (oldNewHisNickList.isEmpty()) {
                    sender.sendMessage(String.format(MessageUtil.getMessage("nickhis.no-result"), args[0], player.getUniqueId()));
                    return;
                }
                StringBuilder builder = new StringBuilder("历史改名记录:");
                for (Pair<String, String> oldNewHis : oldNewHisNickList) {
                    builder.append('\n')
                            .append("old(").append(oldNewHis.getK()).append(')')
                            .append("->")
                            .append("new(").append(oldNewHis.getK()).append(')');
                }
                sender.sendMessage(builder.toString());
            });
            return true;
        } else if ("nick".equalsIgnoreCase(label)) {
            if (args.length < 1) {
                sender.sendMessage(MessageUtil.getMessage("nick.insufficient-arguments"));
                return true;
            }

            if (!sender.hasPermission("mysqlnicks.nick")) {
                sender.sendMessage(MessageUtil.getMessage("nick.no-permission"));
                return true;
            }

            //The player who's nickname is been changed
            final Player target;
            String nickname;
            if (args.length == 2 && sender.hasPermission("mysqlnicks.staff")) {
                target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(MessageUtil.getMessage("nick.invalid-player"));
                    return true;
                }
                nickname = args[1];
            } else {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(MessageUtil.getMessage("nick.no-console"));
                    return true;
                }
                target = ((Player) sender);
                nickname = args[0];
            }

            if (!VaultHook.hasMoney(mySQLNicks.getConfig().getDouble("cost"), target)) {
                sender.sendMessage(MessageUtil.getMessage("nick.target-no-money"));
                return true;
            }

            final FileConfiguration config = mySQLNicks.getConfig();
            if (config.getBoolean("limit.enabled")) {
                boolean includeColors = config.getBoolean("limit.include-color");
                int length = config.getInt("limit.length");
                String input = includeColors ? nickname : ChatColor.stripColor(MessageUtil.format(nickname));
                if (input.length() > length && !sender.hasPermission("mysqlnicks.bypass.limit")) {
                    sender.sendMessage(MessageUtil.getMessage("nick.exceeds-limit"));
                    return true;
                }
            }

            nickname = nickname.replace("§", "&");
            if (!sender.hasPermission("mysqlnicks.nick.color")) {
                if (!sender.hasPermission("mysqlnicks.nick.color.simple") && !MessageUtil.stripLegacy(nickname).equals(nickname)) {
                    sender.sendMessage(MessageUtil.getMessage("nick.no-codes"));
                    return true;
                }
                if (!sender.hasPermission("mysqlnicks.nick.color.hex") && !MessageUtil.stripHex(nickname).equals(nickname)) {
                    sender.sendMessage(MessageUtil.getMessage("nick.no-hex"));
                    return true;
                }
            }
            if (!sender.hasPermission("mysqlnicks.nick.format") && (nickname.contains("&l") ||
                    nickname.contains("&m") || nickname.contains("&n") || nickname.contains("&o") ||
                    nickname.contains("&r"))) {
                sender.sendMessage(MessageUtil.getMessage("nick.no-formatting"));
                return true;
            }
            if (!sender.hasPermission("mysqlnicks.nick.magic") && nickname.contains("&k")) {
                sender.sendMessage(MessageUtil.getMessage("nick.no-magic"));
                return true;
            }

            if (nickname.equalsIgnoreCase("off"))
                nickname = null;
            final String path;
            if (sender instanceof Player) {
                path = "nick." + (target.equals(sender) ? "" : "staff.");
            } else {
                path = "nick.staff.";
            }
            if (!VaultHook.withdraw(mySQLNicks.getConfig().getDouble("cost"), target)) {
                target.sendMessage(MessageUtil.getMessage("nick.target-failed"));
                return true;
            }
            Optional<String> oldNickname = mySQLNicks.getNameCacheService().getCache(target.getUniqueId());
            if (!DataUtil.setNickname(target.getUniqueId(), nickname)) {
                sender.sendMessage(MessageUtil.getMessage(path + "error",
                        "%player%", target.getName()));
                return true;
            }
            DataUtil.setHisNickname(target.getUniqueId(), oldNickname.orElseGet(target::getDisplayName), nickname);
            target.sendMessage(MessageUtil.getMessage("nick.target-success"));
            if (nickname == null) {
                sender.sendMessage(MessageUtil.getMessage(path + "unset",
                        "%player%", target.getName()));
                return true;
            }
            sender.sendMessage(MessageUtil.getMessage(path + "set",
                    "%player%", target.getName(),
                    "%nickname%", nickname));
            return true;
        }
        return true;
    }
}
