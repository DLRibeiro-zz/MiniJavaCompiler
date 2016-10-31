package visitor;

import symboltable.Method;
import symboltable.SymbolTable;
import ast.And;
import ast.ArrayAssign;
import ast.ArrayLength;
import ast.ArrayLookup;
import ast.Assign;
import ast.Block;
import ast.BooleanType;
import ast.Call;
import ast.ClassDeclExtends;
import ast.ClassDeclSimple;
import ast.False;
import ast.Formal;
import ast.Identifier;
import ast.IdentifierExp;
import ast.IdentifierType;
import ast.If;
import ast.IntArrayType;
import ast.IntegerLiteral;
import ast.IntegerType;
import ast.LessThan;
import ast.MainClass;
import ast.MethodDecl;
import ast.Minus;
import ast.NewArray;
import ast.NewObject;
import ast.Not;
import ast.Plus;
import ast.Print;
import ast.Program;
import ast.StatementList;
import ast.This;
import ast.Times;
import ast.True;
import ast.Type;
import ast.VarDecl;
import ast.While;

public class TypeCheckVisitor implements TypeVisitor {

	private SymbolTable symbolTable;
	private symboltable.Class currClass;
	private Method currMethod;
	public TypeCheckVisitor(SymbolTable st) {
		symbolTable = st;
	}

