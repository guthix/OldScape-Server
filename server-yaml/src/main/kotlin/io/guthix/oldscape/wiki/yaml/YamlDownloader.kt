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
package io.guthix.oldscape.wiki.yaml

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.guthix.oldscape.server.blueprints.ExtraNpcConfig
import io.guthix.oldscape.server.blueprints.equipment.ExtraBodyConfig
import io.guthix.oldscape.server.blueprints.equipment.ExtraHeadConfig
import io.guthix.oldscape.server.blueprints.equipment.ExtraWeaponConfig
import io.guthix.oldscape.wiki.npcWikiDownloader
import io.guthix.oldscape.wiki.objectWikiDownloader
import io.guthix.oldscape.wiki.wikitext.ObjectWikiDefinition
import mu.KotlinLogging
import java.nio.file.Path


private val logger = KotlinLogging.logger {  }

fun main(args: Array<String>) {
    YamlDownloader.main(args)
}

object YamlDownloader {
    @JvmStatic
    fun main(args: Array<String>) {
        val serverDir = Path.of("../../Oldscape-Server/src/main/resources")
        val cacheDir = serverDir.resolve("cache")
        val configDir = serverDir.resolve("config")
        val npcDir = configDir.resolve("npcs")
        val objDir = configDir.resolve("objects")

        val yamlFactory = YAMLFactory()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        val objectMapper = ObjectMapper(yamlFactory)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerKotlinModule()

        val writer = objectMapper.writer(
            DefaultPrettyPrinter().withObjectIndenter(DefaultIndenter().withLinefeed("\r\n"))
        )
        objectMapper.writeObjs(writer, cacheDir, objDir)
        objectMapper.writeNpcs(writer, cacheDir, npcDir, "Npcs.yaml")
    }

