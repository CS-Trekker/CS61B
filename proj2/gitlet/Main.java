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
        // TODO: what if args is empty?
        if (args == null) {
            throw new GitletException("Please enter a command");
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs("init", args, 1);
                Repository.initCommand();
                break;
            case "add":
                validateNumArgs("add", args, 2);
                Repository.addCommand(args[1]);
                break;
            case "commit":
                validateNumArgs("commit", args, 2);
                Repository.commitCommand(args[1]);
            case "rm":
                validateNumArgs("rm", args, 2);
                Repository.rmCommand(args[1]);
                break;
            case "log":
                validateNumArgs("log", args, 1);
                Repository.logCommand();
                break;
            case "global-log":
                validateNumArgs("global-log", args, 1);
                Repository.globallogCommand();
                break;
            case "find":
                validateNumArgs("find", args, 2);
                Repository.findCommand(args[1]);
                break;
            case "status":
                validateNumArgs("status", args, 1);
                Repository.statusCommand();
                break;
            case "checkout":

                Repository.checkoutCommand(args[1]);
                break;
            case "branch":
                validateNumArgs("branch", args, 2);
                Repository.branchCommand(args[1]);
                break;
            case "rm-branch":
                validateNumArgs("rm-branch", args, 2);
                Repository.rmbranchCommand(args[1]);
                break;
            case "reset":
                validateNumArgs("reset", args, 2);
                Repository.resetCommand(args[1]);
                break;
            case "merge":
                validateNumArgs("merge", args, 2);
                Repository.mergeCommand(args[1]);
                break;
            default:
                throw new GitletException("Illegal command");
        }
    }

    /**
     * Check whether the number of parameters corresponding to cmd when entering commands in the command line meets the requirements
     * @param cmd
     * @param args
     * @param n
     */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}
