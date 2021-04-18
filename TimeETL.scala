package com.example.bigdata

import org.apache.spark.sql._
import org.apache.spark.sql.functions.{col, monotonically_increasing_id}

object TimeETL {

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("TimeETL")
      .enableHiveSupport()
      .getOrCreate()
    import spark.implicits._

    val dataSources = List("NorthEngland", "SouthEngland", "Scotland")
    val timesDFArray = new Array[DataFrame](3)

    for((dataSourceName, i) <- dataSources.view.zipWithIndex) {
      timesDFArray(i) = getDataFromSource(dataSourceName, spark, args(0))
    }

    timesDFArray(0)
      .union(timesDFArray(1))
      .union(timesDFArray(2))
      .distinct()
      .withColumn("month", functions.month(col("count_date")))
      .withColumn("quarter", functions.quarter(col("count_date")))
      .withColumn("week_day", functions.date_format(col("count_date"), "E"))
      .withColumn("date", functions.to_date(functions.date_format(col("count_date"), "yyyy-MM-dd")))
      .withColumn("time_id", monotonically_increasing_id())
      .select(col("time_id"), col("year"), col("month"), col("date"),
        col("hour"), col("quarter"), col("week_day"))
      .write
      .insertInto("time")
  }

  def getDataFromSource(dataSourceName: String, spark: SparkSession, dataFolderPath: String): DataFrame = {
    val mainDataDF = spark.read
      .format("org.apache.spark.csv")
      .option("header", true)
      .option("inferSchema", true)
      .csv(s"$dataFolderPath/mainData$dataSourceName.csv")

    mainDataDF
      .select(col("year"), col("count_date"), col("hour"))
  }

}
