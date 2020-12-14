package io.github.essentialsx.itemdbgenerator.providers.alias;

import io.github.essentialsx.itemdbgenerator.providers.item.ItemProvider;

import java.util.stream.Stream;

public interface AliasProvider {

    Stream<String> get(ItemProvider.Item item);

}
