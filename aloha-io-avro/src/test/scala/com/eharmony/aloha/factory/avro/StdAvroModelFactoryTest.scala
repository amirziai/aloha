package com.eharmony.aloha.factory.avro

import com.eharmony.aloha.audit.impl.avro.Score
import com.eharmony.aloha.factory.ModelFactory
import com.eharmony.aloha.models.Model
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.commons.io.IOUtils
import org.apache.commons.{vfs => vfs1, vfs2}
import com.eharmony.aloha.io.vfs.{Vfs1, Vfs2}

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

/**
  * Created by deak on 3/2/17.
  */
@RunWith(classOf[BlockJUnit4ClassRunner])
class StdAvroModelFactoryTest {
  import StdAvroModelFactoryTest._

  @Test def testHappyPathUrlVfs1Config(): Unit = {
    // Create the Aloha Vfs URL.
    val vfs = Vfs1(vfs1.VFS.getManager.resolveFile(SchemaUrl))

    val factory: ModelFactory[GenericRecord, Score] =
      StdAvroModelFactory.fromConfig(UrlConfig(vfs, ReturnType, Imports)).get

    val model: Model[GenericRecord, Score] = factory.fromString(ModelJson).get

    assertEquals(7d, model(record).getValue.asInstanceOf[Double], 0)
  }

  @Test def testHappyPathUrlVfs2Config(): Unit = {
    // Create the Aloha Vfs URL.
    val vfs = Vfs2(vfs2.VFS.getManager.resolveFile(SchemaUrl))

    val factory: ModelFactory[GenericRecord, Score] =
      StdAvroModelFactory.fromConfig(UrlConfig(vfs, ReturnType, Imports)).get

    val model: Model[GenericRecord, Score] = factory.fromString(ModelJson).get

    assertEquals(7d, model(record).getValue.asInstanceOf[Double], 0)
  }

  @Test def testHappyPathApplyDefaultVfs2(): Unit = {
    val factory: ModelFactory[GenericRecord, Score] =
      StdAvroModelFactory(SchemaUrl, ReturnType, Imports).get

    val model: Model[GenericRecord, Score] = factory.fromString(ModelJson).get

    assertEquals(7d, model(record).getValue.asInstanceOf[Double], 0)
  }

  @Test def testHappyPathApplyVfs1(): Unit = {
    val factory: ModelFactory[GenericRecord, Score] =
      StdAvroModelFactory(SchemaUrl, ReturnType, Imports, useVfs2 = false).get

    val model: Model[GenericRecord, Score] = factory.fromString(ModelJson).get

    assertEquals(7d, model(record).getValue.asInstanceOf[Double], 0)
  }

  private[this] def record = {
    val r = new GenericData.Record(TheSchema)
    r.put("req_str_1", "smart handsome stubborn")
    r
  }
}

object StdAvroModelFactoryTest {
  private lazy val TheSchema = {
    val is = getClass.getClassLoader.getResourceAsStream("avro/class7.avpr")
    try new Schema.Parser().parse(is) finally IOUtils.closeQuietly(is)
  }

  private val SchemaUrl = "res:avro/class7.avpr"

  private val Imports = Seq("com.eharmony.aloha.feature.BasicFunctions._", "scala.math._")

  private val ReturnType = "Double"

  private val ModelJson =
    """
      |{
      |  "modelType": "Regression",
      |  "modelId": { "id": 0, "name": "" },
      |  "features" : {
      |    "my_attributes": "${req_str_1}.split(\"\\\\W+\").map(v => (s\"=$v\", 1.0))"
      |  },
      |  "weights": {
      |    "my_attributes=handsome": 1,
      |    "my_attributes=smart": 2,
      |    "my_attributes=stubborn": 4
      |  }
      |}
    """.stripMargin
}

