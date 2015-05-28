package com.eharmony.matching.featureSpecExtractor.vw.cb.json

import com.eharmony.matching.featureSpecExtractor.json.{Namespace, SparseSpec}
import com.eharmony.matching.featureSpecExtractor.vw.json.VwJsonLike
import spray.json.DefaultJsonProtocol

import scala.collection.{immutable => sci}


final case class VwContextualBanditJson(
        imports: sci.Seq[String],
        features: sci.IndexedSeq[SparseSpec],
        namespaces: Option[Seq[Namespace]] = Some(Nil),
        normalizeFeatures: Option[Boolean] = Some(false),
        cbAction: String,
        cbCost: String,
        cbProbability: String)
extends VwJsonLike


object VwContextualBanditJson extends DefaultJsonProtocol {
    implicit val labeledVwJsonFormat = jsonFormat7(VwContextualBanditJson.apply)
}