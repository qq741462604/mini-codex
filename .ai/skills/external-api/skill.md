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


Mandatory Override Rules:

以下规则优先级最高。

如果本节和下文任何规则冲突，必须以本节为准。

1. Target 修改格式

- Target 已存在时，只能使用 patch_file，禁止 write_file 修改 Target。
- patch_file 前必须 read_file 完整 Target。
- patch_file 的 oldText 必须使用最近一次 read_file 的完整 Target 内容。
- patch_file 的 newText 必须是基于 oldText 修改后的完整 Target 内容。
- newText 必须保留 package、import、class 定义、extends、implements、已有字段、init 方法、handle 方法签名、已有日志、try-catch 和已有业务逻辑。
- 禁止用模板重写 Target；只能在完整 oldText 基础上添加必要 import、字段和 handle 内部逻辑。
- 禁止给 Target 新增 @Component、@Service、@Configuration、@RequiredArgsConstructor。
- 禁止把 Target 改造成 Spring Bean。
- 禁止修改 Target 原有继承关系，例如 extends RcesEventHandler。
- 禁止修改 Target 原有 handle 方法签名。
- 如果 Target 原本不是 Spring Bean，不要在 Target 中使用构造器注入。
- Target 中允许新增 Client 字段，但必须不破坏原生命周期；优先按项目已有 Handler 依赖获取方式处理。
- Target 新增 Client 字段时必须有可证明的初始化来源，禁止只声明 private XxxApiClient xxxApiClient。
- 禁止使用 if (xxxApiClient != null) 包裹外部接口调用来绕过未初始化问题。
- 如果找不到不破坏 Target 生命周期的 Client 获取方式，禁止修改 Target，必须停在 Plan 中读取已有 Handler 依赖获取样例。

2. RestTemplate 和 timeout

- 如果用户任务包含 timeout，必须让 timeout 配置真实生效，不能只写入 application.yml。
- timeout 必须来自 XxxApiConfig.timeout。
- 如果项目没有可证明已配置 timeout 的 RestTemplate Bean，必须新增业务命名的 RestTemplate 配置类，例如 UserApiRestTemplateConfig。
- 业务命名 RestTemplate 配置类必须使用 XxxApiConfig 创建 RestTemplate Bean，并设置 connectTimeout 和 readTimeout。
- 每个外部接口只能创建一个 RestTemplate 配置类。
- 每个外部接口只能创建一个 RestTemplate Bean。
- 创建前必须 search_code 检查是否已经存在同名 Bean，例如 userApiRestTemplate。
- 如果已存在同名 Bean 或同业务 RestTemplateConfig，禁止再次创建，必须复用或 patch 现有配置。
- 推荐使用 SimpleClientHttpRequestFactory 设置超时。
- RestTemplate Bean 名称必须业务隔离，例如 userApiRestTemplate。
- XxxApiClient 必须通过构造器注入指定 Bean，可使用 @Qualifier("userApiRestTemplate")。
- 禁止在 Client 里 new RestTemplate。
- 禁止在 Client 里忽略 config.timeout。

RestTemplateConfig 示例:

```java
@Configuration
@RequiredArgsConstructor
public class UserApiRestTemplateConfig {

    private final UserApiConfig config;

    @Bean("userApiRestTemplate")
    public RestTemplate userApiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(config.getTimeout());
        factory.setReadTimeout(config.getTimeout());
        return new RestTemplate(factory);
    }
}
```

3. Client 注入与调用

- XxxApiClient 必须使用 @Component。
- 如果需要 @Qualifier 指定业务 RestTemplate Bean，必须使用显式构造器注入。
- 如果不需要 @Qualifier，允许使用 @RequiredArgsConstructor。
- XxxApiClient 字段必须是 final。
- 禁止 @Autowired 字段注入。
- 禁止 @Value。
- URL、method、timeout 必须全部来自 XxxApiConfig。
- Client 必须返回 DTO。
- Client 禁止修改 EventData。

Client 示例:

```java
@Component
public class UserApiClient {

    private final RestTemplate restTemplate;

    private final UserApiConfig config;

    public UserApiClient(
            @Qualifier("userApiRestTemplate") RestTemplate restTemplate,
            UserApiConfig config
    ) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public UserResponseDTO queryUser(String userId) {
        URI uri = UriComponentsBuilder.fromUriString(config.getUrl())
                .queryParam("userId", userId)
                .build()
                .toUri();

        ResponseEntity<UserResponseDTO> response = restTemplate.exchange(
                uri,
                HttpMethod.valueOf(config.getMethod()),
                null,
                UserResponseDTO.class
        );
        return response.getBody();
    }
}
```

4. Target 写入 EventData

