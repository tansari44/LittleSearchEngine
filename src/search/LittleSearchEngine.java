package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 */
class Occurrence {
        /**
         * Document in which a keyword occurs.
         */
        String document;
        
        /**
         * The frequency (number of times) the keyword occurs in the above document.
         */
        int frequency;
        
        /**
         * Initializes this occurrence with the given document,frequency pair.
         * 
         * @param doc Document name
         * @param freq Frequency
         */
        public Occurrence(String doc, int freq) {
                document = doc;
                frequency = freq;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
                return "(" + document + "," + frequency + ")";
        }
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
        
        /**
         * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
         * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
         * order of occurrence frequencies.
         */
        HashMap<String,ArrayList<Occurrence>> keywordsIndex;
        
        /**
         * The hash table of all noise words - mapping is from word to itself.
         */
        HashMap<String,String> noiseWords;
        
        /**
         * Creates the keyWordsIndex and noiseWords hash tables.
         */ 
        public LittleSearchEngine() {
                keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
                noiseWords = new HashMap<String,String>(100,2.0f);
        }
        
        /**
         * This method indexes all keywords found in all the input documents. When this
         * method is done, the keywordsIndex hash table will be filled with all keywords,
         * each of which is associated with an array list of Occurrence objects, arranged
         * in decreasing frequencies of occurrence.
         * 
         * @param docsFile Name of file that has a list of all the document file names, one name per line
         * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
         * @throws FileNotFoundException If there is a problem locating any of the input files on disk
         */
        public void makeIndex(String docsFile, String noiseWordsFile) 
        throws FileNotFoundException {
                
                boolean legitimate = false;
                
                while(!legitimate)
                {
                        try
                        {
                                Scanner sc = new Scanner(new File(noiseWordsFile));
                                legitimate = true;
                        }
                        catch(FileNotFoundException e)
                        {
                                return;
                        }
                }
                
                Scanner sc = new Scanner(new File(noiseWordsFile));
                
                while (sc.hasNext()) {
                        String word = sc.next();
                        noiseWords.put(word,word);
                }
                
                legitimate = false;
                
                while(!legitimate)
                {
                        try
                        {
                                sc = new Scanner(new File(docsFile));
                                legitimate = true;
                        }
                        catch(FileNotFoundException e)
                        {
                                return;
                        }
                }
                
                sc = new Scanner(new File(docsFile));
                while (sc.hasNext()) {
                        String docFile = sc.next();
                        HashMap<String,Occurrence> kws = loadKeyWords(docFile);
                        mergeKeyWords(kws);
                }
                
        }

        /**
         * Scans a document, and loads all keywords found into a hash table of keyword occurrences
         * in the document. Uses the getKeyWord method to separate keywords from other words.
         * 
         * @param docFile Name of the document file to be scanned and loaded
         * @return Hash table of keywords in the given document, each associated with an Occurrence object
         * @throws FileNotFoundException If the document file is not found on disk
         */
        public HashMap<String,Occurrence> loadKeyWords(String docFile) 
        throws FileNotFoundException {

                HashMap<String,Occurrence> keywords = new HashMap<String,Occurrence>();
                
                boolean legitimate = false;
                
                while(!legitimate)
                {
                        try
                        {
                                Scanner phrases = new Scanner(new File(docFile));
                                legitimate = true;
                        }
                        catch(FileNotFoundException e)
                        {
                                return keywords;
                        }
                }
                
                Scanner phrases = new Scanner(new File(docFile));
                int freq = 1;
                
                        while (phrases.hasNext())
                        {
                                String word = phrases.next();

                                if(getKeyWord(word) != null)
                                {        
                                        word = getKeyWord(word);
                                        if(!keywords.containsKey(word))
                                        {
                                                Occurrence occur = new Occurrence(docFile,freq);
                                                keywords.put(word, occur);
                                        }
                                        else
                                        {
                                                keywords.get(word).frequency++;
                                        }
                                }
                        }
                        
                return keywords;
        }
        
        /**
         * Merges the keywords for a single document into the master keywordsIndex
         * hash table. For each keyword, its Occurrence in the current document
         * must be inserted in the correct place (according to descending order of
         * frequency) in the same keyword's Occurrence list in the master hash table. 
         * This is done by calling the insertLastOccurrence method.
         * 
         * @param kws Keywords hash table for a document
         */
        public void mergeKeyWords(HashMap<String,Occurrence> kws) {

                ArrayList<Occurrence> list = new ArrayList<Occurrence>();
                
                for(String key: kws.keySet())
                {        
                        Occurrence occurrence = kws.get(key);
                        
                        if(!keywordsIndex.containsKey(key))
                        {
                                ArrayList<Occurrence> occurList = new ArrayList<Occurrence>();                                
                                occurList.add(occurrence);
                                keywordsIndex.put(key, occurList);
                        }
                        else
                        {
                                list = keywordsIndex.get(key);
                                list.add(occurrence);
                                insertLastOccurrence(list);
                                keywordsIndex.put(key, list);
                        }        
                }
        }
        
