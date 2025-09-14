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
> 如果遇到无法解析junit的问题，在“项目结构”——“库”中导入"javalib"
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
## resume
![[Pasted image 20250826164230.png]]
> 让程序从当前暂停的断点处恢复正常执行，直到遇到下一个断点或者程序自然结束

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
// 无法修改数组A的内容
public static void multiplyBy3(int[] A) {
	for (int x : A) {
		x = x * 3;
	}
}

// 可以修改
public static void multiplyBy2(int[] A) {
	int[] B = A;
	for (int i = 0; i < B.length; i += 1) {
		B[i] *= 2;
	}
}
```
> 增强型for循环不能用来修改基本类型数组元素，因为它操作的是元素值的拷贝。
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
  
        a.sniff(d);  
        d.praise(d);  
        a.praise(d);      // 此处注意，输出：u r cool animal
    }  
}  
  
interface Animal {  
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
> `a.praise(d)`在编译期确定了方法签名是`praise(Animal a)`，然后在运行期，jvm发现a动态类型是Dog，所以先在Dog类中寻找`praise(Animal a)`这个方法签名，结果没找到，所以再在Animal类中找，找到了，输出"u r cool animal"


> 编译期中，只关注参数的静态类型

# 9、Extends, Casting, Higher Order Functions
## 子类的构造函数必须调用父类的
### 例子1
```java
// 父类只有一种构造函数（需要两个参数）
public class IntNode {
	public int item;
	public IntNode next;
	public IntNode(int i, IntNode n) {
		item = i;
		next = n;
	}
}
```

```java
// 子类 (正确示范)
public class LastNode extends IntNode {
	public LastNode() {
		super(0, null);
	}
}
```
```java
// 子类（错误示范）
public class LastNode extends IntNode {
	public LastNode() {
		item = 0;               // 因为编译器试图向LastNode的构造器中插入super()，却发现父类没有这个无参数的构造函数
		next = n;
	}
}
```
### 例子2
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
        // 关键点：父类 SLList 存在无参数构造函数，
        // 因此即使不写上面这行 super()，编译器也会自动为你隐式添加。
        
        deletedItems = new SLList<Item>();
    }
    
    public VengefulSLList(Item x) {
        super(x);
        // 编译器不会帮你自动添加一个super(x)
        
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
# prep4
[难题地址](https://sp21.datastructur.es/materials/discussion/examprep04.pdf) (第二题)
（有关如何在已经基本构造好的DLList后面正确地加上一个尾节点，从而消除空指针异常）

# lab2
## 强制类型转换2
```java
(float) a / b
// 先把 a 转成 float，再跟 b 做除法（浮点运算）

(float) (a / b)
// 先做整数除法 a / b（丢掉小数），再转成 float
```

# pro1
## java.util.Iterator接口
```java
import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
	//省略

	private class LinkedListDequeIterator implements Iterator<T> {
        private Node currentNode;

        public LinkedListDequeIterator() {
            currentNode = sentinel.next;
        }

        @Override
        public boolean hasNext() {
            return currentNode != sentinel;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException("No more elements to iterate over.");
            }
            T returnItem = currentNode.item;
            currentNode = currentNode.next;
            return returnItem;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }
}
```
> 之所以不需要 `import java.lang.Iterable;` 就可以直接在类定义中使用 `Iterable<T>`，是因为 **`java.lang` 包是 Java 中唯一一个被自动导入到每一个源代码文件中的包**
## java.util.Comparator接口
> 这个接口只需要实现一个方法
> `int compare(T o1, T o2)`
> （如果o1小于o2，返回负整数；如果o1大于o2，返回正整数；相等，返回0）

```java
import java.util.Comparator;

public static class intComp implements Comparator<Integer> {
	@Override
	public int compare(Integer o1, Integer o2) {
		return (o1 - o2);
	}
} 
```

> 一个使用intComp的例子
```java
@Test
public void maxTest() {
	MaxArrayDeque<Integer> mad = new MaxArrayDeque<>(new intComp);  // 注意： 这里要传入的是intComp的一个实例
		mad.addLast(1);
        mad.addLast(2);
        mad.addLast(3);
        assertEquals(3, (int) mad.max());
	}
}
```
# Math
## Math.round
| 方法签名                        | 输入类型     | 输出类型   |
| --------------------------- | -------- | ------ |
| `long Math.round(double a)` | `double` | `long` |
| `int Math.round(float a)`   | `float`  | `int`  |
## Math.sqrt
Math.sqrt(int x) 或者 Math.sqrt(double x) 返回的都是double类型
## Math.random
Math.random()的结果是double,范围在`[0.0, 1.0)`
## Math.pow
| 方法签名                                  | 输入类型               | 输出类型     |
| ------------------------------------- | ------------------ | -------- |
| `double Math.pow(double a, double b)` | `double`, `double` | `double` |

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


> 切换不同版本的java（在终端里）
```pwsh
scoop reset openjdk21
scoop reset temurin17-jdk
```

> String的`indexOf`方法，在一个字符串中查找另一个字符或子字符串**首次**出现的位置（索引）。如果找不到，它会返回 `-1`


> 接口中的方法可以用default修饰符，然后写出方法体，这样在实现类中不重写这个方法也不会报错


```java
(C) c0.m2();
// 已知： m2方法返回void
```
> 点运算符 `.` 的优先级高于类型转换 `(C)`，所以会CE