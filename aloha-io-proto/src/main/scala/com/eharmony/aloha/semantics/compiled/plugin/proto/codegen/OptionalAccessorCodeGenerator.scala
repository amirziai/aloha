package com.eharmony.aloha.semantics.compiled.plugin.proto.codegen

import com.eharmony.aloha.semantics.compiled.plugin.proto.accessor.FieldAccessor

trait OptionalAccessorCodeGenerator[A <: FieldAccessor] extends RequiredAccessorCodeGenerator[A] {
    def generateHas(a: A): String
}
