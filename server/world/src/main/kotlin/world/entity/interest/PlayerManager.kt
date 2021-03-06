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

import io.guthix.oldscape.dim.TileUnit
import io.guthix.oldscape.dim.tiles
import io.guthix.oldscape.server.net.game.out.PlayerInfoPacket
import io.guthix.oldscape.server.world.World
import io.guthix.oldscape.server.world.entity.Player
import io.guthix.oldscape.server.world.map.Tile
import io.netty.channel.ChannelFuture
import kotlinx.serialization.Serializable

class PlayerManager(val index: Int) {
    var localPlayerCount: Int = 0

    val localPlayers: Array<Player?> = arrayOfNulls(World.MAX_PLAYERS)

    val localPlayerIndexes: IntArray = IntArray(World.MAX_PLAYERS)

    var externalPlayerCount: Int = 0

    val externalPlayerIndexes: IntArray = IntArray(World.MAX_PLAYERS)

    val regionIds: IntArray = IntArray(World.MAX_PLAYERS)

    val skipFlags: ByteArray = ByteArray(World.MAX_PLAYERS)

    internal fun initialize(world: World, player: Player) {
        localPlayers[index] = player
        localPlayerIndexes[localPlayerCount++] = index
        for (playerIndex in 1 until World.MAX_PLAYERS) {
            if (index != playerIndex) {
                val externalPlayer = world.players[playerIndex]
                regionIds[playerIndex] = externalPlayer?.pos?.regionId ?: 0
                externalPlayerIndexes[externalPlayerCount++] = playerIndex
            }
        }
    }

    internal fun synchronize(world: World, player: Player): List<ChannelFuture> = listOf(
        player.ctx.write(PlayerInfoPacket(world.players, this, player))
    )

    companion object {
        val SIZE: TileUnit = 32.tiles

        val RANGE: TileUnit = SIZE / 2.tiles

        val REGION_SIZE: TileUnit = 8192.tiles

        const val MESSAGE_DURATION: Int = 4
    }
}

@Serializable
data class Style(
    val hair: Int,
    val beard: Int,
    val torso: Int,
    val arms: Int,
    val legs: Int,
    val hands: Int,
    val feet: Int
)

@Serializable
data class Colours(
    var hair: Int,
    var torso: Int,
    var legs: Int,
    var feet: Int,
    var skin: Int
)


@Serializable
data class StanceSequences(
    var stand: Int,
    var turn: Int,
    var walk: Int,
    var turn180: Int,
    var turn90CW: Int,
    var turn90CCW: Int,
    var run: Int
)

@Serializable
enum class Gender(val opcode: Int) { MALE(0), FEMALE(1) }

val Tile.regionId: Int
    get() = (floor.value shl 16) or ((x / PlayerManager.REGION_SIZE).value shl 8) or
        (y / PlayerManager.REGION_SIZE).value