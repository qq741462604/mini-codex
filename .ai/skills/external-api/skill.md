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


Rules:


1. Target保护规则


修改Target之前:

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


Target修改必须:

使用:

oldText + newText


其中:

oldText:

必须来自read_file返回内容。


newText:

只能包含:

原oldText代码

+

新增调用代码



禁止:

oldText为空。


禁止:

newText包含完整DataPrepEventHandler类。



2. Target业务修改规则


Target.handle只能增加:

1. 获取业务字段

2. 调用Client

3. eventData.putDataAndOriginData()


禁止:

handle出现:

- HTTP代码
- URL
- timeout
- RestTemplate创建
- JSON解析
- Response解析


禁止:

直接调用:

HttpURLConnection

禁止:

new URL()

禁止:

new RestTemplate()



3. Existing Code Protection


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
- 已有try-catch结构
- 已有return流程


禁止:

删除已有逻辑。


禁止:

移动已有代码。



Architecture:


必须生成:


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



调用链必须保持:


配置
↓
Config
↓
Client
↓
Target



4. Implementation执行顺序


必须严格:


Step1:

read_file Target


Step2:

分析项目是否存在:

RestTemplate Bean


必须执行search_code:


RestTemplate

@Configuration

@Bean

RestTemplateBuilder



Step3:

修改application.yml


必须先有:


external.user-api


配置。



Step4:

创建:


UserApiConfig.java



UserApiConfig必须绑定application.yml。


禁止自行生成默认值。


Step5:

创建Client


Step6:

如果需要:

创建RestTemplateConfig


Step7:

patch修改Target.handle



禁止:

跳过read_file。


禁止:

未创建Client直接修改Target。


禁止:

Target引用不存在类。



5. Multi File Write Order


如果任务需要:


create Config

create Client

modify Target


必须:


write_file(Config)


完成


↓


write_file(Client)


完成


↓


write_file(Target patch)



禁止:

Target修改早于Client创建。


禁止:

Target修改早于Config创建。



6. Config Rule


接口配置必须来自:

application.yml


禁止:


- Java代码保存URL
- Java代码保存timeout
- static final保存接口配置
- Client保存接口地址



配置类必须命名:


业务名称 + ApiConfig


例如:


UserApiConfig



禁止:


ApiConfig

HttpConfig

Config



ApiConfig必须:


@Component

@ConfigurationProperties(prefix="external.xxx")



示例:


@Component
@ConfigurationProperties(prefix="external.user-api")
public class UserApiConfig {

    private String url;
    
    private Integer timeout;
    
    private String method;

}



禁止:


@Configuration
public class UserApiConfig



禁止:

ApiConfig创建RestTemplate Bean。

========================
Config Responsibility Lock
========================


Config分两种:

1. ApiConfig

2. HttpConfig



ApiConfig:

负责:

接口业务配置


例如:

UserApiConfig


只能包含:


url

timeout

method


禁止:


@Bean

@Configuration

RestTemplate

RestTemplateBuilder



ApiConfig禁止创建任何Spring基础设施Bean。



例如禁止:


public class UserApiConfig {


    @Bean
    public RestTemplate xxx()

}



========================


HttpConfig:


负责:

Spring HTTP客户端Bean


例如:


RestTemplateConfig


只能包含:


@Bean
RestTemplate


禁止:

url

timeout

method

业务接口名称



禁止:


UserApiConfig创建RestTemplate。

禁止:

ExternalApiConfig创建RestTemplate。



如果创建RestTemplate:

只能创建:


RestTemplateConfig.java



禁止:

xxxApiConfig.java

xxxConfig.java

ExternalApiConfig.java



7. Application Config Rule


如果项目存在:

application.yml


必须修改已有application.yml。


禁止:

创建Java配置替代application.yml。



格式:


external:

  user-api:

    url: xxx
    
    timeout: 3000
    
    method: GET



8. RestTemplate Rule


HTTP客户端优先级:


1. RestTemplate

2. WebClient

3. 项目已有HTTP封装

4. JDK HttpURLConnection



禁止默认生成:

HttpURLConnection



如果使用RestTemplate:


Client必须:


@Component

@RequiredArgsConstructor

public class XxxApiClient {


    private final RestTemplate restTemplate;


    private final XxxApiConfig config;


}



禁止:


RestTemplate restTemplate = new RestTemplate();



禁止:


方法内部创建RestTemplate。



如果项目不存在RestTemplate Bean:


只有search_code确认不存在后:


允许创建:


RestTemplateConfig



RestTemplateConfig只能:


创建Bean



例如:


@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(){
    
        return new RestTemplate();
    
    }

}



禁止:

RestTemplateConfig保存业务接口配置。



========================
Client Config Binding Lock
========================


Client必须依赖:

XxxApiConfig


例如:


private final UserApiConfig config;



禁止:


@Value读取接口配置


禁止:


${xxx.url}


禁止:


默认URL


例如禁止:


@Value("${user.api.url:http://localhost:8081/user}")



Client禁止出现:


http://

https://



Client获取地址只能:


config.getUrl()



Client获取timeout只能:


config.getTimeout()

========================
RestTemplate Bean Rule
========================


整个任务最多只能创建一个RestTemplate Bean。


如果项目已有:

RestTemplate Bean


禁止创建。


如果不存在:


只能创建:


RestTemplateConfig



禁止:

ExternalApiConfig

UserApiConfig

UserHttpConfig


创建RestTemplate Bean。

10. DTO Rule


接口返回JSON:

必须优先生成DTO。


例如:


UserResponseDTO


禁止:

Map解析业务返回。



Client返回:


DTO



禁止:


String queryUserName(String userId)



禁止固定业务方法:


queryUserName



方法名称必须根据接口能力生成。


例如:


queryUserInfo

getUserDetail

fetchUser



11. Dynamic Field Rule


用户任务中的字段:

只是接口映射。


禁止固定:


userId

customerName

name



必须根据任务生成字段映射。



12. Target Dependency Rule


如果Target需要:


UserApiClient


必须确认:

UserApiClient.java

已经创建成功。


禁止:


先修改Target

后创建Client。



13. Skill Completion Check


任务完成前必须确认:


[ ] Target read_file成功

[ ] Config存在

[ ] application.yml存在配置

[ ] Client存在

[ ] DTO存在(如果需要)

[ ] Target只patch修改

[ ] Target调用Client

[ ] Target使用putDataAndOriginData



如果任何条件失败:


禁止结束任务。

========================
Generated Code Verify
========================


如果生成代码出现:


@Value("${")


判定失败。


如果生成代码出现:


@Bean


并且类名包含:


ApiConfig


判定失败。


如果生成代码出现:


http://


并且文件不是:


application.yml


判定失败。


如果Client没有:


private final XxxApiConfig

判定失败。



Implementation:


必须严格执行:


Step1:

read_file Target


Step2:

search_code确认RestTemplate能力


Step3:

create application.yml配置


Step4:

create XxxApiConfig


Step5:

create XxxApiClient


Step6:

create DTO


Step7:

modify Target.handle



禁止跳过步骤。



Forbidden:


- rewrite Target
- create Target
- replace Target
- copy Target template
- modify Target signature
- modify Target parent class
- delete Target fields
- delete Target logic
- move Target code
- create Controller
- HTTP code in handle
- hardcode URL
- hardcode timeout
- new RestTemplate()
- HttpURLConnection默认生成
- JSON解析放Target
- Client直接put eventData
- static接口配置
- ApiConfig保存RestTemplate
- Target引用不存在类
- write Target before Client
- write Target before Config

