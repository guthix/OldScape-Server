/*
 * Copyright 2018-2021 Guthix
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.guthix.oldscape.server.core.obj.template

import io.guthix.oldscape.server.Property
import io.guthix.oldscape.server.template.ObjTemplate
import io.guthix.oldscape.server.template.Template
import io.guthix.oldscape.server.template.TemplateNotFoundException
import io.guthix.oldscape.server.world.entity.Obj
import kotlinx.serialization.Serializable

val Obj.weight: Float get() = weightTemplate.weight

private val Obj.weightTemplate: ObjWeightTemplate
    get() = template.weight ?: throw TemplateNotFoundException(id, ObjWeightTemplate::class)

internal val ObjTemplate.weight: ObjWeightTemplate? by Property { null }

@Serializable
data class ObjWeightTemplate(override val ids: List<Int>, val weight: Float) : Template