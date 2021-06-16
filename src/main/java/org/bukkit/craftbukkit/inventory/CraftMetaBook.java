package org.bukkit.craftbukkit.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.text.Text.Serializer;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.inventory.CraftMetaItem.SerializableMeta;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.meta.BookMeta;

import static org.spigotmc.ValidateUtils.limit;
import java.util.AbstractList;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

@DelegateDeserialization(SerializableMeta.class)
public class CraftMetaBook extends CraftMetaItem implements BookMeta {

    protected static final ItemMetaKey BOOK_TITLE = new ItemMetaKey("title");
    protected static final ItemMetaKey BOOK_AUTHOR = new ItemMetaKey("author");
    protected static final ItemMetaKey BOOK_PAGES = new ItemMetaKey("pages");
    protected static final ItemMetaKey RESOLVED = new ItemMetaKey("resolved");
    protected static final ItemMetaKey GENERATION = new ItemMetaKey("generation");
    protected static final int MAX_PAGES = 100;
    protected static final int MAX_PAGE_LENGTH = 320; // 256 limit + 64 characters to allow for psuedo colour codes
    protected static final int MAX_TITLE_LENGTH = 32;

    protected String title;
    protected String author;
    // We store the pages in their raw original text representation. See SPIGOT-5063, SPIGOT-5350, SPIGOT-3206
    // For writable books (CraftMetaBook) the pages are stored as plain Strings.
    // For written books (CraftMetaBookSigned) the pages are stored in Minecraft's JSON format.
    protected List<String> pages; // null and empty are two different states internally
    protected Boolean resolved = null;
    protected Integer generation;

    CraftMetaBook(CraftMetaItem meta) {
        super(meta);

        if (meta instanceof CraftMetaBook) {
            CraftMetaBook bookMeta = (CraftMetaBook) meta;
            this.title = bookMeta.title;
            this.author = bookMeta.author;
            this.resolved = bookMeta.resolved;
            this.generation = bookMeta.generation;

            if (bookMeta.pages != null) {
                this.pages = new ArrayList<String>(bookMeta.pages.size());
                if (meta instanceof CraftMetaBookSigned) {
                    if (this instanceof CraftMetaBookSigned) {
                        pages.addAll(bookMeta.pages);
                    } else {
                        // Convert from JSON to plain Strings:
                        pages.addAll(Lists.transform(bookMeta.pages, CraftChatMessage::fromJSONComponent));
                    }
                } else {
                    if (this instanceof CraftMetaBookSigned) {
                        // Convert from plain Strings to JSON:
                        // This happens for example during book signing.
                        for (String page : bookMeta.pages) {
                            // We don't insert any non-plain text features (such as clickable links) during this conversion.
                            Text component = CraftChatMessage.fromString(page, true, true)[0];
                            pages.add(CraftChatMessage.toJSON(component));
                        }
                    } else {
                        pages.addAll(bookMeta.pages);
                    }
                }
            }
        }
    }

    CraftMetaBook(NbtCompound tag) {
        super(tag);

        if (tag.contains(BOOK_TITLE.NBT)) {
            this.title = tag.getString(BOOK_TITLE.NBT);
        }

        if (tag.contains(BOOK_AUTHOR.NBT)) {
            this.author = tag.getString(BOOK_AUTHOR.NBT);
        }

        if (tag.contains(RESOLVED.NBT)) {
            this.resolved = tag.getBoolean(RESOLVED.NBT);
        }

        if (tag.contains(GENERATION.NBT)) {
            generation = tag.getInt(GENERATION.NBT);
        }

        if (tag.contains(BOOK_PAGES.NBT)) {
            NbtList pages = tag.getList(BOOK_PAGES.NBT, CraftMagicNumbers.NBT.TAG_STRING);
            this.pages = new ArrayList<String>(pages.size());

            boolean expectJson = (this instanceof CraftMetaBookSigned);
            // Note: We explicitly check for and truncate oversized books and pages,
            // because they can come directly from clients when handling book edits.
            for (int i = 0; i < Math.min(pages.size(), MAX_PAGES); i++) {
                String page = pages.getString(i);
                // There was an issue on previous Spigot versions which would
                // result in book items with pages in the wrong text
                // representation. See SPIGOT-182, SPIGOT-164
                if (expectJson) {
                    page = CraftChatMessage.fromJSONOrStringToJSON(page, false, true, MAX_PAGE_LENGTH, false);
                } else {
                    page = validatePage(page);
                }
                this.pages.add(page);
            }
        }
    }

