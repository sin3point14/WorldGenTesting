// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.sin3point14;

import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2i;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

@Requires(@Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = 18))) //beware
@Produces(VolcanoFacet.class)
public class VolcanoFacetProvider implements FacetProvider {
    private WhiteNoise noise;
    private FastRandom random;

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(VolcanoFacet.class).extendBy(Volcano.MAXHEIGHT, 0, (int) (Volcano.MAXGRIDSIZE / Volcano.MINSLOPE));
        VolcanoFacet volcanoFacet = new VolcanoFacet(region.getRegion(), border);
        SurfaceHeightFacet facet = region.getRegionFacet(SurfaceHeightFacet.class);
        Rect2i worldRegion = facet.getWorldRegion();

        for (int wz = worldRegion.minY(); wz <= worldRegion.maxY(); wz++) {
            for (int wx = worldRegion.minX(); wx <= worldRegion.maxX(); wx++) {
                int surfaceHeight = TeraMath.floorToInt(facet.getWorld(wx, wz));
                if (surfaceHeight >= volcanoFacet.getWorldRegion().minY() &&
                        surfaceHeight <= volcanoFacet.getWorldRegion().maxY()) {
                    if (noise.noise(wx, wz) > 0.9999) {
                        volcanoFacet.setWorld(wx, surfaceHeight, wz, new Volcano(random));
                    }
                }
            }
        }

        region.setRegionFacet(VolcanoFacet.class, volcanoFacet);
    }

    @Override public void setSeed(long seed) {
        noise = new WhiteNoise(seed);
        random = new FastRandom(seed);
    }
}
