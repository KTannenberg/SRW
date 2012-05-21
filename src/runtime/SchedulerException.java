package runtime;

public class SchedulerException extends Exception {

    public SchedulerException() {
        super("Unknown exception occured in scheduler, see log for details");
    }

    public SchedulerException(String message) {
        super(message);
    }

    public SchedulerException(Throwable cause) {
        super(cause);
    }

    public SchedulerException(String message, Throwable cause) {
        super(message, cause);
    }
}
