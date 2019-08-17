package consoleUI;

import java.util.Scanner;

public class Main {

    //private static Repository repo;

    public static void main(String[] args) {
        //MagitOperations magit = new MagitOperations();
        System.out.println("Welcome to Magit!\n");
        printMenu();
        System.out.println();
        int userSelectionNum = getValidUserOperSelction();
        if (MenuItem.isExitCode(userSelectionNum))
            System.out.println("Goodbye!");
        else {
            MenuItem userSelection = MenuItem.getItemByInt(userSelectionNum);
            userSelection.eval();
        }
        //git + github test22
    }

    private static void printMenu() {
        for (MenuItem item : MenuItem.values())
            System.out.println(item);
        System.out.println(MenuItem.EXIT_OPER_CODE + ". Quit program");
    }

    private static int getValidUserOperSelction() {
        Scanner scanner = new Scanner(System.in);
        int selection;
        boolean validInput = false;
        System.out.println("Please type the number of the operation you want to do:");

        do {
            while (!scanner.hasNextInt()) {
                String input = scanner.nextLine();
                System.out.println(input + " is not a valid number. Please type a one...");
            }
            selection = scanner.nextInt();
            //scanner.skip(".*");
            scanner.nextLine();

            if (MenuItem.isOperNumValid(selection) || MenuItem.isExitCode(selection))
                validInput = true;
            else
                System.out.println(selection + " is not a valid operation code. Please type a one...");

        } while (validInput == false);

        return selection;
    }


}
