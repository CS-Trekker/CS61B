[toc]
# 1、Hello World(Intro)
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
## 三元运算符(?)
```java
n % 2 == 0 ? n/2 : 3*n + 1
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

# 5、SLLists, Nested Classes, Sentinel Nodes
## 封装（Encapsulation）
`private` 变量只能在声明它的类内部被访问。这意味着类的外部代码，无论是其他类还是 `main` 方法，都无法直接看到或操作这个变量
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
# 14、Disjoint Sets(DS/并查集)
## 优化过程
> `Quick Find`
> > 数据结构: 使用一个整数数组 id[]。
> >当且仅当 id[p] == id[q] 时，p 和 q 连通
> >Union ($O(N)$): 这是瓶颈。要将 p 和 q 合并，必须遍历整个数组，将所有等于 id[p] 的值改为 id[q]

>`Quick Union`
> > 数据结构: parent[] 数组（有时也叫 id[]）。
> > 含义: parent[i] 表示元素 i 的父节点。
> > 根节点: 如果 i == parent[i]，则 i 是根节点
> > Union: 只需要将 p 的根节点指向 q 的根节点。即 parent[rootP] = rootQ
> > 问题在于树可能过高，另外find复杂度退化为($O(N)$)

> `Weighted Quick Union (加权快速合并)`
> > 数据结构: 增加了一个额外的数组 size[]，用来记录以 i 为根的树中的元素个数。
> > 策略: 在 union 时，总是将 小树（Size 小） 连接到 大树（Size 大） 的根节点下。
> > 效率估计：假设有N个元素，进行了$M_U$次union和$M_C$次connected操作，我的分析结果是 $\text{Cost} \approx  2 \cdot M_U \cdot \log N + 2 \cdot M_C \cdot \log N$，但是忘了创建数据结构时进行了一次循环，以及标准大O写法还要`去掉常数、保留各项最高阶`，所以正确答案应该是$O(N + M_U \log N + M_C \log N)$

> `Quick Union with Path Compression (路径压缩)` 
```java
public int find(int p) {
    // 第一步：先找到根节点 (root)
    int root = p;
    while (root != id[root]) {
        root = id[root];
    }
    
    // 第二步：路径压缩 (Flatten the tree)
    // 再次从 p 开始向上遍历，把路径上所有节点的父节点直接设为 root
    while (p != root) {
        int newp = id[p]; // 暂存 p 的原父节点
        id[p] = root;     // 核心操作：直接指向根节点
        p = newp;         // 移动到原父节点继续处理
    }
    
    return root;
}
```
# 15、Asymptotics II
> 1. O(f(n))：上界（不会比它快得夸张）
> 2. Ω(f(n))：下界（不会比它慢得夸张）
> 3. Θ(f(n))：紧确界（同时满足 O 和 Ω）

> 空循环也要花时间执行“循环本身” —— 比如更新 i、判断条件、跳转等。所以时间复杂度依然由循环次数决定，而不是由循环体内容决定

### 主定理（适用于“除法”形式的递归关系）
> $$T(n) = a \cdot T\left(\frac{n}{b}\right) + f(n)$$这里的变量含义非常重要：$n$：问题的规模。$a$：递归调用的数量（子问题个数），必须 $a \ge 1$。$b$：问题规模缩小的倍数，必须 $b > 1$。$f(n)$：除了递归调用之外，当前层函数所做的工作（例如分割问题、合并结果、打印、循环等）。
> 三种情况 (Three Cases)你需要计算一个关键指数： $C_{crit} = \log_b a$。然后比较 $n^{C_{crit}}$ 和 $f(n)$。
> 情况 1：叶子太沉 
> 条件： $n^{\log_b a}$ > $f(n)$ （必须是多项式级的“大于”）通俗理解：子问题分裂得太快了，最后一层的节点数量极其庞大，主要的时间都花在处理这海量的子问题上，当前层的 $f(n)$ 微不足道。结论：$$T(n) = \Theta(n^{\log_b a})$$情况 2：势均力敌
> 条件： $n^{\log_b a}$ == $f(n)$ 通俗理解：每一层递归的工作量总和都是一样的。这就好比归并排序，每一层加起来都是 $O(N)$。总时间 = 每层工作量 $\times$ 层数（$\log n$）。结论：$$T(n) = \Theta(n^{\log_b a} \cdot \log n)$$(注：如果是 $f(n) = n^{\log_b a} \log^k n$，结论就是 $\Theta(n^{\log_b a} \log^{k+1} n)$)
> 情况 3：树根太沉
> 条件： $f(n)$ > $n^{\log_b a}$  通俗理解：当前层做的工作太重了（比如递归里套了个巨慢的循环），导致递归下去的那些开销相比之下可以忽略不计。结论：$$T(n) = \Theta(f(n))$$

# 16、BST
> bst的删除操作遵循Hibbard Deletion 算法
![[Pasted image 20251126133235.png|160]]
![[Pasted image 20251126133255.png|168]]
# 17、B-Tree
- 所有叶子节点到根节点的距离必须相同。
- 一个包含 $k$ 个元素的非叶子节点，必须正好有 $k+1$ 个子节点。
# 18、红黑树
- 从根节点到任何空链接 (Null Link) 的路径上，经过的“黑色链接”数量必须相同。（黑平衡）
- 没有任何节点可以同时与两个红链接相连
![[Pasted image 20251127140515.png|500]]
![[Pasted image 20251127140454.png|500]]
![[Pasted image 20251127140427.png|500]]
# 19、Hashing
## 哈希优化
![[Pasted image 20251127144950.png|500]]
![[Pasted image 20251127144412.png|500]]
- 如果将可变的 (Mutable)对象作为 Key 放入哈希表之后调用了 setVal，对象的哈希值会改变，但它在哈希表中的位置（桶索引）不会自动更新。这导致 HashMap 再也无法找到该对象。
- 如果我们把哈希表的大小 $M$ 始终设为 2 的幂（2, 4, 8, 16...），会导致我们只利用了 HashCode 的最后几位
	- 要么让 桶长度 $M$ 为 2 的幂，乘数 $R$ 选个奇数（Java 的做法`R=31`）。(原因在于Java 已经对 hashCode 进行了“扰动函数”，把高位信息“混入”低位)
	- 要么让 桶长度 $M$ 为质数，乘数 $R$ 只要不是 $M$ 的倍数就行。

## StringBuilder, charAt和substring
```java
private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
private static final Random random = new Random();

