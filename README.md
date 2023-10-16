# 项目介绍

区别于传统的BI，用户（数据分析者）只需要导入最原始的数据集，输入想要进行分析的目标（比如输入，帮我分析一下网站的增长趋势），就能利用AI自动生成一个符合要求的图标以及结论

**优点：**让不会数据分析的同学也能通过输入目标快速完成数据分析，大幅节约人力成本

​		    会用到AI接口

## 需求分析

- 智能分析：用户目标和原始数据（图标类型），可以自动生成图标和分析结论
- 图表管理
- 图表生成的异步化（消息队列）
- 对接AI能力

## 项目架构

![image-20231002135847531](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231002135847531.png)

**优化：**

![image-20231002140017345](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231002140017345.png)

## 技术栈
### 后端

- Spring Boot
- MySQL数据库
- MyBatis Plus数据库访问框架
- 消息队列（RabbitMQ）
- AI能力（Open AI 接口开发/现成AI接口）
- Excel的上传和数据的解析（Easy Excel）
- Swagger + Knife4j项目接口文档
- Hutool工具库

# 前端

## 前端项目初始化

- 安装node.js 版本最好大于16.14.0

- 进入Ant design Pro 查看初始化流程

- 确定要存放的目录

- 进入目录 执行cmd

运行下面命令

![image-20231002155623946](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231002155623946.png)

![image-20231002155707608](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231002155707608.png)

### 报错解决

#### 删除国际化

https://github.com/ant-design/ant-design-pro/issues/10452

![image-20231002161147585](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231002161147585.png)

移除国际化后菜单没有了，在路由配置中写入name

![image-20231002161804749](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231002161804749.png)

## 修改注册按钮

```react
submitter={
  {
    searchConfig: {
      submitText: '注册'
    }
  }}
```

**未登录可以进入注册页面**

![image-20231003182243854](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231003182243854.png)

# 后端

## 数据库表设计

- 用户表

```mysql
-- 创建库
create database if not exists BI;

-- 切换库
use BI;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    phone        varchar(11)                            comment '手机号',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userAccount (userAccount)
) comment '用户' collate = utf8mb4_unicode_ci;

```

- 图表信息表

```mysql
-- 图表信息表
create table if not exists chart
(
    id           bigint auto_increment comment 'id' primary key,
    userId       bigint  null  comment '用户id',
    goal  		 text	null   comment '分析目标',
    chartData    text   null   comment '图表数据',
    `name`       varchar(128)   null comment '图表名称',
    chartType    varchar(128)   comment '图标类型',
    genChart	 text   null   comment '生成的图表数据',
    genResult	 text   null   comment '生成的分析结论',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
) comment '用户' collate = utf8mb4_unicode_ci;
```

## 后端初始化

使用自定义SpringBoot项目开发模板+mybatisX插件自动生成基础CRUD代码模板

## 使用AI生成BI图表流程

…………

## 功能

### 登录注册

### 智能分析功能

**业务流程**

1. 用户输入
   1. 分析目标
   2. 上传原始数据
   3. 更精细的控制图标
2. 后端校验
   1. 校验用户输入是否合法
   2. 成本控制（次数统计和校验，鉴权等）
3. 把处理后的数据输入个给AI模型（调用AI接口）让AI模型给我们提供图表信息、结论文本
4. 图表信息、结论文本在前端进行展示

#### 文件上传（前后端）

![image-20231016222912984](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231016222912984.png)

文件上传后，使用easyExcel读取处理文件

#### Excel处理

- 因为AI对话模型能处理的文本长度很有限，只有3-4k个Token，所以我们要先将数据压缩，将XLSX文件转换为CSV，

使用阿里的`EasyExcel库`来读取`Excel`文件

**这段代码使用 EasyExcel 库读取一个 excel 文件，并指定了 excel 文件类型为 `XLSX`，然后读取第一个 sheet，指定了表头所在行号为 0，最后进行同步读取。**

> - `EasyExcel.read(file)`：使用 EasyExcel 的静态方法 `read()` 读取指定的 excel 文件。其中 `file` 表示要读取的 excel 文件对象。
> - `.excelType(ExcelTypeEnum.XLSX)`：指定要读取的文件的格式，这里使用的是 `XLSX` 格式
> - `.sheet()`：表示读取该文件的第一个 sheet，如果想要读取其他 sheet，可以在括号中指定 sheet 的索引号或名称，例如 `.sheet(1)` 或 `.sheet("Sheet2")`。
> - `.headRowNumber(0)`：指定表头所在的行号，这里为 0 表示第一行是表头。
> - `.doReadSync()`：执行同步读取操作，读取 excel 文件中的所有数据并返回结果

```java
/**
 * excel相关工具类
 *
 * @author Han
 * @data 2023/10/3
 * @apiNode
 */
public class ExcelUtils {
    public static String excelToCsv(MultipartFile multipartFile) {
        File file = null;
        try {
            file = ResourceUtils.getFile("classpath:网站数据.xlsx");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<Map<Integer,String>> list = EasyExcel.read(file)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet()
                .headRowNumber(0)
                .doReadSync();
        System.out.println(list);
        return "";
    }

    public static void main(String[] args) {
        excelToCsv(null);
    }
}
```

测试结果

![image-20231003211821769](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231003211821769.png)

**exel文档数据压缩逻辑**

![image-20231006220829926](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231006220829926.png)

### AI调用

**BI模型id:**`1659171950288818178L`

#### 生成AI模型

**给AI设置预设**

- `你是一个数据分析师，接下来我会给你我的原始数据和分析目标，请告诉我你的分析结论`
- 作用：提高AI的回答准确率

![image-20231004133252556](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231004133252556.png)

- **AI角色设定**

