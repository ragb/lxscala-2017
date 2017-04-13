package co.enear.lxscala.twitter.entities

case class UploadedMedia(
  media_id: Long,
  media_id_str: String,
  size: Int,
  image: Option[Image] = None,
  video: Option[Video] = None
)
