package org.bukkit.craftbukkit.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonParseException;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent.Literal;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CraftChatMessage {

    private static final Pattern LINK_PATTERN = Pattern.compile("((?:(?:https?):\\/\\/)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:[" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + " \\n]|$))))");
    private static final Map<Character, Formatting> formatMap;

    static {
        Builder<Character, Formatting> builder = ImmutableMap.builder();
        for (Formatting format : Formatting.values()) builder.put(Character.toLowerCase(format.toString().charAt(1)), format);
        formatMap = builder.build();
    }

    public static Formatting getColor(ChatColor color) {
        return formatMap.get(color.getChar());
    }

    public static ChatColor getColor(Formatting format) {
        switch (format) {
            case AQUA:
                return ChatColor.AQUA;
            case BLACK:
                return ChatColor.BLACK;
            case BLUE:
                return ChatColor.BLUE;
            case BOLD:
                return ChatColor.BOLD;
            case DARK_AQUA:
                return ChatColor.DARK_AQUA;
            case DARK_BLUE:
                return ChatColor.DARK_BLUE;
            case DARK_GRAY:
                return ChatColor.DARK_GRAY;
            case DARK_GREEN:
                return ChatColor.DARK_GREEN;
            case DARK_PURPLE:
                return ChatColor.DARK_PURPLE;
            case DARK_RED:
                return ChatColor.DARK_RED;
            case GOLD:
                return ChatColor.GOLD;
            case GRAY:
                return ChatColor.GRAY;
            case GREEN:
                return ChatColor.GREEN;
            case ITALIC:
                return ChatColor.ITALIC;
            case LIGHT_PURPLE:
                return ChatColor.LIGHT_PURPLE;
            case RED:
                return ChatColor.RED;
            case RESET:
                return ChatColor.RESET;
            case STRIKETHROUGH:
                return ChatColor.STRIKETHROUGH;
            case UNDERLINE:
                return ChatColor.UNDERLINE;
            case WHITE:
                return ChatColor.WHITE;
            case YELLOW:
                return ChatColor.YELLOW;
            case OBFUSCATED:
                return ChatColor.MAGIC;
            default:
                return null;
        }
    }

    private static final class StringMessage {
        private static final Pattern INCREMENTAL_PATTERN = Pattern.compile("(" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + "[0-9a-fk-orx])|((?:(?:https?):\\/\\/)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:[" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + " \\n]|$))))|(\\n)", Pattern.CASE_INSENSITIVE);
        private static final Pattern INCREMENTAL_PATTERN_KEEP_NEWLINES = Pattern.compile("(" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + "[0-9a-fk-orx])|((?:(?:https?):\\/\\/)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:[" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + " ]|$))))", Pattern.CASE_INSENSITIVE);
        private static final Style RESET = Style.EMPTY;

        private final List<Text> list = new ArrayList<Text>();
        private Text currentChatComponent = Text.of("");
        private Style modifier = Style.EMPTY;
        private final Text[] output;
        private int currentIndex;
        private StringBuilder hex;
        private final String message;

        private StringMessage(String message, boolean keepNewlines) {
            this.message = message;
            if (message == null) {
                output = new Text[]{currentChatComponent};
                return;
            }
            list.add(currentChatComponent);

            Matcher matcher = (keepNewlines ? INCREMENTAL_PATTERN_KEEP_NEWLINES : INCREMENTAL_PATTERN).matcher(message);
            String match = null;
            boolean needsAdd = false;
            while (matcher.find()) {
                int groupId = 0;
                while ((match = matcher.group(++groupId)) == null) {/*NOOP*/}
                int index = matcher.start(groupId);
                if (index > currentIndex) {
                    needsAdd = false;
                    appendNewComponent(index);
                }
                switch (groupId) {
                case 1:
                    char c = match.toLowerCase(java.util.Locale.ENGLISH).charAt(1);
                    Formatting format = formatMap.get(c);

                    if (c == 'x') {
                        hex = new StringBuilder("#");
                    } else if (hex != null) {
                        hex.append(c);

                        if (hex.length() == 7) {
                            modifier = RESET.withColor(TextColor.parse(hex.toString()).result().get());
                            hex = null;
                        }
                    } else if (format.isModifier() && format != Formatting.RESET) {
                        switch (format) {
                        case BOLD:
                            modifier = modifier.withBold(Boolean.TRUE);
                            break;
                        case ITALIC:
                            modifier = modifier.withItalic(Boolean.TRUE);
                            break;
                        case STRIKETHROUGH:
                            modifier.strikethrough = Boolean.TRUE;
                            break;
                        case UNDERLINE:
                            // TODO BROKEN
                            break;
                        case OBFUSCATED:
                            modifier.obfuscated = Boolean.TRUE;
                            break;
                        default:
                            throw new AssertionError("Unexpected message format");
                        }
                    } else modifier = RESET.withColor(format);// Color resets formatting

                    needsAdd = true;
                    break;
                case 2:
                    if (!(match.startsWith("http://") || match.startsWith("https://"))) match = "http://" + match;
                    modifier = modifier.withClickEvent(new ClickEvent(Action.OPEN_URL, match));
                    appendNewComponent(matcher.end(groupId));
                    modifier = modifier.withClickEvent((ClickEvent) null);
                    break;
                case 3:
                    if (needsAdd) appendNewComponent(index);
                    currentChatComponent = null;
                    break;
                }
                currentIndex = matcher.end(groupId);
            }
            if (currentIndex < message.length() || needsAdd) appendNewComponent(message.length());
            output = list.toArray(new Text[list.size()]);
        }

        private void appendNewComponent(int index) {
            Text addition = Text.literal(message.substring(currentIndex, index)).setStyle(modifier);
            currentIndex = index;
            if (currentChatComponent == null) {
                currentChatComponent = Text.of("");
                list.add(currentChatComponent);
            }
            currentChatComponent.getSiblings().add(addition);
        }

        private Text[] getOutput() {
            return output;
        }
    }

    public static Text wrapOrNull(String message) {
        return (message == null || message.isEmpty()) ? null : Text.of(message);
    }

    public static Text wrapOrEmpty(String message) {
        return (message == null) ? Text.of("") : Text.of(message);
    }

    public static Text fromStringOrNull(String message) {
        return fromStringOrNull(message, false);
    }

    public static Text fromStringOrNull(String message, boolean keepNewlines) {
        return (message == null || message.isEmpty()) ? null : fromString(message, keepNewlines)[0];
    }

    public static Text[] fromString(String message) {
        return fromString(message, false);
    }

    public static Text[] fromString(String message, boolean keepNewlines) {
        return new StringMessage(message, keepNewlines).getOutput();
    }

    public static String fromComponent(Text component) {
        return fromComponent(component, Formatting.BLACK);
    }

    public static String toJSON(Text component) {
        return Text.Serialization.toJsonString(component);
    }
    
    public static ArrayList<Text> list(Text txt) {
        ArrayList<Text> arr = new ArrayList<>();
        if (!arr.contains(txt))
            arr.add( txt );
        for (Text tx : txt.getSiblings()) {
            arr.addAll( list(tx) );
        }
        return arr;
    }

    public static String fromComponent(Text component, Formatting defaultColor) {
        if (component == null) return "";
        StringBuilder out = new StringBuilder();

       // IText it = (IText) component;

        for (Text c : list(component)) {
            Style modi = ((Text)c).getStyle();
            out.append(modi.getColor() == null ? defaultColor : modi.getColor());
            if (modi.isBold()) out.append(Formatting.BOLD);
            if (modi.isItalic()) out.append(Formatting.ITALIC);
            if (modi.isUnderlined()) out.append(Formatting.UNDERLINE);
            if (modi.isStrikethrough()) out.append(Formatting.STRIKETHROUGH);
            if (modi.isObfuscated()) out.append(Formatting.OBFUSCATED);

            c.visit((x) -> {
                out.append(x);
                return Optional.empty();
            });
        }
        
        return out.toString();//.replaceFirst("^(" + defaultColor + ")*", "");
    }

    public static Text fixComponent(MutableText component) {
        Matcher matcher = LINK_PATTERN.matcher("");
        return fixComponent(component, matcher);
    }

    private static Text fixComponent(MutableText component, Matcher matcher) {
        Literal text;
        String msg;
        if (component.getContent() instanceof Literal && matcher.reset(msg = (text = (Literal)component.getContent()).string()).find()) {
            matcher.reset();
            Style modifier = component.getStyle();
            ArrayList<Text> extras = new ArrayList<Text>();
            ArrayList<Text> extrasOld = new ArrayList<Text>(component.getSiblings());
            component = Text.empty();
            int pos = 0;
            while (matcher.find()) {
                Object match = matcher.group();
                if (!((String)match).startsWith("http://") && !((String)match).startsWith("https://")) {
                    match = "http://" + (String)match;
                }
                MutableText prev = Text.literal(msg.substring(pos, matcher.start()));
                prev.setStyle(modifier);
                extras.add(prev);
                MutableText link = Text.literal(matcher.group());
                Style linkModi = modifier.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, (String)match));
                link.setStyle(linkModi);
                extras.add(link);
                pos = matcher.end();
            }
            MutableText prev = Text.literal(msg.substring(pos));
            prev.setStyle(modifier);
            extras.add(prev);
            extras.addAll(extrasOld);
            for (Text c2 : extras) {
                component.append(c2);
            }
        }
        List<Text> extras = component.getSiblings();
        for (int i2 = 0; i2 < extras.size(); ++i2) {
            Text comp = extras.get(i2);
            if (comp.getStyle() == null || comp.getStyle().getClickEvent() != null) continue;
            extras.set(i2, CraftChatMessage.fixComponent(comp.copy(), matcher));
        }
        if (component.getContent() instanceof TranslatableTextContent) {
            Object[] subs = ((TranslatableTextContent)component.getContent()).getArgs();
            for (int i3 = 0; i3 < subs.length; ++i3) {
                Object comp = subs[i3];
                if (comp instanceof Text) {
                    Text c3 = (Text)comp;
                    if (c3.getStyle() == null || c3.getStyle().getClickEvent() != null) continue;
                    subs[i3] = CraftChatMessage.fixComponent(c3.copy(), matcher);
                    continue;
                }
                if (!(comp instanceof String) || !matcher.reset((String)comp).find()) continue;
                subs[i3] = CraftChatMessage.fixComponent(Text.literal((String)comp), matcher);
            }
        }
        return component;
    }

    private CraftChatMessage() {
    }

    // Paper start

    public static String trimMessage(String message, int maxLength) {
        if (message != null && message.length() > maxLength) {
            return message.substring(0, maxLength);
        } else {
            return message;
        }
    }

    public static String fromStringToJSON(String message) {
        return fromStringToJSON(message, false);
    }

    public static String fromStringToJSON(String message, boolean keepNewlines) {
        Text component = CraftChatMessage.fromString(message, keepNewlines)[0];
        return CraftChatMessage.toJSON(component);
    }

    public static String fromJSONOrStringToJSON(String message) {
        return fromJSONOrStringToJSON(message, false);
    }

    public static String fromJSONOrStringToJSON(String message, boolean keepNewlines) {
        return fromJSONOrStringToJSON(message, false, keepNewlines, Integer.MAX_VALUE, false);
    }

    public static String fromJSONOrStringOrNullToJSON(String message) {
        return fromJSONOrStringOrNullToJSON(message, false);
    }

    public static String fromJSONOrStringOrNullToJSON(String message, boolean keepNewlines) {
        return fromJSONOrStringToJSON(message, true, keepNewlines, Integer.MAX_VALUE, false);
    }

    public static Text fromJSONOrNull(String jsonMessage) {
        if (jsonMessage == null) return null;
        try {
            return fromJSON(jsonMessage); // Can return null
        } catch (JsonParseException ex) {
            return null;
        }
    }

    public static Text fromJSON(String jsonMessage) throws JsonParseException {
        return Text.Serialization.fromJson(jsonMessage);
    }

    public static String fromJSONOrStringToJSON(String message, boolean nullable, boolean keepNewlines, int maxLength, boolean checkJsonContentLength) {
        if (message == null) message = "";
        if (nullable && message.isEmpty()) return null;
        // If the input can be parsed as JSON, we use that:
        Text component = fromJSONOrNull(message);
        if (component != null) {
            if (checkJsonContentLength) {
                String content = fromComponent(component);
                String trimmedContent = trimMessage(content, maxLength);
                if (!content.equals(trimmedContent)) { // identity comparison is fine here
                    // Note: The resulting text has all non-plain text features stripped.
                    return fromStringToJSON(trimmedContent, keepNewlines);
                }
            }
            return message;
        } else {
            // Else we interpret the input as legacy text:
            message = trimMessage(message, maxLength);
            return fromStringToJSON(message, keepNewlines);
        }
    }

    public static Text[] fromString(String message, boolean keepNewlines, boolean plain) {
        return new StringMessage(message, keepNewlines/*, plain*/).getOutput();
    }

    public static String fromJSONComponent(String jsonMessage) {
        Text component = CraftChatMessage.fromJSONOrNull(jsonMessage);
        return CraftChatMessage.fromComponent(component);
    }



}
