package co.enear.lxscala.twitter.entities

import io.circe.generic.JsonCodec

@JsonCodec case class StatusSearch(statuses: List[Tweet], search_metadata: SearchMetadata)

@JsonCodec case class SearchMetadata(
  completed_in: Double,
  max_id: Long,
  max_id_str: String,
  next_results: Option[String],
  query: String,
  refresh_url: String,
  count: Int,
  since_id: Long,
  since_id_str: String
)

@JsonCodec case class StatusMetadata(iso_language_code: String, result_type: String)
