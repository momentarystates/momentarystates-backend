package commons

case class Binary(
    fileName: String,
    contentType: String,
    length: Long,
    md5: String,
    data: Array[Byte]
)
