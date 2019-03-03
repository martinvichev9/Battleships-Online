package bg.sofia.uni.fmi.mjt.battleships.enums;

public enum Command {

    CREATE("create-game"), LIST("list-games"), JOIN("join-game"), SAVE("save-game"),
    SAVED("saved-games"), LOAD("load-game"), DELETE("delete-game"),CONNECT("connect"),
    EXIT("exit"), HELP("help");

    private String command;

    private Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return this.command;
    }

}
