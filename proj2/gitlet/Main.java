package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author CS-Trekker
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     *  java gitlet.Main add hello.txt
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Please enter a command");
            System.exit(0);
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args, 1);
                Repository.initCommand();
                break;
            case "add":
                validateNumArgs(args, 2);
                Repository.addCommand(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                Repository.commitCommand(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                Repository.rmCommand(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1);
                Repository.logCommand();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                Repository.globallogCommand();
                break;
            case "find":
                validateNumArgs(args, 2);
                Repository.findCommand(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                Repository.statusCommand();
                break;
            case "checkout":

                Repository.checkoutCommand(args);
                break;
            case "branch":
                validateNumArgs(args, 2);
                Repository.branchCommand(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                Repository.rmbranchCommand(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                Repository.resetCommand(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);
                Repository.mergeCommand(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    /**
     * Check whether the number of parameters corresponding to cmd when entering commands in the command line meets the requirements
     * @param cmd
     * @param args
     * @param n
     */
    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
