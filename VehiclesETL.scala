package com.example.bigdata
import org.apache.spark.sql._

object VehiclesETL {

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .appName("VehiclesETL")
      .enableHiveSupport()
      .getOrCreate()
    import spark.implicits._

    val vehiclesDF = Seq(
      (1, "pedal_cycles", "pedal_cycles"),
      (2, "two_wheeled_motor_vehicles", "two_wheeled_motor_vehicles"),
      (3, "cars_and_taxis", "smaller_cars"),
      (4, "buses_and_coaches", "smaller_cars"),
      (5, "lgvs", "lgvs"),
      (6, "hgvs_2_rigid_axle", "hgvs"),
      (7, "hgvs_3_rigid_axle", "hgvs"),
      (8, "hgvs_4_or_more_rigid_axle", "hgvs"),
      (9, "hgvs_3_or_4_articulated_axle", "hgvs"),
      (10, "hgvs_5_articulated_axle", "hgvs"),
      (11, "hgvs_6_articulated_axle", "hgvs")
    ).toDF("vehicle_id", "type", "category")

    vehiclesDF
      .write
      .insertInto("vehicles")
  }

}