	// MainClass m;
	// ClassDeclList cl;
	public Type visit(Program n) {
		
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.elementAt(i).accept(this);
		}
		n.m.accept(this);
		return null;
	}

	// Identifier i1,i2;
	// Statement s;
	public Type visit(MainClass n) {
		currClass = symbolTable.getClass(n.i1.toString());
		n.i1.accept(this);
		n.i2.accept(this);
		n.s.accept(this);
		currClass = null;
		return null;
	}

	// Identifier i;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclSimple n) {
		currClass = symbolTable.getClass(n.i.toString());
		n.i.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		currClass = null;
		return null;
	}

	// Identifier i;
	// Identifier j;
	// VarDeclList vl;
	// MethodDeclList ml;
	public Type visit(ClassDeclExtends n) {
		currClass = symbolTable.getClass(n.i.toString());
		n.i.accept(this);
		n.j.accept(this);
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.ml.size(); i++) {
			n.ml.elementAt(i).accept(this);
		}
		return null;
	}

	// Type t;
	// Identifier i;
	public Type visit(VarDecl n) {
		n.t.accept(this);
		n.i.accept(this);
		return null;
	}

	// Type t;
	// Identifier i;
	// FormalList fl;
	// VarDeclList vl;
	// StatementList sl;
	// Exp e;
	public Type visit(MethodDecl n) {
		n.t.accept(this);
		currMethod = symbolTable.getMethod(n.i.toString(), currClass.getId());
		n.i.accept(this);
		for (int i = 0; i < n.fl.size(); i++) {
			n.fl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.vl.size(); i++) {
			n.vl.elementAt(i).accept(this);
		}
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		n.e.accept(this);
		currMethod = null;
		return null;
	}

	// Type t;
	// Identifier i;
	public Type visit(Formal n) {

		n.i.accept(this);
		return n.t.accept(this);
	}

	public Type visit(IntArrayType n) {
		return n;
	}

	public Type visit(BooleanType n) {
		return n;
	}

	public Type visit(IntegerType n) {
		return n;
	}

	// String s;
	public Type visit(IdentifierType n) {
		return symbolTable.getVarType(currMethod, currClass, n.s);
	}

	// StatementList sl;
	public Type visit(Block n) {
		for (int i = 0; i < n.sl.size(); i++) {
			n.sl.elementAt(i).accept(this);
		}
		return null;
	}

	// Exp e;
	// Statement s1,s2;
	public Type visit(If n) {

		boolean b1 = symbolTable.compareTypes(n.e.accept(this), new BooleanType());
		if(!b1) {System.err.println("Type-Checking error : BooleanType expected");System.exit(0);}
		n.s1.accept(this);
		n.s2.accept(this);
		return null;
	}

	// Exp e;
	// Statement s;
	public Type visit(While n) {
		boolean b1 =symbolTable.compareTypes(n.e.accept(this), new BooleanType());
		if(!b1){System.err.println("Type-Checking error : BooleanType expected");System.exit(0);};
		n.s.accept(this);
		return null;
	}

	// Exp e;
	public Type visit(Print n) {
		n.e.accept(this);
		return null;
	}

	// Identifier i;
	// Exp e;
	public Type visit(Assign n) {
		Type t1 = symbolTable.getVarType(currMethod, currClass, n.i.toString());
		boolean b1 = symbolTable.compareTypes(t1, n.e.accept(this));
		if(!b1) {System.err.println("Can't assign" + n.e.accept(this).toString()+ "to a " + t1.toString()+ " variable");
		System.exit(0);}
		return null;
	}

	// Identifier i;
	// Exp e1,e2;
	public Type visit(ArrayAssign n) {
		n.i.accept(this);
		n.e1.accept(this);
		boolean b1 = symbolTable.compareTypes(n.e2.accept(this),new IntegerType());
		if(!b1) {System.err.println("Type-Checking error : IntegerType expected");
		System.out.println("AA");
		System.exit(0);}
		return null;
	}

	// Exp e1,e2;
	public Type visit(And n) {
		boolean b1 = symbolTable.compareTypes(n.e1.accept(this), new BooleanType());
		if(!b1){ System.err.println("Type-Checking error : BooleanType expected");
		System.out.println("AND");
		System.exit(0);
		}boolean b2 = symbolTable.compareTypes(n.e2.accept(this), new BooleanType());
		if(!b2){ System.err.println("Type-Checking error : BooleanType expected");
		System.out.println("AND");
		System.exit(0);}
		return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(LessThan n) {
		boolean b1 =symbolTable.compareTypes(n.e1.accept(this), new IntegerType());
		if(!b1) {System.err.println("Type-Checking error : IntegerType expected");
		System.out.println("LT");
		System.exit(0);}
		boolean b2 =symbolTable.compareTypes(n.e1.accept(this), new IntegerType());
		if(!b2) {System.err.println("Type-Checking error : IntegerType expected");
		System.out.println("LT");
		System.exit(0);
		}return new BooleanType();
	}

	// Exp e1,e2;
	public Type visit(Plus n) {
		boolean b1 =symbolTable.compareTypes(n.e1.accept(this), new IntegerType());
		boolean b2 =symbolTable.compareTypes(n.e1.accept(this), new IntegerType());
		if(!b1){System.err.println("Type-Checking error : IntegerType expected");
		System.out.println("Plus");
		System.exit(0);}
		if(!b2){System.err.println("Type-Checking error : IntegerType expected");
		System.out.println("Plus");
		System.exit(0);
		}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Minus n) {
		boolean b1 =symbolTable.compareTypes(n.e1.accept(this), new IntegerType());
		boolean b2 =symbolTable.compareTypes(n.e1.accept(this), new IntegerType());
		if(!b1){ System.err.println("Type-Checking error : IntegerType expected");
		System.exit(0);
		}if(!b2) {System.err.println("Type-Checking error : IntegerType expected");
		System.exit(0);
		}return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(Times n) {
		boolean b1 =symbolTable.compareTypes(n.e1.accept(this), new IntegerType());
		boolean b2 =symbolTable.compareTypes(n.e1.accept(this), new IntegerType());
		if(!b1){ System.err.println("Type-Checking error : IntegerType expected");
		System.exit(0);}
		if(!b2){ System.err.println("Type-Checking error : IntegerType expected");
		System.exit(0);}
		return new IntegerType();
	}

	// Exp e1,e2;
	public Type visit(ArrayLookup n) {
		boolean b1 =symbolTable.compareTypes(n.e1.accept(this), new IntArrayType());
		boolean b2 =symbolTable.compareTypes(n.e2.accept(this), new IntegerType());
		if(!b1){
			System.err.print("Type-Checking error : IntArrayType expected");
			System.out.println("AL");
			System.exit(0);
		}
		if(!b2){ System.err.println("Type-Checking error : IntegerType expected");
		System.out.println("AL");
		System.exit(0);}
		return new IntegerType();
	}

	// Exp e;
	public Type visit(ArrayLength n) {
		boolean b1 = symbolTable.compareTypes(n.e.accept(this), new IntArrayType());
		if(!b1) {System.err.println("Type-Checking error : IntArrayType expected");
		System.exit(0);}
		return new IntegerType();
	}

	// Exp e;
	// Identifier i;
	// ExpList el;
	public Type visit(Call n) {
		Type t = n.e.accept(this);
		n.i.accept(this);
		boolean accepted = true;
		Method m;
		if(t == null){
			m = symbolTable.getMethod(n.i.toString(), currClass.getId()); 
		}else{
			m = symbolTable.getMethod(n.i.toString(), t.toString());
		}
		
		for (int i = 0; i < n.el.size(); i++) {
			accepted = symbolTable.compareTypes(n.el.elementAt(i).accept(this),m.getParamAt(i).type());
			if(accepted == false) System.err.println("Method " + m.getId() + " paramns mismatch error in param number :" + i);
		}
		if(t == null){
			return symbolTable.getMethodType(n.i.toString(), currClass.getId());
		}else{
			return symbolTable.getMethodType(n.i.toString(),t.toString());
		}
		
	}

	// int i;
	public Type visit(IntegerLiteral n) {
		return new IntegerType();
	}

	public Type visit(True n) {
		return new BooleanType();
	}

	public Type visit(False n) {
		return new BooleanType();
	}

	// String s;
	public Type visit(IdentifierExp n) {
		return symbolTable.getVarType(currMethod,currClass,n.s);
	}

	public Type visit(This n) {
		return new IdentifierType(currClass.getId());
	}

	// Exp e;
	public Type visit(NewArray n) {
		n.e.accept(this);
		return new IntArrayType();
	}

	// Identifier i;
	public Type visit(NewObject n) {
		return new IdentifierType(n.i.toString());
	}

	// Exp e;
	public Type visit(Not n) {
		boolean b1 = symbolTable.compareTypes(n.e.accept(this), new BooleanType());;
		if(!b1) {
			System.err.println("Type-Checking error : BooleanType expected");
			System.exit(0);
		}return new BooleanType();
	}

	// String s;
	public Type visit(Identifier n) {
		return null;
	}

	@Override
	public Type visit(StatementList statementList) {
		// TODO Auto-generated method stub
		return null;
	}
}
