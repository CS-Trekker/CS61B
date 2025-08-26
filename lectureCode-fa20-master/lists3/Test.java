public class Test {
    public static void main(String[] args) {
        int[] ls = new int[2];
        int first = ls[0];
        
        ls[0] = 8;
        ls[1] = 9;
        System.out.println(first);

        int[][] triangle = new int[4][];
        int[] first1 = triangle[0];
        
        triangle[0] = new int[]{1};
        triangle[1] = new int[]{1, 1};
        triangle[2] = new int[]{1, 2, 1};
        triangle[3] = new int[]{1, 3, 3, 1};

        System.out.println(first1);
    }
}