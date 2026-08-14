package com.parswha.createchemworks.chemistry;

import java.util.Map;

/** Properties for a compound discovered by the live chemistry calculator. */
public record CalculatedCompound(String formula, Map<Integer, Integer> composition,
                                 double molarMass, String bondType, String phase,
                                 double meltingPointK, double boilingPointK,
                                 String confidence) { }
