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

import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

@Produces(SurfaceHeightFacet.class)
public class SurfaceProvider implements FacetProvider {

    private static final int MINHEIGHT = 70;
    private static final int MAXHEIGHT = 120;
    private static final int MINGRIDSIZE = 8;
    private static final int MAXGRIDSIZE = 12;
    private static final float MINSLOPE = 0.7f;
    private static final float MAXSLOPE = 1.0f;

    private Noise tileableNoise;
    private FastRandom random;

    private int height;
    private float innerRadius;
    private float outerRadius;
    private int gridSize;

    @Override
    public void setSeed(long seed) {
        random = new FastRandom(seed);
        gridSize = random.nextInt(MINGRIDSIZE, MAXGRIDSIZE);
        tileableNoise = new SimplexNoise(seed, gridSize);
    }

    public float noiseWrapper(int x, int y, float xCenter, float yCenter, float minDistance, float maxDistance) {
        Vector2f relative = new Vector2f((float) x - xCenter, (float) y - yCenter);

        float plainNoise = tileableNoise.noise(relative.x / 30f, relative.y / 30f);

        if (relative.equals(Vector2f.zero())) {
//            return 1.0f + plainNoise / 10f;
            return 0f;
        }
        float scaledAngle = (((float) Math.atan2(relative.y, relative.x) + (float) Math.PI) * ((float) gridSize * SimplexNoise.TILEABLE1DMAGICNUMBER)) / (2.0f * (float) Math.PI);

        float b = 1.0f / minDistance;
        float a = 1.0f / maxDistance - b;

        float adjustedNoise = (a * ((tileableNoise.noise(scaledAngle, scaledAngle) + 1.0f) / 2.0f) + b) * relative.length();

        float clampedInvertedNoise = (float) Math.pow((1.0f - TeraMath.clamp(adjustedNoise)), 1.2f);

        float anotherIntermediate = (clampedInvertedNoise * (1 + plainNoise / 10f)) / 1.1f;

        if(anotherIntermediate > 0.7f) {
            anotherIntermediate -= 2 * (anotherIntermediate - 0.7f);
        }

        return anotherIntermediate;
    }

    @Override
    public void initialize() {
        height = random.nextInt(MINHEIGHT, MAXHEIGHT);
        innerRadius = height / random.nextFloat(MINSLOPE, (MAXSLOPE + 2 * MINSLOPE) / 3);
        outerRadius = height / random.nextFloat((MINSLOPE + 2 * MAXSLOPE) / 3, MAXSLOPE);
    }

    @Override
    public void process(GeneratingRegion region) {
        // Create our surface height facet (we will get into borders later)
        Border3D border = region.getBorderForFacet(SurfaceHeightFacet.class);
        SurfaceHeightFacet facet = new SurfaceHeightFacet(region.getRegion(), border);

        // Loop through every position in our 2d array
        Rect2i processRegion = facet.getWorldRegion();
        for (BaseVector2i position: processRegion.contents()) {
            facet.setWorld(position, noiseWrapper(position.x(), position.y(), 0, 0, innerRadius, outerRadius) * height);
        }

        // Pass our newly created and populated facet to the region
        region.setRegionFacet(SurfaceHeightFacet.class, facet);
    }
}
