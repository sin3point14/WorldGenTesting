/*
 * Copyright 2020 MovingBlocks
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
package org.sin3point14;

import org.terasology.math.ChunkMath;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

import java.util.AbstractMap;
import java.util.Map;

public class VolcanoRasterizer implements WorldRasterizer {
    @In
    private WorldGeneratorPluginLibrary worldGeneratorPluginLibrary;
    private Block stone;
    private Block lava;

    @Override
    public void initialize() {
        stone = CoreRegistry.get(BlockManager.class).getBlock("CoreAssets:Stone");
        lava = CoreRegistry.get(BlockManager.class).getBlock("CoreAssets:Stone");
    }

//    public float noiseWrapper(int x, int y) {
//        float baseNoise = regionNoise.noise(x, y);
//        float plainNoise = tileableNoise.noise(x / 30f, y / 30f);
//        float clampedInvertedNoise = (float) Math.pow(baseNoise, 2f);
//        float anotherIntermediate = (clampedInvertedNoise * (1 + plainNoise / 10f)) / 1.1f;
//
//        if (anotherIntermediate > 0.7f) {
//            anotherIntermediate -= 2 * (anotherIntermediate - 0.7f);
//        }
//
//        return anotherIntermediate;
//    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        VolcanoFacet volcanoFacet = chunkRegion.getFacet(VolcanoFacet.class);

        for (Map.Entry<BaseVector3i, Volcano> entry : volcanoFacet.getWorldEntries().entrySet()) {

            Vector3i basePosition = new Vector3i(entry.getKey());
            Volcano volcano = entry.getValue();

//            int size = 40;
//            int min = 0;
//            int height = (40 + 1) / 2;

            for (int i = 0; i <= Volcano.MAXWIDTH; i++) {
                for (int k = 0; k <= Volcano.MAXWIDTH; k++) {
                    Vector3i chunkBlockPosition = new Vector3i(i, 0, k).add(basePosition);

                    VolcanoHeightInfo blockInfo = volcano.getHeightAndIsLava(chunkBlockPosition.x, chunkBlockPosition.z);
//                    int height = volcano.getHeightAndIsLava(chunkBlockPosition.x, chunkBlockPosition.z);

                    for (int j = 0; j <= blockInfo.height; j++) {
//                    for (int j = 0; j <= height; j++) {
                        Vector3i chunkBlockPosition2 = new Vector3i(i, j, k).add(basePosition);
                        if (chunk.getRegion().encompasses(chunkBlockPosition2)) {
                            if (blockInfo.isLava) {
                                chunk.setBlock(ChunkMath.calcBlockPos(chunkBlockPosition2), lava);
                            } else {
                                chunk.setBlock(ChunkMath.calcBlockPos(chunkBlockPosition2), stone);
                            }
                        }
                    }
                }
//                min++;
//                size--;
            }

        }
//            SurfaceHeightFacet surfaceHeightFacet = chunkRegion.getFacet(SurfaceHeightFacet.class);
//        for (Vector3i position : chunkRegion.getRegion()) {
//            float surfaceHeight = surfaceHeightFacet.getWorld(position.x, position.z);
//            if (position.y < surfaceHeight) {
//                chunk.setBlock(ChunkMath.calcBlockPos(position), dirt);
//            }
//        }
    }
}
