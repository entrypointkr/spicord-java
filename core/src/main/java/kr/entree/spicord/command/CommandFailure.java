package kr.entree.spicord.command;

import lombok.Data;

public interface CommandFailure {
    @Data
    class Unknown implements CommandFailure {
        private final String commandLabel;
    }

    @Data
    class FewArguments implements CommandFailure {
    }

    @Data
    class InvalidArguments implements CommandFailure {

    }

    @Data
    class Error implements CommandFailure {
        private final Throwable error;
        private final String message;
    }

    @Data
    class NoPermission implements CommandFailure {
        private final String permission;
        private final Commander who;
    }

    @Data
    class InvalidChannel implements CommandFailure {
        private final String channel;
    }

    @Data
    class Message implements CommandFailure {
        private final String message;
    }
}
