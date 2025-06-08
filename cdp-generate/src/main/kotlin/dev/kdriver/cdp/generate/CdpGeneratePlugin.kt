package dev.kdriver.cdp.generate

import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.net.URL

class CdpGeneratePlugin : Plugin<Project> {

    private val schemaUrl =
        "https://raw.githubusercontent.com/ChromeDevTools/debugger-protocol-viewer/master/pages/_data/tot.json"

    override fun apply(project: Project) {
        project.tasks.register("generateCdp") {
            group = "kdriver"

            doLast {
                val json = JsonSlurper().parse(URL(schemaUrl)) as Map<*, *>
                val domains = json["domains"] as List<Map<String, *>>
                val parsed = domains.map { domain ->
                    Domain(
                        domain = domain["domain"] as String,
                        description = domain["description"] as String?,
                        dependencies = (domain["dependencies"] as List<String>?) ?: emptyList(),
                        types = (domain["types"] as List<Map<String, *>>?)?.map { type ->
                            Domain.Type(
                                id = type["id"] as String,
                                type = type["type"] as String,
                                description = type["description"] as String?,
                                enum = (type["enum"] as List<String>?) ?: emptyList(),
                                properties = (type["properties"] as List<Map<String, *>>?)?.map { property ->
                                    Domain.Type.Property(
                                        name = property["name"] as String,
                                        type = property["type"] as String?,
                                        description = property["description"] as String?,
                                        ref = property["\$ref"] as String?,
                                        optional = property["optional"] as Boolean? ?: false,
                                        items = (property["items"] as Map<String, String>?) ?: emptyMap()
                                    )
                                } ?: emptyList()
                            )
                        } ?: emptyList(),
                        commands = (domain["commands"] as List<Map<String, *>>?)?.map { command ->
                            Domain.Command(
                                name = command["name"] as String,
                                description = command["description"] as String?,
                                handlers = command["handlers"] as List<String>? ?: emptyList(),
                                parameters = (command["parameters"] as List<Map<String, *>>?)?.map { parameter ->
                                    Domain.Command.Parameter(
                                        name = parameter["name"] as String,
                                        type = parameter["type"] as String?,
                                        ref = parameter["\$ref"] as String?,
                                        optional = parameter["optional"] as Boolean? ?: false,
                                        description = parameter["description"] as String?,
                                        items = (parameter["items"] as Map<String, String>?) ?: emptyMap()
                                    )
                                } ?: emptyList(),
                                returns = (command["returns"] as List<Map<String, *>>?)?.map { ret ->
                                    Domain.Command.Return(
                                        name = ret["name"] as String,
                                        description = ret["description"] as String?,
                                        ref = ret["\$ref"] as String?,
                                        optional = ret["optional"] as Boolean? ?: false,
                                        experimental = ret["experimental"] as Boolean? ?: false,
                                        type = ret["type"] as String?,
                                        items = (ret["items"] as Map<String, String>?) ?: emptyMap()
                                    )
                                } ?: emptyList(),
                                redirect = command["redirect"] as String?,
                                deprecated = command["deprecated"] as Boolean? ?: false
                            )
                        } ?: emptyList(),
                        events = (domain["events"] as List<Map<String, *>>?)?.map { event ->
                            Domain.Event(
                                name = event["name"] as String,
                                description = event["description"] as String?,
                                parameters = (event["parameters"] as List<Map<String, *>>?)?.map { parameter ->
                                    Domain.Event.Parameter(
                                        name = parameter["name"] as String,
                                        description = parameter["description"] as String?,
                                        ref = parameter["\$ref"] as String?,
                                        type = parameter["type"] as String?,
                                        optional = parameter["optional"] as Boolean? ?: false,
                                        items = (parameter["items"] as Map<String, String>?) ?: emptyMap()
                                    )
                                } ?: emptyList(),
                                handlers = event["handlers"] as List<String>? ?: emptyList()
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
