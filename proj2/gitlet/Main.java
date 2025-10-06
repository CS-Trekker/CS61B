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
                // TODO: handle the `init` command
                validateNumArgs("init", args, 1);
                Repository.initCommand();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                break;
            // TODO: FILL THE REST IN
            case "rm":
                break;
            case "status":
                // TODO: handle the 'status' command
                validateNumArgs("status", args, 1);
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
