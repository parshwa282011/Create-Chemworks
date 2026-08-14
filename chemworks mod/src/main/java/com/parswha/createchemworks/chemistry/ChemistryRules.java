package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.client.Elements.ElectronBlock;
import com.parswha.createchemworks.client.Elements.Element;

import java.util.List;
import java.util.Set;

public final class ChemistryRules {
    private static final Set<Integer> DIATOMIC_ELEMENTS = Set.of(1, 7, 8, 9, 17, 35, 53);
    private ChemistryRules() { }

    public static int valenceElectrons(Element element) {
        return switch (element.block()) {
            case S -> element.number() == 2 ? 2 : Math.min(2, element.column() + 1);
            case P -> Math.max(3, Math.min(8, element.column() - 10));
            case D -> Math.max(1, Math.min(4, element.column() - 1));
            case F, T -> Math.max(2, Math.min(4, element.column() % 5));
        };
    }

    public static List<Integer> commonOxidationStates(Element element) {
        if (isNobleGas(element)) return List.of(0);
        int valence = valenceElectrons(element);
        if (element.block() == ElectronBlock.P && valence > 4) {
            return List.of(-(8 - valence), valence - 4, valence);
        }
        if (element.block() == ElectronBlock.D || element.block().ordinal() >= ElectronBlock.F.ordinal()) {
            return List.of(2, Math.min(4, Math.max(3, valence)));
        }
        return List.of(Math.min(valence, 4));
    }

    public static boolean isNobleGas(Element element) {
        return element.number() == 2 || (element.block() == ElectronBlock.P && element.column() == 18);
    }

    public static boolean isMetal(Element element) {
        if (element.number() == 1 || isNobleGas(element)) return false;
        return switch (element.block()) {
            case D, F, T, S -> true;
            case P -> false;
        };
    }

    public static int elementalMoleculeSize(Element element) {
        return DIATOMIC_ELEMENTS.contains(element.number()) ? 2 : 1;
    }

    public static String elementalFormula(Element element) {
        return element.symbol() + (elementalMoleculeSize(element) == 2 ? "₂" : "");
    }
}
