[toc]
# 1、Hello World(Intro)
```java
public class HelloWorld {
	public static void main(String[] args) {
		System.out.println("Hello World!");
	}
}
```
## 注释规范
```java
/**
 * 表示一个简单的数学工具类。
 * 提供了常用的数学计算方法。
 *
 * @author 张三
 * @version 1.0
 * @since 2025-08-06
 */
public class MathUtils {

    /**
     * 计算两个整数的最大公约数。
     *
     * @param a 第一个正整数
     * @param b 第二个正整数
     * @return 最大公约数
     * @throws IllegalArgumentException 如果参数非正
     */
    public static int gcd(int a, int b) {
        if (a <= 0 || b <= 0) {
            throw new IllegalArgumentException("参数必须为正整数");
        }
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    // 示例：求两个数之和
    public static int sum(int x, int y) {
        return x + y;
    }
}
```
## Switch
```java
public class Month {
    public static int month(int month){
        return switch (month) {
            case 2 -> 28;
            case 4, 6, 9, 11 -> 30;
            default -> 31;
        };
    }
    public static void main(String[] args){
        int month = Integer.parseInt(args[0]);
        System.out.println(month(month));
    }
}
```
# 2、Classes
## static
> 如果将 makenoise 方法声明为 static，编译时会报错，因为它无法访问 `非static` 的 weight 变量
```java
public class dog {
    public int weight;

    // 构造器
    public dog(int w) {
        weight = w;
    }

    public void makenoise() {
        if (weight < 10) {
            System.out.println("Yip");
        } else if (weight < 30) {
            System.out.println("Bark");
        } else {
            System.out.println("Woof");
        }
    }
}
```

> 包含了一些静态方法和静态常量的类（例如`Math 类`）, 其方法和常量是直接属于类本身的，而不是属于某个具体的对象。
> 所以，不需要创建一个 Math 类的实例（对象）来调用它的方法
```java
public class PoolCalculator {
    public static void main(String[] args) {
        double radius = 5.0; // 假设游泳池的半径是 5 米

        // 直接通过类名 Math 访问静态常量 PI 和静态方法 pow
        double area = Math.PI * Math.pow(radius, 2);

        System.out.println("游泳池的面积是：" + area + "平方米");
    }
}
```
# 3、Testing
## Junit
```java
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSort {
    @Test
    public void testSort() {
        String[] input = {"i", "have", "an", "egg"};
        String[] expected = {"an", "egg", "have", "i"};

        String[] actual = Sort.sort(input);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testFindSmallest() {
        String[] input = {"i", "have", "an", "egg"};
        int expected = 2;

        int actual = Sort.findSmallest(input, 0);
        assertEquals(expected, actual);
    }

    @Test
    public void testSwap() {
        String[] input = {"i", "have", "an", "egg"};
        String[] expected = {"an", "have", "i", "egg"};

        String[] actual = Sort.swap(input, 0, 2);
        assertArrayEquals(expected, actual);
    }
}
```
## 关于assertEquals
```java
String s1 = new String("hello");
String s2 = new String("hello");
assertEquals(s1, s2); // 断言成功，因为String重写了equals方法，内容相同

Object o1 = new Object();
Object o2 = new Object();
assertEquals(o1, o2); // 断言失败，因为Object的默认equals是==比较，两个对象引用不同
```
# 4、References, Recursion, and Lists
## double和int
- **整数用 int** → 精确、快、内存少、逻辑清晰
- **小数用 double** → 
	- `double` 是浮点数，用二进制近似表示小数。
	- 对很大的整数，`double` 可能表示不精确
	- 占内存多，运算慢
