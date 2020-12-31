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
package io.guthix.oldscape.server.world.entity

import io.guthix.oldscape.dim.TileUnit
import io.guthix.oldscape.server.PropertyHolder
import io.guthix.oldscape.server.world.map.Tile
import kotlin.reflect.KProperty

interface Entity : PropertyHolder {
    val pos: Tile

    val sizeX: TileUnit

    val sizeY: TileUnit

    var orientation: Int

    override val properties: MutableMap<KProperty<*>, Any?>
}