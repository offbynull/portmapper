package com.offbynull.portmapper.io.process;

final class TerminatedMessage {
    private int id;
    private Integer exitCode;

    TerminatedMessage(int id, Integer exitCode) { // exitcode may be null
        this.id = id;
        this.exitCode = exitCode;
    }

    int getId() {
        return id;
    }

    Integer getExitCode() {
        return exitCode;
    }

}
