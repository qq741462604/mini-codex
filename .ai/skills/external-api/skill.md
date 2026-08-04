# External API Client Skill


Keywords:

接口
外调
HTTP
REST
RestTemplate
调用接口


Target:

class: DataPrepEventHandler

method: handle


# Rules


## 1. Target Protection Rule


修改Target类之前:

必须执行:

read_file读取Target完整文件。


禁止:

- 创建新的Target类
- 复制Target模板
- 重写Target文件
- 替换package
- 替换import
- 替换class定义
- 替换extends
- 替换implements
- 修改init方法
- 修改handle方法签名


Target只能使用patch方式修改。


Target修改必须使用:

oldText + newText


oldText规则:

- 必须来自read_file返回内容
- 必须包含真实源码
- 禁止为空


newText规则:

只能:

原oldText代码

+

新增业务调用代码


禁止:

- newText包含完整Target类
- newText重新生成package
- newText重新生成import
- newText重新生成class


---

## 2. Target Modification Scope


Target.handle只能增加:


1. 获取业务字段


2. 调用Client


3. eventData.putDataAndOriginData()


允许位置:


必须找到:


```java
EventData eventData =
(EventData)context.getUserData()
.get(ContextKeyConstant.EVENT_DATA);


Map<String,Object> eventFields =
eventData.getData();
```

只能在:

eventData.getData()

之后增加代码。

禁止:

- 移动已有代码
- 删除已有代码
- 修改try-catch结构
- 修改日志
- 修改return流程

------

## 3. Existing Code Protection

修改Target必须保留:

- package
- import
- class
- extends
- implements
- init方法
- handle方法签名
- 已有字段
- 已有日志
- 已有异常处理

禁止:

- 删除字段
- 删除逻辑
- 修改父类
- 修改方法参数
- 修改方法返回类型

------

# Architecture

必须生成:

```
application.yml

        |
        v

UserApiConfig

        |
        v

UserApiClient

        |
        v

DataPrepEventHandler.handle
```

调用链必须保持:

```
配置
 ↓
ApiConfig
 ↓
ApiClient
 ↓
Target
```

禁止:

```
Target
 ↓
HTTP
```

------

# Implementation

必须严格执行:

## Step1

read_file Target

禁止:

未读取Target直接修改。

------

## Step2

分析项目HTTP能力。

必须search_code:

```
RestTemplate

@Configuration

@Bean

RestTemplateBuilder
```

检查:

是否已经存在RestTemplate Bean。

------

## Step3

创建application.yml配置。

如果存在application.yml:

必须修改已有文件。

禁止:

创建Java配置替代。

格式:

```
external:
  user-api:
    url: xxx
    timeout: 3000
    method: GET
```

------

## Step4

创建ApiConfig。

命名规则:

必须:

业务名称 + ApiConfig

例如:

```
UserApiConfig
```

禁止:

```
ApiConfig
Config
HttpConfig
ExternalApiConfig
```

------

## Step5

创建Client。

必须:

Client独立文件。

------

## Step6

如果不存在RestTemplate Bean:

创建:

```
RestTemplateConfig
```

否则禁止创建。

------

## Step7

patch修改Target.handle。

必须使用:

oldText + newText

------

# Config Rule

## ApiConfig职责

ApiConfig只负责业务接口配置。

必须:

```
@Component
@ConfigurationProperties(prefix="external.user-api")
public class UserApiConfig {

    private String url;

    private Integer timeout;

    private String method;

}
```

必须包含:

- url
- timeout
- method

禁止:

- @Bean
- RestTemplate
- RestTemplateBuilder
- HTTP客户端创建

禁止:

```
@Configuration
public class UserApiConfig
```

------

# Http Config Rule

Spring基础设施配置:

只能:

```
RestTemplateConfig
```

例如:

```
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
```

禁止:

```
UserApiConfig
ExternalApiConfig
UserHttpConfig
```

创建RestTemplate Bean。

------

# Application Config Rule

