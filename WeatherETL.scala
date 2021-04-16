package com.example.bigdata
import org.apache.spark.sql._
import org.apache.spark.sql.functions.{col, concat, lit, monotonically_increasing_id, to_timestamp}

object WeatherETL {

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("WeatherETL")
      .enableHiveSupport()
      .getOrCreate()
    import spark.implicits._

    spark.sql("""CREATE TABLE `weather_measurements` (
       `weather_id` bigint,
       `local_authority_ons_code` string,
       `time` timestamp)
      ROW FORMAT DELIMITED
      FIELDS TERMINATED BY ','
      STORED AS TEXTFILE""")

    val weatherDS = spark
      .read
      .textFile(args(0) + "/weather.txt")
      .map(line => getWeatherDataFromLine(line))
      .filter(_.isDefined)
      .map(_.get)
      .filter(wm => wm.conditions != "Unknown" && wm.conditions != "null")
      .as[WeatherMeasure]

    val weatherToInsertDF = weatherDS
      .map(_.conditions)
      .distinct()
      .withColumnRenamed("value", "description")
      .withColumn("weather_id", monotonically_increasing_id())
      .select(col("weather_id"), col("description"))

    weatherToInsertDF
      .write
      .insertInto("weather")

    weatherDS
      .dropDuplicates("local_authority_ons_code", "date", "hour")
      .join(weatherToInsertDF, weatherDS("conditions") === weatherToInsertDF("description"))
      .select(col("weather_id"), col("local_authority_ons_code"),
        to_timestamp(concat(col("date"), lit(" "), col("hour")), "dd/MM/yyyy HH"))
      .write
      .insertInto("weather_measurements")

  }

  def getWeatherDataFromLine(line: String): Option[WeatherMeasure] = {
    val pattern = """^.+ of (.+) on (.+) at (.+):.+ the following weather conditions were reported: (.+)$""".r
    line match {
      case pattern(local_authority_ons_code, date, hour, conditions) =>
                Some(WeatherMeasure(local_authority_ons_code, date, hour, conditions))
      case _ => None
    }
  }

  case class WeatherMeasure(local_authority_ons_code: String, date: String, hour: String, conditions: String)
}
