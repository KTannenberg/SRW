package runtime;

public class SchedulerException extends Exception {
    
    private static final long serialVersionUID = -7812434122973641569L;

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
