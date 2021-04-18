package com.example.bigdata

import org.apache.spark.sql._
import org.apache.spark.sql.functions.{col, monotonically_increasing_id}

object RoadsETL {

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("RoadsETL")
      .enableHiveSupport()
      .getOrCreate()
    import spark.implicits._

    val dataSources = List("NorthEngland", "SouthEngland", "Scotland")
    val roadsDSArray = new Array[DataFrame](3)

    for((dataSourceName, i) <- dataSources.view.zipWithIndex) {
      roadsDSArray(i) = getDataFromSource(dataSourceName, spark, args(0))
    }

    roadsDSArray(0)
      .union(roadsDSArray(1))
      .union(roadsDSArray(2))
      .distinct()
      .withColumn("road_id", monotonically_increasing_id())
      .select(col("road_id"), col("road_name"), col("road_category"), col("road_type"))
      .write
      .insertInto("roads")

  }

  def getDataFromSource(dataSourceName: String, spark: SparkSession, dataFolderPath: String): DataFrame = {
    val mainDataDF = spark.read
      .format("org.apache.spark.csv")
      .option("header", true)
      .option("inferSchema", true)
      .csv(s"$dataFolderPath/mainData$dataSourceName.csv")

    mainDataDF
      .select(col("road_name"), col("road_category"), col("road_type"))
  }

}
