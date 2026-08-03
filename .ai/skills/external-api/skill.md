- # External API Integration Skill


  Keywords:

  接口
  外调
  调用接口
  HTTP
  REST


  Target:

  class:
  DataPrepEventHandler


  method:
  handle

  

  Execution Type:

  MULTI_FILE_MODIFICATION

  

  Files Required:


  必须创建:

  1. Client

  路径:

  根据项目包结构生成


  职责:

  负责HTTP调用


  2. Config

  路径:

  config包


  职责:

  保存:

  - url
  - timeout
  - method

  

  3. DTO

  如果接口请求或者返回需要对象:

  必须创建DTO

  

  Modification:


  修改 Target:

  DataPrepEventHandler.handle


  增加:

  1. 调用Client

  2. 获取返回结果

  3. eventData.putDataAndOriginData写回

  

  Hard Rule:


  执行顺序:


  Step1:

  检查项目是否存在类似Client


  如果存在:

  复用


  如果不存在:

  必须create Client


  Step2:

  create Config


  Step3:

  create DTO(如果需要)


  Step4:

  修改Target类

  

  禁止:


  只修改Target引用不存在Client。


  禁止生成:


  UserApiClient.xxx()


  除非:

  UserApiClient.java已经存在。



Target Modification:


如果新增Client:

必须:

1. 添加private final Client字段

2. 使用构造注入

3. 禁止new Client()