private static String getRandomString(int len) {
	StringBuilder sb = new StringBuilder(len);          // 这里容易忘记括号里的len，导致后面要进行resize
	for (int i = 0; i < len; i++) {
		int index = random.nextInt(CHARS.length());
		sb.append(CHARS.charAt(index));             // String的charAt方法
	}
	return sb.toString();
}
```
## 其他
- 修改 key 对象的字段（比如 name、charisma）——会立刻影响 HashMap 里对应的 key 对象内容,但不会改变该 key 对象在 HashMap 中所处的桶位置
- put 时，如果 key 与桶中某个 key equal 但不是同一对象，只修改那个节点的 value，不会改变任何节点的 key 对象引用关系（HashMap 永远保持 第一次插入时的 key 引用）
- **Java 的契约：** 如果 `a.equals(b)` 为 `true`，那么 `a.hashCode()` **必须** 等于 `b.hashCode()`。
# 20、Heaps and PQs

# proj1
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
## Math.floorMod
> `int b = Math.floorMod(a, 7);`相当于int b = (((a % 7) + 7) % 7); 
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


> 用来描述算法时间复杂度的两个符号`Θ`和`O`还是有区别的, `O`指的是`小于或等于`

> `Objects.equals(Object a, Object b)`可以用来处理可能有null的比较

> `hashCode()`是每个Object都有的方法，不过一般需要重写，否则会出现内容相同的两个对象哈希值不同的情况

> Integer.MAX_VALUE是 -2,147,483,648，而它的相反数是2,147,483,648
  这个数字 超过了 int 能表示的最大值（2,147,483,647）。
>于是溢出，结果"绕回去"，变成它自己

- 数组 (Array) 使用 .length (属性)。
- 字符串 (String) 使用 .length() (方法)。
- 集合 (List/ArrayList) 必须使用 .size() (方法)。

