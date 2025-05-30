package com.ghhccghk.musicplay.data

import com.ghhccghk.musicplay.data.songurl.Classmap
import com.ghhccghk.musicplay.data.songurl.Ipmap
import com.ghhccghk.musicplay.data.songurl.TransParam


data class SongDataBase(
    val `data`: SongData,
    val error_code: Int,
    val error_msg: String,
    val status: Int
)

data class SongData(
    val AlgPath: String,
    val aggregation: Aggregation,
    val allowerr: Int,
    val chinesecount: Int,
    val correctionforce: Int,
    val correctionrelate: String,
    val correctionsubject: String,
    val correctiontip: String,
    val correctiontype: Int,
    val from: Int,
    val isshareresult: Int,
    val istag: Int,
    val istagresult: Int,
    val lists: List<SongLists>,
    val page: Int,
    val pagesize: Int,
    val searchfull: Int,
    val sec_aggre: SecAggre,
    val sec_aggre_v2: List<Any?>,
    val sectag_info: SectagInfo,
    val size: Int,
    val subjecttype: Int,
    val total: Int
)

class Aggregation

data class SongLists(
    val A320Privilege: Int,
    val ASQPrivilege: Int,
    val Accompany: Int,
    val AlbumAux: String,
    val AlbumID: String,
    val AlbumName: String,
    val AlbumPrivilege: Int,
    val AudioCdn: Int,
    val Audioid: Int,
    val Auxiliary: String,
    val Bitrate: Int,
    val Category: Int,
    val Duration: Int,
    val ExtName: String,
    val FailProcess: Int,
    val FileHash: String,
    val FileName: String,
    val FileSize: Int,
    val FoldType: Int,
    val Grp: List<Grp>,
    val HQBitrate: Int,
    val HQDuration: Int,
    val HQExtName: String,
    val HQFailProcess: Int,
    val HQFileHash: String,
    val HQFileSize: Int,
    val HQPayType: Int,
    val HQPkgPrice: Int,
    val HQPrice: Int,
    val HQPrivilege: Int,
    val HasAlbum: Int,
    val HeatLevel: Int,
    val HiFiQuality: Int,
    val ID: String,
    val Image: String,
    val IsOriginal: Int,
    val M4aSize: Int,
    val MatchFlag: Int,
    val MixSongID: String,
    val MvHash: String,
    val MvTrac: Int,
    val MvType: Int,
    val OldCpy: Int,
    val OriOtherName: String,
    val OriSongName: String,
    val OtherName: String,
    val OwnerCount: Int,
    val PayType: Int,
    val PkgPrice: Int,
    val PrepublishInfo: PrepublishInfoX,
    val Price: Int,
    val Privilege: Int,
    val Publish: Int,
    val PublishAge: Int,
    val PublishDate: String,
    val PublishTime: String,
    val QualityLevel: Int,
    val RankId: Int,
    val Res: ResX,
    val ResBitrate: Int,
    val ResDuration: Int,
    val ResFileHash: String,
    val ResFileSize: Int,
    val SQBitrate: Int,
    val SQDuration: Int,
    val SQExtName: String,
    val SQFailProcess: Int,
    val SQFileHash: String,
    val SQFileSize: Int,
    val SQPayType: Int,
    val SQPkgPrice: Int,
    val SQPrice: Int,
    val SQPrivilege: Int,
    val Scid: Int,
    val ShowingFlag: Int,
    val SingerId: List<Int>,
    val SingerName: String,
    val Singers: List<SingerX>,
    val SongLabel: String,
    val SongName: String,
    val Source: String,
    val SourceID: Int,
    val Suffix: String,
    val SuperBitrate: Int,
    val SuperDuration: Int,
    val SuperExtName: String,
    val SuperFileHash: String,
    val SuperFileSize: Int,
    val TagContent: String,
    val TagDetails: List<TagDetail>,
    val TopID: Int,
    val TopicRemark: String,
    val TopicUrl: String,
    val Type: String,
    val Uploader: String,
    val UploaderContent: String,
    val bitflag: Int,
    val isPrepublish: Int,
    val mvTotal: Int,
    val mvdata: List<MvdataX>,
    val recommend_type: Int,
    val trans_param: TransParamX,
    val vvid: String
)

class SecAggre

data class SectagInfo(
    val is_sectag: Int
)

