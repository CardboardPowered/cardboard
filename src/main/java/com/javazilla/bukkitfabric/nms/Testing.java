/*
* Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.javazilla.bukkitfabric.nms;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import net.fabricmc.loader.api.MappingResolver;
// import net.fabricmc.loader.launch.common.MappingConfiguration;
// import net.fabricmc.mapping.tree.ClassDef;
// import net.fabricmc.mapping.tree.Descriptored;
// import net.fabricmc.mapping.tree.TinyTree;
// import net.fabricmc.mappings.EntryTriple;

/**
 * @deprecated
 */
@Deprecated
public class Testing implements MappingResolver {

    @Override
    public Collection<String> getNamespaces() {
        return null;
    }

    @Override
    public String getCurrentRuntimeNamespace() {
        return "Deprecated";
    }

    @Override
    public String mapClassName(String namespace, String className) {
        return "Deprecated";
    }

    @Override
    public String unmapClassName(String namespace, String className) {
        return "Deprecated";
    }

    @Override
    public String mapFieldName(String namespace, String owner, String name, String descriptor) {
        return "Deprecated";
    }

    @Override
    public String mapMethodName(String namespace, String owner, String name, String descriptor) {
        return "Deprecated";
    }
}
