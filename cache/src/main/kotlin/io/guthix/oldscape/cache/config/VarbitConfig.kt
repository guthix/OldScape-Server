/*
 * Copyright 2018-2020 Guthix
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
package io.guthix.oldscape.cache.config

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.io.IOException

public data class VarbitConfig(override val id: Int) : Config(id) {
    var varpId: Int = 0
    var lsb: Short = 0
    var msb: Short = 0

    override fun encode(): ByteBuf  = if(varpId != 0 && lsb.toInt() != 0 && msb.toInt() != 0) {
        Unpooled.buffer(6).apply {
            writeOpcode(1)
            writeShort(varpId)
            writeByte(lsb.toInt())
            writeByte(msb.toInt())
            writeOpcode(0)
        }
    } else {
        Unpooled.buffer(1).apply { writeOpcode(0) }
    }

    public companion object : ConfigCompanion<VarbitConfig>() {
        override val id: Int = 14

        override fun decode(id: Int, data: ByteBuf): VarbitConfig {
            val varbitConfig = VarbitConfig(id)
            decoder@ while (true) {
                when(val opcode = data.readUnsignedByte().toInt()) {
                    0 -> break@decoder
                    1 -> {
                        varbitConfig.varpId = data.readUnsignedShort()
                        varbitConfig.lsb = data.readUnsignedByte()
                        varbitConfig.msb = data.readUnsignedByte()
                    }
                    else -> throw IOException("Did not recognise opcode $opcode.")
                }
            }
            return varbitConfig
        }
    }
}