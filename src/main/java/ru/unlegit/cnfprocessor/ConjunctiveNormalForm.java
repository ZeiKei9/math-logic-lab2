package ru.unlegit.cnfprocessor;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record ConjunctiveNormalForm(char[] universe, Operand[][] disjuncts) implements Comparable<ConjunctiveNormalForm> {

    public static ConjunctiveNormalForm fromString(String string) {
        string = string.replaceAll("\\s+", "");

        Operand[][] disjuncts;

        if (string.contains("&")) {
            disjuncts = Arrays.stream(string.split("&"))
                    .map(ConjunctiveNormalForm::disjunctFromString)
                    .toArray(Operand[][]::new);
        } else {
            disjuncts = new Operand[][]{disjunctFromString(string)};
        }

        Character[] symbolSet = Arrays.stream(disjuncts)
                .flatMap(Arrays::stream)
                .map(Operand::symbol)
                .distinct()
                .toArray(Character[]::new);
        char[] universe = new char[symbolSet.length];

        for (int i = 0; i < symbolSet.length; i++) {
            universe[i] = symbolSet[i];
        }

        return new ConjunctiveNormalForm(universe, disjuncts);
    }

    private static Operand[] disjunctFromString(String string) {
        if (string.charAt(0) == '(') {
            if (string.charAt(string.length() - 1) != ')') {
                throw new IllegalArgumentException("Ошибка формата КНФ в дюзъюнкте '%s'".formatted(string));
            }

            string = string.substring(1, string.length() - 1);
        }

        return Arrays.stream(string.split("\\|"))
                .map(Operand::fromString)
                .toArray(Operand[]::new);
    }

    @Override
    public String toString() {
        return Arrays.stream(disjuncts).map(operands -> Arrays.stream(operands)
                .map(Operand::toString)
                .collect(Collectors.joining(" | ", "(", ")"))
        ).collect(Collectors.joining(" & "));
    }

    public boolean isGenerallySignificant() {
        if (universe.length > 32) {
            throw new RuntimeException("Невозможно проверить общезначимость КНФ с размер юнивёрсума %d > 32 ".formatted(
                    universe.length
            ));
        }

        int valuesLimit = 1 << universe.length;

        for (int values = 0; values < valuesLimit; values++) {
            for (Operand[] disjunct : disjuncts) {
                if (computeDisjunct(disjunct, values) == 0) return false;
            }
        }

        return true;
    }

    public int[] findFalseInterpretations() {
        List<Integer> interpretations = new ArrayList<>();

        int valuesLimit = 1 << universe.length;

        for (int values = 0; values < valuesLimit; values++) {
            for (Operand[] disjunct : disjuncts) {
                if (computeDisjunct(disjunct, values) == 0) {
                    interpretations.add(values);
                    break;
                }
            }
        }

        int[] interpretationsArray = new int[interpretations.size()];

        for (int i = 0; i < interpretationsArray.length; i++) {
            interpretationsArray[i] = interpretations.get(i);
        }

        return interpretationsArray;
    }

    private int computeDisjunct(Operand[] disjunct, int values) {
        int result = (values & indexOf(disjunct[0].symbol())) & 0x1;

        for (int i = 1; i < disjunct.length; i++) {
            result |= (values & indexOf(disjunct[i].symbol()));
        }

        return result;
    }

    private int indexOf(char symbol) {
        for (int i = 0; i < universe.length; i++) {
            if (universe[i] == symbol) return i;
        }

        throw new RuntimeException("unreachable point");
    }

    public ConjunctiveNormalForm resolution(ConjunctiveNormalForm cnf) {
        List<Operand[]> newDisjuncts = new ArrayList<>();

        for (Operand[] disjunctA : disjuncts) {
            for (Operand[] disjunctB : cnf.disjuncts) {
                char contradictionSymbol = '!';

                for (Operand operandA : disjunctA) {
                    for (Operand operandB : disjunctB) {
                        if (operandA.symbol() == operandB.symbol() && operandA.inversion() != operandB.inversion()) {
                            contradictionSymbol = operandA.symbol();
                            break;
                        }
                    }

                    if (contradictionSymbol != '!') break;
                }

                if (contradictionSymbol != '!') {
                    char finalContradictionSymbol = contradictionSymbol;

                    Operand[] disjunct = Stream.concat(
                            Arrays.stream(disjunctA).filter(operand -> operand.symbol() != finalContradictionSymbol),
                            Arrays.stream(disjunctB).filter(operand -> operand.symbol() != finalContradictionSymbol)
                    ).distinct().sorted().toArray(Operand[]::new);

                    if (disjunct.length > 0) {
                        newDisjuncts.add(disjunct);
                    }
                }
            }
        }

        if (newDisjuncts.isEmpty()) return null;

        return new ConjunctiveNormalForm(universe, newDisjuncts.toArray(Operand[][]::new));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConjunctiveNormalForm cnf) || disjuncts.length != cnf.disjuncts.length) return false;

        return IntStream.range(0, disjuncts.length).allMatch(i -> disjunctsEquals(disjuncts[i], cnf.disjuncts[i]));
    }

    private boolean disjunctsEquals(Operand[] left, Operand[] right) {
        return (left.length == right.length) && IntStream.range(0, left.length).allMatch(i -> left[i].equals(right[i]));
    }

    @Override
    public int compareTo(@NotNull ConjunctiveNormalForm cnf) {
        if (disjuncts.length != cnf.disjuncts.length) return Integer.compare(disjuncts.length, cnf.disjuncts.length);

        for (int i = 0; i < disjuncts.length; i++) {
            Operand[] disjunct = disjuncts[i];
            Operand[] otherDisjunct = cnf.disjuncts[i];

            if (disjunct.length == otherDisjunct.length) {
                for (int j = 0; j < disjuncts.length; j++) {
                    Operand operand = disjunct[j];
                    Operand otherOperand = otherDisjunct[j];

                    int operandsComparison = operand.compareTo(otherOperand);

                    if (operandsComparison != 0) {
                        return operandsComparison;
                    }
                }
            } else {
                return Integer.compare(disjunct.length, otherDisjunct.length);
            }
        }

        return 0;
    }
}