# 5、SLLists, Nested Classes, Sentinel Nodes
## 封装（Encapsulation）
`private` 变量只能在声明它的类内部被访问。这意味着类的外部代码，无论是其他类还是 `main` 方法，都无法直接看到或操作这个变量
## 内部类
```java
public class Car {
    private String color; // 外部类的非静态实例变量
    private static int numberOfWheels = 4; // 外部类的静态类变量

    public Car(String color) {
        this.color = color;
    }

    // 这是一个非静态内部类，它与外部类的实例绑定
    public class NonStaticEngine {
        public void printCarDetails() {
            // 非静态内部类可以直接访问外部类的非静态成员 color
            System.out.println("非静态内部类访问 -> The car's color is: " + color);

            // 也可以访问外部类的静态成员 numberOfWheels
            System.out.println("非静态内部类访问 -> The number of wheels is: " + numberOfWheels);
        }
    }

    // 这是一个静态内部类，它不与外部类的实例绑定
    public static class StaticEngine {
        public void printCarDetails() {
            // 静态内部类不能直接访问外部类的非静态成员 color
            // System.out.println("静态内部类访问 -> The car's color is: " + color); // 编译错误！

            // 但可以访问外部类的静态成员 numberOfWheels
            System.out.println("静态内部类访问 -> The number of wheels is: " + numberOfWheels);
        }
        
        // 如果想访问 color，必须传入一个 Car 实例
        public void printCarColor(Car car) {
            System.out.println("静态内部类通过实例访问 -> The car's color is: " + car.color);
        }
    }

    public static void main(String[] args) {
        // 创建一个外部类 Car 的实例
        Car myCar = new Car("Red");

        // --- 演示非静态内部类 ---
        System.out.println("--- 演示非静态内部类 ---");
        // 非静态内部类需要通过外部类的实例来创建
        Car.NonStaticEngine myNonStaticEngine = myCar.new NonStaticEngine();
        myNonStaticEngine.printCarDetails();
        
        System.out.println(); // 打印一个空行分隔输出
        
        // --- 演示静态内部类 ---
        System.out.println("--- 演示静态内部类 ---");
        // 静态内部类可以直接创建，不需要外部类的实例
        Car.StaticEngine myStaticEngine = new Car.StaticEngine();
        myStaticEngine.printCarDetails();
        
        // 静态内部类如果想访问外部类的非静态成员，需要显式传入外部类实例
        myStaticEngine.printCarColor(myCar);
    }
}
```
## 值传递 (pass-by-value)
```java
public class Foo {  
    public int x, y;  
  
    public Foo(int x, int y) {  
        this.x = x;  
        this.y = y;  
    }  
  
    public static void switcheroo(Foo a, Foo b) {  
        Foo temp = a;  
        a = b;  
        b = temp;  
    }  
  
    public static void fliperoo(Foo a, Foo b) {  
        Foo temp = new Foo(a.x, a.y);  
        a.x = b.x;  
        a.y = b.y;  
        b.x = temp.x;  
        b.y = temp.y;  
    }

	public static void main(String[] args) {  
    Foo foobar = new Foo(10, 20);  
    Foo baz = new Foo(30, 40);  

	// switcheroo不能交换foobar和baz
    switcheroo(foobar, baz);  
    // fliperoo
    fliperoo(foobar, baz);  
	}
}
```
> 传进函数的，不是对象，也不是“指针本身”，而是一个对象引用的拷贝，在方法中修改这个拷贝不会影响外部变量的引用指向


```python
def switcheroo(a, b):
    a, b = b, a

x = [10, 20]
y = [30, 40]
switcheroo(x, y)
print(x, y)                   # [10, 20] [30, 40]  也没变！
```

## 增强型for循环
```java
public class QuikMaths {
    public static void multiplyBy3(int[] A) {
        for (int x : A) {
            x = x * 3;
        }
    }

    public static void multiplyBy2(int[] A) {
        int[] B = A;
        for (int i = 0; i < B.length; i += 1) {
            B[i] *= 2;
        }
    }

    public static void main(String[] args) {
        int[] arr;
        arr = new int[]{2, 3, 3, 4};
        multiplyBy3(arr);
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            if (i < arr.length - 1) System.out.print(", ");
        }
        // 2, 3, 3, 4

        System.out.println();
        
        arr = new int[]{2, 3, 3, 4};
        multiplyBy2(arr);
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            if (i < arr.length - 1) System.out.print(", ");
        }
        // 4, 6, 6, 8
    }
}
```
> 增强型for循环不能用来修改基本类型数组元素，因为它操作的是元素值的拷贝。
> 需要用索引循环修改数组元素。
# 6、DLLists, Arrays
## 同时声明+初始化
```java
// 1. 先声明，后赋值 → 必须用 new
int[] w;
w = {9, 10, 11, 12, 13}; // ❌ 编译报错
w = new int[]{9, 10, 11, 12, 13}; // ✅ 合法

// 2. 同时声明+初始化 → 可以省略 new
int[] w = {9, 10, 11, 12, 13}; // ✅ 合法
```
## arraycopy
```java
System.arraycopy(a, 0, b, 3, 2);
```
```python
b[3: 5] = a[0: 2]
```
## pass-by-value2
```java
int[] ls = new int[2];
int first = ls[0];

ls[0] = 8;
ls[1] = 9;
System.out.println(first);

int[][] triangle = new int[4][];
int[] first1 = triangle[0];

triangle[0] = new int[]{1};

System.out.println(first1);
```

