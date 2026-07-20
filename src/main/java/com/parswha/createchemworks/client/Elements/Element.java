package com.parswha.createchemworks.client.Elements;

import java.util.Objects;

public record Element(
        int number,
        String symbol,
        String name,
        int period,
        int column,
        ElectronBlock block
) {
    public Element {
        if (number < 1 || period < 1 || period > ElementsData.PERIODS) {
            throw new IllegalArgumentException("Invalid atomic number or period");
        }
        Objects.requireNonNull(symbol);
        Objects.requireNonNull(name);
        Objects.requireNonNull(block);
    }

    public boolean belongsOnMainTable() {
        return block == ElectronBlock.S
                || block == ElectronBlock.P
                || block == ElectronBlock.D;
    }

    public boolean acceleratorOnly() {
        return period >= 10;
    }
}
