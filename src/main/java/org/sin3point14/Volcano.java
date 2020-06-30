// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.sin3point14;

import org.terasology.utilities.procedural.RegionSelectorNoise;
import org.terasology.utilities.random.FastRandom;

public class Volcano {
    public static final int MINHEIGHT = 70;
    public static final int MAXHEIGHT = 120;
    public static final int MINGRIDSIZE = 8;
    public static final int MAXGRIDSIZE = 12;
    public static final float MINSLOPE = 0.7f;
    public static final float MAXSLOPE = 1.0f;

    public int height;
    public float innerRadius;
    public float outerRadius;

    public Volcano(FastRandom random) {
        height = random.nextInt(MINHEIGHT, MAXHEIGHT);
        innerRadius = height / random.nextFloat(MINSLOPE, (MAXSLOPE + 2 * MINSLOPE) / 3);
        outerRadius = height / random.nextFloat((MINSLOPE + 2 * MAXSLOPE) / 3, MAXSLOPE);
//        regionNoise = new RegionSelectorNoise(seed, gridSize, 0, 0, innerRadius, outerRadius);
    }
}
