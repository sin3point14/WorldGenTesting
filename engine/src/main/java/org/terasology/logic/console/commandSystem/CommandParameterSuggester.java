/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.console.commandSystem;

import com.google.common.collect.Sets;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.console.Console;
import org.terasology.module.sandbox.API;
import org.terasology.naming.Name;
import org.terasology.network.ClientComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;

import java.util.Collection;
import java.util.Set;

/**
 * A class used for suggesting command parameter values
 *
 * @author Limeth
 */
@API
public interface CommandParameterSuggester<T> {
    /**
     * @param resolvedParameters Currently entered values of the types declared in the command method
     * @return A collection of suggested matches.
     */
    Set<T> suggest(EntityRef sender, Object... resolvedParameters);
}
