import java.util.Scanner;

public class Corgi {

  public static void main(String[] args) throws Exception {

    System.out.print("Enter name of Corgi program file: ");
    Scanner keys = new Scanner( System.in );
    String name = keys.nextLine();

    Lexer lex = new Lexer( name );
    Parser parser = new Parser( lex );

    // start with <statements>
    Node root = parser.parseProgram();

    // execute the parse tree
    root.execute();

    /**
     * Made it optional to graphically display the parse tree, fixes issue where the program would not wait for
     * inputs and immediately start drawing the tree, putting the TreeViewer after root.execute() also
     * fixes the problem but doesn't give the user the chance to view the output
     * 
     */  
    System.out.println("Would you like to view the parse tree? (yes/no)");
    String choice = keys.nextLine();
    choice = choice.toLowerCase();
    if(choice.equals("yes")){
      // display parse tree for debugging/testing:
      TreeViewer viewer = new TreeViewer("Parse Tree", 0, 0, 800, 500, root );
    } else {
      System.exit(1);
    }

  }// main

}
