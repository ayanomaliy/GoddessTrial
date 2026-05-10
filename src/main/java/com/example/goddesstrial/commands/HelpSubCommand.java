package com.example.goddesstrial.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import javax.annotation.Nonnull;

/**
 * /trial help - Show available commands
 */
public class HelpSubCommand extends CommandBase {

    public HelpSubCommand() {
        super("help", "Show available commands");
        this.setPermissionGroup(null);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        context.sendMessage(Message.raw(""));
        context.sendMessage(Message.raw("=== GoddessTrial Commands ==="));
        context.sendMessage(Message.raw("/trial help - Show this help message"));
        context.sendMessage(Message.raw("/trial info - Show plugin information"));
        context.sendMessage(Message.raw("/trial reload - Reload configuration"));
        context.sendMessage(Message.raw("/trial ui - Open the dashboard UI"));
        context.sendMessage(Message.raw("========================"));
    }
}