package kr.loldiscordbot.bot;

import java.util.Arrays;

public enum Lane {
    TOP("탑", "TOP"),
    JUNGLE("정글", "JUNGLE"),
    MID("미드", "MID"),
    ADC("원딜", "ADC"),
    SUPPORT("서포터", "SUPPORT");

    private final String koreanName;
    private final String value;

    Lane(String koreanName, String value) {
        this.koreanName = koreanName;
        this.value = value;
    }

    public String koreanName() {
        return koreanName;
    }

    public String value() {
        return value;
    }

    public static Lane fromValue(String value) {
        return Arrays.stream(values())
                .filter(lane -> lane.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown lane value: " + value));
    }
}
