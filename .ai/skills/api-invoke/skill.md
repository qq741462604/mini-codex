# API接口调用增强


Target:

class:
DataPrepEventHandler


method:
handle



Keywords:

- 接口调用
- 外调接口
- HTTP
- API
- 查询接口



Rules:


1. 所有接口调用必须在handle方法中增加调用入口。

2. 不允许创建新的Controller。

3. 不允许创建新的Service。

4. 不允许改变已有报文处理流程。

5. 必须使用已有eventData.putDataAndOriginData()写回结果。


Implementation:


根据用户提供的接口信息生成调用代码。



用户提供的信息包括:


接口地址:

例如:

http://xxx/api/query


请求方式:

GET/POST


超时时间:

例如:

3000ms


请求字段:

例如:

userId


返回字段:

例如:

cityCode


目标字段:

例如:

cityName



实现要求:


1.

handle方法中:


找到:

EventData eventData


Map<String,Object> eventFields = eventData.getData();



在已有字段处理逻辑之后追加:


apiInvoke(eventData);



禁止删除已有调用。



2.

新增private方法:


private void apiInvoke(EventData eventData){

}



方法内部:


1. 从eventData获取请求参数。


2. 调用外部接口。


3. 设置连接超时时间。


4. 设置读取超时时间。


5. 解析返回结果。


6. 使用:


eventData.putDataAndOriginData(
    targetKey,
    value
);


写入报文。



代码必须根据用户提供接口信息生成。


Forbidden:


禁止:

1. mock返回数据。

2. 固定返回值。

3. 创建假的接口。

4. 删除已有字段加工逻辑。

5. 修改handle已有结构。

6. 创建新的Service。