// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.sin3point14;

import org.terasology.entitySystem.Component;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generator.plugin.RegisterPlugin;

@RegisterPlugin
@Requires(@Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = Volcano.MAXWIDTH, bottom = 0, top = Volcano.MAXHEIGHT)))
@Produces(VolcanoFacet.class)
public class VolcanoFacetProvider implements FacetProviderPlugin {
    private Noise noise;

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(VolcanoFacet.class).maxWith(Volcano.MAXHEIGHT, 0, Volcano.MAXWIDTH);
        VolcanoFacet volcanoFacet = new VolcanoFacet(region.getRegion(), border);
        SurfaceHeightFacet facet = region.getRegionFacet(SurfaceHeightFacet.class);
        Rect2i worldRegion = facet.getWorldRegion();

        for (int wz = worldRegion.minY(); wz <= worldRegion.maxY(); wz++) {
            for (int wx = worldRegion.minX(); wx <= worldRegion.maxX(); wx++) {
                int surfaceHeight = TeraMath.floorToInt(facet.getWorld(wx, wz));
                if (surfaceHeight >= volcanoFacet.getWorldRegion().minY()
                        && surfaceHeight <= volcanoFacet.getWorldRegion().maxY()) {
                    if (noise.noise(wx, wz) > 0.9999) {
                        volcanoFacet.setWorld(wx, surfaceHeight, wz, new Volcano(wx + (Volcano.MAXWIDTH / 2), wz + (Volcano.MAXWIDTH / 2)));
                    }
                }
            }
        }

        region.setRegionFacet(VolcanoFacet.class, volcanoFacet);
    }

    @Override public void setSeed(long seed) {
        // comment this for testing and
//        noise = new SubSampledNoise(new WhiteNoise(seed), new Vector2f(0.1f, 0.1f), Integer.MAX_VALUE);
        // uncomment this for testing
         noise = new WhiteNoise(seed);
    }
}
