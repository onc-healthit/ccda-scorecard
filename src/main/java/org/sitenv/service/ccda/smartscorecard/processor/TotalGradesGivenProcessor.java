package org.sitenv.service.ccda.smartscorecard.processor;

import java.util.Arrays;
import java.util.List;

import org.sitenv.service.ccda.smartscorecard.model.TotalGradesGiven;
import org.sitenv.service.ccda.smartscorecard.repositories.postgres.StatisticsRepository;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TotalGradesGivenProcessor {
	
	@Autowired
	StatisticsRepository statisticsRepository;
	
	public TotalGradesGiven calculateTotalGradesGiven() {
		TotalGradesGiven grades = new TotalGradesGiven();
		convertScoresToGradesAndSetCounts(getScores(), grades);
		return grades;
	}	
	
	private List<Integer> getScores() {
		return statisticsRepository.getAllDocScores();
	}
	
	private static void convertScoresToGradesAndSetCounts(List<Integer> scores, TotalGradesGiven grades) {		
		int aPlusCount = 0;
		int aMinusCount = 0;
		int bPlusCount = 0;
		int bMinusCount = 0;
		int cCount = 0;
		int dCount = 0;
		
		if (scores != null && !scores.isEmpty()) {
			for (Integer score : scores ) {
				if (score != null) {
					
					String grade = ApplicationUtil.calculateGradeFromScore(score);
					
					switch (grade.toUpperCase()) {
					case "A+":
						aPlusCount++;
						break;
					case "A-":
						aMinusCount++;
						break;
					case "B+":
						bPlusCount++;
						break;
					case "B-":
						bMinusCount++;
						break;						
					case "C":
						cCount++;
						break;
					case "D":
						dCount++;
					}
				}
			}
		}
		
		grades.setAPlusGrades(aPlusCount);
		grades.setAMinusGrades(aMinusCount);
		grades.setBPlusGrades(bPlusCount);
		grades.setBMinusGrades(bMinusCount);
		grades.setCGrades(cCount);
		grades.setDGrades(dCount);		
	}	

}