data class Grp(
    val A320Privilege: Int,
    val ASQPrivilege: Int,
    val Accompany: Int,
    val AlbumAux: String,
    val AlbumID: String,
    val AlbumName: String,
    val AlbumPrivilege: Int,
    val AudioCdn: Int,
    val Audioid: Int,
    val Auxiliary: String,
    val Bitrate: Int,
    val Category: Int,
    val Duration: Int,
    val ExtName: String,
    val FailProcess: Int,
    val FileHash: String,
    val FileName: String,
    val FileSize: Int,
    val HQBitrate: Int,
    val HQDuration: Int,
    val HQExtName: String,
    val HQFailProcess: Int,
    val HQFileHash: String,
    val HQFileSize: Int,
    val HQPayType: Int,
    val HQPkgPrice: Int,
    val HQPrice: Int,
    val HQPrivilege: Int,
    val HasAlbum: Int,
    val HeatLevel: Int,
    val HiFiQuality: Int,
    val ID: String,
    val Image: String,
    val IsOriginal: Int,
    val M4aSize: Int,
    val MatchFlag: Int,
    val MixSongID: String,
    val MvHash: String,
    val MvTrac: Int,
    val MvType: Int,
    val OldCpy: Int,
    val OriOtherName: String,
    val OriSongName: String,
    val OtherName: String,
    val OwnerCount: Int,
    val PayType: Int,
    val PkgPrice: Int,
    val PrepublishInfo: PrepublishInfoX,
    val Price: Int,
    val Privilege: Int,
    val Publish: Int,
    val PublishAge: Int,
    val PublishDate: String,
    val PublishTime: String,
    val QualityLevel: Int,
    val RankId: Int,
    val Res: ResX,
    val ResBitrate: Int,
    val ResDuration: Int,
    val ResFileHash: String,
    val ResFileSize: Int,
    val SQBitrate: Int,
    val SQDuration: Int,
    val SQExtName: String,
    val SQFailProcess: Int,
    val SQFileHash: String,
    val SQFileSize: Int,
    val SQPayType: Int,
    val SQPkgPrice: Int,
    val SQPrice: Int,
    val SQPrivilege: Int,
    val Scid: Int,
    val ShowingFlag: Int,
    val SingerId: List<Int>,
    val SingerName: String,
    val Singers: List<SingerX>,
    val SongLabel: String,
    val SongName: String,
    val Source: String,
    val SourceID: Int,
    val Suffix: String,
    val Super: Super,
    val SuperBitrate: Int,
    val SuperDuration: Int,
    val SuperExtName: String,
    val SuperFileHash: String,
    val SuperFileSize: Int,
    val TagContent: String,
    val TagDetails: List<Any?>,
    val TopID: Int,
    val TopicRemark: String,
    val TopicUrl: String,
    val Type: String,
    val Uploader: String,
    val UploaderContent: String,
    val bitflag: Int,
    val isPrepublish: Int,
    val mvTotal: Int,
    val mvdata: List<MvdataX>,
    val recommend_type: Int,
    val trans_param: TransParam,
    val vvid: String
)

data class PrepublishInfoX(
    val DisplayTime: String,
    val Id: Int,
    val PublishTime: String,
    val ReserveCount: Int
)

data class ResX(
    val FailProcess: Int,
    val PayType: Int,
    val PkgPrice: Int,
    val Price: Int,
    val Privilege: Int
)

data class SingerX(
    val id: Int,
    val ip_id: Int,
    val name: String
)

data class TagDetail(
    val content: String,
    val rankid: Int,
    val type: Int,
    val version: Int
)

data class MvdataX(
    val hash: String,
    val id: String,
    val trk: String,
    val typ: Int
)

data class TransParamX(
    val appid_block: String,
    val cid: Int,
    val classmap: ClassmapX,
    val cpy_attr0: Int,
    val cpy_grade: Int,
    val cpy_level: Int,
    val display: Int,
    val display_rate: Int,
    val free_for_ad: Int,
    val hash_multitrack: String,
    val hash_offset: HashOffset,
    val ipmap: Ipmap,
    val language: String,
    val musicpack_advance: Int,
    val ogg_128_filesize: Int,
    val ogg_128_hash: String,
    val ogg_320_filesize: Int,
    val ogg_320_hash: String,
    val pay_block_tpl: Int,
    val qualitymap: Qualitymap,
    val songname_suffix: String,
    val union_cover: String
)

data class Super(
    val FailProcess: Int,
    val PayType: Int,
    val PkgPrice: Int,
    val Price: Int,
    val Privilege: Int
)

data class TransParam(
    val appid_block: String,
    val cid: Int,
    val classmap: Classmap,
    val cpy_attr0: Int,
    val cpy_grade: Int,
    val cpy_level: Int,
    val display: Int,
    val display_rate: Int,
    val hash_multitrack: String,
    val hash_offset: HashOffset,
    val ipmap: Ipmap,
    val language: String,
    val musicpack_advance: Int,
    val ogg_128_filesize: Int,
    val ogg_128_hash: String,
    val ogg_320_filesize: Int,
    val ogg_320_hash: String,
    val pay_block_tpl: Int,
    val qualitymap: Qualitymap,
    val union_cover: String
)

data class Classmap(
    val attr0: Int
)

data class HashOffset(
    val clip_hash: String,
    val end_byte: Int,
    val end_ms: Int,
    val file_type: Int,
    val offset_hash: String,
    val start_byte: Int,
    val start_ms: Int
)

data class Ipmap(
    val attr0: Long
)

data class Qualitymap(
    val attr0: Int,
    val attr1: Int
)

data class ClassmapX(
    val attr0: Long
)