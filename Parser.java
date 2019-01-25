/*
    This class provides a recursive descent parser
    for Corgi (a simple calculator language),
    creating a parse tree which can be interpreted
    to simulate execution of a Corgi program
*/

import java.util.*;
import java.io.*;

public class Parser {

   private Lexer lex;

   public Parser( Lexer lexer ) {
      lex = lexer;
   }

   public Node parseProgram() {
      System.out.println("-----> parsing <program>:");

      Node first = parseFuncCall();

      // Look ahead to see if a funcDef follows the funcCall
      Token token = lex.getNextToken();

      if(token.isKind("eof")){
        return new Node("prgrm", first, null, null);
      } else {
        lex.putBackToken(token);
        Node second = parseFuncDefs();
        return new Node("prgrm", first, second, null);
      }
   }

   private Node parseFuncCall(){
      System.out.println("-----> parsing <funcCall>:");

      Token token = lex.getNextToken();

      String function = token.getDetails();

      token = lex.getNextToken();
      errorCheck( token, "single", "(" );
      token = lex.getNextToken();

      if(token.isKind("single")){ // testing change token.matches("single", ")")

        return new Node("fcall", function, null, null, null);

      } else {
        lex.putBackToken(token);
        Node first = parseArgs();
        token = lex.getNextToken();
        errorCheck(token, "single", ")");
        return new Node("fcall", function, first, null, null);
      }
          
   } //<funcCall>

   private Node parseFuncDefs(){
      System.out.println("-----> parsing <funcDefs>:");

      Node first = parseFuncDef();

      Token token = lex.getNextToken();

      if(token.isKind("eof")){
        return new Node("fdefs", first, null, null);
      } else {
          lex.putBackToken(token);
          Node second = parseFuncDefs();
          return new Node("fdefs", first, second, null);
      }

   } // <funcDefs>

   private Node parseFuncDef(){
      System.out.println("-----> parsing <params>:");

      Token token = lex.getNextToken();

      errorCheck( token, "var", "def" );
      token = lex.getNextToken();
      String function = token.getDetails();
      token = lex.getNextToken();
      errorCheck( token, "single", "(" );
      token = lex.getNextToken();

      if(token.matches("single", ")")){
         token = lex.getNextToken();

         if(token.getDetails() == "end"){
            return new Node("fdef", function, null, null, null);
         } else {
            lex.putBackToken(token);
            Node second = parseStatements();
            return new Node("fdef", function, null, second, null);
         }
      } else {

         lex.putBackToken(token);
         Node first = parseParams();
         token = lex.getNextToken();

         if(token.getDetails() == "end"){
            return new Node("fdef", function, first, null, null);
         } else {
            lex.putBackToken(token);
            Node second = parseStatements();
            return new Node("fdef", function, first, second, null);
         }
      }
   } // <funcDef>

   private Node parseParams(){
      System.out.println("-----> parsing <params>:");

      //Node first = parseFactor();

      Token token = lex.getNextToken();
      String param = token.getDetails();
      token = lex.getNextToken();

      if(token.getDetails().equals(")")){
        return new Node("params", param, null, null, null);
      } else {
        errorCheck(token, "single", ",");
        Node first = parseParams();
        return new Node("params", param, first, null, null);
      }
   } // <params>

   private Node parseArgs(){
      System.out.println("-----> parsing <args>:");

      Node first = parseExpr();

      Token token = lex.getNextToken();

      if(token.getDetails().equals(")")){
        lex.putBackToken(token);    
        return new Node("args", first, null, null);
      } else {
        errorCheck(token, "single", ",");
        Node second = parseArgs();
        return new Node("args", first, second, null);
      }
   } // <args>

   private Node parseStatements() {
      System.out.println("-----> parsing <statements>:");

      Node first = parseStatement();

      Token token = lex.getNextToken();

      if ( token.isKind("eof") || token.matches("var", "else") || token.matches("var", "end")) {
         return new Node( "stmts", first, null, null );
      }
      else {
         lex.putBackToken( token );
         Node second = parseStatements();
         return new Node( "stmts", first, second, null );
      }
   }// <statements>

