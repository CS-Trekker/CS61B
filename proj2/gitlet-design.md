# Gitlet Design Document

**Name**: CS-Trekker

## Classes and Data Structures

### Commit

#### Instance Valuables
* Message - 提交信息
* Timestamp - 提交的时间戳，调用构造函数时对其进行赋值
* Parent - 父提交
* OtherParent - 父提交最多有2个，而子提交可以有很多
* Child - 子提交（Commit列表）
* uid - 计算得到的哈希值？ - 可以用来作为Commit作为文件保存时的文件名
* ??? - 记录这个Commit状态下本地目录有哪些文件，然后这些文件都有一个指针或者作为一个指针指向某个Blob，代表这个文件在当前状态下应该是这个Blob

#### Class Valuables
* COMMIT_DIR - commit对象存放的目录(.gitlet/commits)?

#### 实例方法
* 一个用来判断是否是“共同祖先”的方法 - 看子提交的数量有没有大于1并且这些子提交都不是“共同祖先”


### Blob

#### 构想

在.gitlet之下放一个blobs目录，然后下面是很多目录

我想做成是很多个目录，每个文件（路径）分配一个目录，每个文件在第一次创建的时候，把里面的内容和文件名一起序列化之后，作为一个“序列化文件”保存在对应的目录下，目录名以这个真实文件的路径来命名，序列化文件的名称是哈希值，这里的哈希值只与文件的实际内容有关，两个相同内容的文件，就算文件名不同，通过sha-1计算出来的哈希值是一样的

之后add了一个文件，先检查本地有没有这个文件；
先计算这个文件的哈希值，然后在去blobs中找到这个文件，如果有，说明之前已经被跟踪过了，直接在暂存区里

#### 实例变量

### 暂存区

* 分为add区和rm区，


## Algorithms
* 执行java gitlet.Main commit命令之后，先检查暂存区的add区和rm区有没有东西，如果有，先复制一份父commit，然后根据两个区对父commit的文件状态那一栏进行修改
* 执行java gitlet.Main checkout <commit-id>命令之后，应该先检查有没有未被stage的修改、新建文件、删除文件，以防发生checkout后这些新改动丢失，确认没有后，删除所有文件和目录，然后根据这个commit记录了的——有哪些文件，每个文件对应特定哈希值的Blob在哪里，根据Blob一一复原这些文件（先根据path/文件名创建空文件，然后把内容写入文件
* 执行java gitlet.Main rm <File> 命令后，如果这个文件在add区，则从add区中移除该文件；如果这个文件在HEAD commit的跟踪列表中，则在rm区添加该文件，否则直接从工作目录中删除该文件
* 执行java gitlet.Main checkout -- <File-name>之后?
---
* 需要写一个方法还是什么的，用来递归遍历当前CWD目录之下有哪些文件，以File类型的
列表输出，这个方法可以用在：和HEAD commit对比，看当前工作区增加、删除了什么文件，对于那些没有增加和删除的，可以计算Blob新的哈希值，与HEAD commit中指向的进行对比，


## Persistence
* 创建一个新提交之后就应该立刻将其保存进.gitlet/commits文件夹之下，以“对象-序列化”的形式保存，以哈希值命名这个文件
* 暂存区的结构设置应该与Commit中用来记录自己记录了哪些文件，每个文件指向Blob的位置，应该要保持一致

## 内容寻址
* 对于文件，应该结合文件路径、文件名、文件具体的内容三个要素进行哈希，序列化之后的文件就是Blob，文件和Blob的哈希值应该是一样的
* 对于Commit，结合message、时间戳、记录的文件列表和相对应的版本，父提交、second父提交5个要素来计算哈希值