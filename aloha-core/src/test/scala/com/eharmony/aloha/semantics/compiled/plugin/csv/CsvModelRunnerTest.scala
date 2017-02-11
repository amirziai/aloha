package com.eharmony.aloha.semantics.compiled.plugin.csv

import com.eharmony.aloha.io.StringReadable
import com.eharmony.aloha.semantics.compiled.plugin.csv.CsvModelRunnerTest.csvExpected
import org.apache.commons.vfs2.VFS
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner

@RunWith(classOf[BlockJUnit4ClassRunner])
class CsvModelRunnerTest {

  @Test def testFileBasedCsvEnum(): Unit = {
    val expected = """
                     |1	VAL_1
                     |2	VAL_2
                     |0
                   """.stripMargin.trim

    val outFile = "ram://file_based_csv"

    val args = Array(
      "--output-type", "Int",
      "--input-file", "res:com/eharmony/aloha/semantics/compiled/plugin/csv/csv_enum_sample_input.txt",
      "--output-file", outFile,
      "--imports", "com.eharmony.aloha.feature.BasicFunctions._",
      "-A",
      "-c", "res:com/eharmony/aloha/semantics/compiled/plugin/csv/input_csv_format.json",
      "--predmissing", "missing_score",
      "res:com/eharmony/aloha/semantics/compiled/plugin/csv/enum_test_model.json"
    )

    CsvModelRunner.main(args)
    val output = StringReadable.fromVfs2(VFS.getManager.resolveFile(outFile))
    assertEquals(expected, output.trim)
  }

  @Test def testInlineCsvEnum(): Unit = {
    val expected = """
                     |1	VAL_1
                     |2	VAL_2
                     |0
                   """.stripMargin.trim

    val outFile = "ram://inline_csv"

    val args = Array(
      "--output-type", "Int",
      "--imports", "com.eharmony.aloha.feature.BasicFunctions._",
      "--input-file", "res:com/eharmony/aloha/semantics/compiled/plugin/csv/csv_enum_sample_input.txt",
      "--output-file", outFile,
      "-A",
      "-E", "my.EnumClass=VAL_1:1,VAL_2:2",
      "-oe", "my.enum_val=my.EnumClass",
      "--predmissing", "missing_score",
      "res:com/eharmony/aloha/semantics/compiled/plugin/csv/enum_test_model.json"
    )

    CsvModelRunner.main(args)
    val output = StringReadable.fromVfs2(VFS.getManager.resolveFile(outFile))
    assertEquals(expected, output.trim)
  }

  @Test def testInlineCsvDef() {
    // Use a temp file so we can get at the results for comparison.  Also shows how to write to a VFS file.
    val tmpFile = "ram://results.txt"

    val args = Array(
      "--missing", "", // The missing field indicator.
      "--sep", "\t", // The field separator character.
      "--ifs", ",", // The intra-field separator string.

      // imports to statically import user defined functions.
      "--imports", "scala.math._,com.eharmony.aloha.feature.BasicFunctions._",
      "--input-file", "res:fizzbuzz_testinput.csv", // File to read from.  If not provided, read STDIN.
      "--output-file", tmpFile, // File to write to.  If not provided, write STDOUT.
      "--output-type", "Double", // Specify a Type for the output.
      "-B", // Output input before the model result.
      "-E", "com.eh.Names=Bill:1,James:2,Dan:4", // Define an enumerated type.

      // Define the first column of input as a field with integer type and the name "profile.user_id".
      // The data here will be available to the model as an Int when a variable "${profile.user_id}" is
      // referenced in the model.  Each subsequent column index is defined implicitly based on order in
      // parameter list.
      "-i", "profile.user_id",
      "-e", "user.name=com.eh.Names",
      "-b", "user.isMale",
      "-i", "user.age",
      "-l", "user.birthdate.unixtime",
      "-f", "user.face.ratio",
      "-d", "user.xyz",
      "-s", "user.name.last",
      "res:fizzbuzz.json" // The VFS URL to the model.
      )

    CsvModelRunner.main(args)

    // Read the result to a string.
    val result = scala.io.Source.fromInputStream(VFS.getManager.resolveFile(tmpFile).getContent.getInputStream).getLines().mkString("\n")

    assertEquals(csvExpected.trim, result.trim)
  }