   private Node parseStatement() {
      System.out.println("-----> parsing <statement>:");

      Token token = lex.getNextToken();

      // --------------->>>   <string>
      if ( token.isKind("string") ){
         return new Node( "printString", token.getDetails(), null, null, null );
      }

      else if ( token.matches("bif0","nl") ) {
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         return new Node( "nl", null, null, null );
      }

      else if ( token.matches("bif1","print") ) {
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         return new Node( "print", first, null, null );
      }

      if( token.isKind("bif1") || token.isKind("bif2")) {
         Node first = parseParams();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         return new Node ( token.getDetails(), first, null, null);
      }

      else if ( token.matches("var","return") ){
         Node first = parseExpr();
         return new Node("return", first, null, null);
      }

      else if ( token.isKind("var") && token.getDetails().equals("if") ) {
         Node first = parseExpr();
         token = lex.getNextToken();
         if(token.getDetails().equals("else")){
            token = lex.getNextToken();
            if(token.getDetails().equals("end")){
               return  new Node("ifelse", first, null, null);
            }
            else{
               lex.putBackToken(token);
               Node third = parseStatements();
               return  new Node("ifelse", first, null, third);
            }
         }
         else{
            lex.putBackToken(token);
            Node second = parseStatements();
            token = lex.getNextToken();
            if(token.getDetails().equals("end")){
               return  new Node("ifelse", first, second, null);
            }
            else{
               lex.putBackToken(token);
               Node third = parseStatements();
               return  new Node("ifelse", first, second, third);
            }
         }
      }

      else if( token.isKind("var") && token.getDetails() != "if" && token.getDetails() != "return" ) {

         Token temp = lex.getNextToken();

         // --------------->>>   <var> = <expr>
         if(temp.getDetails().equals("=")) {
            String varName = token.getDetails();
            Node first = parseExpr();
            errorCheck(temp, "single", "=");
            return new Node("sto", varName, first, null, null);
         }
         // --------------->>>   <funcCall>()
         else if(temp.getDetails().equals("(")) {
            lex.putBackToken(temp);
            lex.putBackToken(token);
            return parseFuncCall();
         }
         else {
            System.out.println("Statement can't start with " + token );
            System.exit(1);
            return null;
         }
      }
      
      else {
         System.out.println("Statement can't start with " + token );
         System.exit(1);
         return null;
      }
   }// <statement>

   private Node parseExpr() {
      System.out.println("-----> parsing <expr>");

      Node first = parseTerm();

      Token token = lex.getNextToken();
 
      if ( token.matches("single", "+") || token.matches("single", "-") 
         ) {
         Node second = parseExpr();
         return new Node( token.getDetails(), first, second, null );
      }
      else {
         lex.putBackToken( token );
         return first;
      }

   }// <expr>

   private Node parseTerm() {
      System.out.println("-----> parsing <term>");

      Node first = parseFactor();

      Token token = lex.getNextToken();

      if ( token.matches("single", "*") || token.matches("single", "/")) {
         Node second = parseTerm();
         return new Node( token.getDetails(), first, second, null );
      } else {
         lex.putBackToken( token );
         return first;
      }

   }// <term>

   private Node parseFactor() {
      System.out.println("-----> parsing <factor>");

      Token token = lex.getNextToken();

      if ( token.isKind("num") ) {
         return new Node("num", token.getDetails(), null, null, null );
      }
      else if ( token.isKind("var") ) {
        Token temp = lex.getNextToken();
        if(temp.matches("single", "(")){
          lex.putBackToken(temp);
          lex.putBackToken(token);
          Node first = parseFuncCall();
          return first;
        }
        else {
          lex.putBackToken(temp);
        }
        return new Node("var", token.getDetails(), null, null, null );
      }
      else if ( token.matches("single","(") ) {
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         return first;
      }
      else if ( token.isKind("bif0") ) {
         String bif = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         return new Node( bif, null, null, null );
      }
      else if ( token.isKind("bif1") ) {
         String bif = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         return new Node( bif, first, null, null );
      }
      else if ( token.isKind("bif2") ) {
         String bif = token.getDetails();
         token = lex.getNextToken();
         errorCheck( token, "single", "(" );
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", "," );
         Node second = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         return new Node( bif, first, second, null );
      }
      else if ( token.matches("single","-") ) {
         Node first = parseFactor();
         return new Node("opp", first, null, null );
      }
      else {
         System.out.println("Factors can not start with " + token );
         System.exit(1);
         return null;
      }

   }// <factor>

  private void errorCheck( Token token, String kind ) {
    if( ! token.isKind( kind ) ) {
      System.out.println("Error:  expected " + token +
                         " to be of kind " + kind );
      System.exit(1);
    }
  }

  private void errorCheck( Token token, String kind, String details ) {
    if( ! token.isKind( kind ) ||
        ! token.getDetails().equals( details ) ) {
      System.out.println("Error:  expected " + token +
                          " to be kind = " + kind +
                          " and details = " + details );
      System.exit(1);
    }
  }

}
