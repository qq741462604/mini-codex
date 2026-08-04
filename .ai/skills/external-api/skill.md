

# External API Client Skill


Keywords:

接口
外调
HTTP


Target:

class: DataPrepEventHandler

method: handle

Rules:

Target Modification Lock


修改Target类:

必须:

1. read_file获取完整文件

2. 保留:

- package
- import
- class
- extends
- implements
- init
- handle签名


只允许:

在:

Map<String,Object> eventFields =
eventData.getData();

之后增加:

字段获取
Client调用
eventData.putDataAndOriginData


禁止:

- 重写handle
- 删除已有逻辑
- 移动已有代码
- 修改try-catch
- 修改日志



修改Target类时:

必须:

1. read_file获取完整文件。

2. 保留:

- package

- import

- class定义

- extends关系

- implements关系

- init方法

- handle方法签名

  

Target修改规则:


Target文件:

必须使用:

oldText + newText


禁止:

create模式修改Target。


只有以下文件允许create:

- Client
- Config
- DTO
- RestTemplateConfig

Target Rewrite Forbidden:


如果目标文件已经存在:

write_file只能使用:

oldText + newText


newText只能包含:

原oldText代码
+
新增调用代码


禁止:

newText包含完整DataPrepEventHandler类。



Architecture:

application.yml

↓

UserApiConfig

↓

UserApiClient

↓

DataPrepEventHandler.handle

Client Return Rule:

Client禁止直接返回业务字段值。

例如:

禁止:

String queryUserName(String userId)


必须:

返回接口响应DTO。

例如:

UserResponseDTO

由Target决定:

哪个字段写入eventData。



RestTemplate Detection Rule:

进入CODING之前:

必须在ANALYSIS阶段:

search_code:

RestTemplate
@Configuration
@Bean


如果发现:

RestTemplate Bean:

禁止创建RestTemplateConfig。


如果没有:

才允许create RestTemplateConfig。



ApiConfig Rule:

配置类必须:

@Component
@ConfigurationProperties(prefix="external.xxx")


禁止:

@Configuration
public class XxxApiConfig

Config Naming Rule:


必须:

业务名称 + ApiConfig


例如:

UserApiConfig


禁止:

ApiConfig

Config



1. 外调代码必须封装Client。

2. handle只能负责:
- 获取字段
- 调用Client
- putDataAndOriginData


3. 允许新增:
- Config
- Client
- DTO
- RestTemplateConfig

Implementation:

执行顺序:


Step1:

必须先读取Target类完整代码。


Step2:

创建接口配置。


Step3:

创建Client。


Step4:

修改Target.handle。

禁止跳过已有代码读取。




必须:

找到:

EventData eventData =
(EventData)context.getUserData()
.get(ContextKeyConstant.EVENT_DATA);


Map<String,Object> eventFields =
eventData.getData();


在该位置之后增加调用。


禁止:

替换handle方法。

Target Write Rule:


Target修改不是创建文件。


Target write_file:

path必须来自read_file返回路径。


input必须:

oldText:
来自read_file原文


newText:
基于oldText增加代码


禁止:

oldText为空字符串

禁止:

newText包含完整class


Config Rule:

接口配置必须来自application.yml。

禁止:

- Client写死URL
- Client写死timeout
- static final保存配置

需要两个不同配置时:

接口参数配置:

xxxApiConfig.java


Spring Bean配置:

xxxHttpConfig.java


两者禁止混淆。


xxxApiConfig:

必须@ConfigurationProperties


xxxHttpConfig:

只能创建Bean。


禁止:

用xxxApiConfig创建RestTemplate Bean。


Client Rule:

优先:

RestTemplate

如果Client使用RestTemplate:


代码结构必须严格如下:

@Component
@RequiredArgsConstructor
public class XxxClient {


    private final RestTemplate restTemplate;


    private final XxxApiConfig config;


}


禁止出现:

RestTemplate restTemplate =

new RestTemplate();


禁止:

方法内部创建RestTemplate;


禁止:

static RestTemplate;

检查RestTemplate Bean:

必须搜索:

RestTemplate

@Configuration

@Bean

RestTemplateBuilder


只有全部确认不存在:

才允许创建RestTemplateConfig




禁止在CODING阶段临时搜索。

只有确认不存在后:

才允许创建RestTemplateConfig。


禁止默认创建。


Handle Rule:

禁止HTTP代码进入handle。

handle只能:

1. 获取请求参数
2. 调Client
3. eventData.putDataAndOriginData

Output:


新增文件:

必须输出完整文件。


已有Target:

禁止输出完整重写文件。

Target只能输出增量修改:
oldText + newText

禁止:

只引用不存在的Client。

Dynamic API Field Rule:


用户任务中的字段:

只是接口映射参数。


必须根据User Task生成。


禁止:

固定使用:

userId
customerName
name


禁止:

创建固定业务方法:

queryUserName()


Client方法应该根据接口能力命名。

New File Dependency Rule:


如果Target修改需要引用新增类:


必须先创建对应文件。


例如:

Target引用:

UserApiClient


必须存在:

UserApiClient.java


禁止:

先修改Target

后创建依赖类。

Write Order Lock:


如果同一个任务存在:

create Client
create Config
modify Target


必须严格:

write_file(Client)

完成

↓

write_file(Config)

完成

↓

write_file(Target)


禁止:

Target write_file 出现在 Client/Config 前。

Multi File Rule:


如果Implementation要求新增多个文件:

必须生成多个write_file。


顺序:

1. Config
2. Client
3. Target修改

Application Config Rule:


如果项目存在application.yml:

必须修改application.yml。


禁止:

创建:

ApiConfig.java

保存url


禁止:

创建:

public class ApiConfig {
 static String url;
}


application.yml示例:

external:
  user-api:
    url: xxx
    timeout: 3000
    method: GET

Skill Completion Check:


只有满足以下全部条件:

1. Config存在
2. Client存在
3. DTO存在(如果需要)
4. Target.handle已经调用Client
5. Target已经调用putDataAndOriginData


才允许结束任务。


如果Client存在但是Target未调用:

继续生成Target修改。

Target Dependency Rule:


如果Target类不是Spring Bean:

禁止使用@Autowired注入。


必须使用项目已有方式获取Client。


如果无法确认:

不得修改类定义增加@Component。

Client Responsibility:


Client负责:

1. 请求发送
2. 参数组装
3. 返回解析


Target禁止:

解析HTTP返回JSON。

Final Execution Check:


生成Plan前必须检查:

[ ] Target是否read_file成功
[ ] 是否存在新增Client
[ ] 是否存在新增Config
[ ] Target是否只修改handle内部
[ ] 是否保留原package/import/class
[ ] 是否没有new RestTemplate
[ ] 是否没有硬编码URL
[ ] 是否没有创建Controller


如果任意一项失败:

禁止生成write_file。

Forbidden:

- 重写整个Target类
- 创建新的Target类模板
- Target引用未创建类。
- 修改父类
- 修改方法签名
- 删除已有字段
- 删除已有逻辑

Before Generate Plan:

必须检查:

Target read_file:
YES

Target path:
EXISTING

If write_file target:

必须:

oldText != ""

否则:

禁止生成该step。

如果发现:

create Target

立即停止。