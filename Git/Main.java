package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author enmanuel hernandez
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     *
     *  The main method takes in command-line arguments and calls the appropriate
     *  methods in the Repository class based on the provided command and operands.
     */
    public static void main(String[] args) {
        // Check if there are no arguments
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        // Extract the first argument
        String firstArg = args[0];

        // Check if the current working directory is an initialized Git directory
        if (!Repository.gitlet(args[0])) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        // Process the command and dispatch to the corresponding method in Repository
        switch (firstArg) {
            case "init":
                if (Repository.checkIfGitletExists()) {
                    System.out.println("A Gitlet version-control system already exists in the current directory.");
                    System.exit(0);
                }
                Repository.Persistence();
                checkOperands(args, 1);
                Repository.Init();
                break;
            case "add":
                checkOperands(args, 2);
                Repository.stage(args[1]);
                break;
            case "commit":
                if (args.length == 1 || args[1].equals("")) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                checkOperands(args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                checkOperands(args, 2);
                Repository.rm(args[1]);
                break;
            case "log":
                checkOperands(args, 1);
                Repository.log();
                break;
            case "global-log":
                checkOperands(args, 1);
                Repository.globalLog();
                break;
            case "find":
                checkOperands(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                checkOperands(args, 1);
                Repository.status();
                break;
            case "restore":
                checkOperands(args, 3, 4);
                if (args[1].compareTo("--") == 0) {
                    Repository.restoreFile(args[2]);
                } else if (args[2].compareTo("--") == 0) {
                    Repository.restoreCommit(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "branch":
                checkOperands(args, 2);
                Repository.branch(args[1]);
                break;
            case "switch":
                checkOperands(args, 2);
                Repository.switchBranch(args[1]);
                break;
            case "rm-branch":
                checkOperands(args, 2);
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                checkOperands(args, 2);
                Repository.reset(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    //Helper method to check the number of operands in command-line arguments.
    private static void checkOperands(String[] args, int expectedNumOperands) {
        if (args.length != expectedNumOperands) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
    //Helper method to check the number of operands within a range in command-line arguments.
    private static void checkOperands(String[] args, int minNumOperands, int maxNumOperands) {
        if (args.length < minNumOperands || args.length > maxNumOperands) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}