package ltd.teaparty.whitelist4qq;

import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.api.MiraiMC;
import me.dreamvoid.miraimc.api.bot.MiraiGroup;
import me.dreamvoid.miraimc.bukkit.event.group.member.MiraiMemberLeaveEvent;
import me.dreamvoid.miraimc.bukkit.event.message.passive.MiraiGroupMessageEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static ltd.teaparty.whitelist4qq.Config.*;
import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST;

public class Whitelist4qq extends JavaPlugin implements Listener {
    private Config config;
    private final ArrayList<Player> cache = new ArrayList<>();

    @Override
    public void onLoad() {
        this.config = new Config(this);
    }

    @Override
    public void onEnable() {
        config.loadConfig();
        Bukkit.getPluginManager().registerEvents(new BindEvent(), this);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("whitelist4qq.admin")) {
            sender.sendMessage("作者：凛娜");
        }
        return true;
    }

    //玩家将要加入服务器（异步）
    public static class BindEvent implements Listener {
        @EventHandler
        public void onPlayerJoin(AsyncPlayerPreLoginEvent e) {
            boolean allow = false;
            try (Connection conn = DriverManager.getConnection(Mysql_url, Mysql_user, Mysql_password)) {
                String sql = "SELECT * FROM bind_qq WHERE game_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, e.getName());

                // 执行查询
                ResultSet rs = stmt.executeQuery();

                // 处理查询结果
                if (rs.next()) {
                    // 如果找到game_id，则继续执行后续操作
                    Bukkit.getLogger().info("玩家" + e.getName() + "已经有白名单了，允许登录");
                    // 在这里添加继续执行的代码
                } else {
                    // 如果没有找到game_id，则执行kickPlayer操作
                    Bukkit.getLogger().info("玩家" + e.getName() + "无法找到白名单，拒绝加入");
                    e.disallow(KICK_WHITELIST,"尚未注册白名单，群里发送 申请白名单+游戏ID 申请");
                }
                stmt.close();
            } catch (SQLException event) {
                event.printStackTrace();
            }
        }
        //监听群聊事件
        @EventHandler
        public void onGroupMessage(MiraiGroupMessageEvent e) {
            if (UsedBotAccounts.contains(e.getBotID()) && UsedGroupAccounts.contains(e.getGroupID()) && UseGroupMessageCommand && e.getMessage().startsWith(BindCommandPrefix)) {
                String playerName = e.getMessage().replace(BindCommandPrefix, "");
                long qq = e.getSenderID();
                MiraiBot.getBot(e.getBotID()).getGroup(e.getGroupID()).sendMessage("正在绑定"+playerName+"给qq号"+qq);
                try (Connection conn = DriverManager.getConnection(Mysql_url, Mysql_user, Mysql_password)) {
                    String insertSql = "INSERT INTO bind_qq (qq, game_id) VALUES (?, ?)";
                    PreparedStatement preparedStatement = conn.prepareStatement(insertSql);
                    // 设置占位符的值
                    preparedStatement.setString(1, String.valueOf(qq));
                    preparedStatement.setString(2, playerName);

                    // 执行INSERT语句
                    int rowsInserted = preparedStatement.executeUpdate();
                    if (rowsInserted > 0) {
                        MiraiBot.getBot(e.getBotID()).getGroup(e.getGroupID()).sendMessage(playerName+Messages_BindSuccess);
                        Bukkit.getLogger().info("玩家" + playerName + "申请了白名单，qq为" + String.valueOf(qq));
                    } else {
                        Bukkit.getLogger().info("玩家" + playerName + "反复申请白名单，拒绝请求");
                        MiraiBot.getBot(e.getBotID()).getGroup(e.getGroupID()).sendMessage(Messages_BindFailed.replace("%id%", Bukkit.getOfflinePlayer(MiraiMC.getBind(e.getSenderID())).getName()));
                    }
                    preparedStatement.close();
                } catch (SQLException event) {
                    event.printStackTrace();
                }
            }
        }
    }
}











