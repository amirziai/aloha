package com.eharmony.aloha.score.audit.take3

import com.eharmony.aloha.id.ModelIdentity

/**
  * Created by ryan on 12/16/16.
  */
case class ConstantModel[T <: TypeCtor1, N, -A, +B <: T#TC[N]](modelId: ModelIdentity, constant: N, auditor: Auditor[ModelIdentity, T, N, B]) extends AuditedModel[T, N, A, B] {
  def apply(a: A): B = auditor.success(modelId, constant)
}
