package reyga.starter.foundation.common_io.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SqlInjectionPatterns {

    private static final List<Pattern> DEFAULT_PATTERNS = List.of(
            Pattern.compile("(?i)(^|\\b)(or|and)\\b\\s+\\d+\\s*=\\s*\\d+\\b"),
            Pattern.compile("(?i)(^|\\b)(or|and)\\b\\s+'[^']*'\\s*=\\s*'[^']*'"),
            Pattern.compile("(?i)(^|\\b)union\\b\\s+all\\b\\s+select\\b"),
            Pattern.compile("(?i)(^|\\b)union\\b\\s+select\\b"),
            Pattern.compile("(?i)(^|\\b)select\\b\\s+.*\\bfrom\\b"),
            Pattern.compile("(?i)(^|\\b)insert\\b\\s+into\\b"),
            Pattern.compile("(?i)(^|\\b)update\\b\\s+\\w+\\s+set\\b"),
            Pattern.compile("(?i)(^|\\b)delete\\b\\s+from\\b"),
            Pattern.compile("(?i)(^|\\b)drop\\b\\s+(table|database)\\b"),
            Pattern.compile("(?i)(^|\\b)truncate\\b\\s+table\\b"),
            Pattern.compile("(?i)\\bexec(ute)?\\b\\s+\\w+"),
            Pattern.compile("(?i)\\bsp_[a-z0-9_]+\\b"),
            Pattern.compile("(?i)--\\s"),
            Pattern.compile("(?i)/\\*.*?\\*/", Pattern.DOTALL),
            Pattern.compile("(?i)\\bwaitfor\\b\\s+delay\\b"),
            Pattern.compile("(?i);\\s*(select|insert|update|delete|drop|truncate|alter)\\b")
    );

    public static List<Pattern> defaults() {
        return DEFAULT_PATTERNS;
    }

    public static List<Pattern> merge(List<Pattern> patterns) {
        int baseSize = patterns != null ? patterns.size() : 0;
        List<Pattern> merged = new ArrayList<>(baseSize);
        if (patterns != null && !patterns.isEmpty()) {
            for (Pattern pattern : patterns) {
                if (pattern != null) {
                    merged.add(pattern);
                }
            }
        }
        return List.copyOf(merged);
    }

    public static List<Pattern> merge(Pattern... patterns) {
        int baseSize = patterns != null ? patterns.length : 0;
        List<Pattern> merged = new ArrayList<>(baseSize);
        if (patterns != null && patterns.length > 0) {
            for (Pattern pattern : patterns) {
                if (pattern != null) {
                    merged.add(pattern);
                }
            }
        }
        return List.copyOf(merged);
    }
}
