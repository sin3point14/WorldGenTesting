// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.sin3point14;

import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.RegionSelectorNoise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.random.FastRandom;

public class Volcano {
    public static final int MINHEIGHT = 40;
    public static final int MAXHEIGHT = 60;
    public static final int MINGRIDSIZE = 8;
    public static final int MAXGRIDSIZE = 12;
    public static final float MINSLOPE = 0.7f;
    public static final float MAXSLOPE = 1.0f;
    public static final int MAXWIDTH = 2 * (int) (MAXHEIGHT / MINSLOPE);

    public int height;
    private int gridSize;
    public float innerRadius;
    public float outerRadius;

    private Noise tileableNoise;
    private RegionSelectorNoise regionNoise;

    public int getHeight(int x, int z) {
        float baseNoise = regionNoise.noise(x, z);
        float plainNoise = tileableNoise.noise(x / 15f, z / 15f);
        float clampedInvertedNoise = (float) Math.pow(baseNoise, 2f);
        float anotherIntermediate = (clampedInvertedNoise * (1 + plainNoise / 10f)) / 1.1f;

        if (anotherIntermediate > 0.7f) {
            anotherIntermediate -= 2 * (anotherIntermediate - 0.7f);
        }

        return (int) (anotherIntermediate * height);
    }

    public Volcano(long seed, int xCenter, int zCenter) {
        FastRandom random = new FastRandom(seed);
        gridSize = random.nextInt(MINGRIDSIZE, MAXGRIDSIZE);
        tileableNoise = new SimplexNoise(seed, gridSize);
        height = random.nextInt(MINHEIGHT, MAXHEIGHT);
        innerRadius = height / random.nextFloat(MINSLOPE, (MAXSLOPE + 2 * MINSLOPE) / 3);
        outerRadius = height / random.nextFloat((MINSLOPE + 2 * MAXSLOPE) / 3, MAXSLOPE);
        regionNoise = new RegionSelectorNoise(seed, gridSize, xCenter, zCenter, innerRadius, outerRadius);
//        regionNoise = new RegionSelectorNoise(seed, gridSize, 0, 0, innerRadius, outerRadius);
    }

}
