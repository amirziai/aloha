//package com.eharmony.aloha.models.ensemble.maxima
//
//import scala.collection.GenTraversableOnce
//import com.eharmony.aloha.score.basic.ModelOutput
//import ModelOutput.Implicits.modelOutputOrdering
//import com.eharmony.aloha.models.ensemble.tie.TieBreaker
//import com.eharmony.aloha.id.ModelIdentity
//import com.eharmony.aloha.models.ensemble.{Ensemble, EnsembleCombiner}
//import com.eharmony.aloha.score.conversions.ScoreConverter
//
//// TODO: Fix variance issue on B
//case class Max[-A, B: Ordering: ScoreConverter](
//        subModels: GenTraversableOnce[Model[A, B]],
//        tieBreaker: TieBreaker[B],
//        modelId: ModelIdentity
//) extends Ensemble[A, B, MaximaList[B], B] with BaseModel[A, B] {
//    require(subModels.size > 0)
//    val combiner = EnsembleCombiner(Zero.zero[B], new Maxima[B], new MaximaSelector(tieBreaker))
//    protected[this] val impl = ScoreConverterW[B]
//}