```java
// 第一种写法：first1 会改变
int[][] triangle = new int[4][];
triangle[0] = new int[]{1};
int[] first1 = triangle[0];     // first1 和 triangle[0] 指向同一个数组 {1}
triangle[0][0] = 2;             // 修改数组内容：{1} 变成 {2}

System.out.println(first1[0]);  // 输出：2 (因为指向同一个被修改的数组)

// 第二种写法：first1 不会改变
int[][] triangle = new int[4][];
triangle[0] = new int[]{1};
int[] first1 = triangle[0];     // first1 指向数组 {1}
triangle[0] = new int[]{2};     // triangle[0] 指向新的数组 {2}

System.out.println(first1[0]);  // 输出：1 (还是指向原来的数组 {1})
```
> 改变数组里的内容 → 所有指向这个数组的变量都会“看到”变化。
> 让数组变量指向一个新的数组 → 只影响这个变量，不影响原来引用的数组。

# 8、Inheritance, Implements
## 运行时**多态**
```java
public class Main {  
    public static void main(String[] args) {  
        Animal a = new Dog();  
        Dog d = new Dog();  
  
        a.greet(d);  
        a.sniff(d);  
        d.praise(d);  
        a.praise(d);      // 此处注意，输出：u r cool animal
    }  
}  
  
interface Animal {  
    default void greet(Animal a) {  
        System.out.println("hello animal");  
    }  
    default void sniff(Animal a) {  
        System.out.println("sniff animal");  
    }  
    default void praise(Animal a) {  
        System.out.println("u r cool animal");  
    }  
}  
  
class Dog implements Animal {  
    @Override  
    public void sniff(Animal a) {  
        System.out.println("dog sniff animal");  
    }  

	/** 此处是overload不是override */
    public void praise(Dog d) {  
        System.out.println("u r cool dog");  
    }  
}
```
> **重载 (overload)** 的选择在 **编译期** 由参数的静态类型决定。  
> **重写 (override)** 的选择在 **运行期** 由对象的实际类型决定。

# 9、Extends, Casting, Higher Order Functions
## 子类的构造函数必须调用父类的构造函数
```java
// 父类 SLList 拥有两种构造函数
public class SLList<Item> {
    public SLList() { /* ... */ }
    public SLList(Item x) { /* ... */ }
}
```
```java
// 子类 VengefulSLList
public class VengefulSLList<Item> extends SLList<Item> {
    SLList<Item> deletedItems;

    public VengefulSLList() {
        super(); 
        // 关键点：由于父类 SLList 存在无参数构造函数，
        // 因此即使不写上面这行 super()，编译器也会自动为你隐式添加。
        
        deletedItems = new SLList<Item>();
    }
    
    public VengefulSLList(Item x) {
        super(x);
        // 关键点：这一行 super(x) 是必需的，不可省略！
        
        deletedItems = new SLList<Item>();
    }
}
```
## 强制类型转换
```java
class Animal {}
class Dog extends Animal {
    public Dog(int size) {
        // ...
    }
}
```
```java
Dog a = new Dog(10);
Animal b = a;    // 向上转型
Dog c = b;         // 向下转型，编译错误
```
```java
Dog a = new Dog(10);
Animal b = a;
Dog c = (Dog) b; // 向下转型成功，b的运行时类型就是Dog
```
# lab2
## 强制类型转换
```java
(float) a / b
// 先把 a 转成 float，再跟 b 做除法（浮点运算）

(float) (a / b)
// 先做整数除法 a / b（丢掉小数），再转成 float
```
## Math.round
| 方法签名                        | 输入类型     | 输出类型   |
| --------------------------- | -------- | ------ |
| `long Math.round(double a)` | `double` | `long` |
| `int Math.round(float a)`   | `float`  | `int`  |
## Math.sqrt
Math.sqrt(int x) 或者 Math.sqrt(double x) 返回的都是double类型
# 零零碎碎
> `this = ...` 在 Java 里永远是不合法的


> `do-while`循环：do中的语句至少执行一次
```java
int x = 0;
do {
	System.out.println(x);
} while (x > 0);
```

> String[] 和 Int[] 放进同一个array
```java
Object[][] arrays = {{"a", "b", "c"}, {1, 2, 3}};
```

> `tree /f`可以显示路径下的文件、目录结构

> 字典序：数字 < 大写字母 < 小写字母 < 汉字