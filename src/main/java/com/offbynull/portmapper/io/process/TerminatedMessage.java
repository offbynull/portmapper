package com.offbynull.portmapper.io.process;

final class TerminatedMessage {
    private int id;
    private Integer exitCode;

    public TerminatedMessage(int id, Integer exitCode) {
        this.id = id;
        this.exitCode = exitCode;
    }

    public int getId() {
        return id;
    }

    public Integer getExitCode() {
        return exitCode;
    }

}
