package com.parswha.createchemworks.chemistry;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record ChemicalReaction(
        ResourceLocation id,
        String equation,
        List<Integer> participatingElements,
        double minimumTemperatureK,
        double pressureKpa,
        double estimatedEnthalpyKj,
        String catalyst,
        String confidence
) { }
