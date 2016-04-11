package ru.semiot.platform.deviceproxyservice.api.drivers;

public class CommandExecutionException extends Exception {

    public enum Reason {
        SYSTEM_NOT_FOUND("Requested system not found!"),
        DRIVER_NOT_FOUND("Driver operating the requested system not found!"),
        BAD_COMMAND("There is a mistake/error in the command!");

        private final String description;

        Reason(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final Reason reason;

    public CommandExecutionException(Reason reason) {
        super(reason.getDescription());

        this.reason = reason;
    }

    public CommandExecutionException(Reason reason, String message) {
        super(reason.getDescription() + " Message: " + message);

        this.reason = reason;
    }

    public CommandExecutionException(Reason reason, Throwable cause) {
        super(reason.getDescription(), cause);

        this.reason = reason;
    }

    public boolean isNotFound() {
        return reason == Reason.SYSTEM_NOT_FOUND || reason == Reason.DRIVER_NOT_FOUND;
    }

    public boolean isBadCommand() {
        return reason == Reason.BAD_COMMAND;
    }

    public static CommandExecutionException systemNotFound() {
        return new CommandExecutionException(Reason.SYSTEM_NOT_FOUND);
    }

    public static CommandExecutionException driverNotFound() {
        return new CommandExecutionException(Reason.DRIVER_NOT_FOUND);
    }

    public static CommandExecutionException driverNotFound(Throwable cause) {
        return new CommandExecutionException(Reason.DRIVER_NOT_FOUND, cause);
    }

    public static CommandExecutionException badCommand(String message) {
        return new CommandExecutionException(Reason.BAD_COMMAND, message);
    }

    public static CommandExecutionException badCommand() {
        return new CommandExecutionException(Reason.BAD_COMMAND);
    }
}
