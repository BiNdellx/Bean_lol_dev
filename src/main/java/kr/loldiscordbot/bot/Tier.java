package kr.loldiscordbot.bot;

import java.awt.Color;
import java.util.Arrays;

public enum Tier {
    IRON("아이언", "IRON", new Color(88, 88, 88)),
    BRONZE("브론즈", "BRONZE", new Color(169, 113, 66)),
    SILVER("실버", "SILVER", new Color(167, 177, 194)),
    GOLD("골드", "GOLD", new Color(241, 196, 15)),
    PLATINUM("플래티넘", "PLATINUM", new Color(77, 208, 181)),
    EMERALD("에메랄드", "EMERALD", new Color(46, 204, 113)),
    DIAMOND("다이아몬드", "DIAMOND", new Color(77, 166, 255)),
    MASTER("마스터", "MASTER", new Color(155, 89, 182)),
    GRANDMASTER("그랜드마스터", "GRANDMASTER", new Color(231, 76, 60)),
    CHALLENGER("챌린저", "CHALLENGER", new Color(0, 184, 217));

    private final String koreanName;
    private final String value;
    private final Color roleColor;

    Tier(String koreanName, String value, Color roleColor) {
        this.koreanName = koreanName;
        this.value = value;
        this.roleColor = roleColor;
    }

    public String koreanName() {
        return koreanName;
    }

    public String value() {
        return value;
    }

    public Color roleColor() {
        return roleColor;
    }

    public static Tier fromValue(String value) {
        return Arrays.stream(values())
                .filter(tier -> tier.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown tier value: " + value));
    }
}
