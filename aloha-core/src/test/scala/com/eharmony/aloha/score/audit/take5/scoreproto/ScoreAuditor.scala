package com.eharmony.aloha.score.audit.take5.scoreproto

import com.eharmony.aloha.id.ModelIdentity
import com.eharmony.aloha.reflect.RefInfo
import com.eharmony.aloha.score.Scores.Score
import com.eharmony.aloha.score.Scores.Score._
import com.eharmony.aloha.score.Scores.Score.BaseScore.ScoreType
import com.eharmony.aloha.score.audit.take5.Auditor

import scala.collection.JavaConversions.{asJavaIterable, iterableAsScalaIterable}

/**
  * Created by ryan on 1/17/17.
  */
sealed abstract class ScoreAuditor[N] extends Auditor[Score, N, Score] {
  type OutputType[+X] = Score
}

object ScoreAuditor {

  def apply[N](implicit ri: RefInfo[N]): Option[ScoreAuditor[N]] = {
    ri match {
      case RefInfo.Nothing => opt(NothingAuditor)
      case RefInfo.Boolean => opt(BooleanAuditor)
      case RefInfo.Byte => opt(ByteAuditor)
      case RefInfo.Short => opt(ShortAuditor)
      case RefInfo.Int => opt(IntAuditor)
      case RefInfo.Long  => opt(LongAuditor)
      case RefInfo.Float => opt(FloatAuditor)
      case RefInfo.Double => opt(DoubleAuditor)
      case RefInfo.String => opt(StringAuditor)
      case _ => None
    }
  }

  def nothingAuditor: ScoreAuditor[Nothing] = NothingAuditor
  def booleanAuditor: ScoreAuditor[Boolean] = BooleanAuditor
  def byteAuditor: ScoreAuditor[Byte] = ByteAuditor
  def shortAuditor: ScoreAuditor[Short] = ShortAuditor
  def intAuditor: ScoreAuditor[Int] = IntAuditor
  def longAuditor: ScoreAuditor[Long] = LongAuditor
  def floatAuditor: ScoreAuditor[Float] = FloatAuditor
  def doubleAuditor: ScoreAuditor[Double] = DoubleAuditor
  def stringAuditor: ScoreAuditor[String] = StringAuditor

  /**
    * Cast `auditor` to `ScoreAuditor[N]` and wrap in an Option.
    * '''NOTE''': ''I hate this method'' but I don't want to require an implicit
    * [[ScoreAuditor]] as a parameter in the apply method because [[ScoreAuditorImpl]]'s
    * `changeType` method would then need the same implicit to be included.  A determination
    * should be made as to the feasibility of this strategy.
    * @param auditor an auditor to wrap
    * @tparam N type of auditor to be returned
    * @return
    */
  private[this] def opt[N](auditor: ScoreAuditor[_]): Option[ScoreAuditor[N]] =
    Option(auditor.asInstanceOf[ScoreAuditor[N]])

  private[this] sealed trait ScoreAuditorImpl[N] extends ScoreAuditor[N] {

    /**
      * Convert an audited value of the natural type to a `BaseScore.Builder`.
      * This builder will be augmented with the model ID and possibly a probablity of
      * the score being produced randomly.  This builder will be built prior to return
      * from the [[Auditor]] API.
      *
      * @param valueToAudit a value that is audited
      * @return a BaseScore.Builder based on the value being audited.
      */
    def boxScore(valueToAudit: N): BaseScore.Builder

    override final def changeType[M: RefInfo]: Option[ScoreAuditor[M]] = ScoreAuditor[M]

    private[aloha] override final def failure(key: ModelIdentity,
                                              errorMsgs: => Seq[String],
                                              missingVarNames: => Set[String],
                                              subValues: Seq[Score]): Score = {
      val b = Score.newBuilder
      if (subValues.nonEmpty)
        b.addAllSubScores(asJavaIterable(subValues))
      val e = ScoreError.newBuilder.setModel(mId(key)).addAllMessages(asJavaIterable(errorMsgs))
      val m = accumulateMissing(missingVarNames, subValues)
      if (m.nonEmpty)
        e.setMissingFeatures(MissingRequiredFields.newBuilder.addAllNames(asJavaIterable(m)))
      b.setError(e).build
    }

    private[aloha] override final def success(key: ModelIdentity,
                                              valueToAudit: N,
                                              missingVarNames: => Set[String],
                                              subValues: Seq[Score],
                                              prob: => Option[Float]): Score = {
      val m = mId(key)
      val b = Score.newBuilder
      if (subValues.nonEmpty)
        b.addAllSubScores(asJavaIterable(subValues))
      val mm = accumulateMissing(missingVarNames, subValues)
      if (mm.nonEmpty)
        b.setError(ScoreError.newBuilder.
          setMissingFeatures(MissingRequiredFields.newBuilder.addAllNames(asJavaIterable(mm))).
          setModel(m))
      val baseScore = boxScore(valueToAudit).setModel(m)
      b.setScore(prob.fold(baseScore)(baseScore.setProbability))
      b.build
    }

    private[this] def mId(modelId: ModelIdentity) =
      ModelId.newBuilder.setId(modelId.getId()).setName(modelId.getName()).build

    private[this] def accumulateMissing(missing: Iterable[String], subScores: Iterable[Score]) = {
      val subMissing = subScores.flatMap(s => iterableAsScalaIterable(s.getError.getMissingFeatures.getNamesList))
      val mm = missing ++ subMissing
      if (mm.nonEmpty) mm.toSeq.distinct else Nil
    }
  }

