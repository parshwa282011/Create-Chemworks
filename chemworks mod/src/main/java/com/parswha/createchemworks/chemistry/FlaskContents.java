package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.client.Elements.Element;
import com.parswha.createchemworks.client.Elements.ElementsData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parser shared by the flask editor, server validation, and reaction tester. */
public record FlaskContents(String display, List<Component> components, int estimatedCharge, boolean possible) {
    private static final Pattern SOLUTE = Pattern.compile("(?:(\\d+(?:\\.\\d+)?)\\s*\\(([^()]+)\\)|([^()]+))");
    private static final Pattern ATOM = Pattern.compile("([A-Z][a-z]?)(\\d*)");

    public static FlaskContents parse(String input) {
        String value = input.trim();
        if (value.equalsIgnoreCase("e_-") || value.equals("e⁻"))
            return new FlaskContents("e⁻", List.of(new Component(1, "e⁻")), -1, true);
        if (value.isEmpty()) throw new IllegalArgumentException("Enter a compound or solution");
        List<Component> result = new ArrayList<>();
        String[] rawTerms = value.split("\\s+\\+\\s+");
        for (String rawTerm : rawTerms) {
            Matcher terms = SOLUTE.matcher(rawTerm.trim());
            if (!terms.matches()) throw new IllegalArgumentException("Solutions use x(compound) + y(compound)");
            double amount = terms.group(1) == null ? 1.0 : Double.parseDouble(terms.group(1));
            String formula = (terms.group(2) == null ? terms.group(3) : terms.group(2)).trim();
            validateFormula(formula);
            if (!(amount > 0) || !Double.isFinite(amount)) throw new IllegalArgumentException("Ratios must be positive");
            result.add(new Component(amount, formula));
        }
        if (result.isEmpty()) throw new IllegalArgumentException("Invalid compound/solution syntax");
        int charge = result.size() == 1 ? estimateCharge(result.getFirst().formula) : 0;
        String display = result.size() == 1 ? result.getFirst().formula
                : result.stream().map(c -> trim(c.amount) + "(" + c.formula + ")").reduce((a,b) -> a + " + " + b).orElse("");
        return new FlaskContents(display, List.copyOf(result), charge, Math.abs(charge) <= 8);
    }

    private static void validateFormula(String formula) {
        if (KnownCompounds.find(formula) != null) return;
        Matcher atoms = ATOM.matcher(formula); int end = 0;
        while (atoms.find()) {
            if (atoms.start() != end) throw new IllegalArgumentException("Invalid formula near: " + formula.substring(end));
            String symbol = atoms.group(1);
            if (ElementsData.all().stream().noneMatch(e -> e.symbol().equals(symbol)))
                throw new IllegalArgumentException("Unknown element: " + symbol);
            int count = atoms.group(2).isEmpty() ? 1 : Integer.parseInt(atoms.group(2));
            if (count < 1 || count > 999) throw new IllegalArgumentException("Atom count must be 1–999");
            end = atoms.end();
        }
        if (end != formula.length() || end == 0) throw new IllegalArgumentException("Invalid formula: " + formula);
    }

    private static int estimateCharge(String formula) {
        KnownCompound known = KnownCompounds.find(formula);
        if (known != null) return 0;
        Matcher matcher = ATOM.matcher(formula); Map<Element,Integer> atoms = new LinkedHashMap<>();
        while (matcher.find()) {
            Element element = ElementsData.all().stream().filter(e -> e.symbol().equals(matcher.group(1))).findFirst().orElseThrow();
            atoms.merge(element, matcher.group(2).isEmpty() ? 1 : Integer.parseInt(matcher.group(2)), Integer::sum);
        }
        if (atoms.size() == 1) return 0;
        int charge = 0;
        for (var atom : atoms.entrySet()) charge += ChemistryRules.commonOxidationStates(atom.getKey()).getFirst() * atom.getValue();
        return charge;
    }
    private static String trim(double value) { return value == Math.rint(value) ? Long.toString((long)value) : Double.toString(value); }
    public record Component(double amount, String formula) { }
}