    CraftMetaBook(Map<String, Object> map) {
        super(map);

        setAuthor(SerializableMeta.getString(map, BOOK_AUTHOR.BUKKIT, true));

        setTitle(SerializableMeta.getString(map, BOOK_TITLE.BUKKIT, true));

        Iterable<?> pages = SerializableMeta.getObject(Iterable.class, map, BOOK_PAGES.BUKKIT, true);
        if (pages != null) {
            this.pages = new ArrayList<String>();
            for (Object page : pages) {
                if (page instanceof String) {
                    internalAddPage(deserializePage((String) page));
                }
            }
        }

        resolved = SerializableMeta.getObject(Boolean.class, map, RESOLVED.BUKKIT, true);
        generation = SerializableMeta.getObject(Integer.class, map, GENERATION.BUKKIT, true);
    }

    protected String deserializePage(String pageData) {
        // We expect the page data to already be a plain String.
        return validatePage(pageData);
    }


    @Override
    void applyToItem(NbtCompound itemData) {
        super.applyToItem(itemData);

        if (hasTitle()) {
            itemData.putString(BOOK_TITLE.NBT, this.title);
        }

        if (hasAuthor()) {
            itemData.putString(BOOK_AUTHOR.NBT, this.author);
        }

        if (pages != null) {
            NbtList list = new NbtList();
            for (String page : pages) {
                list.add(NbtString.of(page));
            }
            itemData.put(BOOK_PAGES.NBT, list);
        }

        if (resolved != null) {
            itemData.putBoolean(RESOLVED.NBT, resolved);
        }

        if (generation != null) {
            itemData.putInt(GENERATION.NBT, generation);
        }
    }

    @Override
    boolean isEmpty() {
        return super.isEmpty() && isBookEmpty();
    }

    boolean isBookEmpty() {
        return !(hasPages() || hasAuthor() || hasTitle());
    }