  private[this] object NothingAuditor extends ScoreAuditorImpl[Nothing] {
    override def boxScore(valueToAudit: Nothing): BaseScore.Builder =
      BaseScore.newBuilder.setType(ScoreType.NONE)
  }

  private[this] object BooleanAuditor extends ScoreAuditorImpl[Boolean] {
    override def boxScore(valueToAudit: Boolean): BaseScore.Builder =
      BaseScore.newBuilder.setType(ScoreType.BOOLEAN).setExtension(
        BooleanScore.impl,
        BooleanScore.newBuilder.setScore(valueToAudit).build
      )
  }

  private[this] object ByteAuditor extends ScoreAuditorImpl[Byte] {
    override def boxScore(valueToAudit: Byte): BaseScore.Builder =
      BaseScore.newBuilder.setType(ScoreType.INT).setExtension(
        IntScore.impl,
        IntScore.newBuilder.setScore(valueToAudit).build
      )
  }

  private[this] object ShortAuditor extends ScoreAuditorImpl[Short] {
    override def boxScore(valueToAudit: Short): BaseScore.Builder =
      BaseScore.newBuilder.setType(ScoreType.INT).setExtension(
        IntScore.impl,
        IntScore.newBuilder.setScore(valueToAudit).build
      )
  }

  private[this] object IntAuditor extends ScoreAuditorImpl[Int] {
    override def boxScore(valueToAudit: Int): BaseScore.Builder =
      BaseScore.newBuilder.setType(ScoreType.INT).setExtension(
        IntScore.impl,
        IntScore.newBuilder.setScore(valueToAudit).build
      )
  }

  private[this] object LongAuditor extends ScoreAuditorImpl[Long] {
    override def boxScore(valueToAudit: Long): BaseScore.Builder =
      BaseScore.newBuilder.setType(ScoreType.LONG).setExtension(
        LongScore.impl,
        LongScore.newBuilder.setScore(valueToAudit).build
      )
  }

  private[this] object FloatAuditor extends ScoreAuditorImpl[Float] {
    override def boxScore(valueToAudit: Float): BaseScore.Builder =
      BaseScore.newBuilder.setType(ScoreType.FLOAT).setExtension(
        FloatScore.impl,
        FloatScore.newBuilder.setScore(valueToAudit).build
      )
  }

  private[this] object DoubleAuditor extends ScoreAuditorImpl[Double] {
    override def boxScore(valueToAudit: Double): BaseScore.Builder =
      BaseScore.newBuilder.setType(ScoreType.DOUBLE).setExtension(
        DoubleScore.impl,
        DoubleScore.newBuilder.setScore(valueToAudit).build
      )
  }

  private[this] object StringAuditor extends ScoreAuditorImpl[String] {
    override def boxScore(valueToAudit: String): BaseScore.Builder =
      BaseScore.newBuilder.setType(ScoreType.STRING).setExtension(
        StringScore.impl,
        StringScore.newBuilder.setScore(valueToAudit).build
      )
  }
}
