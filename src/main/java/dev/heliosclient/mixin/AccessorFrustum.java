/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package dev.heliosclient.mixin;

import net.minecraft.client.render.Frustum;
import org.joml.FrustumIntersection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Frustum.class)
public interface AccessorFrustum {

    @Accessor
    FrustumIntersection getFrustumIntersection();

    @Accessor
    void setFrustumIntersection(FrustumIntersection vector4f);

    @Accessor
    double getX();

    @Accessor
    void setX(double x);

    @Accessor
    double getY();

    @Accessor
    void setY(double y);

    @Accessor
    double getZ();

    @Accessor
    void setZ(double z);
}
