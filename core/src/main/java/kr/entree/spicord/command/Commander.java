package kr.entree.spicord.command;

public interface Commander {
    String getName();

    boolean hasPermission(String permission);

    void sendMessage(String message);
}