        /**
         * Given a word, returns it as a keyword if it passes the keyword test,
         * otherwise returns null. A keyword is any word that, after being stripped of any
         * trailing punctuation, consists only of alphabetic letters, and is not
         * a noise word. All words are treated in a case-INsensitive manner.
         * 
         * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
         * 
         * @param word Candidate word
         * @return Keyword (word without trailing punctuation, LOWER CASE)
         */
        public String getKeyWord(String word) {

                word = word.trim();
                char end = word.charAt(word.length()-1);
                
                while(end == '.' || end == ',' || end == '?' || end == ':' || end == ';' || end == '!')
                {
                        word = word.substring(0, word.length()-1);
                        
                        if(word.length() > 1)
                        {
                                end = word.charAt(word.length()-1);
                        }
                        else
                        {
                                break;
                        }
                }
                
                word = word.toLowerCase();

                for(String noiseWord: noiseWords.keySet())
                {
                        if(word.equalsIgnoreCase(noiseWord))
                        {
                                return null;
                        }
                }
                
                for(int j = 0; j < word.length(); j++)
                {
                        if(!Character.isLetter(word.charAt(j)))
                        {
                                return null;
                        }
                }
                return word;
        }
        
        /**
         * Inserts the last occurrence in the parameter list in the correct position in the
         * same list, based on ordering occurrences on descending frequencies. The elements
         * 0..n-2 in the list are already in the correct order. Insertion is done by
         * first finding the correct spot using binary search, then inserting at that spot.
         * 
         * @param occurences List of Occurrences
         * @return Sequence of mid point indexes in the input list checked by the binary search process,
         *         null if the size of the input list is 1. This returned array list is only used to test
         *         your code - it is not used elsewhere in the program.
         */
        public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occurences) {
                
                if(occurences.size() == 1)
                {
                        return null;
                }
                
                int last = occurences.get(occurences.size()-1).frequency;
                Occurrence temp = occurences.get(occurences.size() -1);
                int lower = 0;
                int upper = occurences.size()-1;
                int middle;
                ArrayList<Integer> middleIndex = new ArrayList<Integer>();
                
        while( lower <= upper )
        {
            middle = ( lower + upper ) / 2;
            middleIndex.add(middle);

            if( last > occurences.get(middle).frequency )
            {                    
                upper = middle-1;
            }
            else if(last < occurences.get(middle).frequency)
            {
                    lower = middle+1;
            }
            else
            {
                    break;
            }
        }
        
        if(middleIndex.get(middleIndex.size()-1) == 0)
        {
                if(temp.frequency < occurences.get(0).frequency)
                {
                        occurences.add(1, temp);
                        occurences.remove(occurences.size()-1);
                        
                        return middleIndex;
                }
        }
        
        occurences.add(middleIndex.get(middleIndex.size()-1), temp);
        occurences.remove(occurences.size()-1);
        return middleIndex;
        
        }
        
        /**
         * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
         * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
         * matching document will only appear once in the result.) Ties in frequency values are broken
         * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
         * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
         * The result set is limited to 5 entries. If there are no matching documents, the result is null.
         * 
         * @param keyword1 First keyword
         * @param keyword1 Second keyword
         * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
         *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
         *         the result is null.
         */
        public ArrayList<String> top5search(String keyword1, String keyword2) {

                ArrayList<String> resulting_list = new ArrayList<String>();
                ArrayList<Occurrence> list_one = new ArrayList<Occurrence>();
                
                keyword1 = keyword1.toLowerCase();
                
                if(keywordsIndex.get(keyword1) != null)
                {
                        list_one = keywordsIndex.get(keyword1);
                }
                
                ArrayList<Occurrence> list_two = new ArrayList<Occurrence>();
                
                keyword2 = keyword2.toLowerCase();
                
                if(keywordsIndex.get(keyword2) != null)
                {
                        list_two = keywordsIndex.get(keyword2);
                }

                for(int i = 0; i < list_one.size(); i++)
                {
                        if(resulting_list.size() <= 4)
                        {
                                int listOne = list_one.get(i).frequency;
                                String docNameOne = list_one.get(i).document;
                                
                                for(int j = 0; j < list_two.size(); j++)
                                {
                                        String docNameTwo = list_two.get(j).document;
                                        int listTwo = list_two.get(j).frequency;
                                        
                                        if(listTwo <= listOne)
                                        {
                                                if(!resulting_list.contains(docNameOne) && resulting_list.size() <= 4)
                                                {
                                                        resulting_list.add(docNameOne);
                                                }
                                        }
                                        else if(listTwo > listOne)
                                        {
                                                if(!resulting_list.contains(docNameTwo) && resulting_list.size() <= 4)
                                                {
                                                        resulting_list.add(docNameTwo);
                                                }
                                        }
                                }
                        }
                }
                
                for(int m = 0; m < resulting_list.size(); m++)
                {
                        if(m + 1 == resulting_list.size())
                        {
                                System.out.print(resulting_list.get(m));
                        }
                        else
                        {
                                System.out.print(resulting_list.get(m) + ", ");
                        }
                } 
                
                if(resulting_list.size() == 0)
                {
                        return null;
                } 
                
                return resulting_list;
        }
}