```tex
你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：
分析需求：
{数据分析的需求或者目标}
原始数据：
{csv格式的原始数据，用,作为分隔符}
请根据以上内容，帮我生成数据分析结论和可视化图标代码
```

- **Prompt预设**

```text

你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：
分析需求：
{数据分析的需求或者目标}
原始数据：
{csv格式的原始数据，用,作为分隔符}
请根据这两部分内容，按照以下指定格式生成内容（此外不要输入任何多余的开头、
结尾、注释）
【【【【【
{前端Echart V5 的 option 配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}
【【【【【
{明确的数据分析结论，越详细越好，不要生成多余注释}

```

**设置示例回答**

- **用户提问**

```text

分析需求：
分析网站用户的增长情况
原始数据：
日期,用户数
1号,10
2号,20
3号,30
```

- **AI生成结果**

```text

根据您提供的数据，以下是对用户增长情况的分析和可视化结果：

【【【【【
// Echart V5 配置对象代码：

option = {
xAxis: {
type: 'category',
data: ['1号', '2号', '3号']
},
yAxis: {
type: 'value'
},
series: [{
data: [10, 20, 5],
type: 'line'
}]
};

【【【【【

从可视化结果来看，在这三天的时间里，网站的用户数呈现起伏的趋势。首先，用户数从10人增长到20人，但在第三天突然下降到了5人。这种不稳定的用户增长情况可能需要引起团队的关注。建议对网站的用户流失原因进行分析，并采取相应的措施来稳定和提升用户数量。
```

**生成图表**

AI无法直接生成图表，但是AI可以生成图标表代码，可以把代码利用前端组件库在网页进行展示

预期生成的图表代码

```javascript
option = {
	xAxis: {
		type: 'category',
		data: ['1号', '2号', '3号']
	},
	yAxis: {
		type: 'value'
	},
	series: [{
		data: [10, 20, 5],
		type: 'line'
	}]
};
```

#### 调用AI模型

**使用鱼聪明AI模型**

- 导入SDK

```xml
<!--鱼聪明SDK-->
<dependency>
    <groupId>com.yucongming</groupId>
    <artifactId>yucongming-java-sdk</artifactId>
    <version>0.0.3</version>
</dependency>
```

- 配置秘钥

![image-20231006162415752](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231006162415752.png)

- 连接AI模型

![image-20231006163101158](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231006163101158.png)

- 测试

![image-20231006163219155](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231006163219155.png)

### 智能生成接口实现（核心）

#### 用户数据处理

> **示例输入**
>
> 分析需求：
> 分析网站用户的增长情况
> 原始数据：【注意，原始数据是从用户传入的文件中使用EasyExcel取出的】
> 日期,用户数
> 1号,10
> 2号,20
> 3号,30

![image-20231006161611859](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231006161611859.png)

#### 完整业务流程

- 获取用户的输入信息
- 校验输入信息的合法性
- 拼接用户输入的内容
- 解析用户上传的文件
- 调用BI模型
- 处理AI返回的数据
- 将数据保存在数据库中
- 返回数据

![image-20231006220123800](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231006220123800.png)



### 图表管理

CRUD，查看自己发布过的图表信息，从数据库中查询

# 项目优化

## 分析结论异步化

**修改库表**

添加异步化图表生成时的状态以及执行信息

```sql
 status varchar(128) not null  default 'wait' comment 'wait running succeed failed',
 execMessage text null comment '执行信息',
```

 异步化使用线程池技术实现

- 生成前

![image-20231009222249000](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231009222249000.png)

- 调用AI执行中

![image-20231009222146799](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231009222146799.png)

- 生成结论后

![image-20231010134118148](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231010134118148.png)

## 消息队列

**核心思想就是使用消息队列替代线程池来实现任务的异步处理**

**使用消息队列异步处理任务消息**

- 首先，创建项目的消息队列以及消息使用的交换机

![image-20231016225037993](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231016225037993.png)

- 构建任务消息生产者

![image-20231016221318803](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231016221318803.png)

- 构建消息消费者

**在消费者中，调用AI服务，进行任务处理，将用户的诉求在这里分析实现**

同时在chartServiceImpl中调用发送者方法

```java
// todo mq
biProducer.sendMessage(String.valueOf(chart.getId()));
```

生产者发送保存到数据库中的Chart对象的id，发送给消费者，在消费者中从数据库中取出chart对象，修改chart的状态、分析结论等信息。

![image-20231016221406705](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231016221406705.png)

- 封装用户输入

因为生产者给消费者传的是已经保存过用户目标，分析的原始数据等数据并保存到数据库中的图表对象了，所以可以传入一个chart对象从这个对象中获取用户输入信息的信息，这个chart对象是根据生产者发送的chartid 从数据库中获取的

![image-20231016222125744](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231016222125744.png)

## 安全问题

用户上传文件类型

请求频率

使用Redisson实现分布式限流

- 使用RateLimiter限流器，限制接口的被调用频率

在调用AI模型的方法中执行这个限流器，限制模型的调用频率

![image-20231010134637494](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231010134637494.png)



![image-20231010134438374](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231010134438374.png)

## 效率

 可使用redis存储生成的图标信息到缓存，提好用户查询图表信息的效率

# 项目遇到的问题

- 后端启动项目端口冲突问题

原因：Windows Hyper-V虚拟化平台占用了端口

先使用: ![image-20231003144006732](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231003144006732.png)命令查看被占用的端口，然后选择一个未被占用的端口启动项目

![image-20231003144105282](https://gitee.com/hyp02/typora_lmage/raw/master/img/image-20231003144105282.png)



# 项目收获

学习到了如何调用Ai接口，从而让自己的项目有AI能力

利用消息队列实现任务的异步化处理，任务队列的持久化，

使用redissonLimiter进行限流

使用EasyExcel来处理文件数据，









