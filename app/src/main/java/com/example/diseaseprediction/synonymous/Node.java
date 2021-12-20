package com.example.diseaseprediction.synonymous;

/**
 * @author Ayman
 * Node
 * Single element in the tree storing a Word object that contains the data
 */
public class Node 
{
	private Node right;
	private Node left;
	private Word word;
	
	public Node(Word word)
	{
		this.word = word;
		this.right = null;
		this.left = null;
	}
	
	/* Setters */
	public void setRight(Node right){this.right = right;}
	public void setLeft(Node left){this.left = left;}
	public void setWord(Word word){this.word = word;}
	
	/* Getters */
	public Node getLeft(){return this.left;}
	public Node getRight(){return this.right;}
	public Word getWord(){return this.word;}
}