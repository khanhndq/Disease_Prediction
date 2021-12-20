package com.example.diseaseprediction.synonymous;

/**
 * @author Ayman
 * Tree
 * Object that will store the dictionary or words
 */
public class Tree 
{
	private Node root;
	
	public Tree(Node root)
	{
		this.root = root;
	}
	
	public void setRoot(Node root){this.root = root;}
	public Node getRoot(){return this.root;}
	
	/* Inserts a new Node with new word value into the tree and keep track of parent */
	public Node insertWord(Node root, Word word)
	{
		if(root == null)
			root = new Node(word);
		
		else if(word.getTitle().compareTo(root.getWord().getTitle()) < 0)
			
			root.setLeft(insertWord(root.getLeft(),word));
		
		else if(word.getTitle().compareTo(root.getWord().getTitle()) > 0)
			
			root.setRight(insertWord(root.getRight(),word));
		
		else
			System.out.println("Duplicate values, nothing done.");
		
		return root;
	}
	
	/* Creates the balanced binary tree from the sorted array of Words 
	 * in a way to keep it balanced so we divided it into sub arrays recursively starting from the middle */
	public Node SArraytoBST(Word[] words, int start, int end)
	{
		if(start > end)
			return null;
		
		else
		{
			int mid = (start+end)/2;
			Node element = new Node(words[mid]);
			element.setLeft(SArraytoBST(words, start, (mid-1)));
			element.setRight(SArraytoBST(words, (mid+1), end));
			
			return element;
		}
	}
	
	/* Searches for a word and returns the Node object storing it, if null is returned then it does not exist */
	public Node findWord(String title,Node current)
	{
		
		if(current == null || title.compareTo(current.getWord().getTitle()) == 0)
			return current;
		
		else if(title.compareTo(current.getWord().getTitle()) < 0)
			return findWord(title,current.getLeft());
		
		else
			return findWord(title,current.getRight());
	}
	
	/* Finds the nearest parent to the target node even if target exists or not */
	public Node getNearestParent(String title,Node current,Node parent)
	{
		if(current == null || (current.getRight() == null && current.getLeft() == null) || title.compareTo(current.getWord().getTitle()) == 0)
			return parent;
		
		else if(title.compareTo(current.getWord().getTitle()) < 0)
			return getNearestParent(title,current.getLeft(),current);
		
		else
			return getNearestParent(title,current.getRight(),current);
	}
	
	/* Traverses a subtree in-order and builds a string of node values near the root of the sub tree 
	 * used for finding similar words to a certain word */
	public String getSimilarNodes(Node currNode,String title)
	{
		String str = "";

			if(currNode.getLeft() != null)
				str += getSimilarNodes(currNode.getLeft(),title);
			
			/* make sure that the word is not repeated as we traverse */
			if(title.compareTo(currNode.getWord().getTitle()) != 0)
				str += currNode.getWord().getTitle()+", ";
			
			if(currNode.getRight() !=null)
			str += getSimilarNodes(currNode.getRight(),title);

		/* Remove the extra comma at end and return */
		return str;
	}
}