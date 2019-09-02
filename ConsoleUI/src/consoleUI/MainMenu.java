package consoleUI;

import java.util.Scanner;

public class MainMenu {

    private static final Magit magit = new Magit();

    public static void main(String[] args) {

        System.out.println("Welcome to Magit!\n");
        boolean toEnd = false;
        do {

            System.out.println();
            magit.printCurrentRepoDetails();
            printMenu();
            System.out.println();
            int userSelectionNum = getValidUserOperSelection();
            if (MenuItem.isExitCode(userSelectionNum)) {
                System.out.println("Goodbye!");
                toEnd = true;
            } else {
                MenuItem userSelection = MenuItem.getItemByInt(userSelectionNum);
                doRequestedOperation(userSelection);
            }
        } while (!toEnd);
    }

    private static void printMenu() {
        System.out.println("Please type the number of the operation you wish to do:");
        for (MenuItem item : MenuItem.values())
            System.out.println(item);
        System.out.println(MenuItem.EXIT_OPER_CODE + ". Quit program");
    }

    private static int getValidUserOperSelection() {
        Scanner scanner = new Scanner(System.in);
        int selection;
        boolean validInput = false;

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

        } while (!validInput);

        return selection;
    }

    private static void doRequestedOperation(MenuItem requestedOper) {
        switch (requestedOper) {
            case NEW_REPO_SCRATCH:
                magit.createNewRepoFromScratch();
                break;
            case TRAVERSE_WC:
                magit.traverseWC();
                break;

            case CHANGE_REPO:
                magit.changeRepo();
                break;

            case TEST_FIRST_COMMIT:
                magit.firstCommit();
                break;

            case TEST_UPDATE_COMMIT_ON_REPO:
                magit.testUpdateCommitDatabases();
                break;

            case PRINT_OBJECTS_TEST:
                magit.printObject();
                break;

        }
    }


}
