/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.cardboardpowered.CardboardMod;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationStore;

/**
 * Cardboard's own user-facing messages, held as Adventure translations rather than as
 * literal text. Anything Cardboard sends is a {@code Component.translatable(...)}, which
 * is resolved against the locale of whoever receives it: a player's client language, or
 * English for the console. Adding a language means dropping another properties file in
 * {@code assets/cardboard/lang} and listing its locale here, nothing else.
 */
public final class CardboardTranslations {

    /** Sent to a command sender who lacks the permission for the command they ran. */
    public static final String COMMAND_NO_PERMISSION = "cardboard.command.no-permission";

    private static final Key SOURCE = Key.key("cardboard", "messages");
    private static final String BUNDLE = "assets/cardboard/lang/messages_%s.properties";

    /** English is the fallback and must stay first; every other locale is optional. */
    private static final List<Locale> BUNDLED = List.of(Locale.ENGLISH, Locale.of("uk"));

    private static boolean registered;

    private CardboardTranslations() {
    }

    /**
     * Registers the bundled languages with the global translator. Safe to call more than
     * once; only the first call does any work.
     */
    public static synchronized void register() {
        if (registered) return;
        registered = true;

        TranslationStore.StringBased<MessageFormat> store = TranslationStore.messageFormat(SOURCE);
        store.defaultLocale(Locale.ENGLISH);

        for (Locale locale : BUNDLED) {
            Map<String, MessageFormat> messages = load(locale);
            if (!messages.isEmpty()) store.registerAll(locale, messages);
        }

        GlobalTranslator.translator().addSource(store);
    }

    private static Map<String, MessageFormat> load(Locale locale) {
        String path = String.format(BUNDLE, locale.toLanguageTag().toLowerCase(Locale.ROOT).replace('-', '_'));

        // Read the file directly instead of going through ResourceBundle, which would fall
        // back to the host machine's locale when a language is missing and hand out the
        // wrong translations for it.
        try (InputStream in = CardboardTranslations.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                CardboardMod.LOGGER.warning("No Cardboard language file for " + locale + " at " + path);
                return Map.of();
            }

            Properties properties = new Properties();
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));

            Map<String, MessageFormat> messages = new HashMap<>(properties.size());
            for (String key : properties.stringPropertyNames())
                messages.put(key, new MessageFormat(properties.getProperty(key), locale));

            return messages;
        } catch (IOException ex) {
            CardboardMod.LOGGER.log(Level.WARNING, "Could not read the Cardboard language file for " + locale, ex);
            return Map.of();
        }
    }

}
