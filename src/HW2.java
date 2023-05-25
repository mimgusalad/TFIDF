import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

class CompareSimilarity implements Comparator<Entry<String, Double>>{

	@Override
	public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
		// TODO Auto-generated method stub
		return o2.getValue().compareTo(o1.getValue());
	}
	
}

class CompareHashCode implements Comparator<Entry<Integer, Double>>{

	@Override
	public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
		// TODO Auto-generated method stub
		return o1.getKey().compareTo(o2.getKey());
	}
}

class Similarity implements Comparable<Similarity>{
	String title;
	double similarity;
	
	public Similarity(String title, double similarity) {
		this.title = title;
		this.similarity = similarity;
	}
	
	
	@Override
	public int compareTo(Similarity o) {
		return Double.compare(o.similarity, this.similarity);
	}

}

public class HW2 {
	public static double calcTF(double termFreqOfDoc, double sumOfTermsFreq) {
		return termFreqOfDoc/sumOfTermsFreq;
	}
	public static double calcIDF(double totalNumOfDocs, double numOfAppearedDocs) {
		return Math.log(totalNumOfDocs/numOfAppearedDocs);
	}
	
	public static int calcDF(Integer term, HashMap<String, HashMap<Integer, Double>> map, HashMap<Integer, Integer> termDocMap) {
		int numOfAppearedDocs=0;
		if(termDocMap.containsKey(term)) return termDocMap.get(term);
		else {
			for(Entry<String, HashMap<Integer, Double>> doc : map.entrySet()) {
				if(doc.getValue().containsKey(term)) numOfAppearedDocs++;
			}
			termDocMap.put(term, numOfAppearedDocs);
			return numOfAppearedDocs;
		}
	}
	
	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(new File("stopwords.txt"));
        HashSet<String> stopwords = new HashSet<>();

        while(sc.hasNext()) {
            String word = sc.next();
            stopwords.add(word);
        }
        
        sc.close();
        
        System.out.print("파일 이름, k, 문서 제목: ");
        Scanner sc2 = new Scanner(System.in);
        String filename = sc2.next();
        int k = sc2.nextInt();
        String target = sc2.nextLine();
        target = target.trim();
        
        sc2.close();
        long beforeTime = System.currentTimeMillis();
        
        
        Scanner file = new Scanner(new File(filename));
        HashMap<String, HashMap<Integer, Double>> map = new HashMap<>();

        String delimeter = "[,.?!:\"\\s]+";
        int numOfTerms=0;
        while(file.hasNextLine()) {
        	String title = file.nextLine();
        	String[] content = file.nextLine().toLowerCase().split(delimeter);
        	int sumOfFreqs=0;

        	HashMap<Integer, Double> termFreq = new HashMap<>(content.length);
        	for(String term:content) {
        		if(!stopwords.contains(term)) {
        			termFreq.put(term.hashCode(), termFreq.getOrDefault(term.hashCode(), (double) 0)+1);
        			sumOfFreqs++;
        			numOfTerms++;
        		}
        	}
        	map.put(title, termFreq);
        	for(Map.Entry<Integer, Double> entry : map.get(title).entrySet()) {
        		Double tf = calcTF(entry.getValue(), sumOfFreqs);
        		map.get(title).put(entry.getKey(), tf);
        	}
        }              
        
        long time2 = System.currentTimeMillis(); 
        long secDiffTime = (time2 - beforeTime)/1000;
        System.out.println("\n읽는데 걸리는 시간 : "+secDiffTime);
        
        System.out.println("제목 제외 단어 수 = "+numOfTerms);
        HashMap<Integer, Integer> termDocMap = new HashMap<>(numOfTerms);
        for(Entry<String, HashMap<Integer, Double>> entry : map.entrySet()) {
        	int numOfAppearedDocs=0;
        	for(Integer term : entry.getValue().keySet()) {
        		numOfAppearedDocs = calcDF(term, map, termDocMap);
        		termDocMap.put(term, numOfAppearedDocs);
        		double idf = calcIDF(map.size(), numOfAppearedDocs);
        		entry.getValue().put(term, idf*entry.getValue().get(term));
        	}
        }
        
        long time3 = System.currentTimeMillis(); 
        long tim33 = (time3 - time2)/1000;
        System.out.println("\n문서 몇개에 나오는지 확인하는 시간 : "+tim33);
        
        List<Entry<Integer, Double>> sortedVector = new ArrayList<>(map.get(target).entrySet());
        Collections.sort(sortedVector, new CompareHashCode());
        System.out.println("결과 1. \""+target+"\"의 TF-IDF 벡터");
        System.out.print("[ ");
        for(Entry<Integer, Double> entry:sortedVector) {
        	System.out.printf("(%d, %.3f) ", entry.getKey(), entry.getValue());
        }
        System.out.println("]\n");
        
        HashMap<String, Double> similarityMap = new HashMap<>(map.size()-1);
        double calcTarget = 0;
      
        for(Entry<Integer, Double> targetEntry : map.get(target).entrySet()) {
        	calcTarget += Math.pow(targetEntry.getValue(), 2);
        }
        
        for(Entry<String, HashMap<Integer, Double>> sampleEntry : map.entrySet()) {
        	Set<Integer> targetEntry = map.get(target).keySet();
        	if(sampleEntry.getKey().equals(target)) continue;
        	else {
        		double calcSample=0;
        		double common=0;
        		for(Entry<Integer, Double> entry : sampleEntry.getValue().entrySet()) {
        			calcSample += Math.pow(entry.getValue(), 2);
        			if(targetEntry.contains(entry.getKey())) common+= entry.getValue()*map.get(target).get(entry.getKey());
        			else{
        				continue;
        			}
        		}
        		double similarity = common / (Math.sqrt(calcTarget)*Math.sqrt(calcSample));
        		if (Double.isFinite(similarity)) {
        			similarityMap.put(sampleEntry.getKey(), similarity);
        		}
        	}
        }
        
        long time4 = System.currentTimeMillis(); 
        long time44 = (time2 - beforeTime)/1000;
        System.out.println("\n유사도 구하는데 걸리는 시간 : "+time44);
        
        
        List<Entry<String, Double>> valueList = new ArrayList<>(similarityMap.entrySet());
        Collections.sort(valueList, new CompareSimilarity());        
        System.out.printf("결과 2. \"%s\"과(와) 유사한 %d개의 문서\n", target, k);
        for(int i=0; i<k;i++) {
        	System.out.printf("%d. %s (유사도=%.5f)\n", i+1, valueList.get(i).getKey(), valueList.get(i).getValue() );
        }
        
        long afterTime = System.currentTimeMillis(); 
        long time5 = (afterTime - time4)/1000;
        System.out.println("\n시간차이(m) : "+time5);

        file.close();
	}
}
