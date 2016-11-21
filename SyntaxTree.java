import java.util.*;

public class SyntaxTree
{
	private static SyntaxTreeNode root = null;
	private static LinkedList<SyntaxTreeNode> traversalList = null;
	
	public SyntaxTreeNode.Interior makeInterior(String op)
	{
		SyntaxTreeNode.Interior node = new SyntaxTreeNode.Interior(op);
		
		if (root == null)
			root = node;
		
		return node;
	}
	
	public SyntaxTreeNode.Interior makeInterior(String op, SyntaxTreeNode... children)
	{
		
		SyntaxTreeNode.Interior node = new SyntaxTreeNode.Interior(op, children);
		
		if (root == null)
			root = node;
		
		//System.out.println(node);
		return node;
	}
	
	public <ValueType> SyntaxTreeNode.Leaf makeLeaf(String op, ValueType lexValue)
	{
		SyntaxTreeNode.Leaf node = new SyntaxTreeNode.Leaf(op, lexValue);
		//System.out.println(node);
		return node;
	}
	
	// Builds the program stack with a post-order traversal of the syntax tree.
	private void buildListPostorder(SyntaxTreeNode startPoint)
	{
		if (startPoint == null) return;
		
		if (startPoint instanceof SyntaxTreeNode.Interior)
		{
			for (int i = 0; i < ((SyntaxTreeNode.Interior) startPoint).numChildren(); i++)
			{
				buildListPostorder(((SyntaxTreeNode.Interior) startPoint).getChild(i));
			}
		}
		
		traversalList.push(startPoint);
	}
	
	// Returns a list containing the intermediate program representation in bottom-up order.
	public LinkedList<SyntaxTreeNode> getTraversalList()
	{
		if (traversalList == null)
		{
			traversalList = new LinkedList<SyntaxTreeNode>();
			buildListPostorder(root);
			return (LinkedList<SyntaxTreeNode>)traversalList.clone();
		}
		else
		{
			return (LinkedList<SyntaxTreeNode>)traversalList.clone();
		}
	}
}