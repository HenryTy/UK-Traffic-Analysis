spark.sql("DROP TABLE IF EXISTS `time`")
spark.sql("DROP TABLE IF EXISTS `vehicles`")
spark.sql("DROP TABLE IF EXISTS `authorities`")
spark.sql("DROP TABLE IF EXISTS `roads`")
spark.sql("DROP TABLE IF EXISTS `weather`")
spark.sql("DROP TABLE IF EXISTS `facts`")

spark.sql("""CREATE TABLE `time` (
 `time_id` bigint,
 `year` int,
 `month` int,
 `date` date,
 `hour` int,
 `quarter` int,
 `week_day` string)
ROW FORMAT SERDE
 'org.apache.hadoop.hive.ql.io.orc.OrcSerde'
STORED AS INPUTFORMAT
 'org.apache.hadoop.hive.ql.io.orc.OrcInputFormat'
OUTPUTFORMAT
 'org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat'""")

spark.sql("""CREATE TABLE `vehicles` (
 `vehicle_id` int,
 `type` string,
 `category` string)
ROW FORMAT SERDE
 'org.apache.hadoop.hive.ql.io.orc.OrcSerde'
STORED AS INPUTFORMAT
 'org.apache.hadoop.hive.ql.io.orc.OrcInputFormat'
OUTPUTFORMAT
 'org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat'""")

spark.sql("""CREATE TABLE `authorities` (
 `ons_code` string,
 `authority_name` string,
 `region_name` string)
ROW FORMAT SERDE
 'org.apache.hadoop.hive.ql.io.orc.OrcSerde'
STORED AS INPUTFORMAT
 'org.apache.hadoop.hive.ql.io.orc.OrcInputFormat'
OUTPUTFORMAT
 'org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat'""")

 spark.sql("""CREATE TABLE `roads` (
 `road_id` bigint,
 `road_name` string,
 `road_category` string,
 `road_type` string)
ROW FORMAT SERDE
 'org.apache.hadoop.hive.ql.io.orc.OrcSerde'
STORED AS INPUTFORMAT
 'org.apache.hadoop.hive.ql.io.orc.OrcInputFormat'
OUTPUTFORMAT
 'org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat'""")

spark.sql("""CREATE TABLE `weather` (
 `weather_id` bigint,
 `description` string)
ROW FORMAT SERDE
 'org.apache.hadoop.hive.ql.io.orc.OrcSerde'
STORED AS INPUTFORMAT
 'org.apache.hadoop.hive.ql.io.orc.OrcInputFormat'
OUTPUTFORMAT
 'org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat'""")

spark.sql("""CREATE TABLE `facts` (
 `vehicle_id` int,
 `time_id` bigint,
 `weather_id` bigint,
 `road_id` bigint,
 `authority_ons_code` string,
 `vehicle_count` int)
ROW FORMAT SERDE
 'org.apache.hadoop.hive.ql.io.orc.OrcSerde'
STORED AS INPUTFORMAT
 'org.apache.hadoop.hive.ql.io.orc.OrcInputFormat'
OUTPUTFORMAT
 'org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat'""")