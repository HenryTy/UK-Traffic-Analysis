package com.example.bigdata
import org.apache.spark.sql._

object AuthoritiesETL {

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("AuthoritiesETL")
      .enableHiveSupport()
      .getOrCreate()
    import spark.implicits._

    val dataSources = List("NorthEngland", "SouthEngland", "Scotland")

    for(dataSourceName <- dataSources) {
      loadDataFromSource(dataSourceName, spark, args(0))
    }

  }

  def loadDataFromSource(dataSourceName: String, spark: SparkSession, dataFolderPath: String): Unit = {
    val authoritiesDS = spark.read
      .format("org.apache.spark.csv")
      .option("header", true)
      .option("inferSchema", true)
      .csv(s"$dataFolderPath/authorities$dataSourceName.csv")

    val regionsDS = spark.read
      .format("org.apache.spark.csv")
      .option("header", true)
      .option("inferSchema", true)
      .csv(s"$dataFolderPath/regions$dataSourceName.csv")

    authoritiesDS
      .join(regionsDS, authoritiesDS("region_ons_code") === regionsDS("region_ons_code"))
      .select(
        authoritiesDS("local_authority_ons_code"),
        authoritiesDS("local_authority_name"),
        regionsDS("region_name")
      )
      .write
      .insertInto("authorities")
  }

}
