package com.parswha.createchemworks.client.Elements;

public enum ElectronBlock {
    S("s", 0xFF2F6BFF, 2, -1, 1),
    P("p", 0xFF2DBE60, 6, -1, 2),
    D("d", 0xFFFF8C2A, 10, -1, 4),
    F("f", 0xFFA855F7, 14, 2, 6),
    H("h", 0xFF00B8D9, 18, -1, 8),
    M("m", 0xFFE23D55, 22, -1, 10),
    A("a", 0xFFD4A017, 26, -1, 12);

    private final String label;
    private final int color;
    private final int capacity;
    private final int gatewayColumn;
    private final int firstPeriod;

    ElectronBlock(String label, int color, int capacity, int gatewayColumn, int firstPeriod) {
        this.label = label;
        this.color = color;
        this.capacity = capacity;
        this.gatewayColumn = gatewayColumn;
        this.firstPeriod = firstPeriod;
    }

    public String label() { return label; }
    public int color() { return color; }
    public int capacity() { return capacity; }
    public int gatewayColumn() { return gatewayColumn; }
    public int firstPeriod() { return firstPeriod; }
    public boolean hasSubTable() { return gatewayColumn >= 0; }
    public boolean appearsInPeriod(int period) { return hasSubTable() && period >= firstPeriod; }
}
