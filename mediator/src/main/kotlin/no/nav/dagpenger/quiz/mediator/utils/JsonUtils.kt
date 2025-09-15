import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

fun jsonNodeToMap(jsonNode: JsonNode): Map<String, Any> = jacksonObjectMapper().convertValue(jsonNode, Map::class.java) as Map<String, Any>

fun jsonStringToMap(jsonString: String): Map<String, Any> = jacksonObjectMapper().readValue(jsonString) as Map<String, Any>
