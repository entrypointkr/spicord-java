package kr.entree.spicord;

import lombok.Data;

import java.util.concurrent.Executor;

@Data
public class Execution {
    private final Executor sync;
    private final Executor async;

    public Execution(Executor sync, Executor async) {
        this.sync = sync;
        this.async = async;
    }
}
