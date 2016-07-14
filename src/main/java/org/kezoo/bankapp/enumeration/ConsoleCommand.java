package org.kezoo.bankapp.enumeration;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ConsoleCommand {
    PUT_ACCOUNT("put", "put account into system. Usage : 'put {account id} {balance} {bank}'"),
    GET_ACCOUNT("get", "print account info. Usage : 'get {account id}'"),
    GETALL("getall", "print list of all accounts. Usage : 'getall'"),
    HELP("help", "print help. Usage :'help'"),
    SEND("send", "transfer money between accounts. Usage :'send {account_id from} {account_id to} {amount}'"),
    CURRENT_BANK("cur", "print bank name of application instance. Usage :'cur'");

    private String name;
    private String description;

    private static Map<String, ConsoleCommand> valueMap;
    static {
        valueMap = Stream.of(values()).collect(Collectors.toMap(ConsoleCommand::getName, c -> c));
    }

    public static ConsoleCommand byName(String name) {
        return valueMap.get(name.toLowerCase());
    }

    ConsoleCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name + " -- " + description;
    }
}
