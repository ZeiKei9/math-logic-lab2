package ru.unlegit.cnfprocessor;

import org.jetbrains.annotations.NotNull;

public record Operand(char symbol, boolean inversion) implements Comparable<Operand> {

    public static Operand fromString(String string) {
        int length = string.length();
        char firstChar = string.charAt(0);

        if (length > 2 || (length == 2 && firstChar != '!')) {
            throw new IllegalArgumentException("Ошибка формата КНФ в операнде '%s'".formatted(string));
        }

        return length == 1 ? new Operand(firstChar, false) : new Operand(string.charAt(1), true);
    }

    @Override
    public String toString() {
        return (inversion ? "!" : "") + symbol;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Operand operand && symbol == operand.symbol && inversion == operand.inversion;
    }

    @Override
    public int compareTo(@NotNull Operand operand) {
        if (symbol == operand.symbol) {
            if (inversion) {
                if (operand.inversion) return 0;
                return -1;
            }
            return -1;
        }

        return Character.compare(symbol, operand.symbol);
    }
}