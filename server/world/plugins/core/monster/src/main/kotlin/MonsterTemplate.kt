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
package io.guthix.oldscape.server.core.monster.template

import io.guthix.oldscape.dim.TileUnit
import io.guthix.oldscape.dim.tiles
import io.guthix.oldscape.server.Property
import io.guthix.oldscape.server.core.stat.AttackType
import io.guthix.oldscape.server.core.stat.CombatBonus
import io.guthix.oldscape.server.core.stat.StyleBonus
import io.guthix.oldscape.server.template.NpcTemplate
import io.guthix.oldscape.server.template.Template
import io.guthix.oldscape.server.template.TemplateNotFoundException
import io.guthix.oldscape.server.world.entity.Npc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

val Npc.maxHit: Int get() = monsterTemplate?.maxHit ?: throw TemplateNotFoundException(id, Npc::maxHit)

val Npc.attackType: AttackType
    get() = monsterTemplate?.attackType ?: throw TemplateNotFoundException(id, Npc::attackType)

val Npc.aggressiveType: AggresiveType? get() = monsterTemplate?.aggressiveType

val Npc.isPoisonous: Boolean? get() = monsterTemplate?.isPoisonous

val Npc.isImmumePoison: Boolean? get() = monsterTemplate?.isImmumePoison

val Npc.isImmuneVenom: Boolean? get() = monsterTemplate?.isImmuneVenom

val Npc.attackSpeed: Int
    get() = monsterTemplate?.attackSpeed ?: throw TemplateNotFoundException(id, Npc::attackSpeed)

val Npc.spawnSequence: Int get() = sequences.spawn ?: throw TemplateNotFoundException(id, Npc::spawnSequence)

val Npc.attackSequence: Int get() = sequences.attack

val Npc.defenceSequence: Int get() = sequences.defence

val Npc.deathSequence: Int get() = sequences.death

private val Npc.sequences: CombatSequences
    get() = monsterTemplate?.sequences ?: throw TemplateNotFoundException(id, Npc::sequences)

val Npc.stats: CombatStats get() = monsterTemplate?.stats ?: throw TemplateNotFoundException(id, Npc::stats)

val Npc.attackBonus: CombatBonus
    get() =
        monsterTemplate?.attackBonus ?: throw TemplateNotFoundException(id, Npc::attackBonus)

val Npc.strengthBonus: CombatBonus
    get() =
        monsterTemplate?.strengthBonus ?: throw TemplateNotFoundException(id, Npc::strengthBonus)

val Npc.defensiveStats: StyleBonus
    get() =
        monsterTemplate?.defensiveStats ?: throw TemplateNotFoundException(id, Npc::defensiveStats)

internal val Npc.monsterTemplate: MonsterTemplate?
    get() = template.monster

internal val NpcTemplate.monster: MonsterTemplate? by Property { null }

@Serializable
sealed class AggresiveType {
    @Serializable
    @SerialName("Never")
    object Never : AggresiveType()

    @Serializable
    @SerialName("Always")
    data class Always(val _range: Int? = null) : AggresiveType() {
        val range: TileUnit get() = _range?.tiles ?: DEFAULT_RANGE
    }

    @Serializable
    @SerialName("Combat")
    data class Combat(val _range: Int? = null) : AggresiveType() {
        val range: TileUnit get() = _range?.tiles ?: DEFAULT_RANGE
    }

    companion object {
        val DEFAULT_RANGE: TileUnit = 10.tiles
    }
}

@Serializable
data class MonsterTemplate(
    override val ids: List<Int>,
    val maxHit: Int? = null,
    val attackType: AttackType? = null,
    val aggressiveType: AggresiveType,
    val isPoisonous: Boolean,
    val isImmumePoison: Boolean,
    val isImmuneVenom: Boolean,
    val attackSpeed: Int? = null,
    val sequences: CombatSequences? = null,
    val stats: CombatStats,
    val attackBonus: CombatBonus,
    val strengthBonus: CombatBonus,
    val defensiveStats: StyleBonus
) : Template

@Serializable
data class CombatStats(
    val health: Int,
    val attack: Int,
    val strength: Int,
    val defence: Int,
    val range: Int,
    val magic: Int
)

@Serializable
data class CombatSequences(
    val spawn: Int? = null,
    val attack: Int,
    val defence: Int,
    val death: Int
)