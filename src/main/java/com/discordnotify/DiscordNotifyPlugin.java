package com.discordnotify.discordnotify;

import javax.security.auth.login.LoginException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class DiscordNotifyPlugin extends JavaPlugin {

    private JDA jda;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initializeDiscordBot();
        getLogger().info("DiscordNotify est activé !");
        FileConfiguration config = getConfig();
        String channelId = config.getString("channel-id");
        String stopMessage = config.getString("start-message", "Le serveur Minecraft vient de démmarer !");

        sendDiscordMessage(channelId, stopMessage);
    }

    @Override
    public void onDisable() {
        FileConfiguration config = getConfig();
        String channelId = config.getString("channel-id");
        String stopMessage = config.getString("stop-message", "Le serveur Minecraft vient de s'arrêter !");

        sendDiscordMessage(channelId, stopMessage);

        if (jda != null) {
            jda.shutdown();
        }

        getLogger().info("DiscordNotify est désactivé !");
    }

    private void initializeDiscordBot() {
        FileConfiguration config = getConfig();
        String botToken = config.getString("bot-token");

        try {
            if (jda != null) {
                jda.shutdown(); // Redémarre proprement si déjà initialisé
            }

            jda = JDABuilder.createDefault(botToken).build();
            jda.awaitReady(); // Attend que le bot soit prêt

            getLogger().info("Bot Discord connecté !");
        } catch (LoginException | InterruptedException e) {
            getLogger().severe("Erreur lors de la connexion au bot Discord : " + e.getMessage());
        }
    }

    private void sendDiscordMessage(String channelId, String message) {
        if (jda != null && channelId != null && message != null) {
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel != null) {
                channel.sendMessage(message).queue();
            } else {
                getLogger().warning("Canal Discord introuvable pour l'ID : " + channelId);
            }
        } else {
            getLogger().warning("Message ou canal Discord non configuré !");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("dsn")) {
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                sender.sendMessage("§aDiscordNotify Commands:");
                sender.sendMessage("§e/dsn reload §f- Recharge la configuration.");
                sender.sendMessage("§e/dsn test 1 §f- Teste le message de démarrage.");
                sender.sendMessage("§e/dsn test 2 §f- Teste le message de fermeture.");
                sender.sendMessage("§e/dsn help §f- Affiche cette liste d'aide.");
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                initializeDiscordBot();
                sender.sendMessage("§aConfiguration rechargée avec succès !");
                return true;
            }

            if (args[0].equalsIgnoreCase("test")) {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /dsn test <1|2>");
                    return true;
                }

                FileConfiguration config = getConfig();
                String channelId = config.getString("channel-id");

                if (args[1].equals("1")) {
                    String startMessage = config.getString("start-message", "Le serveur Minecraft vient de démarrer !");
                    sendDiscordMessage(channelId, startMessage);
                    sender.sendMessage("§aMessage de démarrage envoyé !");
                } else if (args[1].equals("2")) {
                    String stopMessage = config.getString("stop-message", "Le serveur Minecraft vient de s'arrêter !");
                    sendDiscordMessage(channelId, stopMessage);
                    sender.sendMessage("§aMessage de fermeture envoyé !");
                } else {
                    sender.sendMessage("§cUsage: /dsn test <1|2>");
                }
                return true;
            }
        }

        return false;
    }
}
