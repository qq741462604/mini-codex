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

type: existing


Rules:


Execution Priority:


本Skill规则优先级高于普通编码规则。


如果以下规则冲突:

write_file

patch_file


必须选择:

patch_file


如果以下规则冲突:

重写类

增量修改


必须选择:

增量修改。


违反Target保护规则:

禁止生成Plan。

Target Patch Absolute Rule:

patch_file修改Target时:

newText禁止重新生成整个文件。

newText必须只包含:
oldText中需要替换的小范围代码
+
新增代码


如果oldText包含:

package
import
class
extends

则newText禁止包含这些内容。


patch_file只允许修改:
字段区域
方法内部


禁止:
newText从package开始
newText包含完整class
newText包含完整文件


错误示例:

package xxx;

public class Xxx {

}

正确示例:

private XxxApiClient client;

@Override
public void handle(){

}

==================================================

Target Protection Rule


修改Target之前:

必须执行:

read_file


read_file必须读取:

完整Target文件。


禁止:

create Target

rewrite Target

replace Target

copy Target template

generate new Target class



Target文件已经存在时:


只能使用:

patch_file


禁止:

write_file修改Target。



patch_file格式:


{
"path":"Target文件路径",
"oldText":"read_file返回的完整文件内容",
"newText":"基于oldText增加后的完整文件内容"
}



oldText规则:


oldText必须来自:

最近一次read_file结果。


禁止使用:


search_code代码片段

import片段

method片段

字段片段

自己拼接oldText



如果没有read_file结果:


禁止生成patch_file。



禁止:

write_file(path=Target)



==================================================

newText Rule


newText必须包含:


完整oldText代码

+

新增代码



禁止:


只输出新增代码。


禁止:


删除oldText已有代码。


禁止:


重新生成整个Target类。



==================================================

Target Structure Protection


修改Target必须保留:


package

import

class定义

extends

implements

已有字段

init方法

handle方法签名

已有日志

已有try-catch结构

已有业务逻辑



禁止:


修改package。


禁止:


修改class定义。


禁止:


修改extends。


禁止:


修改implements。


禁止:


修改init方法。


禁止:


修改handle方法签名。


import规则:


允许增加新增依赖import。


禁止删除已有import。


禁止替换已有import。



==================================================

Target Atomic Modification Rule


Target修改必须一次完成。


禁止:


分离import修改。


分离字段修改。


分离method修改。



禁止产生:


write_file Target import


write_file Target field


write_file Target method



必须:


一次patch_file完成Target修改。



==================================================

Handle Rule


handle只能负责:


1. 获取业务字段。


2. 调用Client。


3. eventData.putDataAndOriginData()



handle禁止出现:


HTTP代码。


URL。


timeout。


RestTemplate创建。


JSON解析。


HttpURLConnection。


new URL。



Target禁止:


直接调用外部接口。



==================================================

Architecture Rule


外部接口调用链必须:


application.yml


↓


XxxApiConfig


↓


XxxApiClient


↓


Target.handle



禁止:


Target直接调用HTTP。



==================================================

Implementation:


必须严格执行:


Step1:

read_file Target



Step2:

search_code:

RestTemplate

@Configuration

@Bean

RestTemplateBuilder



Step3:

判断是否存在RestTemplate Bean。


如果存在:

禁止创建RestTemplateConfig。


必须复用已有Bean。



如果不存在:

允许创建RestTemplateConfig。



Step4:

create application.yml



Step5:

create XxxApiConfig



Step6:

create XxxApiClient



Step7:

create DTO



Step8:

patch_file修改Target



禁止:


跳过read_file。


禁止:


没有Client修改Target。


禁止:


没有Config修改Target。



==================================================

Config Rule


接口配置必须来自:


application.yml



禁止:


Java代码保存URL。


Java代码保存timeout。


static接口配置。



ApiConfig命名:


业务名称 + ApiConfig



