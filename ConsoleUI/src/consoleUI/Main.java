package consoleUI;

import engine.Repository;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.InvalidPathException;
import java.util.Scanner;

public class Main {

    private static Repository repo;

    public static void main(String[] args) {
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


    public static void createNewRepoFromScratchWrapper() {
        repo = createNewRepoFromScratch(repo);
    }


    private static Repository createNewRepoFromScratch(Repository newRepo) {
        //Repository newRepo = null;
        String newRepoName = null;
        String requestedParentPath = null;
        Scanner scanner = new Scanner(System.in);
        boolean finishInputLoop = false;

        do {
            System.out.print("Please enter your new repo name: ");
            try {
                newRepoName = scanner.nextLine();
                //verify name basic requirements
                if (newRepoName.isEmpty() || newRepoName.startsWith(" ") || newRepoName.endsWith(" ")) {
                    System.out.println("Please enter a non-empty name which doesn't starts or ends with a space.");
                    continue; //skip to end of the loop iteration (ask the user for a name again...)
                }

                System.out.print("Please enter where to save your new repo (the parent directory " +
                        "e.g. C:\\magit): ");
                requestedParentPath = scanner.nextLine();

                Repository.checkNewRepoPath(requestedParentPath, newRepoName);
                String newRepoPath = requestedParentPath.concat("\\" + newRepoName);
                newRepo = Repository.newRepoFromScratchCreator(newRepoName, newRepoPath);

                System.out.println("Your repo " + newRepoName + " has been created successfully at " + newRepoPath + " !");
                finishInputLoop = true;

            } catch (InvalidPathException e) {
                System.out.println(e);
                System.out.println("The parent path you supplied doesn't exist. Please enter an existing one.");
            } catch (FileAlreadyExistsException e) {
                System.out.println(e);
                System.out.println("There is already a folder named " + newRepoName + " in the path " + requestedParentPath
                        + " . Please supply another valid path or another name for your repo.");
            } catch (IOException e) {
                System.out.println(e);
                System.out.println("General I/O Error! Please try to create the repo again.");
                finishInputLoop = true;
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Unknown Error! Please try to create the repo again.");
                finishInputLoop = true;
            }
        } while (!finishInputLoop);

        return newRepo;
    }

}
