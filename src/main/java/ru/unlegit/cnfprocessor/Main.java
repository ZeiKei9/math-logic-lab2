package ru.unlegit.cnfprocessor;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public final class Main {

    public static void main(String[] args) {
        System.out.println("Введите формулы-посылки (для окончания ввода формул-посылок передайте пустую строку):");
        List<ConjunctiveNormalForm> premises = new LinkedList<>();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String line = scanner.nextLine().trim();

                if (line.isEmpty()) {
                    if (premises.isEmpty()) {
                        System.out.println("Введите хотя бы одну формулу-посылку");
                    } else {
                        break;
                    }
                } else {
                    premises.add(ConjunctiveNormalForm.fromString(line));
                }
            }
        }

        System.out.println("\nВыводим формулы-следствия...\n");

        List<ConjunctiveNormalForm> inferences = LogicalInference.infer(premises);

        System.out.println("Формулы-следствия: ");

        inferences.forEach(inference -> System.out.println(" - " + inference));
    }
}