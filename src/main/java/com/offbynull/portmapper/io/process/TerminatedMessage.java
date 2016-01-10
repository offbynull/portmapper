package com.offbynull.portmapper.io.process;

final class TerminatedMessage {
    private Integer id;
    private int exitCode;

    public TerminatedMessage(Integer id, int exitCode) {
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
