package com.parswha.createchemworks.machinery;

public interface ChemicalNode {
    ChemicalVolume chemicalVolume();
    default boolean permitsFlow() { return true; }
}
