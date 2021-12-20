package com.example.diseaseprediction.synonymous;

public class Word implements Comparable<Word>
{
	private String title;
	private String[] synonyms;
	
	public Word(String title,String[] synonyms)
	{
		this.title = title;
		this.synonyms = synonyms;
	}
	
	public String getTitle(){ return this.title; }
	public String[] getSynonyms(){ return this.synonyms; }

	@Override
	public int CompareTo(Word otherStudent) {
		if(this.title.compareTo(otherStudent.title) == 0){
            return 0;
        } else if(this.title.compareTo(otherStudent.title) < 0){
            return -1;
        } else{
            return 1;
        }
	}
	
	
}

interface Comparable<Word>{
    int CompareTo(Word otherStudent); 
}