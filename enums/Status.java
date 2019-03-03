package bg.sofia.uni.fmi.mjt.battleships.enums;

public enum Status {

    PENDING("pending"), IN_PROGRESS("in progress");

    private String status;

    private Status (String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

}
