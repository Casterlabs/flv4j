wget "https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"
ffmpeg -i "./BigBuckBunny_320x180.mp4" -c copy -f flv "./bigbuckbunny_320x180_h264_aac.flv"
rm "./BigBuckBunny_320x180.mp4"