// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.sin3point14;

import org.terasology.entitySystem.Component;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generation.facets.base.BaseFieldFacet2D;
import org.terasology.world.generator.plugin.RegisterPlugin;

@RegisterPlugin
//@Requires(@Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = Volcano.MAXWIDTH, bottom = 0, top =
//        Volcano.MAXHEIGHT)))
@Requires({
        @Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = Volcano.MAXWIDTH)),
        @Facet(value = SeaLevelFacet.class, border = @FacetBorder(sides = Volcano.MAXWIDTH))
})
@Produces(VolcanoFacet.class)
public class VolcanoProvider implements FacetProviderPlugin {
    private Noise noise;

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(VolcanoFacet.class).extendBy(0, Volcano.MAXHEIGHT * 2,
                Volcano.MAXWIDTH);
        VolcanoFacet volcanoFacet = new VolcanoFacet(region.getRegion(), border);
        SurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(SurfaceHeightFacet.class);
        Rect2i worldRegion = surfaceHeightFacet.getWorldRegion();
        SeaLevelFacet seaLevelFacet = region.getRegionFacet(SeaLevelFacet.class);


        for (int wz = worldRegion.minY(); wz <= worldRegion.maxY(); wz++) {
            for (int wx = worldRegion.minX(); wx <= worldRegion.maxX(); wx++) {
                int surfaceHeight = TeraMath.floorToInt(surfaceHeightFacet.getWorld(wx, wz));
                int seaLevel = seaLevelFacet.getSeaLevel();
                if (surfaceHeight > seaLevel && noise.noise(wx, wz) > 0.9999) {
                    int lowestY = getLowestY(new Vector3i(wx, surfaceHeight, wz), surfaceHeightFacet);

//                if (surfaceHeight >= volcanoFacet.getWorldRegion().minY()
//                        && surfaceHeight <= volcanoFacet.getWorldRegion().maxY()) {
                    if (lowestY >= volcanoFacet.getWorldRegion().minY()
                            && lowestY <= volcanoFacet.getWorldRegion().maxY()) {
//                    if (noise.noise(wx, wz) > 0.9999 && checkGradient(new Vector3i(wx, surfaceHeight, wz),
//                    surfaceHeightFacet)) {
//                    int lowestY = getLowestY(new Vector3i(wx, surfaceHeight, wz), surfaceHeightFacet);
//                    if (wx == 0 && wz == 0) {
                        volcanoFacet.setWorld(wx, lowestY, wz, new Volcano(wx + (Volcano.MAXWIDTH / 2),
//                                volcanoFacet.setWorld(wx, surfaceHeight, wz, new Volcano(wx + (Volcano.MAXWIDTH / 2),
                                wz + (Volcano.MAXWIDTH / 2)));
                    }
                }
            }
        }

        region.setRegionFacet(VolcanoFacet.class, volcanoFacet);
    }

    @Override
    public void setSeed(long seed) {
        // comment this for testing and
//        noise = new SubSampledNoise(new WhiteNoise(seed), new Vector2f(0.1f, 0.1f), Integer.MAX_VALUE);
        // uncomment this for testing
        noise = new WhiteNoise(seed);
    }

    private int minY(Vector3i corner, BaseFieldFacet2D facet) {

        Vector3i stepX = new Vector3i(10, 0, 0);
        Vector3i stepZ = new Vector3i(0, 0, 10);
        Vector3i start = new Vector3i(corner);
        Vector3i end = new Vector3i(corner).add(Volcano.MAXWIDTH, 0, Volcano.MAXWIDTH);
        int maxY = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        for (Vector3i pos = new Vector3i(start); pos.x <= end.x; pos.add(stepX)) {
            for (pos.setZ(start.z); pos.z <= end.z; pos.add(stepZ)) {

                Rect2i checkRegion = Rect2i.createFromMinAndMax(pos.x() - 3, pos.z() - 3, pos.x() + 3, pos.z() + 3);

                if (facet.getWorldRegion().contains(checkRegion)) {
                    float xDiff = Math.abs(facet.getWorld(pos.x() + 3, pos.z()) - facet.getWorld(pos.x() - 3, pos.z()));
                    float yDiff = Math.abs(facet.getWorld(pos.x(), pos.z() + 3) - facet.getWorld(pos.x(), pos.z() - 3));
                    float xyDiff = Math.abs(facet.getWorld(pos.x() + 3, pos.z() + 3) - facet.getWorld(pos.x() - 3,
                            pos.z() - 3));
                    maxY = Math.max(Math.max(Math.max(maxY, (int) xDiff), (int) xyDiff), (int) yDiff);
//                    minY = Math.min(Math.max(Math.min(minY, (int) xDiff), (int) xyDiff), (int) yDiff);
//                    if (xDiff > 2 || yDiff > 2 || xyDiff > 2) {
//                        return false;
//                    }
                }
            }
        }
        return minY;
    }

    private int getLowestY(Vector3i corner, BaseFieldFacet2D facet) {

        //Note- check edges only
        Vector3i stepX = new Vector3i(10, 0, 0);
        Vector3i stepZ = new Vector3i(0, 0, 10);
        Vector3i start = new Vector3i(corner);
        Vector3i end = new Vector3i(corner).add(Volcano.MAXWIDTH, 0, Volcano.MAXWIDTH);
        int lowestY = Integer.MAX_VALUE;
        for (Vector3i pos = new Vector3i(start); pos.x <= end.x; pos.add(stepX)) {
            for (pos.setZ(start.z); pos.z <= end.z; pos.add(stepZ)) {
                if (facet.getWorldRegion().contains(pos.x(), pos.z())) {
                    int y = (int) facet.getWorld(pos.x(), pos.z());
                    lowestY = Math.min(y, lowestY);
                }
//                Rect2i checkRegion = Rect2i.createFromMinAndMax(pos.x() - 3, pos.z() - 3, pos.x() + 3, pos.z() + 3);
//
//                if (facet.getWorldRegion().contains(checkRegion)) {
//                    float xDiff = Math.abs(facet.getWorld(pos.x() + 3, pos.z()) - facet.getWorld(pos.x() - 3, pos.z
//                    ()));
//                    float yDiff = Math.abs(facet.getWorld(pos.x(), pos.z() + 3) - facet.getWorld(pos.x(), pos.z() -
//                    3));
//                    float xyDiff = Math.abs(facet.getWorld(pos.x() + 3, pos.z() + 3) - facet.getWorld(pos.x() - 3,
//                            pos.z() - 3));
//                    if (xDiff > 2 || yDiff > 2 || xyDiff > 2) {
//                        return false;
//                    }
//                }
            }
        }
        return lowestY;
    }
}
