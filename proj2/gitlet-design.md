# Gitlet Design Document

**Name**: CS-Trekker

## Classes and Data Structures

### .gitlet目录结构
```text
.gitlet/
    blobs/
        blob1
        blob2
    commits/
        commit1
        commit2
    trees/
        tree1
        tree2
    stage/
        add/
            addFile1
            addFile2
        rm/
            rmFile1
            rmFile2
```

### Commit

#### Instance Valuables
* message - 提交信息
* timestamp - 提交的时间戳，调用构造函数时对其进行赋值
* parent - 父提交
* otherParent - 父提交最多有2个，而子提交可以有很多
* child - 子提交（Commit列表）
* tree - 引用一个Tree对象，这个Tree可能会使用哈希表来实现

#### Class Valuables

#### 实例方法
* getHash() - 返回这个Commit对象的哈希值，也是Commit被序列化保存之后的文件名
* 一个用来判断是否是“共同祖先”的方法 - 看子提交的数量有没有大于1并且这些子提交都不是“共同祖先”，这个暂时先不管，到时候做merge时再来折腾

### Blob

#### 构想

一个文件，序列化之后，根据其内容计算哈希值（与文件名/文件路径无关），然后保存在.gitlet/blobs/目录下，用哈希值命名

之后add了一个文件，先检查本地有没有这个文件；
先计算这个文件的哈希值，然后在去blobs中找到这个文件，如果有，说明之前已经被跟踪过了，直接在暂存区里

### Tree

#### 构想
Blob用来记录文件内容，但与目录结构无关

一个Commit对应一个Tree，如果有目录结构的改变（例如新建、删除文件）或者有文件内容的修改，就不是同一个Tree了

status命令时应该用HEAD_Commit的Tree与当前工作目录（结合暂存区）进行比较

#### 实例变量

* blobs - 是哈希表类型，里面的键值对是“文件名 -> Blob的哈希值”
* subtrees - 也是哈希表类型，里面的键值对是“子目录名 -> 子目录所对应的Tree的哈希值”

### 暂存区Stage

* .gitlet/stage
* 如果add中已经有了一样的blob，有可能是相同内容不同名的文件先后被add，也可能是同一个文件之前已经被add过了
* HEAD(commit) -> HEAD(tree) -> 


## Algorithms
* 执行java gitlet.Main commit命令之后，先检查暂存区的add区和rm区有没有东西，如果有，先复制一份父commit，然后根据两个区对父commit的文件状态那一栏进行修改
* 执行java gitlet.Main rm <File> 命令后，如果这个文件在add区，则从add区中移除该文件；如果这个文件在HEAD commit的跟踪列表中，则在rm区添加该文件，否则直接从工作目录中删除该文件
* 执行java gitlet.Main checkout <commit-id>命令之后，应该先检查有没有未被stage的修改、新建文件、删除文件，以防发生checkout后这些新改动丢失，确认没有后，删除所有文件和目录，然后根据这个commit记录了的——有哪些文件，每个文件对应特定哈希值的Blob在哪里，根据Blob一一复原这些文件（先根据path/文件名创建空文件，然后把内容写入文件
* 执行java gitlet.Main checkout -- <File-name>之后?

## Persistence
* 创建一个新提交之后就应该立刻将其保存进.gitlet/commits文件夹之下，以“对象-序列化”的形式保存，以哈希值命名这个文件
* 暂存区的结构设置应该与Commit中用来记录自己记录了哪些文件，每个文件指向Blob的位置，应该要保持一致

## 内容寻址
* 对于文件，应该结合文件路径、文件名、文件具体的内容三个要素进行哈希，序列化之后的文件就是Blob，文件和Blob的哈希值应该是一样的
* 对于Commit，结合message、时间戳、记录的文件列表和相对应的版本，父提交、second父提交5个要素来计算哈希值