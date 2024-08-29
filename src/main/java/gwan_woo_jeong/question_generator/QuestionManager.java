package gwan_woo_jeong.question_generator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class QuestionManager {
	public static List<Integer> getExistingQuestions() {
		File folder = new File(GlobalValues.fileDirPath);
		File[] listOfFiles = folder.listFiles();
		List<Integer> existingNumbers = new ArrayList<>();

		if (listOfFiles != null) {
			for (File file : listOfFiles) {
				String fileName = file.getName();

				String filteredName = "";
				for (int i = 0; i < fileName.length(); i++) {
					char c = fileName.charAt(i);
					if (c >= '0' && c <= '9') {
						filteredName += c;
					}
				}

				if(!filteredName.isEmpty()) {
					int number = Integer.parseInt(filteredName);
					existingNumbers.add(number);
					filteredName = "";
				}
			}
		}

		return existingNumbers;
	}

	public static HashMap<Integer, String> getTargetQuestions(List<Integer> existingNumbers, HashMap<Integer, String> newQuestions) {
		HashMap<Integer, String> targetQuestions = new HashMap<>();

		Set<Integer> newQuestionNumbers = newQuestions.keySet();

		for (Integer newQuestionNumber : newQuestionNumbers) {
			if (!existingNumbers.contains(newQuestionNumber)) {
				targetQuestions.put(newQuestionNumber, newQuestions.get(newQuestionNumber));
			}
		}

		return targetQuestions;
	}
}
