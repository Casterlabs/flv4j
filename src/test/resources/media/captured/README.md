Captured from OBS.

ffmpeg -f flv -listen 1 -i rtmp://0.0.0.0:1935/live/app -c copy capture.flv
ffmpeg -i capture.flv -t 1s -c copy avc1_aac.flv # Rename accordingly
