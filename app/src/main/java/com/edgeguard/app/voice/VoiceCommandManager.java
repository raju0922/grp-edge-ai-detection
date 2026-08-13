package com.edgeguard.app.voice;

import java.util.Locale;

public class VoiceCommandManager {
    public enum Command {
        WHAT_AHEAD, LEFT, RIGHT, MOVING, DANGER, DESCRIBE, STOP, START, UNKNOWN
    }

    public Command parse(String text) {
        if (text == null) return Command.UNKNOWN;
        String s = text.toLowerCase(Locale.US).trim();

        if (s.contains("stop")) return Command.STOP;
        if (s.contains("start")) return Command.START;
        if (s.contains("left")) return Command.LEFT;
        if (s.contains("right")) return Command.RIGHT;
        if (s.contains("moving")) return Command.MOVING;
        if (s.contains("danger") || s.contains("dangerous")) return Command.DANGER;
        if (s.contains("describe")) return Command.DESCRIBE;
        if (s.contains("ahead") || s.contains("front")) return Command.WHAT_AHEAD;

        return Command.UNKNOWN;
    }
}
