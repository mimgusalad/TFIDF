import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;

class CompareHashcode implements Comparator<Integer>{
	@Override
	public int compare(Integer o1, Integer o2) {
		return o1.compareTo(o2);
	}
	
}
public class HW2 {
	public static double calcTF(double termFreqOfDoc, double sumOfTermsFreq) {
		return termFreqOfDoc/sumOfTermsFreq;
	}
	public static double calcIDF(double totalNumOfDocs, double numOfAppearedDocs) {
		return Math.log(totalNumOfDocs/numOfAppearedDocs);
	}
	

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(new File("stopwords.txt"));
        HashSet<String> stopwords = new HashSet<>();

        while(sc.hasNext()) {
            String word = sc.next();
            stopwords.add(word);
        }
        
        sc.close();
        
        System.out.println("파일 이름: ");
        Scanner sc2 = new Scanner(System.in);
        String filename = sc2.next();
        int k = sc2.nextInt();
        String target = sc2.nextLine();
        target = target.trim();
        
        sc2.close();
        
        Scanner file = new Scanner(new File(filename));
        HashMap<String, HashMap<Integer, Double>> map = new HashMap<>();

        String delimeter = "[,.?!:\"\\s]+";
        
        while(file.hasNextLine()) {
        	String title = file.nextLine();
        	String[] content = file.nextLine().toLowerCase().split(delimeter);
        	//String[] terms = content.split(delimeter);

        	HashMap<Integer, Double> termFreq = new HashMap<>(content.length);
        	for(String term:content) {
        		//System.out.println(term+" = "+term.hashCode());
        		if(!stopwords.contains(term))
        			termFreq.put(term.hashCode(), termFreq.getOrDefault(term.hashCode(), (double) 0)+1);
        	}
        	map.put(title, termFreq);
        }
        
        HashMap<Integer, Integer> idfMap = new HashMap<>();
        
        int sumOfTermsFreq=0;
        for(Entry<String, HashMap<Integer, Double>> entry : map.entrySet()) {
        	for(Integer key:entry.getValue().keySet()) {
        		if(entry.getValue().containsKey(key)) idfMap.put(key, idfMap.getOrDefault(key, 0)+1);
        		sumOfTermsFreq+=entry.getValue().get(key);
        	}
        	for(Integer key:entry.getValue().keySet()) {
        		Double tf = calcTF(entry.getValue().get(key), sumOfTermsFreq);
        		entry.getValue().put(key, tf);
        		
        	}
        	sumOfTermsFreq=0;
        }
        
        for(Entry<String, HashMap<Integer, Double>> entry : map.entrySet()) {
        	//System.out.println(entry.getKey());
        	for(Integer key:entry.getValue().keySet()) {
        		Double idf = calcIDF(map.size(), idfMap.get(key));
        		entry.getValue().put(key, entry.getValue().get(key)*idf);
        	}
        	
        	//System.out.println(entry.getValue());
        }
        System.out.println(map.get(target));
        file.close();
        
	}
        
}
