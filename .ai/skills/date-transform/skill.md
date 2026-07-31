# date-transform

keywords:

- 日期
- 时间
- 转换
- 格式
- yyyyMMdd
- yyyy-MM-dd
- timestamp
- 拼接
- 合并



Target:

class:
DataPrepEventHandler

method:
handle


Rules:

1. 所有日期加工逻辑必须在DataPrepEventHandler中完成。

2. 根据用户需求识别:
   - 输入字段
   - 输入格式
   - 输出字段
   - 输出格式

3. 支持:
   - 单字段日期转换
   - 多字段日期拼接转换

4. 不创建新的Service、Controller、DTO。

5. 不改变已有报文结构。

Implementation:


如果handle中已经存在其他字段加工逻辑:

禁止删除。

必须保留已有调用。


新增日期转换能力:


1. handle方法:


如果存在:

Map<String,Object> eventFields = eventData.getData();


保留。


在其后增加:

dateTransform(eventData);



禁止替换已有:

xxx(eventData);



2. 新增private方法:


private void dateTransform(EventData eventData){

}


方法内部:


根据用户需求生成转换逻辑。


支持:


单字段转换:

输入:

sourceField
sourceFormat

输出:

targetField
targetFormat



例如:

oprdate

yyyyMMdd


-->

newDate

yyyy-MM-dd HH:mm:ss



支持:


多字段组合:


oprDate + oprTime


yyyyMMdd + hhmmss


-->

allDate


yyyy-MM-dd HH:mm:ss



禁止:

删除已有日期转换。


禁止:

修改已有字段转换。


禁止:

覆盖已有handle逻辑。

