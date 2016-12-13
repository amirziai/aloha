package com.eharmony.aloha.score.audit

import com.eharmony.aloha.id.ModelIdentity

/**
  *
  * Created by ryan on 12/12/16.
  *
  * @tparam A model's input type
  * @tparam N model's "''natural output type''".
  * @tparam B model's audited output type.
  */
trait AuditedModel[-A, N, +B] extends Model[A, B] {
  def auditor: Auditor[ModelIdentity, N, B]
}
