import streamlit as st
import yt_dlp
import os

# ページの設定
st.set_page_config(page_title="YouTube Audio Extractor", page_icon="✨")

# カスタムCSSでAndroid版の雰囲気を再現（ピンク基調）
st.markdown("""
    <style>
    .stApp {
        background-color: #FFF5F7;
    }
    .stButton>button {
        background-color: #FF4081;
        color: white;
        border-radius: 10px;
        height: 3em;
        width: 100%;
        font-weight: bold;
    }
    .title {
        color: #FF4081;
        text-align: center;
        font-weight: bold;
        font-size: 2.5em;
        margin-bottom: 30px;
    }
    </style>
    """, unsafe_allow_html=True)

st.markdown('<p class="title">✨ YouTube to MP3 / MP4 ✨</p>', unsafe_allow_html=True)

# URL入力
url = st.text_input("YouTubeのURLを貼り付けてください", placeholder="https://www.youtube.com/watch?v=...")

# ダウンロード関数
def download_video(video_url, is_audio_only):
    ydl_opts = {
        'outtmpl': 'downloads/%(title)s.%(ext)s',
    }

    if is_audio_only:
        ydl_opts.update({
            'format': 'bestaudio/best',
            'postprocessors': [{
                'key': 'FFmpegExtractAudio',
                'preferredcodec': 'mp3',
                'preferredquality': '192',
            }],
        })
    else:
        ydl_opts.update({
            'format': 'bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best',
        })

    try:
        with st.spinner('ダウンロード中... (数分かかる場合があります)'):
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                ydl.download([video_url])
        st.success('完了しました！ downloads フォルダを確認してください。')
    except Exception as e:
        st.error(f'エラーが発生しました: {str(e)}')

# ボタン配置
col1, col2 = st.columns(2)
with col1:
    if st.button("音源(MP3)を保存 🎵"):
        if url:
            download_video(url, True)
        else:
            st.warning("URLを入力してください")

with col2:
    if st.button("動画(MP4)を保存 🎬"):
        if url:
            download_video(url, False)
        else:
            st.warning("URLを入力してください")

# ダンス動画エリア
st.write("---")
video_file_path = "dance_anime.mp4"

if os.path.exists(video_file_path):
    # loop=True, autoplay=True, muted=True で自動ループ再生をシミュレート
    st.video(video_file_path, loop=True, autoplay=True, muted=True)
else:
    st.info("💡 スクリプトと同じフォルダに `dance_anime.mp4` を置くと、ここに女の子が表示されます。")
    # 代わりのプレースホルダー表示（デモ用）
    st.markdown("""
        <div style="height: 300px; background-color: #ffe0e9; border: 2px dashed #ff4081; border-radius: 15px; display: flex; align-items: center; justify-content: center; color: #ff4081;">
            ここにダンス動画が表示されます (dance_anime.mp4)
        </div>
    """, unsafe_allow_html=True)
