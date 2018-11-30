/*
 * Authors: Haoyu Song and Dale Skrien
 * Date: Spring and Summer, 2018
 *
 * In the grammar below, the variables are enclosed in angle brackets.
 * The notation "::=" is used instead of "-->" to separate a variable from its rules.
 * The special character "|" is used to separate the rules for each variable.
 * All other symbols in the rules are terminals.
 * EMPTY indicates a rule with an empty right hand side.
 * All other terminal symbols that are in all caps correspond to keywords.
 */
package proj10AbramsDeutschDurstJones.bantam.parser;

import static proj10AbramsDeutschDurstJones.bantam.lexer.Token.Kind.*;
import proj10AbramsDeutschDurstJones.bantam.ast.*;
import proj10AbramsDeutschDurstJones.bantam.lexer.*;
import proj10AbramsDeutschDurstJones.bantam.util.*;
import proj10AbramsDeutschDurstJones.bantam.util.Error;

/**
 * This class constructs an AST from a legal Bantam Java program.  If the
 * program is illegal, then one or more error messages are displayed.
 */
public class Parser
{
    // instance variables
    private Scanner scanner;
    private Token currentToken; // the lookahead token
    private ErrorHandler errorHandler;
    private String fileName;

    // constructor
    public Parser(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }


    /**
     * parse the given file and return the root node of the AST
     * @param filename The name of the Bantam Java file to be parsed
     * @return The Program node forming the root of the AST generated by the parser
     */
    public Program parse(String filename) throws CompilationException {
        this.fileName = filename;
        this.scanner = new Scanner(filename, this.errorHandler);
        this.currentToken = this.scanner.scan();
        return parseProgram();
    }


    /*
     * <Program> ::= <Class> | <Class> <Program>
     */
    private Program parseProgram() throws CompilationException {
        int position = currentToken.position;
        ClassList classList = new ClassList(position);

        while (currentToken.kind != EOF) {
            Class_ aClass = parseClass();
            classList.addElement(aClass);
        }

        return new Program(position, classList);
    }


    /*
     * <Class> ::= CLASS <Identifier> <ExtendsClause> { <MemberList> }
     * <ExtendsClause> ::= EXTENDS <Identifier> | EMPTY
     * <MemberList> ::= EMPTY | <Member> <MemberList>
     */
    private Class_ parseClass() throws CompilationException {
        int position = currentToken.position;

        // handle CLASS
        if (currentToken.kind != CLASS) {
            handleUnexpectedToken("Missing class");
        }
        // handle name
        currentToken = scanner.scan();
        String name = parseIdentifier();
        // handle extends clause
        String parent = null;
        if (currentToken.kind == EXTENDS) {
            currentToken = scanner.scan();
            parent = parseIdentifier();
        }
        // handle member list
        MemberList memberList = new MemberList(position);
        if (currentToken.kind != LCURLY) {
            handleUnexpectedToken("Missing {");
        }
        currentToken = scanner.scan();
        while (currentToken.kind != RCURLY) {
            Member aMember = parseMember();
            memberList.addElement(aMember);
        }

        return new Class_(position, fileName, name, parent, memberList);
    }


