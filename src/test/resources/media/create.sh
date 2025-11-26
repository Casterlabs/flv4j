#!/bin/bash

 function use_ffmpeg() {
    ffmpeg -hide_banner -loglevel level+error -i "./sources/BigBuckBunny_320x180.mp4" -t 10 -y $@ ; echo ""
}

# ------------------
# ---- Standard ----
# ------------------
rm -rf ./standard
mkdir -p ./standard/video
mkdir -p ./standard/audio

ffmpeg -hide_banner -loglevel level+error -i "./sources/fbvp6_829kb.vp6" -c copy -f flv "./standard/video/on2_vp6.flv" -y ; echo ""

# 	Video
use_ffmpeg -an -c flv1 -f flv "./standard/video/sorenson_h263.flv" -y
use_ffmpeg -an -c flashsv -f flv "./standard/video/screen.flv" -y
use_ffmpeg -an -c flashsv2 -f flv "./standard/video/screen2.flv" -y
#use_ffmpeg -an -c vp6f -f flv "./standard/video/on2_vp6.flv" -y
#use_ffmpeg -an -c vp6a -f flv "./standard/video/on2_vp6_alpha.flv" -y
use_ffmpeg -an -c h264 -f flv "./standard/video/h264.flv" -y

# 	Audio
# use_ffmpeg -vn -ar 44100 -ac 1 -c pcm_s16be -f flv "./standard/audio/lpcm.flv" # Broken?
use_ffmpeg -vn -ar 44100 -ac 1 -c adpcm_swf -f flv "./standard/audio/adpcm.flv"
use_ffmpeg -vn -ar 44100 -ac 2 -c mp3 -f flv "./standard/audio/mp3.flv"
use_ffmpeg -vn -ar 44100 -ac 1 -c pcm_s16le -f flv "./standard/audio/lpcm_le.flv"
use_ffmpeg -vn -ar 16000 -ac 1 -c nellymoser -f flv "./standard/audio/nellymoser16mono.flv"
use_ffmpeg -vn -ar  8000 -ac 1 -c nellymoser -f flv "./standard/audio/nellymoser8mono.flv"
use_ffmpeg -vn -ar 22050 -ac 1 -c nellymoser -f flv "./standard/audio/nellymoser.flv"
use_ffmpeg -vn -ar 44100 -ac 1 -c pcm_mulaw -f flv "./standard/audio/mulaw.flv"
use_ffmpeg -vn -ar 44100 -ac 1 -c pcm_alaw -f flv "./standard/audio/alaw.flv"
use_ffmpeg -vn -ar 48000 -ac 2 -c aac -f flv "./standard/audio/aac.flv"
use_ffmpeg -vn -ar 16000 -ac 1 -c speex -f flv "./standard/audio/speex.flv"
#use_ffmpeg -vn -ar 44100 -ac 2 -c mp3 -sample_fmt u8 -f flv "./standard/audio/mp3.flv"


# ------------------
# -- Non-Standard --
# ------------------
rm -rf ./nonstandard
mkdir -p ./nonstandard/video
mkdir -p ./nonstandard/audio

cp "./sources/HEVC-1-codecid_12.flv" "./nonstandard/video/hevc.flv"

# 	Video
use_ffmpeg -an -c h263 -strict -1 -vf scale=352:288 -f flv "./nonstandard/video/realh263.flv" -y
use_ffmpeg -an -c mpeg4 -strict -1 -f flv "./nonstandard/video/mpeg4.flv" -y
# use_ffmpeg -an -c hevc -strict -1 -f flv "./nonstandard/video/hevc.flv" -y # Can't actually get ffmpeg to produce this :(


# ------------------
# ---- Veovera -----
# ------------------
rm -rf ./veovera
mkdir -p ./veovera/video
mkdir -p ./veovera/audio

# 	Video
use_ffmpeg -an -c hevc -strict -1 -f flv "./veovera/video/hvc1.flv" -y
use_ffmpeg -an -c librav1e -strict -1 -f flv "./veovera/video/av01.flv" -y
use_ffmpeg -an -c vp9 -strict -1 -f flv "./veovera/video/vp09.flv" -y

# 	Audio
use_ffmpeg -vn -c libopus -strict -1 -f flv "./veovera/audio/Opus.flv" -y
use_ffmpeg -vn -c flac -strict -1 -f flv "./veovera/audio/fLaC.flv" -y
use_ffmpeg -vn -c ac3 -strict -1 -f flv "./veovera/audio/ac-3.flv" -y
use_ffmpeg -vn -c eac3 -strict -1 -f flv "./veovera/audio/ec-3.flv" -y
