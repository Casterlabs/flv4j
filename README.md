# flv4j

Our in-house library for FLV, RTMP and AMF.

## Goals

- Fully parse FLV, RTMP and AMF
- Have sane data structures
- Be fault tolerant to future extensions to the protocol (e.g [Veovera's enhanced format](https://veovera.org/docs/enhanced/enhanced-rtmp-v2))

### Non-goals

- Decode or encode video or audio codecs/formats.
- Provide high-level APIs for creating services.
- Provide APIs for playing or capturing video or audio.

## What works

✅: Fully Supported  
❌: Not Supported  
➖: Partially Supported  
❓: Not Sure

### AMF

- ✅ Version 0
- ❌ Version 3

### FLV

- ✅ (De)muxing
- ✅ File Header
- ➖ Audio Tag
  - ✅ Header
    - ✅ Codec/Format
    - ✅ Sample Rate
    - ✅ Sample Size
    - ✅ Audio Channels
  - ✅ [Enhanced Header](https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-audio)
    - ✅ Type
    - ✅ Modifiers
    - ✅ Multitrack
  - ➖ Codec/Format Structures
    - ➖ Standard AAC
    - ❌ Enhanced Codec/Format Structures
- ✅ Script Tag (AMF)
- ➖ Video Tag
  - ✅ Header
    - ✅ Frame Type
    - ✅ Codec
      - ✅ Non-Standard REALH263 (codec 8)
      - ✅ Non-Standard MPEG4 (codec 9)
      - ✅ Non-Standard HEVC (codec 12)
  - ✅ [Enhanced Header](https://veovera.org/docs/enhanced/enhanced-rtmp-v2#enhanced-video)
    - ✅ Type
    - ✅ Modifiers
    - ✅ Multitrack
  - ➖ Codec/Format Structures
    - ➖ Standard H264
    - ➖ Non-Standard HEVC (codec 12)
    - ➖ Enhanced
	  - ➖ H264
	  - ➖ HEVC

### RTMP

- ✅ Handshake
- ✅ Chunk Streams
- ✅ [Enhanced](https://veovera.org/docs/enhanced/enhanced-rtmp-v2)

## Relevant specs

- [SWF](https://web.archive.org/web/20120526025653/http://wwwimages.adobe.com/www.adobe.com/content/dam/Adobe/en/devnet/swf/pdf/swf_file_format_spec_v10.pdf)
- [FLV](https://rtmp.veriskope.com/pdf/video_file_format_spec_v10_1.pdf)
- [AMF0](https://rtmp.veriskope.com/pdf/amf0-file-format-specification.pdf)
- [RTMP](https://rtmp.veriskope.com/docs/spec/)
- [Veovera's Enhanced RTMP](https://veovera.org/docs/enhanced/enhanced-rtmp-v2)
