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
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

@Produces(SurfaceHeightFacet.class)
public class SurfaceProvider implements FacetProvider {

    private Noise tileableNoise;

    // Vary this!!!
    private final int gridSize = 7;

    private final float simplexMagicNumber = 0.5773502691896258f;

    @Override
    public void setSeed(long seed) {
        tileableNoise = new SimplexNoise(seed, gridSize);
    }

    public float noiseWrapper(int x, int y, float xCenter, float yCenter, float minDistance, float maxDistance) {
        Vector2f relative = new Vector2f((float) x - xCenter, (float) y - yCenter);
        if (relative.equals(Vector2f.zero())) {
            return 1.0f;
        }
        float scaledAngle = (((float) Math.atan2(relative.y, relative.x) + (float) Math.PI) * ((float) gridSize * simplexMagicNumber)) / (2.0f * (float) Math.PI);

        float b = 1.0f / minDistance;
        float a = 1.0f / maxDistance - b;

        float adjustedNoise = (a * ((tileableNoise.noise(scaledAngle, scaledAngle) + 1.0f) / 2.0f) + b) * relative.length();

        return (1.0f - TeraMath.clamp(adjustedNoise));
    }

    @Override
    public void process(GeneratingRegion region) {
        // Create our surface height facet (we will get into borders later)
        Border3D border = region.getBorderForFacet(SurfaceHeightFacet.class);
        SurfaceHeightFacet facet = new SurfaceHeightFacet(region.getRegion(), border);

        // Loop through every position in our 2d array
        Rect2i processRegion = facet.getWorldRegion();
        for (BaseVector2i position: processRegion.contents()) {
            facet.setWorld(position, noiseWrapper(position.x(), position.y(), 0, 0, 20, 50) * 30);
        }

        // Pass our newly created and populated facet to the region
        region.setRegionFacet(SurfaceHeightFacet.class, facet);
    }
}
