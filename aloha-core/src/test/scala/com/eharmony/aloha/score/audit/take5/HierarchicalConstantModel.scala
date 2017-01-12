package com.eharmony.aloha.score.audit.take5

/**
  * Created by ryan on 1/11/17.
  */
import com.eharmony.aloha.id.ModelIdentity

/**
  * Created by ryan on 1/11/17.
  */
case class HierarchicalConstantModel[U, N, -A, +B <: U](
    modelId: ModelIdentity,
    constant: N,
    sub: Model[A, U],
    auditor: Auditor[U, N, B]
) extends AuditedModel[U, N, A, B] {
  def apply(a: A): B = auditor.success(modelId, constant, Set.empty, Seq(sub(a)), None)
}
