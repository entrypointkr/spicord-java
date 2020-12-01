package kr.entree.spicord.lang;

public enum Lang {
    ERROR("에러가 발생했습니다. 개발자에게 제보해주세요!"),
    RELOADED("모든 설정을 다시 불러왔습니다."),
    DISCORD_MESSAGE_SENT("디스코드로 보낸 메시지: %s"),
    UNKNOWN_CHANNEL("다음 채널은 존재하지 않습니다: %s"),
    UNKNOWN_COMMAND("잘못된 명령어 사용입니다."),
    PERMISSION_DENIED("다음 권한이 없습니다: %s"),
    ;

    private final String message;

    Lang(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public final String format(Object... args) {
        return String.format(getMessage(), args);
    }
}
