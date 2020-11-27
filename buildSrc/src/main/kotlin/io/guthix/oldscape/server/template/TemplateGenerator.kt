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
package io.guthix.oldscape.server.template

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.guthix.js5.Js5Cache
import io.guthix.js5.container.disk.Js5DiskStore
import io.guthix.oldscape.cache.ConfigArchive
import io.guthix.oldscape.cache.config.EnumConfig
import io.guthix.oldscape.cache.config.LocationConfig
import io.guthix.oldscape.cache.config.NpcConfig
import io.guthix.oldscape.cache.config.ObjectConfig
import io.guthix.oldscape.server.template.imp.writeEnumTemplates
import io.guthix.oldscape.server.template.imp.writeIntTemplates
import io.guthix.oldscape.server.template.imp.writeNamedConfigTemplates
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import java.io.File
import java.io.PrintWriter
import java.nio.file.Path

class TemplateGenerator : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.withType(JavaPlugin::class.java) {
            val sourceSets = target.properties["sourceSets"] as SourceSetContainer
            sourceSets.getByName("main").java.srcDir(sourceSet)
        }
        val templateGenTask = target.task("templateGeneration") { task ->
            task.doFirst {
                val resourceDir = "${target.projectDir}/src/main/resources"
                Js5Cache(Js5DiskStore.open(File("$resourceDir/cache").toPath())).use { cache ->
                    val configArchive = cache.readArchive(ConfigArchive.id)

                    val locs = LocationConfig.load(configArchive.readGroup(LocationConfig.id))
                    target.writeNamedConfigTemplates("Loc", locs, true)

                    val objs = ObjectConfig.load(configArchive.readGroup(ObjectConfig.id))
                    target.writeNamedConfigTemplates("Obj", objs, false)

                    val npcs = NpcConfig.load(configArchive.readGroup(NpcConfig.id))
                    target.writeNamedConfigTemplates("Npc", npcs, false)

                    val enums = EnumConfig.load(configArchive.readGroup(EnumConfig.id))
                    target.writeEnumTemplates(enums, objs, locs)

                    target.writeIntTemplates("SpotAnims", "SpotAnimId")
                    target.writeIntTemplates("Inventories", "InventoryId")
                    target.writeIntTemplates("Sequences", "SequenceId")
                    target.writeIntTemplates("Varbits", "VarbitId")
                    target.writeIntTemplates("CS2s", "CS2Id")
                    target.writeIntTemplates("Varps", "VarpId")
                }
            }
        }
        val classesTask = target.tasks.getByName("compileKotlin")
        templateGenTask.group = BasePlugin.BUILD_GROUP
        classesTask.dependsOn(templateGenTask)
    }

    companion object {
        const val sourceSet: String = "src/main/generated"

        const val packageName: String = "io.guthix.oldscape.server.template"

        val packageDir: String = packageName.replace(".", "/")

        val warningHeader: String =
            "/* Dont EDIT! This file is automatically generated by ${TemplateGenerator::class.qualifiedName}. */"
    }
}

fun createSourceTree(target: Project): Path {
    val srcDir = File("${target.projectDir}/${TemplateGenerator.sourceSet}").toPath()
        .resolve(TemplateGenerator.packageDir)
    srcDir.toFile().mkdirs()
    return srcDir
}

fun configNameToIdentifier(id: Int, name: String): String {
    fun String.removeTags(): String {
        val builder = StringBuilder(length)
        var inTag = false
        forEach {
            if (it == '<') {
                inTag = true
            } else if (it == '>') {
                inTag = false
            } else if (!inTag) {
                builder.append(it)
            }
        }
        return "$builder"
    }

    val normalizedName = name.toUpperCase().replace(' ', '_').replace(Regex("[^a-zA-Z\\d_:]"), "").removeTags()
    val propName = if (normalizedName.isNotEmpty()) normalizedName + "_$id" else "$id"
    return if (propName.first().isDigit()) "`$propName`" else propName
}

data class NamedId(val id: Int, val name: String)

fun Project.readNamedIds(name: String): List<NamedId> {
    val resourceDir = "${projectDir}/src/main/resources"
    return ObjectMapper(YAMLFactory()).registerKotlinModule()
        .readValue(
            File(resourceDir).toPath().resolve("template/$name.yaml").toFile(),
            object : TypeReference<List<NamedId>>() {}
        )
}

fun PrintWriter.printFileHeader(vararg supressions: String) {
    println("""
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
    """.trimIndent())
    println(TemplateGenerator.warningHeader)
    print("@file:Suppress(\"PropertyName\", \"ObjectPropertyName\"")
    supressions.forEach { print(", \"$it\"") }
    println(")")
    println("package ${TemplateGenerator.packageName}")
}

