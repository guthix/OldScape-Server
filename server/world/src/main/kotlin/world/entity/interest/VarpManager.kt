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
package io.guthix.oldscape.server.world.entity.interest

import io.guthix.oldscape.server.net.game.out.VarpLargePacket
import io.guthix.oldscape.server.net.game.out.VarpSmallPacket
import io.guthix.oldscape.server.template.VarbitTemplate
import io.guthix.oldscape.server.world.entity.Player
import io.netty.channel.ChannelFuture
import kotlin.math.pow

class VarpManager {
    val varps: MutableMap<Int, Int> = mutableMapOf()

    private val changes = mutableMapOf<Int, Int>()

    fun updateVarp(id: Int, value: Int) {
        varps[id] = value
        changes[id] = value
    }

    fun updateVarbit(template: VarbitTemplate, value: Int) {
        val bitSize = (template.msb - template.lsb) + 1
        if (value > 2.0.pow(bitSize) - 1) throw IllegalArgumentException("Value $value to big for this varbit.")
        var curVarp = varps[template.varpId] ?: 0
        curVarp = curVarp.clearBits(template.msb, template.lsb)
        curVarp = curVarp or value shl template.lsb
        varps[template.varpId] = curVarp
        changes[template.varpId] = curVarp
    }

    private fun Int.setBits(msb: Int, lsb: Int): Int = this xor ((1 shl (msb + 1)) - 1) xor ((1 shl lsb) - 1)

    @Suppress("INTEGER_OVERFLOW")
    private fun Int.clearBits(msb: Int, lsb: Int) = ((1 shl 4 * 8 - 1) - 1).setBits(msb, lsb) and this

    internal fun synchronize(player: Player): List<ChannelFuture> = changes.map { (id, value) ->
        if (value <= Byte.MIN_VALUE || value >= Byte.MAX_VALUE) {
            player.ctx.write(VarpLargePacket(id, value))
        } else {
            player.ctx.write(VarpSmallPacket(id, value))
        }
    }

    internal fun postProcess(): Unit = changes.clear()
}