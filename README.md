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
  - ➖ [Enhanced Header](https://veovera.org/docs/enhanced/enhanced-rtmp-v2)
    - ✅ Type
    - ✅ Modifiers
    - ✅ Multitrack
  - ❌ Codec/Format Structures
- ✅ Script Tag (AMF)
- ➖ Video Tag
  - ✅ Header
    - ✅ Frame Type
    - ✅ Codec
  - ❌ [Enhanced Header](https://veovera.org/docs/enhanced/enhanced-rtmp-v2)
  - ❌ Codec Structures

### RTMP

- ✅ Handshake
- ✅ Chunk Streams
- ❓ [Enhanced](https://veovera.org/docs/enhanced/enhanced-rtmp-v2)
