package ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import gramatica.miniJavaParser.*;

public class ASTBuilder {
	
	public ASTBuilder(){
		
	}
	
	public Program visitGoal(GoalContext gc){
		MainClass main = this.visitMain(gc.mainClass());
		ClassDeclList classList = this.visitClassDeclList(gc.classDeclaration());
		Program prog = new Program(main,classList);
		return prog;
	}

	private ClassDeclList visitClassDeclList(List<ClassDeclarationContext> cd) {
		ClassDeclList cdList = new ClassDeclList();
		for(int i = 0; i<cd.size(); i++){
			cdList.addElement(this.visitClassDecl(cd.get(i)));
		}
		return cdList;
	}

	private ClassDecl visitClassDecl(ClassDeclarationContext cdc) {
		List<TerminalNode> tokens = cdc.IDENTIFIER();
		ClassDecl cds;
		if(tokens.size() < 2){
			cds = new ClassDeclSimple(new Identifier(tokens.get(0).getText()),this.visitVarDeclList(cdc.varDeclaration()), this.visitMethodDeclList(cdc.methodDeclaration()));
		}else{
		    cds = new ClassDeclExtends(new Identifier(tokens.get(0).getText()),new Identifier(tokens.get(3).getText()),this.visitVarDeclList(cdc.varDeclaration()), this.visitMethodDeclList(cdc.methodDeclaration()));
		}
		return cds;
	}

	private MethodDeclList visitMethodDeclList(List<MethodDeclarationContext> md) {
		MethodDeclList mdList = new MethodDeclList();
		for(int i = 0; i<md.size(); i++){
			mdList.addElement(this.visitMethodDecl(md.get(i)));
		}
		return mdList;
	}

	private MethodDecl visitMethodDecl(MethodDeclarationContext md) {
		List<TypeContext> tipos = md.type();
		List<TerminalNode> tokens = md.IDENTIFIER();
		MethodDecl newMd = null;
		FormalList args = new FormalList();
		Type tipoMetodo = this.visitType(tipos.get(0));
		Identifier nomeMetodo = new Identifier(tokens.get(0).getText());
		for(int i = 1; i< tipos.size();i++){
			args.addElement(new Formal(this.visitType(tipos.get(i)),new Identifier(tokens.get(i).getText())));
		}
		VarDeclList variaveis = this.visitVarDeclList(md.varDeclaration());
		StatementList statements = this.visitStatementList(md.statement());
		Exp exp = this.visitExp(md.expression());
		newMd = new MethodDecl(tipoMetodo,nomeMetodo,args,variaveis,statements,exp);
		return newMd;
	}

	private Exp visitExp(ExpressionContext expression) {
	
		TerminalNode operandos = expression.OPERAND();
		TerminalNode ids = expression.IDENTIFIER();
		TerminalNode numero = expression.INTEGER_LITERAL();
		List<ExpressionContext> expr = expression.expression();
		String text = expression.getText();
		
		if(operandos != null){
			Exp exp1 = this.visitExp(expr.get(0));
			Exp exp2 = this.visitExp(expr.get(1));
			switch(operandos.getText()){
			case "&&": return new And(exp1,exp2);
			case "<" : return new LessThan(exp1,exp2);
			case "+" : return new Plus(exp1,exp2);
			case "-" : return new Minus(exp1,exp2);
			case "*" : return new Times(exp1,exp2);
			}
		}else if(expr.size()==2 && text.contains("[") && text.contains("]")){
			return new ArrayLookup(this.visitExp(expr.get(0)),this.visitExp(expr.get(1)));
		}else if(expr.size() == 1 && !text.contains("new") && text.contains("length")){
			return new ArrayLength(this.visitExp(expr.get(0)));
		}else if(expr.size() >= 1 && ids != null){
			List<ExpressionContext> expr2 = new ArrayList<ExpressionContext>();
			for(int i =1; i< expr.size();i++){
				expr2.add(expr.get(i));
			}
			return new Call(this.visitExp(expr.get(0)),new Identifier(ids.getText()), this.visitExpList(expr2));
		}else if(numero != null){
			return new IntegerLiteral(Integer.parseInt(numero.getText()));
		}else if(ids != null && !text.contains("new")){
			return new IdentifierExp(ids.getText());
		}else if(text.contains("true")){
			return new True();
		}else if(text.contains("false")){
			return new False();
		}else if(text.equals("this")){
			return new This();
		}else if(text.contains("new")){
			if(expr.size() == 1){
				return new NewArray(this.visitExp(expr.get(0)));
			}else{
				return new NewObject(new Identifier(ids.getText()));
			}
		}else if(text.contains("!")){
			return new Not(this.visitExp(expr.get(0)));
		}
		return this.visitExp(expr.get(0));	
	}

