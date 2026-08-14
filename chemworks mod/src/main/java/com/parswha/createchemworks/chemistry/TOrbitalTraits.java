package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.client.Elements.ElectronBlock;
import com.parswha.createchemworks.client.Elements.Element;

import java.util.List;

/** The 36 deliberately positive, drawback-free endgame traits of period 8/9 t metals. */
public final class TOrbitalTraits {
    private static final List<String> TRAITS = List.of(
            "Vibration damping", "Electrical conductivity", "Processing efficiency", "Adaptive tooling",
            "Structural integrity", "Mining speed", "Night efficiency", "Low weight", "Heat resistance",
            "Melee power", "Alloy compatibility", "Operating speed", "Sonic pulse", "Energy transfer",
            "Durability conservation", "Durability", "Armor penetration", "Weakest-trait amplification",
            "Magic capacity", "Resonance amplification", "Response speed", "Sonic range",
            "Rotational efficiency", "Environmental detection", "Global trait amplification",
            "Impact reflection", "Knockback resistance", "Vibration suppression", "RPM power conversion",
            "Resonant weakening", "Perfect alloy cohesion", "Impact energy storage", "Aerial control",
            "Focused sonic projection", "Pump pressure tolerance", "Chemical analysis"
    );

    private TOrbitalTraits() { }

    public static String trait(Element element) {
        if (element.block() != ElectronBlock.T) throw new IllegalArgumentException("Element is not in the t orbital");
        int index = element.period() == 8 ? element.number() - 121 : element.number() - 171 + 18;
        if (index < 0 || index >= TRAITS.size()) throw new IllegalArgumentException("Unknown t-orbital element");
        return TRAITS.get(index);
    }

    public static List<String> all() { return TRAITS; }
}