接口配置必须来自:

application.yml

禁止:

- Java保存URL
- Java保存timeout
- static final保存配置
- Client保存接口地址

禁止:

```
private static final String URL
```

禁止:

```
private static final int TIMEOUT
```

------

# Client Rule

HTTP客户端优先级:

1. RestTemplate
2. WebClient
3. 项目已有HTTP封装
4. HttpURLConnection

禁止默认生成:

HttpURLConnection

------

如果使用RestTemplate:

Client必须结构:

```
@Component
@RequiredArgsConstructor
public class XxxApiClient {


    private final RestTemplate restTemplate;


    private final XxxApiConfig config;


}
```

禁止:

```
new RestTemplate()
```

禁止:

方法内部创建RestTemplate。

禁止:

```
static RestTemplate
```

------

# Client Config Binding Rule

Client必须依赖:

```
private final XxxApiConfig config;
```

例如:

```
private final UserApiConfig config;
```

禁止:

```
@Value("${xxx.url}")
```

禁止:

```
@Value("${xxx.timeout}")
```

禁止:

默认URL。

禁止:

Client出现:

```
http://
https://
```

------

# DTO Rule

接口返回JSON:

必须优先创建DTO。

例如:

```
UserResponseDTO
```

Client返回:

DTO

禁止:

```
String queryUserName()
```

禁止:

Client解析业务字段。

禁止:

```
Map<String,Object>
```

解析业务响应。

------

# Client Responsibility

Client负责:

1. 参数组装
2. HTTP请求
3. DTO解析

Target负责:

1. 获取字段
2. 调Client
3. putDataAndOriginData

禁止:

Client直接修改:

```
eventData
```

------

# Dynamic Field Rule

用户任务字段:

只是动态映射。

禁止固定:

```
userId
customerName
name
```

禁止生成固定方法:

```
queryUserName()
```

方法名称必须体现接口能力。

例如:

```
queryUserInfo()

getUserDetail()

fetchUser()
```

------

# Dependency Rule

如果Target需要:

```
UserApiClient
```

必须先创建:

```
UserApiClient.java
```

禁止:

先修改Target

后创建Client。

------

# Multi File Write Order

如果任务需要:

```
Config
Client
Target
```

必须:

```
write_file(Config)

完成

↓

write_file(Client)

完成

↓

write_file(Target patch)
```

禁止:

Target先写入。

------

# Target Write Rule

Target write_file:

path:

必须来自read_file。

input必须:

```
oldText
newText
```

禁止:

```
create Target
```

禁止:

```
oldText=""
```

------

# Skill Completion Check

结束任务前必须确认:

```
[ ] Target read_file成功

[ ] application.yml存在接口配置

[ ] ApiConfig存在

[ ] Client存在

[ ] DTO存在(如果需要)

[ ] RestTemplate检查完成

[ ] Target只patch修改

[ ] Target调用Client

[ ] Target使用putDataAndOriginData

[ ] 没有HTTP代码进入Target
```

任何一项失败:

禁止结束任务。

------

# Generated Code Verify

如果发现:

```
@Value("${
```

失败。

如果发现:

```
@Bean
```

并且类名包含:

```
ApiConfig
```

失败。

如果发现Client包含:

```
http://
https://
```

失败。

如果发现:

```
new RestTemplate()
```

失败。

如果发现:

```
HttpURLConnection
```

默认生成:

失败。

如果Client没有:

```
private final XxxApiConfig config;
```

失败。

------

# Forbidden

- rewrite Target
- create Target
- replace Target
- copy Target template
- modify Target signature
- modify Target parent class
- delete Target fields
- delete Target logic
- move Target code
- HTTP code in handle
- URL hardcode
- timeout hardcode
- new RestTemplate()
- HttpURLConnection default generation
- JSON parsing in Target
- Response parsing in Target
- Client直接put eventData
- static接口配置
- ApiConfig创建RestTemplate
- Target引用不存在类
- Target write before Client
- Target write before Config
- create Controller