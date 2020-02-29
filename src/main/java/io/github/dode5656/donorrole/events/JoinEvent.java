package io.github.dode5656.donorrole.events;

import io.github.dode5656.donorrole.DonorRole;
import io.github.dode5656.donorrole.utilities.Message;
import io.github.dode5656.donorrole.utilities.MessageManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JoinEvent implements Listener {
    private DonorRole plugin;

    public JoinEvent(DonorRole plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PostLoginEvent e) {
        JDA jda = plugin.getJDA();
        ProxiedPlayer player = e.getPlayer();
        Configuration playerCache = plugin.getPlayerCache().read();
        MessageManager messageManager = plugin.getMessageManager();

        if (playerCache == null) return;
        if (playerCache.contains("verified." + player.getUniqueId().toString())) {
            Guild guild = jda.getGuildById(plugin.getConfig().getString("server-id"));

            if (guild == null) {

                player.sendMessage(new TextComponent(messageManager.format(Message.ERROR)));
                plugin.getLogger().severe(Message.INVALIDSERVERID.getMessage());

                return;

            }

            Member member = guild.getMemberById(playerCache.getString("verified." + player.getUniqueId().toString()));

            if (member == null) return;

            List<Role> memberRoles = member.getRoles();

            Collection<String> roles = plugin.getConfig().getSection("roles").getKeys();
            List<String> roleIDs = new ArrayList<>();
            List<String> removed = new ArrayList<>();
            for (String role : roles) {
                String value = plugin.getConfig().getSection("roles").getString(role);
                if (player.hasPermission("donorrole.role." + role) && !memberRoles.contains(guild.getRoleById(value))) {
                    roleIDs.add(value);
                } else if (!player.hasPermission("donorrole.role." + role) && memberRoles.contains(guild.getRoleById(value))) {
                    removed.add(value);
                }

            }

            if (roleIDs.isEmpty() && removed.isEmpty()) return;

            for (String roleID : roleIDs) {
                Role role = guild.getRoleById(roleID);
                if (role == null) {
                    continue;
                }
                guild.addRoleToMember(member, role).queue();
            }

            for (String roleID: removed) {
                Role role = guild.getRoleById(roleID);
                if (role == null) continue;
                guild.removeRoleFromMember(member, role).queue();
            }

            player.sendMessage(new TextComponent(messageManager.format(Message.UPDATEDROLES)));
        }

    }

}