	private ExpList visitExpList(List<ExpressionContext> expressionContext) {
		ExpList expL = new ExpList();
		List<ExpressionContext> exprs = expressionContext;
		for(int i = 0; i<exprs.size();i++){
			expL.addElement(this.visitExp(exprs.get(i)));
		}
		return expL;
	}

	private StatementList visitStatementList(List<StatementContext> statement) {
		StatementList stList = new StatementList();
		for(int i = 0; i <statement.size();i++){
			stList.addElement(this.visitStatement(statement.get(i)));
		}
		return stList;
	}

	private Statement visitStatement(StatementContext statementContext) {
		Statement returnStmt = null;
		List<StatementContext> sts = statementContext.statement();
		TerminalNode tokens = statementContext.IDENTIFIER();
		List<ExpressionContext> expr = statementContext.expression();
		if(sts.size() == 2 && expr.size()==1 ){
			returnStmt = new If(this.visitExp(expr.get(0)),this.visitStatement(sts.get(0)),this.visitStatement(sts.get(1)));
		}else if(expr.size() == 1 && sts.size()==1){
			returnStmt = new While(this.visitExp(expr.get(0)),this.visitStatement(sts.get(0)));
		}else if(tokens != null && expr.size() == 2){
			returnStmt = new ArrayAssign(new Identifier(tokens.getText()), this.visitExp(expr.get(0)),this.visitExp(expr.get(1)));
		}else if(tokens != null && expr.size()==1){
			returnStmt = new Assign(new Identifier(tokens.getText()), this.visitExp(expr.get(0)));
		}else if(expr.size() == 1){
			returnStmt = new Print(this.visitExp(expr.get(0)));
		}else {
			returnStmt = new Block(this.visitStatementList(sts));
		}
		return returnStmt;
	}
	


	private Type visitType(TypeContext typeContext) {
		Type retornoType = null;
		TerminalNode id = typeContext.IDENTIFIER();
		if(id != null){
			retornoType = new IdentifierType(id.getText());
		}
		String retorno = typeContext.getText();
		if(retorno.equals("boolean")){
			retornoType = new BooleanType();
		}else if(retorno.contains("int") && retorno.contains("[") && retorno.contains("]")){
			retornoType = new IntArrayType();
		}else if(retorno.equals("int")){
			retornoType = new IntegerType();
		} 
		return retornoType;
	}

	private VarDeclList visitVarDeclList(List<VarDeclarationContext> varDeclaration) {
		VarDeclList retorno = new VarDeclList();
		for(int i = 0; i < varDeclaration.size();i++){
			retorno.addElement(this.visitVarDecl(varDeclaration.get(i)));
		}
		return retorno;
	}

	private VarDecl visitVarDecl(VarDeclarationContext vdc) {
		VarDecl retorno = null;
		Type tipo = this.visitType(vdc.type());
		Identifier id = new Identifier(vdc.IDENTIFIER().getText());
		retorno = new VarDecl(tipo, id);
		return retorno;
	}

	private MainClass visitMain(MainClassContext mainClass) {
		List<TerminalNode> id = mainClass.IDENTIFIER();
		StatementContext stmt = mainClass.statement();
		return new MainClass(new Identifier(id.get(0).getText()), new Identifier(id.get(1).getText()), this.visitStatement(stmt));
	}
}
