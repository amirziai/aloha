package com.eharmony.aloha.score.audit.take5

import com.eharmony.aloha.id.ModelIdentity
import com.eharmony.aloha.reflect.RefInfo

/**
  * Created by ryan on 1/11/17.
  */
/**
  * Created by ryan on 1/9/17.
  */
trait Auditor[U, N, +B <: U] {
//  type OutputType[+X] <: U
//
//  /**
//    * Should this be done via implicit resolution?
//    * {{{
//    * def changeType[M](implicit auditor: Auditor[U, M, OutputType[M]]): Auditor[U, M, OutputType[M]] = auditor
//    * }}}
//    * @tparam M a new natural type.
//    * @return
//    */
//  // TODO: Figure out if we should do this via implicit score resolution.
//  def changeType[M: RefInfo]: Option[Auditor[U, M, OutputType[M]]]

  private[aloha] def failure(key: ModelIdentity,
                             errorMsgs: => Seq[String],
                             missingVarNames: => Set[String],
                             subValues: Seq[U]): B

  private[aloha] def success(key: ModelIdentity,
                             valueToAudit: N,
                             missingVarNames: => Set[String],
                             subValues: Seq[U],
                             prob: => Option[Float]): B

//  private[aloha] def unapply[Y >: B](value: Y): Option[N]
}

