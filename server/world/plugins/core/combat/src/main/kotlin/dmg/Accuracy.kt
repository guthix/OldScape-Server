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
package io.guthix.oldscape.server.core.combat.dmg

import io.guthix.oldscape.server.core.combat.damageMultiplier
import io.guthix.oldscape.server.core.combat.findBonus
import io.guthix.oldscape.server.core.combat.player.currentStyle
import io.guthix.oldscape.server.core.equipment.attackBonus
import io.guthix.oldscape.server.core.equipment.defenceBonus
import io.guthix.oldscape.server.core.monster.template.attackBonus
import io.guthix.oldscape.server.core.monster.template.attackType
import io.guthix.oldscape.server.core.monster.template.defensiveStats
import io.guthix.oldscape.server.core.monster.template.stats
import io.guthix.oldscape.server.core.prayer.prayerMultiplier
import io.guthix.oldscape.server.core.stat.AttackType
import io.guthix.oldscape.server.world.entity.Npc
import io.guthix.oldscape.server.world.entity.Player
import kotlin.math.floor

private fun Player.effectiveAttack(): Double =
    (floor(stats.attack.status * prayerMultiplier.attack) + currentStyle.style.attackBonus + 8) *
        damageMultiplier.attack

private fun Npc.effectiveAttack(): Double = (stats.attack + 8) * damageMultiplier.attack

private fun Player.effectiveRange(): Double =
    (floor(stats.ranged.status * prayerMultiplier.range) + currentStyle.style.rangeBonus + 8) *
        damageMultiplier.strength

private fun Npc.effectiveRange(): Double = (stats.range + 8) * damageMultiplier.range

private fun Player.effectiveMagic(): Double =
    (floor(stats.ranged.status * prayerMultiplier.magic) + 8) * damageMultiplier.magic

private fun Npc.effectiveMagic(): Double = (stats.magic + 8) * damageMultiplier.magic

private fun Player.effectiveDefence(): Double =
    (floor(stats.defence.status * prayerMultiplier.defence) + currentStyle.style.defenceBonus + 8) *
        damageMultiplier.defence

private fun Npc.effectiveDefence(): Double =
    (stats.defence + 8) * damageMultiplier.defence

private fun Player.maxAttackRol(): Double =
    effectiveAttack() * (equipment.attackBonus.findBonus(currentStyle.attackType) + 64)

private fun Npc.maxAttackRol(): Double =
    effectiveAttack() * (attackBonus.melee + 64)

private fun Player.maxRangeRol(): Double = effectiveRange() * (equipment.attackBonus.range + 64)

private fun Npc.maxRangeRol(): Double =
    effectiveRange() * (attackBonus.range + 64)

private fun Player.maxMagicRol(): Double = effectiveMagic() * (equipment.attackBonus.magic + 64)

private fun Npc.maxMagicRol(): Double =
    effectiveMagic() * (attackBonus.magic + 64)

private fun Player.maxDefenceRol(attackType: AttackType): Double =
    effectiveDefence() * (equipment.defenceBonus.findBonus(attackType) + 64)

private fun Npc.maxDefenceRol(attackType: AttackType): Double =
    effectiveDefence() * (defensiveStats.findBonus(attackType) + 64)

private fun calcRoll(attackRoll: Double, defenceRoll: Double) =
    if (attackRoll > defenceRoll) 1 - (defenceRoll + 2) / (2 * (attackRoll + 1))
    else attackRoll / (2 * (defenceRoll + 1))

internal fun Player.accuracy(other: Player): Double =
    calcRoll(maxAttackRol(), other.maxDefenceRol(currentStyle.attackType))

internal fun Player.accuracy(other: Npc): Double =
    calcRoll(maxAttackRol(), other.maxDefenceRol(currentStyle.attackType))

internal fun Npc.accuracy(other: Player): Double = calcRoll(maxAttackRol(), other.maxDefenceRol(attackType)) // TODO

internal fun Npc.accuracy(other: Npc): Double = calcRoll(maxAttackRol(), other.maxDefenceRol(attackType)) // TODO