    /* Fields and Methods
     * <Member> ::= <Field> | <Method>
     * <Method> ::= <Type> <Identifier> ( <Parameters> ) <Block>
     * <Field> ::= <Type> <Identifier> <InitialValue> ;
     * <InitialValue> ::= EMPTY | = <Expression>
     */
    private Member parseMember() throws CompilationException {
        int position = currentToken.position;

        String type = parseType();
        String name = parseIdentifier();

        // handle method
        if (currentToken.kind == LPAREN) {
            currentToken = scanner.scan();
            FormalList formalList = parseParameters();
            if (currentToken.kind != RPAREN) {
                handleUnexpectedToken("Missing )");
            }
            currentToken = scanner.scan();
            StmtList stmtList = ((BlockStmt) parseBlock()).getStmtList();
            return new Method(position, type, name, formalList, stmtList);
        }

        // handle field
        Expr init = null;
        if (currentToken.kind == ASSIGN)) {
            currentToken = scanner.scan();
            init = parseExpression();
            currentToken = scanner.scan();
            return new Field(position, type, name, init);
        }
        if (currentToken.kind != SEMICOLON)) {
            handleUnexpectedToken("Missing ;");
        }
        currentToken = scanner.scan();

        return new Field(position, type, name, init);
    }


    //-----------------------------------

    /* Statements
     *  <Stmt> ::= <WhileStmt> | <ReturnStmt> | <BreakStmt> | <DeclStmt>
     *              | <ExpressionStmt> | <ForStmt> | <BlockStmt> | <IfStmt>
     */
    private Stmt parseStatement() throws CompilationException {
        Stmt stmt;

        switch (currentToken.kind) {
            case IF:
                stmt = parseIf();
                break;
            case LCURLY:
                stmt = parseBlock();
                break;
            case VAR:
                stmt = parseDeclStmt();
                break;
            case RETURN:
                stmt = parseReturn();
                break;
            case FOR:
                stmt = parseFor();
                break;
            case WHILE:
                stmt = parseWhile();
                break;
            case BREAK:
                stmt = parseBreak();
                break;
            default:
                stmt = parseExpressionStmt();
        }

        return stmt;
    }


    /*
     * <WhileStmt> ::= WHILE ( <Expression> ) <Stmt>
     */
    private Stmt parseWhile() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != WHILE) {
            handleUnexpectedToken("Missing while");
        }
        currentToken = scanner.scan();

        if (currentToken.kind != LPAREN) {
            handleUnexpectedToken("Missing (");
        }
        Expr expr = parseExpression();
        if (currentToken.kind != RPAREN) {
            handleUnexpectedToken("Missing )");
        }

        Stmt stmt = parseStatement();

        return new WhileStmt(position, expr, stmt);
    }


    /*
     * <ReturnStmt> ::= RETURN <Expression> ; | RETURN ;
     */
    private Stmt parseReturn() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != RETURN) {
            handleUnexpectedToken("Missing return");
        }
        currentToken = scanner.scan();

        Expr expr = parseExpression();
        if (currentToken.kind != SEMICOLON) {
            handleUnexpectedToken("Missing ;");
        }
        currentToken = scanner.scan();

        return new ReturnStmt(position, expr);
    }


    /*
     * BreakStmt> ::= BREAK ;
     */
    private Stmt parseBreak() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != BREAK) {
            handleUnexpectedToken("Missing break");
        }
        currentToken = scanner.scan();
        if (currentToken.kind != SEMICOLON) {
            handleUnexpectedToken("Missing ;");
        }
        currentToken = scanner.scan();

        return new BreakStmt(position(;)
    }


    /*
     * <ExpressionStmt> ::= <Expression> ;
     */
    private ExprStmt parseExpressionStmt() throws CompilationException {
        int position = currentToken.position;

        Expr expr = parseExpression();
        if (currentToken.kind != SEMICOLON) {
            handleUnexpectedToken("Missing ;");
        }
        currentToken = scanner.scan();

        return new ExprStmt(position, expr);
    }


    /*
     * <DeclStmt> ::= VAR <Identifier> = <Expression> ;
     * every local variable must be initialized
     */
    private Stmt parseDeclStmt() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != VAR) {
            handleUnexpectedToken("Missing var");
        }
        currentToken = scanner.scan();
        String name = parseIdentifier();
        if (currentToken.kind != ASSIGN) {
            handleUnexpectedToken("Missing =");
        }
        currentToken = scanner.scan();
        Expr initExpr = parseExpression();
        if (currentToken.kind != SEMICOLON) {
            handleUnexpectedToken("Missing ;");
        }
        currentToken = scanner.scan();

        return new DeclStmt(position, name, initExpr);
    }


    /*
     * <ForStmt> ::= FOR ( <Start> ; <Terminate> ; <Increment> ) <STMT>
     * <Start>     ::= EMPTY | <Expression>
     * <Terminate> ::= EMPTY | <Expression>
     * <Increment> ::= EMPTY | <Expression>
     */
    private Stmt parseFor() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != FOR) {
            handleUnexpectedToken("Missing for");
        }
        currentToken = scanner.scan();

        if (currentToken.kind != LPAREN) {
            handleUnexpectedToken("Missing (");
        }
        currentToken = scanner.scan();

        Expr start = null;
        if (currentToken.kind != SEMICOLON) {
            start = parseExpression();
            if (currentToken.kind != SEMICOLON) {
                handleUnexpectedToken("Missing ;");
            }
        }
        currentToken = scanner.scan();

        Expr terminate = null;
        if (currentToken.kind != SEMICOLON) {
            terminate = parseExpression();
            if (currentToken.kind != SEMICOLON) {
                handleUnexpectedToken("Missing ;");
            }
        }
        currentToken = scanner.scan();

        Expr increment = null;
        if (currentToken.kind != SEMICOLON) {
            increment = parseExpression();
            if (currentToken.kind != SEMICOLON) {
                handleUnexpectedToken("Missing ;");
            }
        }

        currentToken = scanner.scan();
        if (currentToken.kind != RPAREN) {
            handleUnexpectedToken("Missing )");
        }
        currentToken = scanner.scan();

        Stmt stmt = parseStatement();

        return new ForStmt(position, start, terminate, increment, stmt);
    }


    /*
     * <BlockStmt> ::= { <Body> }
     * <Body> ::= EMPTY | <Stmt> <Body>
     */
    private Stmt parseBlock() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != LCURLY) {
            handleUnexpectedToken("Missing {");
        }
        currentToken = scanner.scan();
        StmtList stmtList = new StmtList(position);
        while (currentToken.kind != RBRACKET) {
            Stmt aStmt = parseStatement();
            stmtList.addElement(aStmt);
        }

        return new BlockStmt(position, stmtList);
    }


    /*
     * <IfStmt> ::= IF ( <Expr> ) <Stmt> | IF ( <Expr> ) <Stmt> ELSE <Stmt>
     */
    private Stmt parseIf() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != IF) {
            handleUnexpectedToken("Missing if");
        }
        currentToken = scanner.scan();

        while (currentToken.kind != LPAREN) {
            handleUnexpectedToken("Missing (");
        }
        currentToken = scanner.scan();

        Expr predExpr = parseExpression();
        while (currentToken.kind != RPAREN) {
            handleUnexpectedToken("Missing )");
        }
        currentToken = scanner.scan();

        Stmt thenStmt = parseStatement();

        Stmt elseStmt = null;
        if (currentToken.kind == ELSE) {
            currentToken = scanner.scan();
            elseStmt = parseStatement();
        }

        return new IfStmt(position, predExpr, thenStmt, elseStmt);
    }


    //-----------------------------------------
    // Expressions
    //Here we introduce the precedence to operations

    /*
     * <Expression> ::= <LogicalOrExpr> <OptionalAssignment>
     * <OptionalAssignment> ::= EMPTY | = <Expression>
     */
    private Expr parseExpression() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseOrExpr();
        if (currentToken.kind == ASSIGN) {
            currentToken = scanner.scan();
            Expr right = parseExpression();
            VarExpr leftVar = (VarExpr) left;
            VarExpr leftRef = (VarExpr) leftVar.getRef();
            left = new AssignExpr(position, leftRef.getName(), leftVar.getName(), right);
        }

        return left;
    }


    /*
     * <LogicalOR> ::= <logicalAND> <LogicalORRest>
     * <LogicalORRest> ::= EMPTY |  || <LogicalAND> <LogicalORRest>
     */
    private Expr parseOrExpr() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseAndExpr();
        while (currentToken.spelling.equals("||")) {
            currentToken = scanner.scan();
            Expr right = parseAndExpr();
            left = new BinaryLogicOrExpr(position, left, right);
        }

        return left;
    }


    /*
     * <LogicalAND> ::= <ComparisonExpr> <LogicalANDRest>
     * <LogicalANDRest> ::= EMPTY |  && <ComparisonExpr> <LogicalANDRest>
     */
    private Expr parseAndExpr() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseEqualityExpr();
        while (currentToken.spelling.equals("&&")) {
            currentToken = scanner.scan();
            Expr right = parseEqualityExpr();
            left = new BinaryLogicAndExpr(position, left, right);
        }

        return left;
    }


    /*
     * <ComparisonExpr> ::= <RelationalExpr> <equalOrNotEqual> <RelationalExpr> |
     *                     <RelationalExpr>
     * <equalOrNotEqual> ::=  == | !=
     */
    private Expr parseEqualityExpr() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseRelationalExpr();

        if (currentToken.spelling.equals("==")) {
            currentToken = scanner.scan();
            Expr right = parseRelationalExpr();
            left = new BinaryCompEqExpr(position, left, right);
        }
        else if (currentToken.spelling.equals("!=")) {
            currentToken = scanner.scan();
            Expr right = parseRelationalExpr();
            left = new BinaryCompNeExpr(position, left, right);
        }

        return left;
    }


    /*
     * <RelationalExpr> ::=<AddExpr> | <AddExpr> <ComparisonOp> <AddExpr>
     * <ComparisonOp> ::=  < | > | <= | >= | INSTANCEOF
     */
    private Expr parseRelationalExpr() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseAddExpr();
        if (currentToken.kind == COMPARE) {
            String comparisonOp = currentToken.spelling;
            currentToken = scanner.scan();
            Expr right = parseAddExpr();
            switch(comparisonOp) {
                case "<":
                    left = new BinaryCompLtExpr(position, left, right);
                    break;
                case ">":
                    left = new BinaryCompGtExpr(position, left, right);
                    break;
                case "<=":
                    left = new BinaryCompGeqExpr(position, left, right);
                    break;
                case ">=":
                    left = new BinaryCompLeqExpr(position, left, right);
                    break;
                default:
                    left = new InstanceofExpr(position, left, ((VarExpr) right).getName());
            }
        }

        return left;
    }


    /*
     * <AddExpr>::＝ <MultExpr> <MoreMultExpr>
     * <MoreMultExpr> ::= EMPTY | + <MultExpr> <MoreMultExpr> | - <MultExpr> <MoreMultExpr>
     */
    private Expr parseAddExpr() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseNewCastOrUnary();

        while (currentToken.kind == PLUSMINUS) {
            String op = currentToken.spelling;
            currentToken = scanner.scan();
            Expr right = parseNewCastOrUnary();
            switch(op) {
                case "+":
                    left = new BinaryArithPlusExpr(position, left, right);
                    break;
                case "-":
                    left = new BinaryArithMinusExpr(position, left, right);
                    break;
            }
        }

        return left;
    }


    /*
     * <MultiExpr> ::= <NewCastOrUnary> <MoreNCU>
     * <MoreNCU> ::= * <NewCastOrUnary> <MoreNCU> |
     *               / <NewCastOrUnary> <MoreNCU> |
     *               % <NewCastOrUnary> <MoreNCU> |
     *               EMPTY
     */
    private Expr parseMultExpr() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseNewCastOrUnary();

        while (currentToken.kind == MULDIV) {
            String op = currentToken.spelling;
            currentToken = scanner.scan();
            Expr right = parseNewCastOrUnary();
            switch(op) {
                case "*":
                    left = new BinaryArithTimesExpr(position, left, right);
                    break;
                case "/":
                    left = new BinaryArithDivideExpr(position, left, right);
                    break;
                case "%":
                    left = new BinaryArithModulusExpr(position, left, right);
                    break;
            }
        }

        return left;
    }

    /*
     * <NewCastOrUnary> ::= < NewExpression> | <CastExpression> | <UnaryPrefix>
     */
    private Expr parseNewCastOrUnary() throws CompilationException {
        Expr expr;

        switch(currentToken.kind) {
            case NEW:
                expr = parseNew();
                break;
            case CAST:
                expr = parseCast();
                break;
            default:
                expr = parseUnaryPrefix();
                break;
        }

        return expr;
    }


    /*
     * <NewExpression> ::= NEW <Identifier> ( ) | NEW <Identifier> [ <Expression> ]
     */
    private Expr parseNew() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != NEW) {
            handleUnexpectedToken("Missing new");
        }
        currentToken = scanner.scan();

        String identifier = parseIdentifier();

        if (currentToken.kind == LPAREN) {
            currentToken = scanner.scan();
            if (currentToken.kind != RPAREN) {
                handleUnexpectedToken("Missing )");
            }
            handleUnexpectedToken("Missing (");
        } else if (currentToken.kind != LBRACKET) {
            currentToken = scanner.scan();
            parseExpression();
            if (currentToken.kind == RBRACKET) {
                handleUnexpectedToken("Missing ]");
            }
        } else {
            handleUnexpectedToken("Missing ( or [");
        }

        return new NewExpr(position, identifier);
    }


    /*
     * <CastExpression> ::= CAST ( <Type> , <Expression> )
     */
    private Expr parseCast() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != LPAREN) {
            handleUnexpectedToken("Missing (");
        }
        currentToken = scanner.scan();

        String type = parseType();

        if (currentToken.kind != COMMA) {
            handleUnexpectedToken("Missing ,");
        }
        currentToken = scanner.scan();

        Expr expr = parseExpression();

        if (currentToken.kind != RPAREN) {
            handleUnexpectedToken("Missing )");
        }
        currentToken = scanner.scan();

        return new CastExpr(position, type, expr);
    }


    /*
     * <UnaryPrefix> ::= <PrefixOp> <UnaryPrefix> | <UnaryPostfix>
     * <PrefixOp> ::= - | ! | ++ | --
     */
    private Expr parseUnaryPrefix() throws CompilationException {
        int position = currentToken.position;

        if (!currentToken.spelling.equals("-") &&
                !currentToken.spelling.equals("!") &&
                !currentToken.spelling.equals("++") &&
                !currentToken.spelling.equals("--" )) {
            return parseUnaryPostfix();
        }

        String op = currentToken.spelling;
        currentToken = scanner.scan();
        switch(op) {
            case "-":
                return new UnaryNegExpr(position, parseUnaryPrefix());
            case "!":
                return new UnaryNotExpr(position, parseUnaryPrefix());
            case "++":
                return new UnaryIncrExpr(position, parseUnaryPrefix(), false);
            default:
                return new UnaryDecrExpr(position, parseUnaryPrefix(), false);
        }
    }

    /*
     * <UnaryPostfix> ::= <Primary> <PostfixOp>
     * <PostfixOp> ::= ++ | -- | EMPTY
     */
    private Expr parseUnaryPostfix() throws CompilationException {
        int position = currentToken.position;

        Expr expr = parsePrimary();
        if (currentToken.spelling.equals("++")) {
            return new UnaryIncrExpr(position, expr, true);
        }
        if (currentToken.spelling.equals("--")) {
            return new UnaryDecrExpr(position, expr, true);
        }

        return null;
    }


    /*
     * <Primary> ::= ( <Expression> ) | <IntegerConst> | <BooleanConst> |
     *                               <StringConst> | <VarExpr> | <DispatchExpr>
     * <VarExpr> ::= <VarExprPrefix> <Identifier> <VarExprSuffix>
     * <VarExprPrefix> ::= SUPER . | THIS . | EMPTY
     * <VarExprSuffix> ::= [ <Expr> ] | EMPTY
     * <DispatchExpr> ::= <DispatchExprPrefix> <Identifier> ( <Arguments> )
     * <DispatchExprPrefix> ::= <Primary> . | EMPTY
     */
    private Expr parsePrimary() throws CompilationException {
    }


    /*
     * <Arguments> ::= EMPTY | <Expression> <MoreArgs>
     * <MoreArgs>  ::= EMPTY | , <Expression> <MoreArgs>
     */
    private ExprList parseArguments() throws CompilationException {
        int position = currentToken.position;
        ExprList exprList = new ExprList(position);

        while (currentToken.kind == COMMA) {
            Expr expr = parseExpression();
            exprList.addElement(expr);
        }

        return exprList;
    }


    /*
     * <Parameters>  ::= EMPTY | <Formal> <MoreFormals>
     * <MoreFormals> ::= EMPTY | , <Formal> <MoreFormals
     */
    private FormalList parseParameters() throws CompilationException {
        int position = currentToken.position;

        FormalList formalList = new FormalList(position);
        while (currentToken.kind == COMMA) {
            Formal formal = parseFormal();
            formalList.addElement(formal);
        }
        return formalList;
    }


    /*
     * <Formal> ::= <Type> <Identifier>
     */
    private Formal parseFormal() throws CompilationException {
        int position = currentToken.position;

        String type = parseType();
        String identifier = parseIdentifier();

        return new Formal(position, type, identifier);
    }


    /*
     * <Type> ::= <Identifier> <Brackets>
     * <Brackets> ::= EMPTY | [ ]
     */
     */
    private String parseType() throws CompilationException {
        String identifier = parseIdentifier();
        currentToken = scanner.scan();
        if (currentToken.kind == LBRACKET){
            scanner.scan();
            if (currentToken.kind != RBRACKET){
                handleUnexpectedToken("Missing ]");
            }
        }
        currentToken = scanner.scan();
        return identifier;
    }


    //----------------------------------------
    //Terminals

    private String parseOperator() { }


    private String parseIdentifier() { }


    private ConstStringExpr parseStringConst() { }


    private ConstIntExpr parseIntConst() { }


    private ConstBooleanExpr parseBoolean() { }


    private void handleUnexpectedToken(String errorMessage) throws CompilationException {
         if (currentToken.kind != COMMENT) {
             errorHandler.register(Error.Kind.PARSE_ERROR, fileName,
                     currentToken.position, errorMessage);
             throw new CompilationException(errorMessage);
         }
         // if token is a comment, move on
         currentToken = scanner.scan();
    }
}