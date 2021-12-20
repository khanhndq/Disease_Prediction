package com.example.diseaseprediction.synonymous;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Ayman
 * Helper Class
 * Static class with static functions to be used as helpers while processing some repetitive things 
 * such as parsing the contents of the text file in this program 
 */

public class Helper 
{
	/* Returns an array of strings of synonyms from comma seperated string 
	 * for example: alien:foreigner,stranger.
	 * */
	public static String[] getSynonyms(String input)
	{
		String synString = input.substring(input.indexOf(':')+1,input.indexOf('.'));
		
		String[] synArr = synString.split(",");
		
		/* Make them all lower case too to prevent problems in comparison later on */
		for(int i = 0; i < synArr.length; i++)
			synArr[i].toLowerCase();
		
		return synArr;
	}
	
	/* Returns a String of the line title from a string
	 * for example: alien:foreigner,stranger.
	 * */
	public static String getTitle(String input)
	{
		return input.substring(0,input.indexOf(':')).toLowerCase();
	}
	
	/* Reads the dictionary text file line by line, creates Word objects and fill 
	 * in an array of certain size matching number of Words needed in dictionary */
	public static Word[] getDictionary(String filename,int size)
	{
		Word[] dicArray = new Word[size];
		try
		{
			FileInputStream fstream = new FileInputStream(filename);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine = "";
			int count = 0;
			while (strLine != null)
			{
			  strLine = br.readLine();
			  if(count < dicArray.length)
				  dicArray[count] = new Word(Helper.getTitle(strLine),Helper.getSynonyms(strLine));
			  count++;
			}

			in.close();

		}catch(FileNotFoundException e){}
		catch(IOException ex){}
		catch(NullPointerException exx){}

		return dicArray;
	}

	public static Word[] getDictionary1(InputStream fstream, int size)
	{
		Word[] dicArray = new Word[size];
		try
		{
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine = "";
			int count = 0;
			while (strLine != null)
			{
				strLine = br.readLine();
				if(count < dicArray.length)
					dicArray[count] = new Word(Helper.getTitle(strLine),Helper.getSynonyms(strLine));
				count++;
			}

			in.close();

		}catch(FileNotFoundException e){}
		catch(IOException ex){}
		catch(NullPointerException exx){}

		return dicArray;
	}
	
	/* Converts an array of strings to a single string seperated by a chosen delimiter */
	public static String arrayToString(String[] arr, String delimiter)
	{
		String str = arr[0];
		
		for(int i = 1; i< arr.length;i++)
			str+=delimiter+arr[i];
		
		return str;
	}
	
	/* Reads a text file into a string and returns it */
	public static String textFileToString(File textFile)
	{
		String strLine = "";
		String text = "";
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(textFile));
			while(strLine != null)
			{
				strLine = br.readLine();
				text += strLine+" ";
			}
		}catch(FileNotFoundException e){}
		catch(IOException ex){}
		catch(NullPointerException exx){}
		
		return text;
	}
	
	/* Removes non alphabet characters from a string such as punctuation and marks*/
	public static String filterString(String str)
	{
		str = str.replaceAll("(\\w+)\\p{Punct}(\\s|$)", "$1$2");
		
		return str;
	}
}