package ast;

import java.util.LinkedList;
import java.util.List;

import visitor.TypeVisitor;
import visitor.Visitor;

public class StatementList extends Statement {
   private List<Statement> list;

   public StatementList() {
      list = new LinkedList<Statement>();
   }

   public void addElement(Statement n) {
      list.add(n);
   }

   public Statement elementAt(int i)  { 
      return list.get(i); 
   }

   public int size() { 
      return list.size(); 
   }

   public void accept(Visitor v) {
	    v.visit(this);
	  }

	  public Type accept(TypeVisitor v) {
	    return v.visit(this);
	  }

	public List<Statement> getList() {
		return list;
	}

	public void setList(List<Statement> list) {
		this.list = list;
	}
}
