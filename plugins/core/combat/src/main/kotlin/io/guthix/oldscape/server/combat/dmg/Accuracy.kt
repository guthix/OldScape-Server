package io.guthix.oldscape.server.combat.dmg

import io.guthix.oldscape.server.blueprints.AttackStyle
import io.guthix.oldscape.server.combat.*
import io.guthix.oldscape.server.prayer.prayerMultiplier
import io.guthix.oldscape.server.world.entity.Character
import io.guthix.oldscape.server.world.entity.Npc
import io.guthix.oldscape.server.world.entity.Player
import kotlin.math.floor

private fun Player.effectiveAttack(): Double =
    (floor(stats.attack.status * prayerMultiplier.attack) + attackStance.attack + 8) * damageMultiplier.attack

private fun Npc.effectiveAttack(): Double =
    ((blueprint.stats?.attack ?: 0) + attackStance.attack + 8) * damageMultiplier.attack

private fun Player.effectiveRange(): Double =
    (floor(stats.ranged.status * prayerMultiplier.range) + attackStance.range + 8) * damageMultiplier.strength

private fun Npc.effectiveRange(): Double =
    ((blueprint.stats?.range ?: 0) + attackStance.range + 8) * damageMultiplier.range

private fun Player.effectiveMagic(): Double =
    (floor(stats.ranged.status * prayerMultiplier.magic) + 8) * damageMultiplier.magic

private fun Npc.effectiveMagic(): Double =
    ((blueprint.stats?.magic ?: 0) + 8) * damageMultiplier.magic

private fun Player.effectiveDefence(): Double =
    (floor(stats.defence.status * prayerMultiplier.defence) + attackStance.defence + 8) * damageMultiplier.defence

private fun Npc.effectiveDefence(): Double =
    ((blueprint.stats?.defence ?: 0) + 8) * damageMultiplier.defence

private fun Player.maxAttackRol(): Double =
    effectiveAttack() * (equipment.attackBonus.findMeleeBonus(attackStyle) + 64)

private fun Npc.maxAttackRol(): Double =
    effectiveAttack() * ((blueprint.attackStats?.typeBonus?.melee ?: 0) + 64)

private fun Player.maxRangeRol(): Double = effectiveRange() * (equipment.attackBonus.range + 64)

private fun Npc.maxRangeRol(): Double =
    effectiveRange() * ((blueprint.attackStats?.typeBonus?.range ?: 0) + 64)

private fun Player.maxMagicRol(): Double = effectiveMagic() * (equipment.attackBonus.magic + 64)

private fun Npc.maxMagicRol(): Double =
    effectiveMagic() * ((blueprint.attackStats?.typeBonus?.magic ?: 0) + 64)

private fun Player.maxDefenceRol(attackStyle: AttackStyle): Double =
    effectiveDefence() * (equipment.defenceBonus.findBonus(attackStyle) + 64)

private fun Npc.maxDefenceRol(attackStyle: AttackStyle): Double =
    effectiveDefence() * ((blueprint.defensiveStats?.findBonus(attackStyle) ?: 0) + 64)

private fun calcRoll(attackRoll: Double, defenceRoll: Double) =
    if(attackRoll > defenceRoll) 1 - (defenceRoll + 2) / (2 * (attackRoll + 1))
    else attackRoll / (2 * (defenceRoll + 1))

internal fun Player.accuracy(other: Player): Double = calcRoll(maxAttackRol(), other.maxDefenceRol(attackStyle))

internal fun Player.accuracy(other: Npc): Double = calcRoll(maxAttackRol(), other.maxDefenceRol(attackStyle))

internal fun Npc.accuracy(other: Player): Double = calcRoll(maxAttackRol(), other.maxDefenceRol(attackStyle))

internal fun Npc.accuracy(other: Npc): Double = calcRoll(maxAttackRol(), other.maxDefenceRol(attackStyle))