    fun ObjectMapper.writeObjs(writer: ObjectWriter, cacheDir: Path, objServerDir: Path) {
        val objWikiData = objectWikiDownloader(cacheDir).filter { it.ids != null }.sortedBy { it.ids!!.first() }
        val equpimentWikiData = objWikiData.filter { it.slot != null }

        val objFileName = "Objects.yaml"
        val objData = objWikiData.filter { it.slot == null }
        val objectFile = Path.of(javaClass.getResource("/").toURI()).resolve(objFileName).toFile()
        writer.writeValue(objectFile, objData.map(ObjectWikiDefinition::toExtraObjectConfig))
        logger.info {
            "Done writing ${objData.size} obj configs to ${objectFile.absoluteFile.absolutePath}"
        }


        val ammoFileName = "AmmunitionEquipment.yaml"
        val ammoData = equpimentWikiData.filter { it.slot!!.equals("ammo", true) }
        val ammunitionFile = Path.of(javaClass.getResource("/").toURI()).resolve(ammoFileName).toFile()
        writer.writeValue(ammunitionFile, ammoData.map(ObjectWikiDefinition::toExtraEquipmentConfig))
        logger.info {
            "Done writing ${ammoData.size} ammunition equipment configs to ${ammunitionFile.absoluteFile.absolutePath}"
        }

        val bodyFileName = "BodyEquipment.yaml"
        val bodyData = equpimentWikiData.filter { it.slot!!.equals("body", true) }
        val bodyFile = Path.of(javaClass.getResource("/").toURI()).resolve(bodyFileName).toFile()
        val bodyServerConfigs = readValue<List<ExtraBodyConfig>>(objServerDir.resolve(bodyFileName).toFile())
        writer.writeValue(bodyFile, bodyData.map { new ->
            val curExtraConfig = bodyServerConfigs.find { (ids) -> new.ids == ids }
            new.toExtraBodyConfig(curExtraConfig)
        })
        logger.info {
            "Done writing ${bodyData.size} body equipment configs to ${bodyFile.absoluteFile.absolutePath}"
        }

        val capeFileName = "CapeEquipment.yaml"
        val capeData = equpimentWikiData.filter { it.slot!!.equals("cape", true) }
        val capeFile = Path.of(javaClass.getResource("/").toURI()).resolve(capeFileName).toFile()
        writer.writeValue(capeFile, capeData.map(ObjectWikiDefinition::toExtraEquipmentConfig))
        logger.info {
            "Done writing ${capeData.size} cape equipment configs to ${capeFile.absoluteFile.absolutePath}"
        }

        val feetFileName = "FeetEquipment.yaml"
        val feetData = equpimentWikiData.filter { it.slot!!.equals("feet", true) }
        val feetFile = Path.of(javaClass.getResource("/").toURI()).resolve(feetFileName).toFile()
        writer.writeValue(feetFile, feetData.map(ObjectWikiDefinition::toExtraEquipmentConfig))
        logger.info {
            "Done writing ${feetData.size} feet equipment configs to ${feetFile.absoluteFile.absolutePath}"
        }

        val handFileName = "HandEquipment.yaml"
        val handData = equpimentWikiData.filter { it.slot!!.equals("hands", true) }
        val handFile = Path.of(javaClass.getResource("/").toURI()).resolve(handFileName).toFile()
        writer.writeValue(handFile, handData.map(ObjectWikiDefinition::toExtraEquipmentConfig))
        logger.info {
            "Done writing ${handData.size} hand equipment configs to ${handFile.absoluteFile.absolutePath}"
        }


        val headFileName = "HeadEquipment.yaml"
        val headData = equpimentWikiData.filter { it.slot!!.equals("head", true) }
        val headFile = Path.of(javaClass.getResource("/").toURI()).resolve(headFileName).toFile()
        val headServerConfigs = readValue<List<ExtraHeadConfig>>(objServerDir.resolve(headFileName).toFile())
        writer.writeValue(headFile, headData.map { new ->
            val curExtraConfig = headServerConfigs.find { (ids) -> new.ids == ids }
            new.toExtraHeadConfig(curExtraConfig)
        })
        logger.info {
            "Done writing ${headData.size} head equipment configs to ${headFile.absoluteFile.absolutePath}"
        }

        val legFileName = "LegEquipment.yaml"
        val legData = equpimentWikiData.filter { it.slot!!.equals("legs", true) }
        val legFile = Path.of(javaClass.getResource("/").toURI()).resolve(legFileName).toFile()
        writer.writeValue(legFile, legData.map(ObjectWikiDefinition::toExtraEquipmentConfig))
        logger.info {
            "Done writing ${legData.size} leg equipment configs to ${legFile.absoluteFile.absolutePath}"
        }


        val neckFileName = "NeckEquipment.yaml"
        val neckData = equpimentWikiData.filter { it.slot!!.equals("neck", true) }
        val neckFile = Path.of(javaClass.getResource("/").toURI()).resolve(neckFileName).toFile()
        writer.writeValue(neckFile, neckData.map(ObjectWikiDefinition::toExtraEquipmentConfig))
        logger.info {
            "Done writing ${neckData.size} neck equipment configs to ${neckFile.absoluteFile.absolutePath}"
        }

        val ringFileName = "RingEquipment.yaml"
        val ringData = equpimentWikiData.filter { it.slot!!.equals("neck", true) }
        val ringFile = Path.of(javaClass.getResource("/").toURI()).resolve(ringFileName).toFile()
        writer.writeValue(ringFile, ringData.map(ObjectWikiDefinition::toExtraEquipmentConfig))
        logger.info {
            "Done writing ${ringData.size} ring equipment configs to ${ringFile.absoluteFile.absolutePath}"
        }

        val shieldFileName = "ShieldEquipment.yaml"
        val shieldData = equpimentWikiData.filter { it.slot!!.equals("shield", true) }
        val shieldFile = Path.of(javaClass.getResource("/").toURI()).resolve(shieldFileName).toFile()
        writer.writeValue(shieldFile, shieldData.map(ObjectWikiDefinition::toExtraEquipmentConfig))
        logger.info {
            "Done writing ${shieldData.size} shield equipment configs to ${shieldFile.absoluteFile.absolutePath}"
        }

        val twoHandFileName = "TwoHandEquipment.yaml"
        val twoHandData = equpimentWikiData.filter { it.slot!!.equals("2h", true) }
        val twoHandFile = Path.of(javaClass.getResource("/").toURI()).resolve(twoHandFileName).toFile()
        val twoHandServerConfigs = readValue<List<ExtraWeaponConfig>>(objServerDir.resolve(twoHandFileName).toFile())
        writer.writeValue(twoHandFile, twoHandData.map { new ->
            val curExtraConfig = twoHandServerConfigs.find { (ids) -> new.ids == ids }
            new.toExtraWeaponConfig(curExtraConfig)
        })
        logger.info {
            "Done writing ${twoHandData.size} two hand equipment configs to ${twoHandFile.absoluteFile.absolutePath}"
        }

        val weaponFileName = "WeaponEquipment.yaml"
        val weaponData = equpimentWikiData.filter { it.slot!!.equals("weapon", true) }
        val weaponFile = Path.of(javaClass.getResource("/").toURI()).resolve(weaponFileName).toFile()
        val weaponServerConfigs = readValue<List<ExtraWeaponConfig>>(objServerDir.resolve(weaponFileName).toFile())
        writer.writeValue(weaponFile, weaponData.map { new ->
            val curExtraConfig = weaponServerConfigs.find { (ids) -> new.ids == ids }
            new.toExtraWeaponConfig(curExtraConfig)
        })
        logger.info {
            "Done writing ${weaponData.size} weapon equipment configs to ${weaponFile.absoluteFile.absolutePath}"
        }
    }

    fun ObjectMapper.writeNpcs(writer: ObjectWriter, cacheDir: Path, npcServerDir: Path, fileName: String) {
        val npcData = npcWikiDownloader(cacheDir).filter { it.ids != null }.sortedBy { it.ids!!.first() }
        val npcFile = Path.of(javaClass.getResource("/").toURI()).resolve(fileName).toFile()
        val npcServerConfigs = readValue<List<ExtraNpcConfig>>(npcServerDir.resolve(fileName).toFile())
        writer.writeValue(npcFile, npcData.map { new ->
            val curExtraConfig = npcServerConfigs.find { cur -> new.ids == cur.ids }
            new.toExtraNpcConfig(curExtraConfig)
        })
        logger.info {
            "Done writing ${npcData.size} npcs to ${npcFile.absoluteFile.absolutePath}"
        }
    }
}