- Target.handle 只能获取字段、调用 Client、写回结果。
- 写回结果必须调用 eventData.putDataAndOriginData(字段名, 字段值)。
- 禁止 eventFields.put(...)。
- 禁止直接修改 eventData.getData() 返回的 Map。
- Target 禁止出现 RestTemplate、URL、timeout、HTTP 请求代码。

5. 完成条件

完成前必须满足:

- application.yml 包含 url、method、timeout。
- XxxApiConfig 包含 url、method、timeout。
- timeout 在 RestTemplate Bean 创建处被使用。
- XxxApiClient 使用构造器注入和 final 字段。
- Target 使用 patch_file 修改。
- Target 调用 Client。
- Target 使用 eventData.putDataAndOriginData。
- Target 不包含 HTTP 代码。
- Target 保留原 class/extends/init/handle 签名。

==================================================


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

newText必须是完整Target文件内容。

newText必须基于oldText增量修改:

- 保留oldText全部已有结构和业务逻辑
- 只新增必要import、字段和handle内部调用代码
- 禁止删除或替换无关代码


如果oldText包含:

package
import
class
extends

则newText必须保留这些内容，禁止修改原有值。


patch_file只允许修改:
字段区域
方法内部


禁止:
newText删除package
newText修改class定义
newText替换完整Target为模板代码


错误示例:

package xxx;

public class Xxx {

}

正确要求:

newText是完整Target文件，且只在完整oldText基础上增加必要依赖字段和handle内部代码。

==================================================

Target Protection Rule


修改Target之前:

必须执行:

read_file


read_file必须读取:

完整Target文件。

Target路径必须来自 search_code 返回的真实 path，或 read_file 返回的真实 path。

禁止根据 package 自行拼接 Target 路径。

如果已经发现:

src/main/java/com/study/plugin/extension/DataPrepEventHandler.java

后续所有 Target read_file/patch_file 都必须继续使用该路径。


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

userApiRestTemplate

RestTemplateConfig



Step3:

判断是否存在RestTemplate Bean。


如果存在:

且能证明已经配置任务要求的timeout:


必须复用已有Bean。



如果不存在:

允许创建RestTemplateConfig。


如果用户任务包含timeout:

除非已有Bean能证明已经设置同等timeout，否则必须创建业务命名RestTemplateConfig。

如果已经存在业务RestTemplateConfig:

禁止新建第二个RestTemplateConfig。

只能patch_file修改已有配置。



Step4:

如果application.yml不存在则write_file创建；如果已存在则read_file后patch_file修改。



Step5:

如果XxxApiConfig不存在则write_file创建；如果已存在则read_file后patch_file修改。



Step6:

如果XxxApiClient不存在则write_file创建；如果已存在则read_file后patch_file修改。



Step7:

如果DTO不存在则write_file创建；如果已存在则read_file后patch_file修改。



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

必须先从Target的package推导业务根包。

如果Target package以.extension结尾，例如:

package com.study.plugin.extension;

则业务根包是:

com.study.plugin

Config、Client、DTO必须创建到业务根包下:

com.study.plugin.config

com.study.plugin.client

com.study.plugin.dto

禁止创建到:

com.study.config

com.study.client

com.study.dto

也禁止创建到Target所在package:

com.study.plugin.extension


必须:

根据Target业务根包和项目已有目录结构选择package。


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

Target新增依赖必须遵守:

不能改变Target原有组件模型。

如果Target原本不是Spring Bean:

禁止新增@Component。

禁止新增@RequiredArgsConstructor。

禁止改成构造器注入。

必须保留原class定义和生命周期。


如果Target原本已经是Spring Bean:

才允许按项目既有风格注入依赖。


禁止为了注入Client而改变class定义、继承关系、构造器、注解和生命周期。

如果无法在不破坏Target结构的情况下完成依赖注入:

禁止继续生成实现Plan。

必须停止并读取已有Handler依赖获取样例；仍找不到时说明需要项目提供既有依赖获取方式。

禁止:

private XxxApiClient xxxApiClient;

禁止:

if (xxxApiClient != null)



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

从XxxApiConfig读取timeout并设置connectTimeout/readTimeout



禁止:


RestTemplateConfig保存业务URL。


RestTemplateConfig硬编码timeout。


RestTemplateConfig缺少业务隔离命名。



==================================================

Client Rule


Client必须:


@Component

构造器注入



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
public class XxxApiClient {


    private final RestTemplate restTemplate;


    private final XxxApiConfig config;


    public XxxApiClient(
            @Qualifier("xxxApiRestTemplate") RestTemplate restTemplate,
            XxxApiConfig config
    ) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

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


Config

Client

DTO

modify Target



必须:


Config不存在则write_file(Config)，已存在则read_file后patch_file(Config)


完成


↓


Client不存在则write_file(Client)，已存在则read_file后patch_file(Client)


完成


↓


DTO不存在则write_file(DTO)，已存在则read_file后patch_file(DTO)


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
