package ltd.teaparty.whitelist4qq;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config {
    private final Whitelist4qq plugin;



    public static String KickMessage;


    public static List<Long> UsedBotAccounts;
    public static List<Long> UsedGroupAccounts;
    public static boolean UseGroupMessageCommand;
    public static String BindCommandPrefix;
    public static String Messages_BindSuccess;
    public static String Messages_BindFailed;
    public static String Mysql_url;
    public static String Mysql_user;
    public static String Mysql_password;

    Config(Whitelist4qq plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        KickMessage = config.getString("general.kick-message", "");
        Mysql_url = config.getString("general.Mysql_url", "");
        Mysql_user = config.getString("general.Mysql_user", "");
        Mysql_password = config.getString("general.Mysql_password", "");
        UsedBotAccounts = config.getLongList("bot.used-bot-accounts");
        UsedGroupAccounts = config.getLongList("bot.used-group-numbers");
        UseGroupMessageCommand = config.getBoolean("bot.use-group-message-command", true);
        BindCommandPrefix = config.getString("bot.bind-command-prefix");
        Messages_BindSuccess = config.getString("bot.messages.bind-success");
        Messages_BindFailed = config.getString("bot.messages.bind-failed");
    }
}