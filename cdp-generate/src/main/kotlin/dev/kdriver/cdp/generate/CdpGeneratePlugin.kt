package dev.kdriver.cdp.generate

import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.net.URL

class CdpGeneratePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("generateCdp") {
            group = "kdriver"

            doLast {
                val json = JsonSlurper()
                    .parse(URL("https://raw.githubusercontent.com/ChromeDevTools/debugger-protocol-viewer/master/pages/_data/tot.json")) as Map<*, *>
                val domains = json["domains"] as List<Map<String, *>>
                val parsed = domains.map {
                    Domain(
                        domain = it["domain"] as String,
                        description = it["description"] as String?,
                        dependencies = (it["dependencies"] as List<String>?) ?: emptyList(),
                        types = (it["types"] as List<Map<String, *>>?)?.map {
                            Domain.Type(
                                id = it["id"] as String,
                                type = it["type"] as String,
                                description = it["description"] as String?,
                                enum = (it["enum"] as List<String>?) ?: emptyList(),
                                properties = (it["properties"] as List<Map<String, *>>?)?.map {
                                    Domain.Type.Property(
                                        name = it["name"] as String,
                                        type = it["type"] as String?,
                                        description = it["description"] as String?,
                                        ref = it["\$ref"] as String?,
                                        optional = it["optional"] as Boolean? ?: false,
                                        items = (it["items"] as Map<String, String>?) ?: emptyMap()
                                    )
                                } ?: emptyList()
                            )
                        } ?: emptyList(),
                        commands = (it["commands"] as List<Map<String, *>>?)?.map {
                            Domain.Command(
                                name = it["name"] as String,
                                description = it["description"] as String?,
                                handlers = it["handlers"] as List<String>? ?: emptyList(),
                                parameters = (it["parameters"] as List<Map<String, *>>?)?.map {
                                    Domain.Command.Parameter(
                                        name = it["name"] as String,
                                        type = it["type"] as String?,
                                        ref = it["\$ref"] as String?,
                                        optional = it["optional"] as Boolean? ?: false,
                                        description = it["description"] as String?,
                                        items = (it["items"] as Map<String, String>?) ?: emptyMap()
                                    )
                                } ?: emptyList(),
                                returns = (it["returns"] as List<Map<String, *>>?)?.map {
                                    Domain.Command.Return(
                                        name = it["name"] as String,
                                        description = it["description"] as String?,
                                        ref = it["\$ref"] as String?,
                                        optional = it["optional"] as Boolean? ?: false,
                                        experimental = it["experimental"] as Boolean? ?: false,
                                        type = it["type"] as String?,
                                        items = (it["items"] as Map<String, String>?) ?: emptyMap()
                                    )
                                } ?: emptyList(),
                                redirect = it["redirect"] as String?,
                                deprecated = it["deprecated"] as Boolean? ?: false
                            )
                        } ?: emptyList(),
                        events = (it["events"] as List<Map<String, *>>?)?.map {
                            Domain.Event(
                                name = it["name"] as String,
                                description = it["description"] as String?,
                                parameters = (it["parameters"] as List<Map<String, *>>?)?.map {
                                    Domain.Event.Parameter(
                                        name = it["name"] as String,
                                        description = it["description"] as String?,
                                        ref = it["\$ref"] as String?,
                                        type = it["type"] as String?,
                                        optional = it["optional"] as Boolean? ?: false,
                                        items = (it["items"] as Map<String, String>?) ?: emptyMap()
                                    )
                                } ?: emptyList(),
                                handlers = it["handlers"] as List<String>? ?: emptyList()
                            )
                        } ?: emptyList()
                    )
                }
                parsed.forEach {
                    val destination = listOf(
                        project.projectDir.path,
                        "src",
                        "commonMain",
                        "kotlin"
                    ).joinToString("/")
                    it.generateClassFile(parsed).writeTo(File(destination))
                }
            }
        }
    }

}