  @Test def testExternalCsvDef() {
    // Use a temp file so we can get at the results for comparison.  Also shows how to write to a VFS file.
    val tmpFile = "ram://results.txt"

    val args = Array(
      "--imports", "scala.math._,com.eharmony.aloha.feature.BasicFunctions._",
      "--input-file", "res:fizzbuzz_testinput.csv", // File to read from.  If not provided, read STDIN.
      "--output-file", tmpFile, // File to write to.  If not provided, write STDOUT.
      "--output-type", "Double", // Specify a Type for the output.
      "-c", "res:fizz_buzz_csv_desc.json",
      "-B", // Output input before the model result.
      "res:fizzbuzz.json" // The VFS URL to the model.
    )

    CsvModelRunner.main(args)

    // Read the result to a string.
    val result = scala.io.Source.fromInputStream(VFS.getManager.resolveFile(tmpFile).getContent.getInputStream).getLines().mkString("\n")

    assertEquals(csvExpected.trim, result.trim)
  }


  /**
   * Protos look like this.  Alan's proto has a first photo id of 1, fizzbuzz returns the id.  Kate's proto
   * has a first photo id of 3, so fizzbuzz returns negative -2.
   *
   * {{{
   * [
   *   {
   *     "name": "Alan"
   *     "gender": "MALE",
   *     "bmi": 23,
   *     "photos": [
   *       { "id": 1, "height": 1, "aspect_ratio": 1 },
   *       { "id": 2, "height": 2, "aspect_ratio": 2 }
   *     ]
   *   },
   *   {
   *     "name": "Kate"
   *     "gender": "FEMALE",
   *     "photos": [
   *       { "id": 3, "height": 3, "aspect_ratio": 3 }
   *     ]
   *   }
   * ]
   * }}}
   */
  @Test def testProto() {
    // Use a temp file so we can get at the results for comparison.  Also shows how to write to a VFS file.
    val tmpFile = "ram://results.txt"

    val expected = Seq(1, -2)

    val args = Array(
      "-p", "com.eharmony.aloha.test.proto.Testing.UserProto",
      "--imports", "scala.math._,com.eharmony.aloha.feature.BasicFunctions._",
      "--input-file", "res:fizz_buzzs.proto",
      "--output-type", "Double",
      "--output-file", tmpFile,
      "res:fizzbuzz_proto.json"
    )

    CsvModelRunner.main(args)

    val actual = scala.io.Source.
                    fromInputStream(VFS.getManager.resolveFile(tmpFile).getContent.getInputStream).
                    getLines().
                    map(_.toDouble).
                    toSeq
    assertEquals(expected, actual)
  }

  @Test def testProtoLoadTest() {
    // Use a temp file so we can get at the results for comparison.  Also shows how to write to a VFS file.
    val tmpFile = "ram://results.txt"

    val predPerLoop = 100
    val threads = 2
    val loops = 2

    val args = Array(
      "-p", "com.eharmony.aloha.test.proto.Testing.UserProto",
      "--imports", "scala.math._,com.eharmony.aloha.feature.BasicFunctions._",
      "--input-file", "res:fizz_buzzs.proto",
      "--output-type", "Double",
      "--output-file", tmpFile,
      "--lt-loops", loops.toString,
      "--lt-threads", threads.toString,
      "--lt-pred-per-loop", predPerLoop.toString,
      "--lt-report-loop-multiple", "1",
      "--lt-use-score-objects", "true",
      "res:fizzbuzz_proto.json"
    )

    CsvModelRunner.main(args)

    val expectedHeaders = Seq(
      "loop_number",
      "pred_sec",
      "running_pred",
      "running_nonempty_pred",
      "mem_used_mb",
      "mem_unallocated_mb",
      "unixtime_ns"
    )

    val expected = 0 to loops map { i =>
      val n = (i * threads * predPerLoop).toString
      Seq(i.toString, n, n)
    }

    val actual = scala.io.Source.
      fromInputStream(VFS.getManager.resolveFile(tmpFile).getContent.getInputStream).
      getLines().
      map(s => s.split("\t").toSeq).
      toVector

    val cols = Set(0, 2, 3)
    val actualData = actual.drop(1).map{ v => v.zipWithIndex.filter{case (x, i) => cols contains i}.unzip._1.toList }

    assertEquals(expectedHeaders, actual.head)
    assertEquals(expected, actualData)
  }
}

private object CsvModelRunnerTest {
  val csvExpected = "1\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t1.0\n2\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t2.0\n3\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t-2.0\n4\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t4.0\n5\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t-4.0\n6\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t-2.0\n7\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t7.0\n8\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t8.0\n9\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t-2.0\n10\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t-4.0\n11\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t11.0\n12\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t-2.0\n13\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t13.0\n14\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t14.0\n15\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t-6.0\n16\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t16.0\n17\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t17.0\n18\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t-2.0\n19\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t19.0\n20\tBill\ttrue\t13\t1417845974683\t1.2\t-1.7976931348623157\tJones\t-4.0"
}
