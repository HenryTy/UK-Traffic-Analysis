package com.example.bigdata

import org.apache.spark.sql._
import org.apache.spark.sql.functions.{col, concat, date_format, lit, to_timestamp, abs, min, format_string}
import org.apache.spark.sql.types.LongType

object FactsETL {

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("FactsETL")
      .enableHiveSupport()
      .getOrCreate()
    import spark.implicits._

    val dataSources = List("NorthEngland", "SouthEngland", "Scotland")

    for(dataSourceName <- dataSources) {
      loadDataFromSource(dataSourceName, spark, args(0))
    }

    spark.sql("DROP TABLE IF EXISTS `weather_measurements`")

  }

  def loadDataFromSource(dataSourceName: String, spark: SparkSession, dataFolderPath: String): Unit = {
    val mainDataDF = spark.read
      .format("org.apache.spark.csv")
      .option("header", true)
      .option("inferSchema", true)
      .csv(s"$dataFolderPath/mainData$dataSourceName.csv")

    val vehicle_types = Seq("pedal_cycles", "two_wheeled_motor_vehicles", "cars_and_taxis", "buses_and_coaches", "lgvs", "hgvs_2_rigid_axle", "hgvs_3_rigid_axle", "hgvs_4_or_more_rigid_axle", "hgvs_3_or_4_articulated_axle", "hgvs_5_articulated_axle", "hgvs_6_articulated_axle")

    val basicTrafficDataDF = mainDataDF.flatMap(r => vehicle_types.zipWithIndex.map(v =>
      (r.getTimestamp(3), r.getInt(4), r.getString(5), r.getInt(v._2 + 17), v._1, r.getString(6))))
      .toDF("count_date", "hour", "local_authoirty_ons_code", "vehicle_count", "vehicle_type", "road_name")

    val weatherMeasurementsDF = spark.sql("SELECT * FROM weather_measurements")

    val trafficDataWithNearWeathers = basicTrafficDataDF
      .withColumn("count_time", to_timestamp(
        concat(
          date_format(col("count_date"), "dd/MM/yyyy"),
          lit(" "),
          format_string("%02d", col("hour"))),
        "dd/MM/yyyy HH"))
      .join(weatherMeasurementsDF,
        basicTrafficDataDF("local_authoirty_ons_code") === weatherMeasurementsDF("local_authority_ons_code") &&
          abs(col("count_time").cast(LongType) - weatherMeasurementsDF("time").cast(LongType)) < 18000,
        "left"
      )
      .withColumn("time_diff", abs(col("count_time").cast(LongType) - weatherMeasurementsDF("time").cast(LongType)))
      .na.fill(0, Seq("time_diff"))
      .dropDuplicates("count_date", "hour", "local_authoirty_ons_code", "vehicle_count", "vehicle_type", "road_name", "time_diff")

    val minTimeDiffs = trafficDataWithNearWeathers
      .groupBy(col("local_authority_ons_code"), col("count_time"))
      .agg(min(col("time_diff")).as("min_diff"))

    val trafficWithWeather = trafficDataWithNearWeathers
      .join(minTimeDiffs,
        trafficDataWithNearWeathers("local_authority_ons_code") === minTimeDiffs("local_authority_ons_code") &&
        trafficDataWithNearWeathers("count_time") === minTimeDiffs("count_time"))
      .where(col("time_diff") === col("min_diff"))

    val timeDF = spark.sql("SELECT * FROM time")

    val vehiclesDF = spark.sql("SELECT * FROM vehicles")

    val roadsDF = spark.sql("SELECT * FROM roads")

    trafficWithWeather
      .join(roadsDF, trafficWithWeather("road_name") === roadsDF("road_name"))
      .join(vehiclesDF, trafficWithWeather("vehicle_type") === vehiclesDF("type"))
      .join(timeDF,
        trafficWithWeather("count_date") === to_timestamp(timeDF("date")) &&
          trafficWithWeather("hour") === timeDF("hour"))
      .select(vehiclesDF("vehicle_id"), timeDF("time_id"), trafficWithWeather("weather_id"),
        roadsDF("road_id"), trafficWithWeather("local_authority_ons_code"), trafficWithWeather("vehicle_count"))
      .write
      .insertInto("facts")

  }

}