    @Override
    boolean applicableTo(Material type) {
        switch (type) {
            case WRITTEN_BOOK:
            case WRITABLE_BOOK:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean hasAuthor() {
        return this.author != null;
    }

    @Override
    public boolean hasTitle() {
        return this.title != null;
    }

    @Override
    public boolean hasPages() {
        return !pages.isEmpty();
    }

    @Override
    public boolean hasGeneration() {
        return generation != null;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public boolean setTitle(final String title) {
        if (title == null) {
            this.title = null;
            return true;
        } else if (title.length() > MAX_TITLE_LENGTH) return false;

        this.title = title;
        return true;
    }

    @Override
    public String getAuthor() {
        return this.author;
    }

    @Override
    public void setAuthor(final String author) {
        this.author = author;
    }

    @Override
    public Generation getGeneration() {
        return (generation == null) ? null : Generation.values()[generation];
    }

    @Override
    public void setGeneration(Generation generation) {
        this.generation = (generation == null) ? null : generation.ordinal();
    }

    @Override
    public String getPage(final int page) {
        Validate.isTrue(isValidPage(page), "Invalid page number");
        return convertDataToPlainPage(pages.get(page - 1));
    }

    protected String convertDataToPlainPage(String pageData) {
        // pageData is expected to already be a plain String.
        return pageData;
    }


    @Override
    public void setPage(final int page, final String text) {
        if (!isValidPage(page))
            throw new IllegalArgumentException("Invalid page number " + page + "/" + pages.size());

        String newText = validatePage(text);
        pages.set(page - 1, convertPlainPageToData(newText));
    }

    @Override
    public void setPages(final String... pages) {
        this.pages.clear();

        addPage(pages);
    }

    @Override
    public void addPage(final String... pages) {
        for (String page : pages) {
            page = validatePage(page);
            internalAddPage(convertPlainPageToData(page));
        }
    }

    private void internalAddPage(String page) {
        // asserted: page != null
        if (this.pages == null) {
            this.pages = new ArrayList<String>();
        } else if (this.pages.size() >= MAX_PAGES) {
            return;
        }
        this.pages.add(page);
    }


    protected String convertPlainPageToData(String page) {
        // Writable books store their data as plain Strings, so we don't need to convert anything.
        return page;
    }


    String validatePage(String page) {
        if (page == null) {
            page = "";
        } else if (page.length() > MAX_PAGE_LENGTH) {
            page = page.substring(0, MAX_PAGE_LENGTH);
        }
        return page;
    }

    @Override
    public int getPageCount() {
        return pages.size();
    }

    @Override
    public List<String> getPages() {
        if (pages == null) return ImmutableList.of();
        return pages.stream().map(this::convertDataToPlainPage).collect(ImmutableList.toImmutableList());
    }

    @Override
    public void setPages(List<String> pages) {
        this.pages.clear();
        for (String page : pages)
            addPage(page);
    }

    private boolean isValidPage(int page) {
        return page > 0 && page <= pages.size();
    }

    @Override
    public CraftMetaBook clone() {
        CraftMetaBook meta = (CraftMetaBook) super.clone();
        if (this.pages != null) {
            meta.pages = new ArrayList<String>(this.pages);
        }
        return meta;
    }


    @Override
    int applyHash() {
        final int original;
        int hash = original = super.applyHash();
        if (hasTitle())
            hash = 61 * hash + this.title.hashCode();
        if (hasAuthor())
            hash = 61 * hash + 13 * this.author.hashCode();
        if (hasPages())
            hash = 61 * hash + 17 * this.pages.hashCode();
        if (hasGeneration())
            hash = 61 * hash + 19 * this.generation.hashCode();

        return original != hash ? CraftMetaBook.class.hashCode() ^ hash : hash;
    }

    @Override
    boolean equalsCommon(CraftMetaItem meta) {
        if (!super.equalsCommon(meta))
            return false;

        if (meta instanceof CraftMetaBook) {
            CraftMetaBook that = (CraftMetaBook) meta;

            return (hasTitle() ? that.hasTitle() && this.title.equals(that.title) : !that.hasTitle())
                    && (hasAuthor() ? that.hasAuthor() && this.author.equals(that.author) : !that.hasAuthor())
                    && (hasPages() ? that.hasPages() && this.pages.equals(that.pages) : !that.hasPages())
                    && (hasGeneration() ? that.hasGeneration() && this.generation.equals(that.generation) : !that.hasGeneration());
        }
        return true;
    }

    @Override
    boolean notUncommon(CraftMetaItem meta) {
        return super.notUncommon(meta) && (meta instanceof CraftMetaBook || isBookEmpty());
    }

    @Override
    ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        super.serialize(builder);

        if (hasTitle()) {
            builder.put(BOOK_TITLE.BUKKIT, title);
        }

        if (hasAuthor()) {
            builder.put(BOOK_AUTHOR.BUKKIT, author);
        }

        if (pages != null) {
            builder.put(BOOK_PAGES.BUKKIT, ImmutableList.copyOf(pages));
        }

        if (resolved != null) {
            builder.put(RESOLVED.BUKKIT, resolved);
        }

        if (generation != null) {
            builder.put(GENERATION.BUKKIT, generation);
        }

        return builder;
    }

    private final BookMeta.Spigot spigot = new BookMeta.Spigot() {

        @Override
        public BaseComponent[] getPage(final int page) {
            return ComponentSerializer.parse(Text.Serializer.toJson( CraftChatMessage.fromStringOrNull(pages.get(page - 1))) );
        }

        @Override
        public void setPage(final int page, final BaseComponent... text) {
            if (!isValidPage(page))
                throw new IllegalArgumentException("Invalid page number " + page + "/" + pages.size());

            BaseComponent[] newText = text == null ? new BaseComponent[0] : text;
            CraftMetaBook.this.pages.set(page - 1, Text.Serializer.fromJson(ComponentSerializer.toString(newText)).asString());
        }

        @Override
        public void setPages(final BaseComponent[]... pages) {
            CraftMetaBook.this.pages.clear();
            addPage(pages);
        }

        @Override
        public void addPage(final BaseComponent[]... pages) {
            for (BaseComponent[] page : pages) {
                if (CraftMetaBook.this.pages.size() >= MAX_PAGES)
                    return;

                if (page == null)
                    page = new BaseComponent[0];

                CraftMetaBook.this.pages.add(Text.Serializer.fromJson(ComponentSerializer.toString(page)).asString());
            }
        }

        @Override
        public List<BaseComponent[]> getPages() {
            final List<String> copy = ImmutableList.copyOf(CraftMetaBook.this.pages);
            return new AbstractList<BaseComponent[]>() {
                @Override
                public BaseComponent[] get(int index) {
                    return ComponentSerializer.parse(Text.Serializer.toJson(CraftChatMessage.fromStringOrNull(copy.get(index))));
                }

                @Override
                public int size() {
                    return copy.size();
                }
            };
        }

        @Override
        public void setPages(List<BaseComponent[]> pages) {
            CraftMetaBook.this.pages.clear();
            for (BaseComponent[] page : pages)
                addPage(page);
        }
    };

    @Override
    public BookMeta.Spigot spigot() {
        return spigot;
    }
    
    
    // Paper start
    @Override
    public net.kyori.adventure.text.Component title() {
        return this.title == null ? null : io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC.deserialize(this.title);
    }

    @Override
    public org.bukkit.inventory.meta.BookMeta title(net.kyori.adventure.text.Component title) {
        this.setTitle(title == null ? null : io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC.serialize(title));
        return this;
    }

    @Override
    public net.kyori.adventure.text.Component author() {
        return this.author == null ? null : io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC.deserialize(this.author);
    }

    @Override
    public org.bukkit.inventory.meta.BookMeta author(net.kyori.adventure.text.Component author) {
        this.setAuthor(author == null ? null : io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC.serialize(author));
        return this;
    }

    @Override
    public net.kyori.adventure.text.Component page(final int page) {
        Validate.isTrue(isValidPage(page), "Invalid page number");
        return this instanceof CraftMetaBookSigned ? net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(pages.get(page - 1)) : io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC.deserialize(pages.get(page - 1));
    }

    @Override
    public void page(final int page, net.kyori.adventure.text.Component data) {
        if (!isValidPage(page)) {
            throw new IllegalArgumentException("Invalid page number " + page + "/" + pages.size());
        }
        if (data == null) {
            data = net.kyori.adventure.text.Component.empty();
        }
        pages.set(page - 1, this instanceof CraftMetaBookSigned ? net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(data) : io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC.serialize(data));
    }

    @Override
    public List<net.kyori.adventure.text.Component> pages() {
        if (this.pages == null) return ImmutableList.of();
        if (this instanceof CraftMetaBookSigned)
            return pages.stream().map(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson()::deserialize).collect(ImmutableList.toImmutableList());
        else
            return pages.stream().map(io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC::deserialize).collect(ImmutableList.toImmutableList());
    }

    @Override
    public BookMeta pages(List<net.kyori.adventure.text.Component> pages) {
        if (this.pages != null) this.pages.clear();
        for (net.kyori.adventure.text.Component page : pages) {
            addPages(page);
        }
        return this;
    }

    @Override
    public BookMeta pages(net.kyori.adventure.text.Component... pages) {
        if (this.pages != null) this.pages.clear();
        addPages(pages);
        return this;
    }

    @Override
    public void addPages(net.kyori.adventure.text.Component... pages) {
        if (this.pages == null) this.pages = new ArrayList<>();
        for (net.kyori.adventure.text.Component page : pages) {
            if (this.pages.size() >= MAX_PAGES) {
                return;
            }

            if (page == null) {
                page = net.kyori.adventure.text.Component.empty();
            }

            this.pages.add(this instanceof CraftMetaBookSigned ? net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(page) : io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC.serialize(page));
        }
    }

    private CraftMetaBook(net.kyori.adventure.text.Component title, net.kyori.adventure.text.Component author, List<net.kyori.adventure.text.Component> pages) {
        super((org.bukkit.craftbukkit.inventory.CraftMetaItem) org.bukkit.Bukkit.getItemFactory().getItemMeta(org.bukkit.Material.WRITABLE_BOOK));
        this.title = title == null ? null : io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC.serialize(title);
        this.author = author == null ? null : io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC.serialize(author);
        this.pages = pages.subList(0, Math.min(MAX_PAGES, pages.size())).stream().map(io.papermc.paper.adventure.PaperAdventure.LEGACY_SECTION_UXRC::serialize).collect(java.util.stream.Collectors.toList());
    }

    static final class CraftMetaBookBuilder implements BookMetaBuilder {
        private net.kyori.adventure.text.Component title = null;
        private net.kyori.adventure.text.Component author = null;
        private final List<net.kyori.adventure.text.Component> pages = new java.util.ArrayList<>();

        @Override
        public BookMetaBuilder title(net.kyori.adventure.text.Component title) {
            this.title = title;
            return this;
        }

        @Override
        public BookMetaBuilder author(net.kyori.adventure.text.Component author) {
            this.author = author;
            return this;
        }

        @Override
        public BookMetaBuilder addPage(net.kyori.adventure.text.Component page) {
            this.pages.add(page);
            return this;
        }

        @Override
        public BookMetaBuilder pages(net.kyori.adventure.text.Component... pages) {
            java.util.Collections.addAll(this.pages, pages);
            return this;
        }

        @Override
        public BookMetaBuilder pages(java.util.Collection<net.kyori.adventure.text.Component> pages) {
            this.pages.addAll(pages);
            return this;
        }

        @Override
        public BookMeta build() {
            return new CraftMetaBook(title, author, pages);
        }
    }

    @Override
    public BookMetaBuilder toBuilder() {
        return new CraftMetaBookBuilder();
    }

    // Paper end

}