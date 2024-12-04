package ru.unlegit.cnfprocessor;

import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class LogicalInference {

    public List<ConjunctiveNormalForm> infer(Collection<ConjunctiveNormalForm> knownForms) {
        List<ConjunctiveNormalForm> inferences = new LinkedList<>(knownForms);

        boolean addedNewForm;

        do {
            addedNewForm = false;
            List<ConjunctiveNormalForm> formsList = new ArrayList<>(inferences);

            for (int i = 0; i < formsList.size(); i++) {
                for (int j = 0; j < formsList.size(); j++) {
                    if (i != j) {
                        ConjunctiveNormalForm cnfA = formsList.get(i);
                        ConjunctiveNormalForm cnfB = formsList.get(j);
                        ConjunctiveNormalForm resolutionForm = cnfA.resolution(cnfB);

                        if (resolutionForm != null && !inferences.contains(resolutionForm)) {
                            inferences.add(resolutionForm);
                            addedNewForm = true;
                        }
                    }
                }
            }
        } while (addedNewForm);

        return inferences.stream().sorted(Comparator.reverseOrder()).toList();
    }
}