例如:


UserApiConfig



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


禁止:


@Bean


禁止:


RestTemplate


禁止:


创建RestTemplate Bean。

Package Rule:

禁止:

Config
Client
DTO

创建到:

Target所在package


必须:

根据项目已有目录结构选择package。


创建前必须:

search_code:
@Configuration
@Component
@RestController
@Service


确定已有package。


例如:

config类:

xxx.config

client类:

xxx.client

dto类:

xxx.dto


禁止:

所有新类都放入Target package。



Target Dependency Rule:

Target新增依赖必须使用:

@RequiredArgsConstructor


禁止:

@Autowired


禁止:

字段注入。


如果Target原本没有构造器:

允许增加:

private final XxxApiClient client;

并增加@RequiredArgsConstructor。



EventData Rule:

Target写入结果:

只能:

eventData.putDataAndOriginData()


禁止:

eventData.put()

禁止:

eventData.set()

禁止:

直接修改内部map。

==================================================

RestTemplate Rule


HTTP客户端优先:


1. 项目已有RestTemplate

2. 新建RestTemplateConfig

3. 其他HTTP客户端



禁止默认创建:


HttpURLConnection



如果创建RestTemplate Bean:


只能创建:


RestTemplateConfig



RestTemplateConfig只能负责:


@Bean

RestTemplate



禁止:


RestTemplateConfig保存业务URL。


RestTemplateConfig保存timeout。


RestTemplateConfig包含业务名称。



==================================================

Client Rule


Client必须:


@Component


@RequiredArgsConstructor



必须使用:


private final RestTemplate restTemplate;


private final XxxApiConfig config;



禁止:


@Autowired


禁止:


@Value



禁止:


new RestTemplate()



禁止:


String url="http://"



禁止:


Client保存接口配置。



Client必须返回:


DTO



禁止:


Client直接修改eventData。



==================================================

Client Code Template Lock


必须生成:


@Component
@RequiredArgsConstructor
public class XxxApiClient {


    private final RestTemplate restTemplate;


    private final XxxApiConfig config;


}



禁止:


@Autowired


禁止:


@Value


禁止:


new RestTemplate()



==================================================

DTO Rule


接口返回JSON:


必须生成DTO。


例如:


UserResponseDTO



Client返回DTO。



禁止:


Map解析业务字段。



==================================================

Dynamic Field Rule


用户任务字段:


只作为接口映射。


禁止固定:


userId

customerName

name



必须根据任务生成字段。



==================================================

Write Order Rule


如果同时存在:


create Config

create Client

create DTO

modify Target



必须:


write_file(Config)


完成


↓


write_file(Client)


完成


↓


write_file(DTO)


完成


↓


patch_file(Target)



禁止:


Target修改早于Config。


禁止:


Target修改早于Client。


禁止:


Target修改早于DTO。



==================================================

Client Responsibility


Client负责:


请求发送。


参数组装。


响应转换DTO。



Target负责:


业务字段获取。


调用Client。


写入eventData。



==================================================

Skill Completion Check


完成任务前必须确认:


[ ] Target read_file成功

[ ] application.yml存在配置

[ ] ApiConfig存在

[ ] Client存在

[ ] DTO存在

[ ] Target使用patch_file修改

[ ] Target调用Client

[ ] Target调用putDataAndOriginData

[ ] Target没有HTTP代码



如果任何条件失败:


禁止结束任务。



==================================================

Forbidden


rewrite Target


create Target


replace Target


copy Target template


modify Target signature


modify Target parent class


delete Target fields


delete Target logic


move Target code


create Controller


HTTP code in handle


hardcode URL


hardcode timeout


new RestTemplate()


HttpURLConnection


JSON解析放Target


Client直接put eventData


static接口配置


ApiConfig保存RestTemplate


Target引用不存在类


write Target before Client


write Target before Config


write Target before read_file


write_file修改Target